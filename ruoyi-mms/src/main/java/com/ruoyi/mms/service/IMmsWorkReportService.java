package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsWorkReport;

/**
 * 报工 Service接口
 *
 * @author ruoyi
 */
public interface IMmsWorkReportService
{
    /** 标准CRUD */
    public List<MmsWorkReport> selectWorkReportList(MmsWorkReport workReport);
    public MmsWorkReport selectWorkReportById(Long reportId);
    public int insertWorkReport(MmsWorkReport workReport);
    public int updateWorkReport(MmsWorkReport workReport);
    public int deleteWorkReportByIds(Long[] reportIds);

    /** 报工审核：0(待审核) → 1(已审核) 或 2(已驳回) */
    public int auditWorkReport(Long reportId, String status, String auditRemark);
}
