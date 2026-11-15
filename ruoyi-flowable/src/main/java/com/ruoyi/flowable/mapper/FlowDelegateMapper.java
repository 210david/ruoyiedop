package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowDelegate;

/**
 * 流程转办记录 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowDelegateMapper
{
    /** 新增转办记录 */
    public int insertDelegate(FlowDelegate delegate);

    /** 某个流程实例的全部转办记录（按转办时间正序） */
    public List<FlowDelegate> selectByInstance(@Param("instanceId") String instanceId);

    /** 我转办出去的记录（按转办时间倒序，已办/流程查询口径用） */
    public List<FlowDelegate> selectByFromUser(@Param("fromId") Long fromId);
}
