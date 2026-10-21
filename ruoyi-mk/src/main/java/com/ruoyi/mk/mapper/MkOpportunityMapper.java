package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOpportunity;

public interface MkOpportunityMapper
{
    public List<MkOpportunity> selectOpportunityList(MkOpportunity opportunity);
    public MkOpportunity selectOpportunityById(Long opportunityId);
    public int insertOpportunity(MkOpportunity opportunity);
    public int updateOpportunity(MkOpportunity opportunity);
    public int deleteOpportunityByIds(Long[] opportunityIds);
    /** 按阶段分组统计 */
    public List<MkOpportunity> selectOpportunityByStage(String stageCode);
    /** 更新商机的阶段和状态 */
    public int updateOpportunityStage(MkOpportunity opportunity);
    /** 查询停滞商机（阶段停留时间超过maxDays） */
    public List<MkOpportunity> selectStagnantOpportunities();
}
