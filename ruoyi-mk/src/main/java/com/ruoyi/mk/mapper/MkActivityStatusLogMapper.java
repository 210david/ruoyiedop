package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkActivityStatusLog;
import org.apache.ibatis.annotations.Param;

/**
 * 活动状态流转日志 Mapper
 *
 * @author ruoyi
 */
public interface MkActivityStatusLogMapper
{
    public List<MkActivityStatusLog> selectStatusLogList(@Param("activityId") Long activityId);
    public int insertStatusLog(MkActivityStatusLog log);
}
