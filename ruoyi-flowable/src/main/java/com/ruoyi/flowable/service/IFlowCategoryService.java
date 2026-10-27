package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowCategory;

/**
 * 流程分类 Service 接口（基于字典 flowable_category，含引用占用校验）
 *
 * @author ruoyi
 */
public interface IFlowCategoryService
{
    /** 查询流程分类列表（含引用统计） */
    public List<FlowCategory> selectFlowCategoryList(FlowCategory query);

    /** 获取流程分类详情 */
    public FlowCategory selectFlowCategoryByCode(Long dictCode);

    /** 新增流程分类 */
    public int insertFlowCategory(FlowCategory category);

    /** 修改流程分类 */
    public int updateFlowCategory(FlowCategory category);

    /** 删除流程分类（被引用时禁止） */
    public int deleteFlowCategoryByCodes(Long[] dictCodes);
}
