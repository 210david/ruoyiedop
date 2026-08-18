package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsWorkReport;

public interface MmsWorkReportMapper {
    public List<MmsWorkReport> selectWorkReportList(MmsWorkReport report);
    public MmsWorkReport selectWorkReportById(Long reportId);
    public int insertWorkReport(MmsWorkReport report);
    public int updateWorkReport(MmsWorkReport report);
    public int deleteWorkReportByIds(Long[] reportIds);
}