package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
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
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkReport;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.mapper.MmsWorkReportMapper;
import com.ruoyi.mms.service.IMmsWorkReportService;

/**
 * 报工 Service实现
 *
 * 报工状态机：
 * 0(待审核) → 1(已审核) / 2(已驳回)
 *
 * 审核通过时联动更新工单：
 * - 工单状态 → 执行中(2) 或 报工中(3)
 * - 工单完工数量累计
 * - 工单合格数量累计
 * - 工单不良数量累计
 *
 * @author ruoyi
 */
@Service
public class MmsWorkReportServiceImpl implements IMmsWorkReportService
{
    @Autowired
    private MmsWorkReportMapper workReportMapper;

    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsWorkReport> selectWorkReportList(MmsWorkReport workReport)
    {
        return workReportMapper.selectWorkReportList(workReport);
    }

    @Override
    public MmsWorkReport selectWorkReportById(Long reportId)
    {
        return workReportMapper.selectWorkReportById(reportId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWorkReport(MmsWorkReport workReport)
    {
        // 自动生成报工单号
        if (StringUtils.isEmpty(workReport.getReportNo()))
        {
            workReport.setReportNo(mkNumberRuleService.generateNumber("mms_work_report"));
        }
        // 默认状态为待审核
        if (StringUtils.isEmpty(workReport.getStatus()))
        {
            workReport.setStatus("0");
        }
        // 默认来源为手动报工
        if (StringUtils.isEmpty(workReport.getSource()))
        {
            workReport.setSource("1");
        }
        workReport.setDelFlag("0");
        workReport.setCreateBy(SecurityUtils.getUsername());
        workReport.setCreateTime(DateUtils.getNowDate());
        workReport.setReportBy(SecurityUtils.getUsername());
        workReport.setReportTime(new Date());
        // 校验工单状态：工单需为已下达(1)或执行中(2)或报工中(3)
        if (workReport.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(workReport.getWorkOrderId());
            if (wo != null)
            {
                if ("0".equals(wo.getStatus()))
                {
                    throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]未下达，不允许报工");
                }
                if ("6".equals(wo.getStatus()) || "8".equals(wo.getStatus()))
                {
                    throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]已关闭/作废，不允许报工");
                }
            }
        }
        return workReportMapper.insertWorkReport(workReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkReport(MmsWorkReport workReport)
    {
        // 已审核的报工不允许修改
        MmsWorkReport existing = workReportMapper.selectWorkReportById(workReport.getReportId());
        if (existing != null && "1".equals(existing.getStatus()))
        {
            throw new ServiceException("报工记录已审核，不允许修改");
        }
        return workReportMapper.updateWorkReport(workReport);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWorkReportByIds(Long[] reportIds)
    {
        // 已审核的报工不允许删除
        for (Long id : reportIds)
        {
            MmsWorkReport report = workReportMapper.selectWorkReportById(id);
            if (report != null && "1".equals(report.getStatus()))
            {
                throw new ServiceException("报工记录[" + report.getReportNo() + "]已审核，不允许删除");
            }
        }
        return workReportMapper.deleteWorkReportByIds(reportIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditWorkReport(Long reportId, String status, String auditRemark)
    {
        MmsWorkReport report = workReportMapper.selectWorkReportById(reportId);
        if (report == null)
        {
            throw new ServiceException("报工记录不存在或已删除");
        }
        // 状态校验：只有待审核(0)可审核
        if (!"0".equals(report.getStatus()))
        {
            throw new ServiceException("报工记录[" + report.getReportNo() + "]当前状态为" + statusName(report.getStatus()) + "，只有待审核状态可审核");
        }
        // status: "1"=通过, "2"=驳回
        if (!"1".equals(status) && !"2".equals(status))
        {
            throw new ServiceException("无效的审核状态值，1=通过，2=驳回");
        }

        report.setStatus(status);
        report.setAuditBy(SecurityUtils.getUsername());
        report.setAuditTime(new Date());
        report.setAuditRemark(auditRemark);
        int rows = workReportMapper.updateWorkReport(report);

        // 审核通过时联动更新工单进度
        if ("1".equals(status) && report.getWorkOrderId() != null)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(report.getWorkOrderId());
            if (wo != null)
            {
                // 累计完工数量
                BigDecimal finishedQty = wo.getFinishedQty() == null ? BigDecimal.ZERO : wo.getFinishedQty();
                BigDecimal qualifiedQty = wo.getQualifiedQty() == null ? BigDecimal.ZERO : wo.getQualifiedQty();
                BigDecimal defectQty = wo.getDefectQty() == null ? BigDecimal.ZERO : wo.getDefectQty();

                BigDecimal addGood = report.getGoodQty() == null ? BigDecimal.ZERO : report.getGoodQty();
                BigDecimal addDefect = report.getDefectQty() == null ? BigDecimal.ZERO : report.getDefectQty();

                wo.setFinishedQty(finishedQty.add(addGood).add(addDefect));
                wo.setQualifiedQty(qualifiedQty.add(addGood));
                wo.setDefectQty(defectQty.add(addDefect));

                // 工单状态联动：已下达(1) → 执行中(2)
                if ("1".equals(wo.getStatus()))
                {
                    wo.setStatus("2");
                    wo.setActualStart(new Date());
                }
                // 执行中(2) → 报工中(3)（有报工记录后）
                else if ("2".equals(wo.getStatus()))
                {
                    wo.setStatus("3");
                }

                wo.setUpdateBy(SecurityUtils.getUsername());
                workOrderMapper.updateWorkOrder(wo);
            }
        }

        return rows;
    }

    // ========== 私有辅助方法 ==========

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待审核";
            case "1": return "已审核";
            case "2": return "已驳回";
            default: return "未知(" + status + ")";
        }
    }

}
