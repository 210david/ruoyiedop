package com.ruoyi.flowable.mapper;

import java.util.List;
import com.ruoyi.flowable.domain.FlowUrgencyLevel;

/**
 * 紧急程度配置 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowUrgencyLevelMapper
{
    /** 条件查询列表 */
    public List<FlowUrgencyLevel> selectFlowUrgencyLevelList(FlowUrgencyLevel query);

    /** 按主键查询 */
    public FlowUrgencyLevel selectFlowUrgencyLevelById(Long levelId);

    /** 可选档位（正常 + 未封存，按 show_order 升序）：供发起页下拉使用 */
    public List<FlowUrgencyLevel> selectSelectableList();

    /** 按档位值查询（校验用） */
    public FlowUrgencyLevel selectByLevelValue(String levelValue);

    /** 新增 */
    public int insertFlowUrgencyLevel(FlowUrgencyLevel level);

    /** 修改 */
    public int updateFlowUrgencyLevel(FlowUrgencyLevel level);

    /** 按主键删除 */
    public int deleteFlowUrgencyLevelById(Long levelId);

    /** 批量删除 */
    public int deleteFlowUrgencyLevelByIds(Long[] levelIds);
}
