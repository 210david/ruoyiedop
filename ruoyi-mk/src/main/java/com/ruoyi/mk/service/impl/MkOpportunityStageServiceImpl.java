package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mk.domain.MkOpportunityStage;
import com.ruoyi.mk.mapper.MkOpportunityStageMapper;
import com.ruoyi.mk.service.IMkOpportunityStageService;

/**
 * 销售阶段配置 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkOpportunityStageServiceImpl implements IMkOpportunityStageService
{
    @Autowired
    private MkOpportunityStageMapper mkOpportunityStageMapper;

    @Override
    public List<MkOpportunityStage> selectStageList(MkOpportunityStage stage)
    {
        return mkOpportunityStageMapper.selectStageList(stage);
    }

    @Override
    public MkOpportunityStage selectStageById(Long stageId)
    {
        return mkOpportunityStageMapper.selectStageById(stageId);
    }

    @Override
    public int insertStage(MkOpportunityStage stage)
    {
        if (stage.getStatus() == null)
        {
            stage.setStatus("0");
        }
        return mkOpportunityStageMapper.insertStage(stage);
    }

    @Override
    public int updateStage(MkOpportunityStage stage)
    {
        return mkOpportunityStageMapper.updateStage(stage);
    }

    @Override
    public int deleteStageByIds(Long[] stageIds)
    {
        return mkOpportunityStageMapper.deleteStageByIds(stageIds);
    }
}
