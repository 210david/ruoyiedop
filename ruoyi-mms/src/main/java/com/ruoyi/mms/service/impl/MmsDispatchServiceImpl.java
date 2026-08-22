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
import com.ruoyi.mms.domain.MmsWoRouteSnapshot;
import com.ruoyi.mms.domain.MmsWorkReport;
import com.ruoyi.mms.mapper.MmsDispatchMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.mapper.MmsWoRouteSnapshotMapper;
import com.ruoyi.mms.mapper.MmsWorkReportMapper;
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
    public int startDispatch(Long dispatchId, String operatorName, Long teamId, String teamName)
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
        // 校验关联工单状态：工单暂停(7)或作废(8)时不允许开工
        if (d.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(d.getWorkOrderId());
            if (wo != null)
            {
                if ("7".equals(wo.getStatus()))
                {
                    throw new ServiceException("关联工单[" + wo.getWorkOrderNo() + "]已暂停，请先恢复工单后再开工");
                }
                if ("8".equals(wo.getStatus()))
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
        d.setActualStart(new Date());
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
                wo.setActualStart(new Date());
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
        if (d.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(d.getWorkOrderId());
            if (wo != null)
            {
                // 累计完工数量
                BigDecimal woFinished = wo.getFinishedQty() == null ? BigDecimal.ZERO : wo.getFinishedQty();
                BigDecimal woQualified = wo.getQualifiedQty() == null ? BigDecimal.ZERO : wo.getQualifiedQty();
                BigDecimal woDefect = wo.getDefectQty() == null ? BigDecimal.ZERO : wo.getDefectQty();

                wo.setFinishedQty(woFinished.add(goodQty).add(defectQty));
                wo.setQualifiedQty(woQualified.add(goodQty));
                wo.setDefectQty(woDefect.add(defectQty));

                // 工单状态联动：已下达(1) → 执行中(2)，执行中(2) → 报工中(3)
                if ("1".equals(wo.getStatus()))
                {
                    wo.setStatus("2");
                    wo.setActualStart(now);
                }
                else if ("2".equals(wo.getStatus()))
                {
                    wo.setStatus("3");
                }

                wo.setUpdateBy(username);
                wo.setUpdateTime(now);
                workOrderMapper.updateWorkOrder(wo);
            }
        }

        // ===== 4. 自动创建下一道工序的派工单 =====
        createNextDispatch(d, username, now);

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
