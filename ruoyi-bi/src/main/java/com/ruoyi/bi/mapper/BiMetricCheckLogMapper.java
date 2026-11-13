package com.ruoyi.bi.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bi.domain.BiMetricCheckLog;

/**
 * BI指标对账日志 Mapper 接口
 *
 * @author ruoyi
 */
public interface BiMetricCheckLogMapper
{
    /**
     * 批量插入对账记录
     */
    public int insertBatch(@Param("list") List<BiMetricCheckLog> list);

    /**
     * 条件查询对账日志（最新批次在前）
     */
    public List<BiMetricCheckLog> selectCheckLogList(BiMetricCheckLog biMetricCheckLog);
}
