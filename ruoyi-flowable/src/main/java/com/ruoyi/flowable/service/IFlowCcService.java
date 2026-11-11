package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowCc;

/**
 * 流程抄送（待阅）Service 接口
 *
 * @author ruoyi
 */
public interface IFlowCcService
{
    /** 我的待阅列表（数据边界固定为当前登录人） */
    public List<FlowCc> selectMyCcList(FlowCc query);

    /** 我的未读抄送数量 */
    public int countUnread();

    /** 标记单条已读 */
    public int markRead(Long ccId);

    /** 全部标记已读 */
    public int markAllRead();

    /**
     * 办理页手动抄送：办理人把当前任务所在的流程抄送给指定人员
     *
     * 与节点级自动抄送（FlowCcListener）共用 flow_cc 表，接收人同样在「待办流程-待阅」查看；
     * 同一流程同一节点同一接收人只保留一条记录（自动抄送与手动抄送互相去重），抄送人本人不抄送。
     *
     * @param taskId   当前任务ID（须为当前登录人办理）
     * @param userIds  抄送接收人用户ID
     * @param comment  抄送说明（可选，仅记日志，不写入流转意见以免干扰审批意见）
     * @return 实际新增的抄送记录数（去重后可能为 0）
     */
    public int sendCc(String taskId, List<Long> userIds, String comment);

    /**
     * 某个流程实例的全部抄送记录
     *
     * 用于办理页 / 流程详情页在「审批意见」区域底部展示「抄送」信息（对齐泛微签批意见区）。
     * 仅按实例查询，不做接收人隔离；调用方须保证当前用户可见该流程。
     *
     * @param instanceId 流程实例ID
     * @return 抄送记录（按抄送时间正序），无记录时返回空列表
     */
    public List<FlowCc> selectByInstance(String instanceId);
}
