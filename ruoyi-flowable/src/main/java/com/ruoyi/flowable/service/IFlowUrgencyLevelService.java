package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowUrgencyLevel;

/**
 * 紧急程度配置 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowUrgencyLevelService
{
    /** 条件查询列表 */
    public List<FlowUrgencyLevel> selectFlowUrgencyLevelList(FlowUrgencyLevel query);

    /** 按主键查询 */
    public FlowUrgencyLevel selectFlowUrgencyLevelById(Long levelId);

    /** 可选档位（供发起页下拉使用） */
    public List<FlowUrgencyLevel> selectSelectableList();

    /** 新增 */
    public int insertFlowUrgencyLevel(FlowUrgencyLevel level);

    /** 修改 */
    public int updateFlowUrgencyLevel(FlowUrgencyLevel level);

    /** 按主键删除 */
    public int deleteFlowUrgencyLevelById(Long levelId);

    /** 批量删除 */
    public int deleteFlowUrgencyLevelByIds(Long[] levelIds);

    /** 校验档位值是否可选（正常且未封存） */
    public boolean isValidSelectable(String levelValue);
}
