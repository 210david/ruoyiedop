package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrKpiScheme;
import com.ruoyi.hr.domain.HrKpiRecord;

/**
 * KPI考核 Service接口（需求三）
 */
public interface IHrKpiService {
    // ---- 方案 ----
    public List<HrKpiScheme> selectSchemeList(HrKpiScheme scheme);

    public HrKpiScheme selectSchemeById(Long schemeId);

    /** 新增方案（含指标明细，权重合计须=100） */
    public int insertScheme(HrKpiScheme scheme);

    /** 修改方案（重建指标明细） */
    public int updateScheme(HrKpiScheme scheme);

    public int deleteSchemeByIds(Long[] schemeIds);

    // ---- 考核记录：发起—自评—评分—确认—申诉 ----
    public List<HrKpiRecord> selectRecordList(HrKpiRecord record);

    /** 发起考核：对指定员工批量生成考核记录 */
    public int initiateRecords(Long schemeId, String period, List<Long> employeeIds);

    /** 自评（状态 发起→已自评） */
    public int selfScore(HrKpiRecord record);

    /** 上级评分（已自评→已评分，最终得分=上级评分） */
    public int superiorScore(HrKpiRecord record);

    /** 确认（已评分→已确认） */
    public int confirmRecord(Long recordId);

    /** 申诉（已确认→申诉中） */
    public int appealRecord(Long recordId, String reason);

    /** 申诉处理（申诉中→已完成，可调整最终得分） */
    public int handleAppeal(Long recordId, String result, java.math.BigDecimal finalScore);

    /** 考核排名（按周期最终得分降序） */
    public List<HrKpiRecord> selectRecordRank(String period);
}
