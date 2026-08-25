package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.domain.MmsDispatch;
import com.ruoyi.mms.domain.MmsFinishReceipt;
import com.ruoyi.mms.domain.MmsQc;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.domain.MmsRouteProcess;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkOrderAuditLog;
import com.ruoyi.mms.domain.MmsWoBomSnapshot;
import com.ruoyi.mms.domain.MmsWoRouteSnapshot;
import com.ruoyi.mms.mapper.MmsBomMapper;
import com.ruoyi.mms.mapper.MmsDispatchMapper;
import com.ruoyi.mms.mapper.MmsFinishReceiptMapper;
import com.ruoyi.mms.mapper.MmsQcMapper;
import com.ruoyi.mms.mapper.MmsRouteMapper;
import com.ruoyi.mms.mapper.MmsScheduleMapper;
import com.ruoyi.mms.mapper.MmsWoBomSnapshotMapper;
import com.ruoyi.mms.mapper.MmsWoRouteSnapshotMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.service.IMmsWorkOrderService;

/**
 * 生产工单 Service实现
 *
 * 工单状态机（精简版，质检与入库解耦为独立业务）：
 * 0(草稿) → 1(已下达) → 2(执行中) → 3(已完工) → 4(已关闭)
 *               ↓            ↓
 *           5(已暂停) ⇄ 1  5(已暂停)
 *
 * 说明：
 * - 报工只是执行中的动作，不再设独立状态
 * - 完工后质检和入库作为独立业务单据流转，不卡住工单状态
 * - 工单完工时自动生成完工质检单（通过工单号关联），质检独立处理
 * - 任意非已关闭/已作废状态 → 6(已作废)
 *
 * @author ruoyi
 */
@Service
public class MmsWorkOrderServiceImpl implements IMmsWorkOrderService
{
    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MmsBomMapper bomMapper;

    @Autowired
    private MmsRouteMapper routeMapper;

    @Autowired
    private MmsDispatchMapper dispatchMapper;

    @Autowired
    private MmsWoBomSnapshotMapper woBomSnapshotMapper;

    @Autowired
    private MmsWoRouteSnapshotMapper woRouteSnapshotMapper;

    @Autowired
    private MmsScheduleMapper scheduleMapper;

    @Autowired
    private MmsQcMapper qcMapper;

    @Autowired
    private MmsFinishReceiptMapper finishReceiptMapper;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsWorkOrder> selectWorkOrderList(MmsWorkOrder workOrder)
    {
        return workOrderMapper.selectWorkOrderList(workOrder);
    }

    @Override
    public MmsWorkOrder selectWorkOrderById(Long workOrderId)
    {
        MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(workOrderId);
        if (wo != null)
        {
            // 查询工单BOM快照和工艺快照，供详情页展示
            List<MmsWoBomSnapshot> bomSnapshots = woBomSnapshotMapper.selectBomSnapshotByWorkOrderId(workOrderId);
            wo.setBomSnapshotList(bomSnapshots);
            List<MmsWoRouteSnapshot> routeSnapshots = woRouteSnapshotMapper.selectRouteSnapshotByWorkOrderId(workOrderId);
            wo.setRouteSnapshotList(routeSnapshots);
            // 查询派工单列表，供详情页展示各工序完成情况
            MmsDispatch query = new MmsDispatch();
            query.setWorkOrderId(workOrderId);
            List<MmsDispatch> dispatchList = dispatchMapper.selectDispatchList(query);
            wo.setDispatchList(dispatchList);
        }
        return wo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWorkOrder(MmsWorkOrder workOrder)
    {
        // 来源类型校验：默认手工创建
        if (StringUtils.isEmpty(workOrder.getSourceType()))
        {
            workOrder.setSourceType("3");
        }
        // 计划生成时，关联计划号必填
        if ("1".equals(workOrder.getSourceType()) && StringUtils.isEmpty(workOrder.getMpsNo()))
        {
            throw new ServiceException("来源类型为计划生成时，关联计划号不能为空");
        }
        // 订单直转时，关联订单号必填
        if ("2".equals(workOrder.getSourceType()) && StringUtils.isEmpty(workOrder.getSourceOrderNo()))
        {
            throw new ServiceException("来源类型为订单直转时，关联销售订单号不能为空");
        }
        // 手工创建时，清空关联计划/订单（避免脏数据）
        if ("3".equals(workOrder.getSourceType()))
        {
            workOrder.setMpsId(null);
            workOrder.setMpsNo(null);
            workOrder.setSourceOrderId(null);
            workOrder.setSourceOrderNo(null);
        }
        // 计划生成时，清空关联订单
        if ("1".equals(workOrder.getSourceType()))
        {
            workOrder.setSourceOrderId(null);
            workOrder.setSourceOrderNo(null);
        }
        // 订单直转时，清空关联计划
        if ("2".equals(workOrder.getSourceType()))
        {
            workOrder.setMpsId(null);
            workOrder.setMpsNo(null);
        }
        // 新建工单默认状态为0(草稿)
        if (StringUtils.isEmpty(workOrder.getStatus()))
        {
            workOrder.setStatus("0");
        }
        workOrder.setDelFlag("0");
        // 自动生成工单号（通过编号规则 mms_work_order 生成）
        if (StringUtils.isEmpty(workOrder.getWorkOrderNo()))
        {
            workOrder.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        }
        // 初始化数量字段
        if (workOrder.getFinishedQty() == null)
        {
            workOrder.setFinishedQty(BigDecimal.ZERO);
        }
        if (workOrder.getQualifiedQty() == null)
        {
            workOrder.setQualifiedQty(BigDecimal.ZERO);
        }
        if (workOrder.getDefectQty() == null)
        {
            workOrder.setDefectQty(BigDecimal.ZERO);
        }
        workOrder.setCreateBy(SecurityUtils.getUsername());
        workOrder.setCreateTime(DateUtils.getNowDate());
        return workOrderMapper.insertWorkOrder(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkOrder(MmsWorkOrder workOrder)
    {
        // 校验：已下达及之后状态不允许修改核心字段
        MmsWorkOrder existing = workOrderMapper.selectWorkOrderById(workOrder.getWorkOrderId());
        if (existing != null && !"0".equals(existing.getStatus()) && !"5".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改工单信息");
        }
        // 来源类型校验
        if (StringUtils.isEmpty(workOrder.getSourceType()))
        {
            workOrder.setSourceType("3");
        }
        if ("1".equals(workOrder.getSourceType()) && StringUtils.isEmpty(workOrder.getMpsNo()))
        {
            throw new ServiceException("来源类型为计划生成时，关联计划号不能为空");
        }
        if ("2".equals(workOrder.getSourceType()) && StringUtils.isEmpty(workOrder.getSourceOrderNo()))
        {
            throw new ServiceException("来源类型为订单直转时，关联销售订单号不能为空");
        }
        if ("3".equals(workOrder.getSourceType()))
        {
            workOrder.setMpsId(null);
            workOrder.setMpsNo(null);
            workOrder.setSourceOrderId(null);
            workOrder.setSourceOrderNo(null);
        }
        if ("1".equals(workOrder.getSourceType()))
        {
            workOrder.setSourceOrderId(null);
            workOrder.setSourceOrderNo(null);
        }
        if ("2".equals(workOrder.getSourceType()))
        {
            workOrder.setMpsId(null);
            workOrder.setMpsNo(null);
        }
        workOrder.setUpdateBy(SecurityUtils.getUsername());
        workOrder.setUpdateTime(DateUtils.getNowDate());
        return workOrderMapper.updateWorkOrder(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWorkOrderByIds(Long[] workOrderIds)
    {
        // 校验：非草稿状态的工单不允许删除
        for (Long id : workOrderIds)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(id);
            if (wo != null && !"0".equals(wo.getStatus()) && !"6".equals(wo.getStatus()))
            {
                throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]非草稿/作废状态，不允许删除");
            }
        }
        // 删除工单前，同步取消关联的已下达排产记录
        scheduleMapper.cancelSchedulesByWorkOrderIds(workOrderIds);
        // 级联取消关联的未完成派工单
        String username = SecurityUtils.getUsername();
        for (Long id : workOrderIds)
        {
            dispatchMapper.updateDispatchStatusByWorkOrder(id, "3", username);
        }
        return workOrderMapper.deleteWorkOrderByIds(workOrderIds);
    }

    // ========== 审核日志 ==========

    @Override
    public List<MmsWorkOrderAuditLog> selectAuditLogByWorkOrderId(Long workOrderId)
    {
        return workOrderMapper.selectAuditLogByWorkOrderId(workOrderId);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);

        // ===== 步骤1：校验状态+BOM =====
        if (!"0".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有草稿状态可下达");
        }
        if (wo.getBomId() == null)
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]未关联BOM，无法下达");
        }
        // 校验BOM状态：必须为已发布(1)
        MmsBom bom = bomMapper.selectBomById(wo.getBomId());
        if (bom == null)
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]关联的BOM不存在，无法下达");
        }
        if (!"1".equals(bom.getStatus()))
        {
            throw new ServiceException("BOM[" + bom.getBomNo() + "]当前状态非已发布，无法下达");
        }

        String username = SecurityUtils.getUsername();
        Date now = DateUtils.getNowDate();

        // ===== 步骤2：复制BOM明细 → 工单BOM快照 =====
        List<MmsBomDetail> bomDetails = bomMapper.selectBomDetailByBomId(wo.getBomId());
        if (bomDetails == null || bomDetails.isEmpty())
        {
            throw new ServiceException("BOM[" + bom.getBomNo() + "]没有明细行，无法下达");
        }
        // 先清理旧快照（防止异常重试后重复数据）
        woBomSnapshotMapper.deleteBomSnapshotByWorkOrderId(workOrderId);
        List<MmsWoBomSnapshot> bomSnapshots = new ArrayList<>();
        for (MmsBomDetail detail : bomDetails)
        {
            MmsWoBomSnapshot snapshot = new MmsWoBomSnapshot();
            snapshot.setWorkOrderId(workOrderId);
            snapshot.setBomId(wo.getBomId());
            snapshot.setBomNo(wo.getBomNo());
            snapshot.setBomVersion(bom.getVersion());
            snapshot.setSeq(detail.getSeq());
            snapshot.setMaterialId(detail.getMaterialId());
            snapshot.setMaterialCode(detail.getMaterialCode());
            snapshot.setMaterialName(detail.getMaterialName());
            snapshot.setSpecModel(detail.getSpecModel());
            snapshot.setUnit(detail.getUnit());
            snapshot.setUsageQty(detail.getUsageQty());
            snapshot.setLossRate(detail.getLossRate());
            snapshot.setIsKeyMaterial(detail.getIsKeyMaterial());
            snapshot.setSupplyType(detail.getSupplyType());
            snapshot.setPickStoreId(detail.getPickStoreId());
            snapshot.setPickStoreName(detail.getPickStoreName());
            snapshot.setIsPhantom(detail.getIsPhantom());
            snapshot.setDelFlag("0");
            snapshot.setCreateBy(username);
            snapshot.setCreateTime(now);
            bomSnapshots.add(snapshot);
        }
        woBomSnapshotMapper.batchInsertBomSnapshot(bomSnapshots);

        // ===== 步骤3：复制工艺明细 → 工单工艺快照 =====
        if (wo.getRouteId() != null)
        {
            MmsRoute route = routeMapper.selectRouteById(wo.getRouteId());
            if (route != null)
            {
                List<MmsRouteProcess> routeProcesses = routeMapper.selectRouteProcessByRouteId(wo.getRouteId());
                if (routeProcesses != null && !routeProcesses.isEmpty())
                {
                    woRouteSnapshotMapper.deleteRouteSnapshotByWorkOrderId(workOrderId);
                    List<MmsWoRouteSnapshot> routeSnapshots = new ArrayList<>();
                    for (MmsRouteProcess rp : routeProcesses)
                    {
                        MmsWoRouteSnapshot snapshot = new MmsWoRouteSnapshot();
                        snapshot.setWorkOrderId(workOrderId);
                        snapshot.setRouteId(wo.getRouteId());
                        snapshot.setRouteNo(wo.getRouteNo());
                        snapshot.setRouteVersion(route.getVersion());
                        snapshot.setStepSeq(rp.getStepSeq());
                        snapshot.setProcessId(rp.getProcessId());
                        snapshot.setProcessCode(rp.getProcessCode());
                        snapshot.setProcessName(rp.getProcessName());
                        snapshot.setResourceId(rp.getResourceId());
                        snapshot.setResourceName(rp.getResourceName());
                        snapshot.setStdTime(rp.getStdTime());
                        snapshot.setPrepTime(rp.getPrepTime());
                        snapshot.setIsKeyProcess(rp.getIsKeyProcess());
                        snapshot.setIsOutsource(rp.getIsOutsource());
                        snapshot.setDelFlag("0");
                        snapshot.setCreateBy(username);
                        snapshot.setCreateTime(now);
                        routeSnapshots.add(snapshot);
                    }
                    woRouteSnapshotMapper.batchInsertRouteSnapshot(routeSnapshots);

                    // ===== 步骤6：生成首工序派工单（支持并行工序） =====
                    // 首工序 = stepSeq 最小的一组工序（相同 step_seq 视为并行，全部生成）
                    int minStepSeq = Integer.MAX_VALUE;
                    for (MmsRouteProcess rp : routeProcesses)
                    {
                        if (rp.getStepSeq() != null && rp.getStepSeq() < minStepSeq)
                        {
                            minStepSeq = rp.getStepSeq();
                        }
                    }
                    for (MmsRouteProcess rp : routeProcesses)
                    {
                        if (rp.getStepSeq() != null && rp.getStepSeq() == minStepSeq)
                        {
                            MmsDispatch dispatch = new MmsDispatch();
                            dispatch.setDispatchNo(mkNumberRuleService.generateNumber("mms_dispatch"));
                            dispatch.setWorkOrderId(workOrderId);
                            dispatch.setWorkOrderNo(wo.getWorkOrderNo());
                            dispatch.setProductCode(wo.getProductCode());
                            dispatch.setProductName(wo.getProductName());
                            dispatch.setSpecModel(wo.getSpecModel());
                            dispatch.setUnit(wo.getUnit());
                            dispatch.setOpSeq(rp.getStepSeq());
                            dispatch.setProcessId(rp.getProcessId());
                            dispatch.setProcessName(rp.getProcessName());
                            // 产能单元优先取工序上绑定的，工序未绑定则用工单头上的
                            if (rp.getResourceId() != null)
                            {
                                dispatch.setResourceId(rp.getResourceId());
                                dispatch.setResourceName(rp.getResourceName());
                            }
                            else
                            {
                                dispatch.setResourceId(wo.getResourceId());
                                dispatch.setResourceName(wo.getResourceName());
                            }
                            dispatch.setPlanQty(wo.getPlanQty());
                            dispatch.setGoodQty(BigDecimal.ZERO);
                            dispatch.setDefectQty(BigDecimal.ZERO);
                            dispatch.setPlanStart(wo.getPlanStart());
                            dispatch.setPlanEnd(wo.getPlanFinish());
                            dispatch.setStatus("0"); // 待开工
                            dispatch.setDelFlag("0");
                            dispatch.setCreateBy(username);
                            dispatch.setCreateTime(now);
                            dispatch.setRemark("工单下达自动生成（首工序" + (routeProcesses.size() > 1 ? "，并行" : "") + "）");
                            dispatchMapper.insertDispatch(dispatch);
                        }
                    }
                }
            }
        }

        // ===== 步骤5：状态推进 + 写下达时间 =====
        wo.setStatus("1");
        wo.setReleaseBy(username);
        wo.setReleaseTime(now);
        wo.setUpdateBy(username);
        wo.setUpdateTime(now);
        int rows = workOrderMapper.updateWorkOrder(wo);

        // ===== 步骤7：记录审核日志 =====
        StringBuilder logRemark = new StringBuilder("工单下达");
        logRemark.append("（BOM快照").append(bomSnapshots.size()).append("行");
        if (wo.getRouteId() != null)
        {
            // 有工艺则追加工序数信息
        }
        logRemark.append("）");
        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "release", logRemark.toString());
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseWorkOrder(Long workOrderId, String pauseReason)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：已下达(1)或执行中(2)可暂停
        if (!"1".equals(wo.getStatus()) && !"2".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有已下达/执行中状态可暂停");
        }
        wo.setStatus("5");
        wo.setPauseReason(pauseReason);
        String username = SecurityUtils.getUsername();
        wo.setUpdateBy(username);
        wo.setUpdateTime(DateUtils.getNowDate());
        int rows = workOrderMapper.updateWorkOrder(wo);

        // 级联暂停派工单：将进行中(1)的派工单改回待开工(0)，暂停期间不允许继续生产
        // 注意：不取消已完成的派工单，不改待开工的派工单（本就未开始）
        List<MmsDispatch> activeDispatches = dispatchMapper.selectActiveDispatchByWorkOrder(workOrderId);
        for (MmsDispatch d : activeDispatches)
        {
            if ("1".equals(d.getStatus()))
            {
                d.setStatus("0"); // 进行中 → 待开工（暂停后恢复需重新开工）
                d.setUpdateBy(username);
                dispatchMapper.updateDispatch(d);
            }
        }

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "pause", "工单暂停" + (StringUtils.isNotEmpty(pauseReason) ? "：" + pauseReason : ""));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：只有已暂停(5)可恢复
        if (!"5".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有已暂停状态可恢复");
        }
        // 恢复到已下达(1)状态
        wo.setStatus("1");
        wo.setPauseReason(null);
        wo.setUpdateBy(SecurityUtils.getUsername());
        wo.setUpdateTime(DateUtils.getNowDate());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "resume", "工单恢复");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：执行中(2)可完工（报工是执行中的动作，不再单独设状态）
        if (!"2".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有执行中状态可完工");
        }
        // 完工条件：合格数 + 不良数 >= 计划数量 * 完工允差（默认100%）
        BigDecimal totalOutput = (wo.getQualifiedQty() == null ? BigDecimal.ZERO : wo.getQualifiedQty())
                .add(wo.getDefectQty() == null ? BigDecimal.ZERO : wo.getDefectQty());
        if (totalOutput.compareTo(wo.getPlanQty()) < 0)
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]产出量(" + totalOutput + ")未达到计划数量(" + wo.getPlanQty() + ")，不允许完工");
        }
        // 工单完工 → 直接进入已完工(3)状态，质检和入库作为独立业务不卡住工单
        wo.setStatus("3");
        // 实际完工时间取所有派工单中最大的actual_end（即最后一道已完工工序的完工时间）
        // 如果没有任何派工单完工（全部未开工），则取当前时间
        MmsDispatch finQuery = new MmsDispatch();
        finQuery.setWorkOrderId(workOrderId);
        List<MmsDispatch> finDispatches = dispatchMapper.selectDispatchList(finQuery);
        Date lastActualEnd = null;
        if (finDispatches != null)
        {
            for (MmsDispatch disp : finDispatches)
            {
                if (disp.getActualEnd() != null && (lastActualEnd == null || disp.getActualEnd().after(lastActualEnd)))
                {
                    lastActualEnd = disp.getActualEnd();
                }
            }
        }
        wo.setActualFinish(lastActualEnd != null ? lastActualEnd : new Date());
        wo.setUpdateBy(SecurityUtils.getUsername());
        wo.setUpdateTime(DateUtils.getNowDate());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "finish", "工单完工");

        // ===== 自动生成完工质检单 =====
        // 工单完工时自动创建一条完工检(qc_type=2)类型的质检单
        // 质检作为独立业务流转，不卡住工单状态
        // 预填充来自工单的检验数量、不良数量等数据，质检员只需确认检验结果即可
        Date now = new Date();
        String username = SecurityUtils.getUsername();
        MmsQc qc = new MmsQc();
        qc.setQcNo(mkNumberRuleService.generateNumber("mms_qc"));
        qc.setWorkOrderId(wo.getWorkOrderId());
        qc.setWorkOrderNo(wo.getWorkOrderNo());
        // 带出工单产品信息
        qc.setProductCode(wo.getProductCode());
        qc.setProductName(wo.getProductName());
        qc.setSpecModel(wo.getSpecModel());
        qc.setUnit(wo.getUnit());
        qc.setPlanQty(wo.getPlanQty());
        // 完工质检单不绑定具体工序，检验的是最终成品
        qc.setQcType("2"); // 末件/完工检
        // 检验数量 = 工单完工数量（合格+不良）
        BigDecimal qualifiedQty = wo.getQualifiedQty() != null ? wo.getQualifiedQty() : BigDecimal.ZERO;
        BigDecimal defectQty = wo.getDefectQty() != null ? wo.getDefectQty() : BigDecimal.ZERO;
        qc.setInspectQty(qualifiedQty.add(defectQty).intValue());
        // 不良数量 = 工单总不良数量
        qc.setDefectQty(defectQty.intValue());
        // 报废数量暂不预填，由质检员判定后填写
        qc.setScrapQty(0);
        // 检验结论默认未判定（留空），等待质检员确认后填写
        qc.setDefectType("");
        qc.setQcResult("");
        qc.setQcBy("");
        qc.setQcTime(null);
        qc.setDelFlag("0");
        qc.setCreateBy(username);
        qc.setCreateTime(now);
        qc.setRemark("工单完工自动生成，待质检员确认检验结果");
        qcMapper.insertQc(qc);

        // ===== 自动创建完工入库单 =====
        // 工单完工时自动创建完工入库单，预填充工单的产品信息和完工数据
        // 入库单作为独立业务单据，入库人可在入库确认时补充批次号等信息
        MmsFinishReceipt finishReceipt = new MmsFinishReceipt();
        finishReceipt.setFinishNo(mkNumberRuleService.generateNumber("mms_finish_receipt"));
        finishReceipt.setWorkOrderId(wo.getWorkOrderId());
        finishReceipt.setWorkOrderNo(wo.getWorkOrderNo());
        finishReceipt.setProductId(wo.getProductId());
        finishReceipt.setProductCode(wo.getProductCode());
        finishReceipt.setProductName(wo.getProductName());
        finishReceipt.setSpecModel(wo.getSpecModel());
        finishReceipt.setUnit(wo.getUnit());
        finishReceipt.setPlanQty(wo.getPlanQty());
        // 完工数量 = 工单的完工数量
        BigDecimal finishedQty = wo.getFinishedQty() != null ? wo.getFinishedQty() : BigDecimal.ZERO;
        finishReceipt.setFinishQty(finishedQty);
        // 合格数量 = 工单的合格数量（复用上面已声明的 qualifiedQty）
        finishReceipt.setQualifiedQty(qualifiedQty);
        // 完工时间 = 工单实际完工时间
        finishReceipt.setFinishTime(now);
        // 完工人 = 当前操作人
        finishReceipt.setFinishBy(username);
        // 判断是否按期完工
        if (wo.getPlanFinish() != null && now != null)
        {
            finishReceipt.setIsOnTime(now.before(wo.getPlanFinish()) || now.equals(wo.getPlanFinish()) ? "1" : "0");
        }
        else
        {
            finishReceipt.setIsOnTime("1");
        }
        finishReceipt.setStatus("0"); // 待入库
        finishReceipt.setDelFlag("0");
        finishReceipt.setCreateBy(username);
        finishReceipt.setCreateTime(now);
        finishReceipt.setRemark("工单完工自动生成");
        finishReceiptMapper.insertFinishReceipt(finishReceipt);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeWorkOrder(Long workOrderId, String closeRemark)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：已完工(3)可正常关闭；执行中(2)可强制关闭（短产关闭），需记录短产原因
        String status = wo.getStatus();
        boolean isForceClose = "2".equals(status);
        if (!isForceClose && !"3".equals(status))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(status) + "，只有执行中/已完工状态可关闭");
        }

        String username = SecurityUtils.getUsername();

        // 如果是强制关闭（短产关闭），级联取消未完成的派工单
        if (isForceClose)
        {
            // 计算短产数量
            BigDecimal totalOutput = (wo.getQualifiedQty() == null ? BigDecimal.ZERO : wo.getQualifiedQty())
                    .add(wo.getDefectQty() == null ? BigDecimal.ZERO : wo.getDefectQty());
            BigDecimal shortQty = wo.getPlanQty().subtract(totalOutput);
            // 级联取消未完成的派工单（待开工0和进行中1 → 已取消3）
            dispatchMapper.updateDispatchStatusByWorkOrder(workOrderId, "3", username);
            // 同步取消关联的排产记录
            scheduleMapper.cancelSchedulesByWorkOrderIds(new Long[]{ workOrderId });

            // 记录短产信息
            String qtyDesc;
            if (shortQty.compareTo(BigDecimal.ZERO) > 0)
            {
                qtyDesc = "短产" + shortQty + wo.getUnit();
            }
            else if (shortQty.compareTo(BigDecimal.ZERO) == 0)
            {
                qtyDesc = "满产";
            }
            else
            {
                qtyDesc = "超产" + shortQty.abs() + wo.getUnit();
            }
            String forceCloseMsg = "强制关闭（" + qtyDesc + "）";
            if (StringUtils.isNotEmpty(closeRemark))
            {
                forceCloseMsg += "：" + closeRemark;
            }
            wo.setCloseRemark(forceCloseMsg);
        }
        else
        {
            wo.setCloseRemark(closeRemark);
        }

        wo.setStatus("4");
        wo.setActualFinish(new Date());
        wo.setUpdateBy(username);
        wo.setUpdateTime(DateUtils.getNowDate());
        int rows = workOrderMapper.updateWorkOrder(wo);

        String logAction = isForceClose ? "forceClose" : "close";
        String logRemark = isForceClose
                ? "工单强制关闭" + (StringUtils.isNotEmpty(closeRemark) ? "：" + closeRemark : "")
                : "工单关闭" + (StringUtils.isNotEmpty(closeRemark) ? "：" + closeRemark : "");
        insertAuditLog(workOrderId, wo.getWorkOrderNo(), logAction, logRemark);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelWorkOrder(Long workOrderId, String cancelReason)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：已关闭(4)和已作废(6)不可作废
        if ("4".equals(wo.getStatus()) || "6".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，不允许作废");
        }
        // 作废条件：已完工(3)状态不允许直接作废，需先处理质检/入库等关联业务
        if ("3".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]已完工，请先关闭工单后再作废");
        }
        String username = SecurityUtils.getUsername();
        wo.setStatus("6");
        wo.setCloseRemark(cancelReason);
        wo.setUpdateBy(username);
        wo.setUpdateTime(DateUtils.getNowDate());
        int rows = workOrderMapper.updateWorkOrder(wo);

        // 工单作废时，同步取消关联的已下达排产记录
        scheduleMapper.cancelSchedulesByWorkOrderIds(new Long[]{ workOrderId });

        // 级联取消未完成的派工单（待开工0和进行中1 → 已取消3）
        dispatchMapper.updateDispatchStatusByWorkOrder(workOrderId, "3", username);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "cancel", "工单作废" + (StringUtils.isNotEmpty(cancelReason) ? "：" + cancelReason : ""));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long splitWorkOrder(Long workOrderId, BigDecimal splitQty)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 校验：只有草稿(0)或已下达(1)状态可拆分
        if (!"0".equals(wo.getStatus()) && !"1".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有草稿/已下达状态可拆分");
        }
        // 校验拆分数量
        if (splitQty == null || splitQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("拆分数量必须大于0");
        }
        if (splitQty.compareTo(wo.getPlanQty()) >= 0)
        {
            throw new ServiceException("拆分数量必须小于原工单计划数量(" + wo.getPlanQty() + ")");
        }

        // 创建新工单（拆分出的部分）
        MmsWorkOrder newWo = new MmsWorkOrder();
        newWo.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        newWo.setOrderType(wo.getOrderType());
        newWo.setMpsId(wo.getMpsId());
        newWo.setMpsNo(wo.getMpsNo());
        newWo.setDemandNo(wo.getDemandNo());
        newWo.setProductId(wo.getProductId());
        newWo.setProductCode(wo.getProductCode());
        newWo.setProductName(wo.getProductName());
        newWo.setSpecModel(wo.getSpecModel());
        newWo.setUnit(wo.getUnit());
        newWo.setPlanQty(splitQty);
        newWo.setBomId(wo.getBomId());
        newWo.setBomNo(wo.getBomNo());
        newWo.setRouteId(wo.getRouteId());
        newWo.setRouteNo(wo.getRouteNo());
        newWo.setResourceId(wo.getResourceId());
        newWo.setResourceName(wo.getResourceName());
        newWo.setPlanStart(wo.getPlanStart());
        newWo.setPlanFinish(wo.getPlanFinish());
        newWo.setPriority(wo.getPriority());
        newWo.setStatus("0"); // 新工单为草稿状态
        newWo.setFinishedQty(BigDecimal.ZERO);
        newWo.setQualifiedQty(BigDecimal.ZERO);
        newWo.setDefectQty(BigDecimal.ZERO);
        newWo.setDelFlag("0");
        newWo.setCreateBy(SecurityUtils.getUsername());
        newWo.setCreateTime(DateUtils.getNowDate());
        newWo.setRemark("由工单[" + wo.getWorkOrderNo() + "]拆分而来");
        workOrderMapper.insertWorkOrder(newWo);

        // 原工单扣减计划数量
        wo.setPlanQty(wo.getPlanQty().subtract(splitQty));
        wo.setUpdateBy(SecurityUtils.getUsername());
        wo.setUpdateTime(DateUtils.getNowDate());
        workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "split",
                "工单拆分：拆出" + splitQty + wo.getUnit() + "至新工单[" + newWo.getWorkOrderNo() + "]");
        insertAuditLog(newWo.getWorkOrderId(), newWo.getWorkOrderNo(), "split",
                "工单拆分产生：来源于工单[" + wo.getWorkOrderNo() + "]，拆分数量" + splitQty + wo.getUnit());

        return newWo.getWorkOrderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createReworkOrder(Long sourceWorkOrderId, BigDecimal reworkQty, String reworkReason)
    {
        MmsWorkOrder sourceWo = getAndCheckWorkOrder(sourceWorkOrderId);
        // 校验：源工单必须是已完工(3)或已关闭(4)状态
        String srcStatus = sourceWo.getStatus();
        if (!"3".equals(srcStatus) && !"4".equals(srcStatus))
        {
            throw new ServiceException("源工单[" + sourceWo.getWorkOrderNo() + "]当前状态为" + statusName(srcStatus)
                    + "，只有已完工/已关闭状态可创建返工工单");
        }
        // 校验返工数量
        if (reworkQty == null || reworkQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("返工数量必须大于0");
        }
        // 返工数量不能超过源工单的计划数量
        BigDecimal srcPlanQty = sourceWo.getPlanQty() == null ? BigDecimal.ZERO : sourceWo.getPlanQty();
        if (reworkQty.compareTo(srcPlanQty) > 0)
        {
            throw new ServiceException("返工数量(" + reworkQty + ")不能超过源工单计划数量(" + srcPlanQty + ")");
        }

        // 创建返工工单
        MmsWorkOrder reworkWo = new MmsWorkOrder();
        reworkWo.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        reworkWo.setOrderType("1"); // 返工工单
        reworkWo.setSourceType("3"); // 手工创建
        reworkWo.setSourceOrderId(sourceWorkOrderId);
        reworkWo.setSourceOrderNo(sourceWo.getWorkOrderNo()); // 复用工单号字段关联源工单
        reworkWo.setProductId(sourceWo.getProductId());
        reworkWo.setProductCode(sourceWo.getProductCode());
        reworkWo.setProductName(sourceWo.getProductName());
        reworkWo.setSpecModel(sourceWo.getSpecModel());
        reworkWo.setUnit(sourceWo.getUnit());
        reworkWo.setPlanQty(reworkQty);
        reworkWo.setBomId(sourceWo.getBomId());
        reworkWo.setBomNo(sourceWo.getBomNo());
        reworkWo.setRouteId(sourceWo.getRouteId());
        reworkWo.setRouteNo(sourceWo.getRouteNo());
        reworkWo.setResourceId(sourceWo.getResourceId());
        reworkWo.setResourceName(sourceWo.getResourceName());
        reworkWo.setPlanStart(DateUtils.getNowDate());
        reworkWo.setPlanFinish(sourceWo.getPlanFinish());
        reworkWo.setPriority(sourceWo.getPriority());
        reworkWo.setStatus("0"); // 草稿状态，需重新下达
        reworkWo.setFinishedQty(BigDecimal.ZERO);
        reworkWo.setQualifiedQty(BigDecimal.ZERO);
        reworkWo.setDefectQty(BigDecimal.ZERO);
        reworkWo.setDelFlag("0");
        String username = SecurityUtils.getUsername();
        reworkWo.setCreateBy(username);
        reworkWo.setCreateTime(DateUtils.getNowDate());
        reworkWo.setRemark("返工工单：来源于工单[" + sourceWo.getWorkOrderNo() + "]，返工数量" + reworkQty
                + sourceWo.getUnit() + (StringUtils.isNotEmpty(reworkReason) ? "，返工原因：" + reworkReason : ""));
        workOrderMapper.insertWorkOrder(reworkWo);

        // 记录审核日志（源工单）
        insertAuditLog(sourceWorkOrderId, sourceWo.getWorkOrderNo(), "rework",
                "创建返工工单[" + reworkWo.getWorkOrderNo() + "]，返工数量" + reworkQty + sourceWo.getUnit()
                        + (StringUtils.isNotEmpty(reworkReason) ? "，原因：" + reworkReason : ""));
        // 记录审核日志（返工工单）
        insertAuditLog(reworkWo.getWorkOrderId(), reworkWo.getWorkOrderNo(), "rework",
                "返工工单创建：来源于工单[" + sourceWo.getWorkOrderNo() + "]，返工数量" + reworkQty + sourceWo.getUnit());

        return reworkWo.getWorkOrderId();
    }

    @Override
    public com.ruoyi.common.core.domain.AjaxResult getReleasePreview(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        if (!"0".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有草稿状态可下达");
        }

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("workOrder", wo);

        // BOM明细
        java.util.List<java.util.Map<String, Object>> bomList = new java.util.ArrayList<>();
        if (wo.getBomId() != null)
        {
            MmsBom bom = bomMapper.selectBomById(wo.getBomId());
            if (bom != null)
            {
                data.put("bom", bom);
                data.put("bomStatusOk", "1".equals(bom.getStatus()));
                List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(wo.getBomId());
                if (details != null)
                {
                    for (MmsBomDetail d : details)
                    {
                        java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                        item.put("seq", d.getSeq());
                        item.put("materialCode", d.getMaterialCode());
                        item.put("materialName", d.getMaterialName());
                        item.put("specModel", d.getSpecModel());
                        item.put("unit", d.getUnit());
                        item.put("usageQty", d.getUsageQty());
                        item.put("lossRate", d.getLossRate());
                        item.put("isKeyMaterial", "1".equals(d.getIsKeyMaterial()));
                        item.put("supplyType", d.getSupplyType());
                        // 计算需求数量 = 计划数量 × 单件用量 × (1 + 损耗率/100)
                        BigDecimal demandQty = wo.getPlanQty()
                                .multiply(d.getUsageQty() == null ? BigDecimal.ZERO : d.getUsageQty());
                        if (d.getLossRate() != null && d.getLossRate().compareTo(BigDecimal.ZERO) > 0)
                        {
                            demandQty = demandQty.multiply(BigDecimal.ONE.add(d.getLossRate().divide(BigDecimal.valueOf(100), 6, BigDecimal.ROUND_HALF_UP)));
                        }
                        item.put("demandQty", demandQty.setScale(4, BigDecimal.ROUND_HALF_UP));
                        bomList.add(item);
                    }
                }
            }
        }
        else
        {
            data.put("bomStatusOk", false);
        }
        data.put("bomDetails", bomList);

        // 工艺工序
        java.util.List<java.util.Map<String, Object>> routeList = new java.util.ArrayList<>();
        if (wo.getRouteId() != null)
        {
            MmsRoute route = routeMapper.selectRouteById(wo.getRouteId());
            if (route != null)
            {
                data.put("route", route);
                List<MmsRouteProcess> processes = routeMapper.selectRouteProcessByRouteId(wo.getRouteId());
                if (processes != null)
                {
                    for (MmsRouteProcess rp : processes)
                    {
                        java.util.Map<String, Object> item = new java.util.LinkedHashMap<>();
                        item.put("stepSeq", rp.getStepSeq());
                        item.put("processCode", rp.getProcessCode());
                        item.put("processName", rp.getProcessName());
                        item.put("stdTime", rp.getStdTime());
                        item.put("prepTime", rp.getPrepTime());
                        item.put("isKeyProcess", "1".equals(rp.getIsKeyProcess()));
                        item.put("isOutsource", "1".equals(rp.getIsOutsource()));
                        routeList.add(item);
                    }
                }
            }
        }
        data.put("routeProcesses", routeList);

        // 校验结果汇总
        java.util.List<String> warnings = new java.util.ArrayList<>();
        if (wo.getBomId() == null)
        {
            warnings.add("未关联BOM，无法下达");
        }
        else
        {
            MmsBom bom = bomMapper.selectBomById(wo.getBomId());
            if (bom == null)
            {
                warnings.add("关联的BOM不存在");
            }
            else if (!"1".equals(bom.getStatus()))
            {
                warnings.add("BOM[" + bom.getBomNo() + "]状态非已发布");
            }
            else
            {
                List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(wo.getBomId());
                if (details == null || details.isEmpty())
                {
                    warnings.add("BOM没有明细行");
                }
            }
        }
        if (wo.getResourceId() == null)
        {
            warnings.add("未分配产能单元，下达后将无法自动生成派工单");
        }
        if (wo.getPlanStart() == null || wo.getPlanFinish() == null)
        {
            warnings.add("未设置计划开工/完工时间");
        }
        data.put("warnings", warnings);
        data.put("canRelease", warnings.stream().noneMatch(w -> w.contains("无法下达") || w.contains("不存在") || w.contains("非已发布") || w.contains("没有明细")));

        return com.ruoyi.common.core.domain.AjaxResult.success(data);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取并校验工单是否存在
     */
    private MmsWorkOrder getAndCheckWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(workOrderId);
        if (wo == null)
        {
            throw new ServiceException("工单不存在或已删除");
        }
        return wo;
    }

    /**
     * 记录审核日志
     */
    private void insertAuditLog(Long workOrderId, String workOrderNo, String action, String remark)
    {
        MmsWorkOrderAuditLog log = new MmsWorkOrderAuditLog();
        log.setWorkOrderId(workOrderId);
        log.setAuditBy(SecurityUtils.getUsername());
        log.setAuditAction(action);
        log.setAuditTime(new Date());
        log.setAuditRemark(remark);
        workOrderMapper.insertAuditLog(log);
    }

    /**
     * 状态名称转换
     */
    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "草稿";
            case "1": return "已下达";
            case "2": return "执行中";
            case "3": return "已完工";
            case "4": return "已关闭";
            case "5": return "已暂停";
            case "6": return "已作废";
            default: return "未知(" + status + ")";
        }
    }

}
