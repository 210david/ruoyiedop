package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsFinishAnalysis;

/**
 * 工单完工分析 Mapper
 *
 * @author ruoyi
 */
public interface MmsFinishAnalysisMapper
{
    /**
     * 工单完工分析明细列表
     */
    public List<MmsFinishAnalysis> selectFinishAnalysisList(MmsFinishAnalysis query);

    /**
     * 汇总统计行
     */
    public MmsFinishAnalysis selectFinishAnalysisSummary(MmsFinishAnalysis query);
}
