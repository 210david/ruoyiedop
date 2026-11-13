package com.ruoyi.bi.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bi.domain.BiMetricDictLog;

/**
 * BI指标字典变更留痕 Mapper 接口
 *
 * @author ruoyi
 */
public interface BiMetricDictLogMapper
{
    /**
     * 批量插入留痕记录
     */
    public int insertBatch(@Param("list") List<BiMetricDictLog> list);

    /**
     * 按指标编码查询留痕（新→旧）
     */
    public List<BiMetricDictLog> selectLogByMetricCode(@Param("metricCode") String metricCode);
}
