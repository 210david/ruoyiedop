package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowCc;

/**
 * 流程抄送（待阅）Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowCcMapper
{
    /** 我的待阅列表（按接收人隔离） */
    public List<FlowCc> selectMyCcList(FlowCc query);

    /** 未读数量 */
    public int countUnread(@Param("receiverId") Long receiverId);

    /** 标记单条已读（仅本人） */
    public int markRead(@Param("ccId") Long ccId, @Param("receiverId") Long receiverId);

    /** 全部标记已读 */
    public int markAllRead(@Param("receiverId") Long receiverId);

    /** 新增抄送记录 */
    public int insertCc(FlowCc cc);

    /** 同一实例同一节点同一接收人是否已存在（防重复抄送） */
    public int countByInstanceTaskReceiver(@Param("instanceId") String instanceId,
                                           @Param("taskId") String taskId,
                                           @Param("receiverId") Long receiverId);

    /** 某个流程实例的全部抄送记录（办理页/详情页展示「抄送」信息用） */
    public List<FlowCc> selectByInstance(@Param("instanceId") String instanceId);
}
