package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mms.domain.MmsFinishAnalysis;
import com.ruoyi.mms.mapper.MmsFinishAnalysisMapper;
import com.ruoyi.mms.service.IMmsFinishAnalysisService;

/**
 * 工单完工分析 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsFinishAnalysisServiceImpl implements IMmsFinishAnalysisService
{
    @Autowired
    private MmsFinishAnalysisMapper mmsFinishAnalysisMapper;

    @Override
    public List<MmsFinishAnalysis> selectFinishAnalysisList(MmsFinishAnalysis query)
    {
        return mmsFinishAnalysisMapper.selectFinishAnalysisList(query);
    }

    @Override
    public MmsFinishAnalysis selectFinishAnalysisSummary(MmsFinishAnalysis query)
    {
        return mmsFinishAnalysisMapper.selectFinishAnalysisSummary(query);
    }
}
