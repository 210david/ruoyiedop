package com.ruoyi.flowable.config;

/**
 * 流程「退回（驳回）」相关常量（单一数据源）
 *
 * 业务口径（对齐泛微）：
 * <ul>
 * <li>驳回 = 退回到前面已经走过的某个节点，而不是继续往下走；</li>
 * <li>被退回的任务落在目标节点办理人的待办里，并在待办「退回」页签单独归类；</li>
 * <li>Flowable 无法把执行流停在「开始事件」上，所以发起人需要接收退回时，
 *     部署时会额外注入一个只有出线、没有入线的「提交申请」用户任务（发起人节点），
 *     正常发起流程不会经过它，只有退回时才跳转过来。</li>
 * </ul>
 *
 * @author ruoyi
 */
public class FlowReturnConstants
{
    /** 发起人（申请）节点的固定节点ID：部署时注入，正常流转不经过 */
    public static final String INITIATOR_NODE_ID = "applyNode";

    /** 用户任务扩展属性：true 表示该节点是发起人（申请）节点 */
    public static final String ATTR_INITIATOR_NODE = "initiatorNode";

    /** 任务级变量：1 表示该任务是被上游退回的 */
    public static final String VAR_RETURNED = "flowReturned";

    /** 任务级变量：退回来源环节名称 */
    public static final String VAR_RETURN_FROM = "flowReturnFrom";

    /** 任务级变量：退回操作人姓名 */
    public static final String VAR_RETURN_BY = "flowReturnBy";

    /** 任务级变量：退回时间（yyyy-MM-dd HH:mm:ss） */
    public static final String VAR_RETURN_TIME = "flowReturnTime";

    /** 可退回节点类型：已走过的用户任务节点 */
    public static final String NODE_TYPE_USER_TASK = "userTask";

    /** 可退回节点类型：发起人（申请）节点 */
    public static final String NODE_TYPE_INITIATOR = "initiator";

    /** 是否发起人节点标记 */
    public static boolean isInitiatorNode(Boolean flag)
    {
        return Boolean.TRUE.equals(flag);
    }
}
