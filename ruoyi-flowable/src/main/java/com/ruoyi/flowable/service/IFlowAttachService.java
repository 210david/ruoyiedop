package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowAttach;

/**
 * 流程附件Service接口
 *
 * @author ruoyi
 */
public interface IFlowAttachService
{
    /** 按流程实例查询附件（相关资源页签） */
    public List<FlowAttach> selectByInstance(String instanceId);

    /** 登记附件（文件本体由 /common/upload 落盘，此处只记流程归属） */
    public int addAttach(FlowAttach attach);

    /** 删除附件（仅上传人本人或管理员） */
    public int deleteAttach(Long attachId);
}
