package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsWorkOrderLog;
import org.apache.ibatis.annotations.Param;

public interface DmsWorkOrderLogMapper
{
    /** 插入操作日志 */
    public int insertLog(DmsWorkOrderLog log);

    /** 按工单ID查询操作日志列表 */
    public List<DmsWorkOrderLog> selectLogByOrderId(@Param("orderId") Long orderId);
}
