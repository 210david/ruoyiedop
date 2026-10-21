package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowProcdefExt;

/**
 * 流程定义扩展（版本备注/活动版本） Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowProcdefExtMapper
{
    public FlowProcdefExt selectFlowProcdefExtByProcdefId(String procdefId);

    public List<FlowProcdefExt> selectFlowProcdefExtByProcKey(String procKey);

    public int insertFlowProcdefExt(FlowProcdefExt ext);

    public int updateFlowProcdefExt(FlowProcdefExt ext);

    /**
     * 同步流程定义名称：按流程 Key 更新 act_re_procdef.NAME_。
     * 用途：模型改名后同步已部署定义名（Flowable 7.2 RepositoryService 无重命名 API）。
     *
     * @param procKey 流程 Key
     * @param name    新的流程名称
     * @return 受影响行数
     */
    public int updateDefinitionNameByKey(@Param("procKey") String procKey, @Param("name") String name);
}
