package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowOpinionPreset;

/**
 * 签字意见-个人常用意见 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowOpinionPresetMapper
{
    /** 某位用户的常用意见（按排序正序） */
    public List<FlowOpinionPreset> selectByUser(@Param("userId") Long userId);

    /** 清空某位用户的常用意见（整体覆盖保存 / 恢复默认时先删后插） */
    public int deleteByUser(@Param("userId") Long userId);

    /** 批量新增（同一事务内由 Service 调用） */
    public int batchInsert(@Param("list") List<FlowOpinionPreset> list);
}
