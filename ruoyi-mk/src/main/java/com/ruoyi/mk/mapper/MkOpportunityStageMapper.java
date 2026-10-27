package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOpportunityStage;

public interface MkOpportunityStageMapper
{
    public List<MkOpportunityStage> selectStageList(MkOpportunityStage stage);
    public MkOpportunityStage selectStageById(Long stageId);
    public int insertStage(MkOpportunityStage stage);
    public int updateStage(MkOpportunityStage stage);
    public int deleteStageByIds(Long[] stageIds);
}
