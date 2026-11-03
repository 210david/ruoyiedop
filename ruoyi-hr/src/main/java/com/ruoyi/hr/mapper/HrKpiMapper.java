package com.ruoyi.hr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hr.domain.HrKpiScheme;
import com.ruoyi.hr.domain.HrKpiItem;
import com.ruoyi.hr.domain.HrKpiRecord;

/**
 * KPI考核 Mapper接口（需求三）
 */
public interface HrKpiMapper {
    // ---- 方案与指标 ----
    public List<HrKpiScheme> selectSchemeList(HrKpiScheme scheme);

    public HrKpiScheme selectSchemeById(Long schemeId);

    public int insertScheme(HrKpiScheme scheme);

    public int updateScheme(HrKpiScheme scheme);

    public int deleteSchemeByIds(Long[] schemeIds);

    public List<HrKpiItem> selectItemsByScheme(Long schemeId);

    public int insertItem(HrKpiItem item);

    public int deleteItemsByScheme(Long schemeId);

    // ---- 考核记录 ----
    public List<HrKpiRecord> selectRecordList(HrKpiRecord record);

    public HrKpiRecord selectRecordById(Long recordId);

    public int insertRecord(HrKpiRecord record);

    public int updateRecord(HrKpiRecord record);

    /** 排名：按周期最终得分降序 */
    public List<HrKpiRecord> selectRecordRank(@Param("period") String period);
}
