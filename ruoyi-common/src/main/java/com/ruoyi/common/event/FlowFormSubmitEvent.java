package com.ruoyi.common.event;

import java.util.Map;
import org.springframework.context.ApplicationEvent;

/**
 * 流程表单提交联动事件
 *
 * 流程管理模块（ruoyi-flowable）与业务模块（如 ruoyi-oa 综合办公）存在依赖方向约束
 * （业务模块依赖 common/flowable 基础能力，flowable 不可反向依赖业务模块），
 * 因此流程发起/办理/暂存时通过 Spring 事件发布表单数据，由业务侧监听器消费，
 * 实现「流程表单浏览框字段 ↔ 业务台账」的关联绑定（如车辆/司机/印章的 flow_instance_id 写回）。
 *
 * 业务侧监听示例：
 * <pre>
 * &#64;TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
 * public void onFlowFormSubmit(FlowFormSubmitEvent event) { ... }
 * </pre>
 *
 * @author ruoyi
 */
public class FlowFormSubmitEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /** 触发阶段：流程发起 */
    public static final String PHASE_START = "start";
    /** 触发阶段：任务办理（流转） */
    public static final String PHASE_COMPLETE = "complete";
    /** 触发阶段：表单暂存 */
    public static final String PHASE_SAVE = "save";

    /** 触发阶段 */
    private final String phase;
    /** 流程定义Key（busUseApply/sealApply 等） */
    private final String processKey;
    /** 表单ID */
    private final Long formId;
    /** 流程实例ID */
    private final String processInstanceId;
    /** 业务数据行ID（表单动态表主键） */
    private final String businessKey;
    /** 表单动态表名 */
    private final String tableName;
    /** 表单字段最新值（合并后的流程变量快照） */
    private final Map<String, Object> variables;
    /** 当前办理节点Key（发起/暂存时可能为空） */
    private final String taskDefinitionKey;
    /** 当前办理节点名称（发起/暂存时可能为空） */
    private final String taskName;

    public FlowFormSubmitEvent(Object source, String phase, String processKey, Long formId,
            String processInstanceId, String businessKey, String tableName, Map<String, Object> variables)
    {
        this(source, phase, processKey, formId, processInstanceId, businessKey, tableName, variables, null, null);
    }

    public FlowFormSubmitEvent(Object source, String phase, String processKey, Long formId,
            String processInstanceId, String businessKey, String tableName, Map<String, Object> variables,
            String taskDefinitionKey, String taskName)
    {
        super(source);
        this.phase = phase;
        this.processKey = processKey;
        this.formId = formId;
        this.processInstanceId = processInstanceId;
        this.businessKey = businessKey;
        this.tableName = tableName;
        this.variables = variables;
        this.taskDefinitionKey = taskDefinitionKey;
        this.taskName = taskName;
    }

    public String getPhase() { return phase; }
    public String getProcessKey() { return processKey; }
    public Long getFormId() { return formId; }
    public String getProcessInstanceId() { return processInstanceId; }
    public String getBusinessKey() { return businessKey; }
    public String getTableName() { return tableName; }
    public Map<String, Object> getVariables() { return variables; }
    public String getTaskDefinitionKey() { return taskDefinitionKey; }
    public String getTaskName() { return taskName; }
}
