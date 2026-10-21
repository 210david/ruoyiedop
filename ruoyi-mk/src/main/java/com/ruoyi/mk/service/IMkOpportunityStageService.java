package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkOpportunityStage;

/**
 * 销售阶段配置 Service接口
 *
 * @author ruoyi
 */
public interface IMkOpportunityStageService
{
    public List<MkOpportunityStage> selectStageList(MkOpportunityStage stage);
    public MkOpportunityStage selectStageById(Long stageId);
    public int insertStage(MkOpportunityStage stage);
    public int updateStage(MkOpportunityStage stage);
    public int deleteStageByIds(Long[] stageIds);
}
