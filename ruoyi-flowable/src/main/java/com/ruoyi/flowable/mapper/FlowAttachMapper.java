package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowAttach;

/**
 * 流程附件Mapper接口
 *
 * @author ruoyi
 */
public interface FlowAttachMapper
{
    /** 按流程实例查询附件 */
    public List<FlowAttach> selectByInstance(@Param("instanceId") String instanceId);

    /** 新增附件 */
    public int insertAttach(FlowAttach attach);

    /** 删除附件（按ID，uploaderId 非空时同时校验上传人） */
    public int deleteAttach(@Param("attachId") Long attachId, @Param("uploaderId") Long uploaderId);
}
