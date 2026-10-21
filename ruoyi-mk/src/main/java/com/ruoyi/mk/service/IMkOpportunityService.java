package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkOpportunity;
import com.ruoyi.mk.domain.MkOpportunityStageLog;

/**
 * 商机 Service接口
 *
 * @author ruoyi
 */
public interface IMkOpportunityService
{
    public List<MkOpportunity> selectOpportunityList(MkOpportunity opportunity);
    public MkOpportunity selectOpportunityById(Long opportunityId);
    public int insertOpportunity(MkOpportunity opportunity);
    public int updateOpportunity(MkOpportunity opportunity);
    public int deleteOpportunityByIds(Long[] opportunityIds);
    /** 按阶段分组查询商机 */
    public List<MkOpportunity> selectOpportunityByStage(String stageCode);

    /** 阶段推进 */
    public int advanceStage(Long opportunityId, String toStageCode, String remark);
    /** 阶段退回 */
    public int retreatStage(Long opportunityId, String toStageCode, String remark);
    /** 赢单关单 */
    public int winOpportunity(Long opportunityId, MkOpportunity param);
    /** 输单关单 */
    public int loseOpportunity(Long opportunityId, MkOpportunity param);
    /** 重新打开 */
    public int reopenOpportunity(Long opportunityId);
    /** 查询阶段变更日志 */
    public List<MkOpportunityStageLog> selectStageLogList(Long opportunityId);
    /** 查询商机关联数据（互动记录、合同、订单） */
    public Map<String, Object> selectOpportunityRelations(Long opportunityId);
    /** 查询停滞商机列表（阶段超时） */
    public List<MkOpportunity> selectStagnantOpportunities();
}
