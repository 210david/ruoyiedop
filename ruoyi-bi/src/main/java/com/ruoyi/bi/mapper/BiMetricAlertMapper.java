package com.ruoyi.bi.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bi.domain.BiMetricAlert;

/**
 * BI指标预警记录 数据层
 *
 * @author ruoyi
 */
public interface BiMetricAlertMapper
{
    /**
     * 预警记录列表（支持指标编码/级别/推送状态/日期范围过滤）
     */
    public List<BiMetricAlert> selectAlertList(BiMetricAlert query);

    /**
     * 按唯一键查询（指标编码+日期+部门+级别）
     */
    public BiMetricAlert selectByUnique(@Param("metricCode") String metricCode,
            @Param("metricDate") Date metricDate, @Param("deptId") Long deptId,
            @Param("alertLevel") String alertLevel);

    /**
     * 查询指标当前未恢复的最高级预警（级别倒序：2红线优先于1黄线，用于升级判断）
     */
    public BiMetricAlert selectOpenAlert(@Param("metricCode") String metricCode);

    /**
     * 查询指标全部未恢复预警（恢复判定用：黄/红多级别记录需一并关闭）
     */
    public List<BiMetricAlert> selectOpenAlerts(@Param("metricCode") String metricCode);

    /**
     * 查询指标某级别最近一次已推送的记录（24h 抑制判断依据）
     */
    public BiMetricAlert selectLatestPushed(@Param("metricCode") String metricCode,
            @Param("alertLevel") String alertLevel);

    /**
     * 新增预警记录
     */
    public int insertAlert(BiMetricAlert alert);

    /**
     * 更新预警记录
     */
    public int updateAlert(BiMetricAlert alert);
}
