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
import com.ruoyi.mms.domain.MmsDispatch;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkOrderAuditLog;
import com.ruoyi.mms.domain.MmsWoRouteSnapshot;
import com.ruoyi.mms.domain.MmsWorkReport;
import com.ruoyi.mms.domain.MmsQc;
import com.ruoyi.mms.mapper.MmsDispatchMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.mapper.MmsWoRouteSnapshotMapper;
import com.ruoyi.mms.mapper.MmsWorkReportMapper;
import com.ruoyi.mms.mapper.MmsQcMapper;
import com.ruoyi.mms.service.IMmsDispatchService;

/**
 * 派工管理 Service实现
 *
 * 派工状态机：
 * 0(待开工) → 1(进行中) → 2(已完成)
 *              ↓
 *          3(已取消)
 *
 * 完工联动逻辑：
 * 1. 回写派工单 goodQty/defectQty/actualEnd
 * 2. 自动生成报工记录（状态=已审核）
 * 3. 联动更新工单 finishedQty/qualifiedQty/defectQty 和状态
 * 4. 自动创建下一道工序的派工单（如果存在后续工序）
 * 5. 最后一道工序完工时自动完工工单（状态→已完工），同时自动生成完工质检单
 *
 * @author ruoyi
 */
@Service
public class MmsDispatchServiceImpl implements IMmsDispatchService
{
    @Autowired
    private MmsDispatchMapper dispatchMapper;

    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private MmsWorkReportMapper workReportMapper;

    @Autowired
    private MmsQcMapper qcMapper;

    @Autowired
    private MmsWoRouteSnapshotMapper woRouteSnapshotMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsDispatch> selectDispatchList(MmsDispatch dispatch)
    {
        return dispatchMapper.selectDispatchList(dispatch);
    }

    @Override
    public MmsDispatch selectDispatchById(Long dispatchId)
    {
        return dispatchMapper.selectDispatchById(dispatchId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDispatch(MmsDispatch dispatch)
    {
        if (StringUtils.isEmpty(dispatch.getStatus()))
        {
            dispatch.setStatus("0");
        }
        dispatch.setDelFlag("0");
        if (StringUtils.isEmpty(dispatch.getDispatchNo()))
        {
            dispatch.setDispatchNo(mkNumberRuleService.generateNumber("mms_dispatch"));
        }
        if (dispatch.getGoodQty() == null)
        {
            dispatch.setGoodQty(BigDecimal.ZERO);
        }
        if (dispatch.getDefectQty() == null)
        {
            dispatch.setDefectQty(BigDecimal.ZERO);
        }
        dispatch.setCreateBy(SecurityUtils.getUsername());
        dispatch.setCreateTime(DateUtils.getNowDate());
        return dispatchMapper.insertDispatch(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDispatch(MmsDispatch dispatch)
    {
        MmsDispatch existing = dispatchMapper.selectDispatchById(dispatch.getDispatchId());
        if (existing != null && !"0".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改派工单信息");
        }
        dispatch.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(dispatch);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDispatchByIds(Long[] dispatchIds)
    {
        for (Long id : dispatchIds)
        {
            MmsDispatch d = dispatchMapper.selectDispatchById(id);
            if (d != null && !"0".equals(d.getStatus()) && !"3".equals(d.getStatus()))
            {
                throw new ServiceException("派工单[" + d.getDispatchNo() + "]非待开工/已取消状态，不允许删除");
            }
        }
        return dispatchMapper.deleteDispatchByIds(dispatchIds);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int startDispatch(Long dispatchId, String operatorName, String operateTime, Long teamId, String teamName, Long resourceId, String resourceName)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if (!"0".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，只有待开工状态可开工");
        }
        // 班组必填校验
        if (teamId == null && d.getTeamId() == null)
        {
            throw new ServiceException("请选择班组");
        }
        // 如果传入了班组则更新
        if (teamId != null)
        {
            d.setTeamId(teamId);
            d.setTeamName(teamName);
        }
        // 如果传入了产能单元则更新
        if (resourceId != null)
        {
            d.setResourceId(resourceId);
            d.setResourceName(resourceName);
        }
        // 产能单元必填校验
        if (d.getResourceId() == null)
        {
            throw new ServiceException("请选择产能单元");
        }
        // 校验关联工单状态：工单暂停(7)或作废(8)时不允许开工
        if (d.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(d.getWorkOrderId());
            if (wo != null)
            {
                if ("5".equals(wo.getStatus()))
                {
                    throw new ServiceException("关联工单[" + wo.getWorkOrderNo() + "]已暂停，请先恢复工单后再开工");
                }
                if ("6".equals(wo.getStatus()))
                {
                    throw new ServiceException("关联工单[" + wo.getWorkOrderNo() + "]已作废，派工单无法开工");
                }
            }
        }
        // 操作人员为空时默认取当前登录用户
        if (StringUtils.isEmpty(operatorName))
        {
            operatorName = SecurityUtils.getUsername();
        }
        d.setUserIds(operatorName);
        d.setStatus("1");
        // 操作时间：用户指定时使用用户输入，否则取当前时间
        Date actualStartDate = new Date();
        if (StringUtils.isNotEmpty(operateTime))
        {
            try
            {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                actualStartDate = sdf.parse(operateTime);
            }
            catch (Exception e)
            {
                // 解析失败时使用当前时间
            }
        }
        d.setActualStart(actualStartDate);
        String username = SecurityUtils.getUsername();
        d.setUpdateBy(username);
        int rows = dispatchMapper.updateDispatch(d);

        // 联动：工单状态从已下达(1) → 执行中(2)
        if (d.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(d.getWorkOrderId());
            if (wo != null && "1".equals(wo.getStatus()))
            {
                wo.setStatus("2");
                wo.setActualStart(actualStartDate);
                wo.setUpdateBy(username);
                workOrderMapper.updateWorkOrder(wo);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishDispatch(Long dispatchId, BigDecimal goodQty, BigDecimal defectQty, String remark, Date actualStart, Date actualEnd)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if (!"1".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，只有进行中状态可完工");
        }
        // 校验完工数据
        if (goodQty == null || goodQty.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("合格数量不能为空且不能小于0");
        }
        if (defectQty == null || defectQty.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("不良数量不能为空且不能小于0");
        }
        // 合格+不良不能超过计划数量
        BigDecimal totalOutput = goodQty.add(defectQty);
        if (d.getPlanQty() != null && totalOutput.compareTo(d.getPlanQty()) > 0)
        {
            throw new ServiceException("完工总量(" + totalOutput + ")超过计划数量(" + d.getPlanQty() + ")，不允许完工");
        }

        String username = SecurityUtils.getUsername();
        Date now = new Date();

        // 实际开始时间：用户填写则用用户值，否则保留开工时记录的时间
        if (actualStart != null)
        {
            d.setActualStart(actualStart);
        }
        // 实际结束时间：用户填写则用用户值，否则取当前系统时间
        if (actualEnd != null)
        {
            // 校验实际结束不能早于实际开始
            Date effectiveStart = d.getActualStart();
            if (effectiveStart != null && actualEnd.before(effectiveStart))
            {
                throw new ServiceException("实际结束时间不能早于实际开始时间");
            }
            d.setActualEnd(actualEnd);
        }
        else
        {
            d.setActualEnd(now);
        }

        // ===== 1. 回写派工单 =====
        d.setStatus("2");
        d.setGoodQty(goodQty);
        d.setDefectQty(defectQty);
        d.setUpdateBy(username);
        int rows = dispatchMapper.updateDispatch(d);

        // ===== 2. 自动生成报工记录（已审核状态） =====
        MmsWorkReport report = new MmsWorkReport();
        report.setReportNo(mkNumberRuleService.generateNumber("mms_work_report"));
        report.setWorkOrderId(d.getWorkOrderId());
        report.setWorkOrderNo(d.getWorkOrderNo());
        report.setProcessId(d.getProcessId());
        report.setProcessName(d.getProcessName());
        report.setResourceId(d.getResourceId());
        report.setResourceName(d.getResourceName());
        report.setGoodQty(goodQty);
        report.setDefectQty(defectQty);
        report.setReportTime(now);
        report.setReportBy(username);
        report.setTeamName(d.getTeamName());
        report.setIsFirstPiece("0");
        report.setStatus("1"); // 已审核（派工完工自动审核）
        report.setSource("2"); // 派工完工自动生成
        report.setAuditBy(username);
        report.setAuditTime(now);
        report.setAuditRemark("派工单完工自动生成");
        report.setDelFlag("0");
        report.setCreateBy(username);
        report.setCreateTime(now);
        report.setRemark(remark);
        workReportMapper.insertWorkReport(report);

        // ===== 3. 联动更新工单进度 =====
        // 修正逻辑：串行工序间数量是流转的（后道计划=前道合格），不能简单累加
        // 重新从所有已完工派工单中计算工单的合格/不良/完工数量
        if (d.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(d.getWorkOrderId());
            if (wo != null)
            {
                // 查询该工单下所有已完工的派工单
                List<MmsDispatch> completedList = dispatchMapper.selectCompletedDispatchByWorkOrder(d.getWorkOrderId());

                // 1. 合格数量：取最大 op_seq 的已完工工序的合格数（并行工序取最小合格数）
                BigDecimal finalGoodQty = BigDecimal.ZERO;
                BigDecimal finalDefectQty = BigDecimal.ZERO; // 最终工序的不良数
                Integer maxOpSeq = null;
                for (MmsDispatch cd : completedList)
                {
                    if (cd.getOpSeq() != null)
                    {
                        if (maxOpSeq == null || cd.getOpSeq() > maxOpSeq)
                        {
                            maxOpSeq = cd.getOpSeq();
                            finalGoodQty = cd.getGoodQty() != null ? cd.getGoodQty() : BigDecimal.ZERO;
                            finalDefectQty = cd.getDefectQty() != null ? cd.getDefectQty() : BigDecimal.ZERO;
                        }
                        else if (cd.getOpSeq().equals(maxOpSeq))
                        {
                            // 同一 op_seq 的并行工序，取最小合格数
                            BigDecimal gq = cd.getGoodQty() != null ? cd.getGoodQty() : BigDecimal.ZERO;
                            if (gq.compareTo(finalGoodQty) < 0)
                            {
                                finalGoodQty = gq;
                            }
                            // 并行工序的不良数取对应最小合格数那条的不良数
                            BigDecimal dq = cd.getDefectQty() != null ? cd.getDefectQty() : BigDecimal.ZERO;
                            if (gq.compareTo(finalGoodQty) == 0 && dq.compareTo(finalDefectQty) != 0)
                            {
                                finalDefectQty = dq;
                            }
                        }
                    }
                }

                // 2. 不良数量：工单总投入量 - 最终合格数量
                //    串行工序中不良是递进淘汰的（前道不良不会流转到后道），不能简单累加各工序不良
                //    总损耗 = 投入量(首工序计划/工单计划) - 最终产出合格数
                //    注意：如果还没有任何工序完工，不良数量应为0
                BigDecimal totalDefect = BigDecimal.ZERO;
                if (maxOpSeq != null)
                {
                    BigDecimal planQty = wo.getPlanQty() != null ? wo.getPlanQty() : BigDecimal.ZERO;
                    totalDefect = planQty.subtract(finalGoodQty);
                    if (totalDefect.compareTo(BigDecimal.ZERO) < 0)
                    {
                        totalDefect = BigDecimal.ZERO;
                    }
                }

                // 3. 完工数量（当前产出） = 最终工序的产出总数 = 最终合格数 + 最终工序不良数
                //    表示最后一道工序实际处理了多少产品
                BigDecimal finishedQty = finalGoodQty.add(finalDefectQty);

                wo.setQualifiedQty(finalGoodQty);
                wo.setDefectQty(totalDefect);
                wo.setFinishedQty(finishedQty);

                // 工单状态联动：已下达(1) → 执行中(2)
                // 报工只是执行中的动作，不再单独设状态
                if ("1".equals(wo.getStatus()))
                {
                    wo.setStatus("2");
                    wo.setActualStart(now);
                }

                wo.setUpdateBy(username);
                wo.setUpdateTime(now);
                workOrderMapper.updateWorkOrder(wo);
            }
        }

        // ===== 4. 自动创建下一道工序的派工单 =====
        createNextDispatch(d, username, now);

        // ===== 5. 检查是否所有工序都已完工，如果是则自动完工工单 =====
        autoFinishWorkOrderIfAllCompleted(d, username, now);

        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelDispatch(Long dispatchId)
    {
        MmsDispatch d = getAndCheckDispatch(dispatchId);
        if ("2".equals(d.getStatus()) || "3".equals(d.getStatus()))
        {
            throw new ServiceException("派工单[" + d.getDispatchNo() + "]当前状态为" + statusName(d.getStatus()) + "，不允许取消");
        }
        d.setStatus("3");
        d.setUpdateBy(SecurityUtils.getUsername());
        return dispatchMapper.updateDispatch(d);
    }

    // ========== 私有辅助方法 ==========

    /**
     * 自动创建下一组工序的派工单（支持并行工序）
     *
     * 并行工序规则：相同 step_seq 的工序视为并行，必须全部完工后才触发下一组。
     * 汇合策略：下一组派工单的计划数量 = 当前组所有并行工序合格数量的最小值（最保守策略）。
     *
     * @param currentDispatch 当前完工的派工单
     * @param username 操作人
     * @param now 当前时间
     */
    private void createNextDispatch(MmsDispatch currentDispatch, String username, Date now)
    {
        if (currentDispatch.getWorkOrderId() == null)
        {
            return;
        }
        // 查询工单的工艺路线快照（按 step_seq 排序）
        List<MmsWoRouteSnapshot> snapshots = woRouteSnapshotMapper
                .selectRouteSnapshotByWorkOrderId(currentDispatch.getWorkOrderId());
        if (snapshots == null || snapshots.isEmpty())
        {
            return;
        }
        Integer currentOpSeq = currentDispatch.getOpSeq();
        if (currentOpSeq == null)
        {
            return;
        }

        // ===== 1. 检查同组（相同 step_seq）的派工单是否全部完工 =====
        // 查询当前工单下所有同 step_seq 的派工单
        MmsDispatch groupQuery = new MmsDispatch();
        groupQuery.setWorkOrderId(currentDispatch.getWorkOrderId());
        groupQuery.setOpSeq(currentOpSeq);
        List<MmsDispatch> groupDispatches = dispatchMapper.selectDispatchList(groupQuery);
        if (groupDispatches != null)
        {
            for (MmsDispatch gd : groupDispatches)
            {
                if (!"2".equals(gd.getStatus()) && !"3".equals(gd.getStatus()))
                {
                    // 同组还有未完工（且未取消）的派工单，不触发下一组
                    return;
                }
            }
        }

        // ===== 2. 找到下一组工序（step_seq > 当前的最小值，可能多道并行） =====
        Integer nextStepSeq = null;
        for (MmsWoRouteSnapshot snapshot : snapshots)
        {
            if (snapshot.getStepSeq() != null && snapshot.getStepSeq() > currentOpSeq)
            {
                if (nextStepSeq == null || snapshot.getStepSeq() < nextStepSeq)
                {
                    nextStepSeq = snapshot.getStepSeq();
                }
            }
        }
        if (nextStepSeq == null)
        {
            // 没有后续工序，说明当前是末道工序，无需创建
            return;
        }

        // ===== 3. 检查是否已存在下一组工序的派工单（避免重复创建） =====
        MmsDispatch nextQuery = new MmsDispatch();
        nextQuery.setWorkOrderId(currentDispatch.getWorkOrderId());
        nextQuery.setOpSeq(nextStepSeq);
        List<MmsDispatch> existingNext = dispatchMapper.selectDispatchList(nextQuery);
        if (existingNext != null && !existingNext.isEmpty())
        {
            // 下一组派工单已存在，不重复创建
            return;
        }

        // ===== 4. 计算汇合数量：取当前组所有已完工工序合格数量的最小值（取消的工序不参与计算） =====
        BigDecimal mergedQty = null;
        if (groupDispatches != null)
        {
            for (MmsDispatch gd : groupDispatches)
            {
                // 只统计已完工(status=2)的工序，取消(status=3)的工序跳过
                if ("2".equals(gd.getStatus()))
                {
                    BigDecimal gq = gd.getGoodQty() != null ? gd.getGoodQty() : BigDecimal.ZERO;
                    if (mergedQty == null || gq.compareTo(mergedQty) < 0)
                    {
                        mergedQty = gq;
                    }
                }
            }
        }
        // 如果没有已完工的工序（理论上不应该走到这里，因为当前工序刚完工），取当前工序的合格数
        if (mergedQty == null)
        {
            mergedQty = currentDispatch.getGoodQty() != null ? currentDispatch.getGoodQty() : BigDecimal.ZERO;
        }

        // ===== 5. 为下一组的每道工序创建派工单 =====
        List<MmsDispatch> nextDispatches = new ArrayList<>();
        int parallelCount = 0;
        for (MmsWoRouteSnapshot snapshot : snapshots)
        {
            if (snapshot.getStepSeq() != null && snapshot.getStepSeq().equals(nextStepSeq))
            {
                parallelCount++;
                MmsDispatch nextDispatch = new MmsDispatch();
                nextDispatch.setDispatchNo(mkNumberRuleService.generateNumber("mms_dispatch"));
                nextDispatch.setWorkOrderId(currentDispatch.getWorkOrderId());
                nextDispatch.setWorkOrderNo(currentDispatch.getWorkOrderNo());
                nextDispatch.setProductCode(currentDispatch.getProductCode());
                nextDispatch.setProductName(currentDispatch.getProductName());
                nextDispatch.setSpecModel(currentDispatch.getSpecModel());
                nextDispatch.setUnit(currentDispatch.getUnit());
                nextDispatch.setOpSeq(snapshot.getStepSeq());
                nextDispatch.setProcessId(snapshot.getProcessId());
                nextDispatch.setProcessName(snapshot.getProcessName());
                nextDispatch.setResourceId(snapshot.getResourceId());
                nextDispatch.setResourceName(snapshot.getResourceName());
                nextDispatch.setPlanQty(mergedQty);
                nextDispatch.setGoodQty(BigDecimal.ZERO);
                nextDispatch.setDefectQty(BigDecimal.ZERO);
                nextDispatch.setPlanStart(currentDispatch.getPlanStart());
                nextDispatch.setPlanEnd(currentDispatch.getPlanEnd());
                nextDispatch.setStatus("0"); // 待开工
                nextDispatch.setDelFlag("0");
                nextDispatch.setCreateBy(username);
                nextDispatch.setCreateTime(now);
                String mergeDesc = groupDispatches != null && groupDispatches.size() > 1
                        ? "，汇合数量=" + mergedQty + "（并行组最小合格数）"
                        : "";
                nextDispatch.setRemark("前道工序完工自动生成（工序" + currentOpSeq + "→" + nextStepSeq
                        + (parallelCount > 1 ? "，并行" : "") + mergeDesc + "）");
                dispatchMapper.insertDispatch(nextDispatch);
                nextDispatches.add(nextDispatch);
            }
        }
    }

    /**
     * 检查工单是否所有工序都已完工（或取消），如果是则自动完工工单
     * 末道工序完工 + 同组工序全部完工 → 工单状态从执行中(2) → 已完工(3)
     * 同时自动生成完工质检单（质检独立业务，不卡住工单状态）
     *
     * @param currentDispatch 当前完工的派工单
     * @param username 操作人
     * @param now 当前时间
     */
    private void autoFinishWorkOrderIfAllCompleted(MmsDispatch currentDispatch, String username, Date now)
    {
        if (currentDispatch.getWorkOrderId() == null)
        {
            return;
        }
        // 查询工单的工艺路线快照（按 step_seq 排序）
        List<MmsWoRouteSnapshot> snapshots = woRouteSnapshotMapper
                .selectRouteSnapshotByWorkOrderId(currentDispatch.getWorkOrderId());
        if (snapshots == null || snapshots.isEmpty())
        {
            return;
        }
        // 查询该工单下所有派工单
        MmsDispatch queryAll = new MmsDispatch();
        queryAll.setWorkOrderId(currentDispatch.getWorkOrderId());
        List<MmsDispatch> allDispatches = dispatchMapper.selectDispatchList(queryAll);
        if (allDispatches == null || allDispatches.isEmpty())
        {
            return;
        }

        // 检查每道工序快照是否都有对应的已完工(2)或已取消(3)的派工单
        for (MmsWoRouteSnapshot snapshot : snapshots)
        {
            boolean found = false;
            for (MmsDispatch disp : allDispatches)
            {
                if (snapshot.getStepSeq() != null && snapshot.getStepSeq().equals(disp.getOpSeq())
                        && ("2".equals(disp.getStatus()) || "3".equals(disp.getStatus())))
                {
                    found = true;
                    break;
                }
            }
            if (!found)
            {
                // 还有工序未完工，不自动完工工单
                return;
            }
        }

        // 所有工序都已完工（或取消），自动完工工单
        MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(currentDispatch.getWorkOrderId());
        if (wo == null)
        {
            return;
        }
        // 只有执行中(2)状态才自动完工
        if (!"2".equals(wo.getStatus()))
        {
            return;
        }
        wo.setStatus("3"); // 已完工
        wo.setActualFinish(now);
        wo.setUpdateBy(username);
        wo.setUpdateTime(now);
        workOrderMapper.updateWorkOrder(wo);

        // 记录审计日志
        MmsWorkOrderAuditLog log = new MmsWorkOrderAuditLog();
        log.setWorkOrderId(wo.getWorkOrderId());
        log.setAuditBy(username);
        log.setAuditAction("finish");
        log.setAuditTime(now);
        log.setAuditRemark("末道工序完工自动完工工单");
        workOrderMapper.insertAuditLog(log);

        // ===== 自动生成完工质检单 =====
        // 工单完工时自动创建一条完工检(qc_type=2)类型的质检单
        // 质检作为独立业务流转，不卡住工单状态
        // 预填充来自派工完工的检验数量、不良数量等数据，质检员只需确认检验结果即可
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
        // 检验数量 = 工单完工数量（合格+不良中的最终产出）
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
    }

    private MmsDispatch getAndCheckDispatch(Long dispatchId)
    {
        MmsDispatch d = dispatchMapper.selectDispatchById(dispatchId);
        if (d == null)
        {
            throw new ServiceException("派工单不存在或已删除");
        }
        return d;
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待开工";
            case "1": return "进行中";
            case "2": return "已完成";
            case "3": return "已取消";
            default: return "未知(" + status + ")";
        }
    }
}
