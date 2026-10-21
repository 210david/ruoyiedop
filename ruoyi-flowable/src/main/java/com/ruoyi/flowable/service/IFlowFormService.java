package com.ruoyi.flowable.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.flowable.domain.FlowForm;

/**
 * 流程表单 Service 接口（建模引擎：表单设计 + 数据表生成 + 表单发起）
 *
 * @author ruoyi
 */
public interface IFlowFormService
{
    /** 查询表单列表 */
    public List<FlowForm> selectFlowFormList(FlowForm form);

    /** 根据ID查询表单 */
    public FlowForm selectFlowFormByFormId(Long formId);

    /** 根据流程Key查询绑定表单 */
    public FlowForm selectFlowFormByProcessKey(String processKey);

    /** 新增表单 */
    public int insertFlowForm(FlowForm form);

    /** 修改表单 */
    public int updateFlowForm(FlowForm form);

    /** 删除表单 */
    public int deleteFlowFormByFormIds(Long[] formIds);

    /** 按表单字段生成数据表（CREATE TABLE），返回表名 */
    public String createTable(Long formId);

    /**
     * 通用流程发起：绑定表单时将表单数据写入数据表并作为业务数据，
     * 未绑定时直接以 variables 发起。返回流程实例ID。
     *
     * @param definitionKey 流程Key
     * @param formId        表单ID（可空）
     * @param variables     流程变量/表单数据
     * @param approverUserId 首节点审批人（可空）
     * @return 流程实例ID
     */
    public String launch(String definitionKey, Long formId, Map<String, Object> variables, Long approverUserId, String title);
}
