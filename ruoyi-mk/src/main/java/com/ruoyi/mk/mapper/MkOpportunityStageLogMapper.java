package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkOpportunityStageLog;

/**
 * 商机阶段变更日志 Mapper
 *
 * @author ruoyi
 */
public interface MkOpportunityStageLogMapper
{
    /** 查询商机的阶段变更日志列表 */
    public List<MkOpportunityStageLog> selectStageLogList(Long opportunityId);

    /** 新增阶段变更日志 */
    public int insertStageLog(MkOpportunityStageLog stageLog);
}
