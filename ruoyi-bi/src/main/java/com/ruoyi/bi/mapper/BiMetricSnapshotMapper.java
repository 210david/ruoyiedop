package com.ruoyi.bi.mapper;

import java.util.Date;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bi.domain.BiMetricSnapshot;

/**
 * BI指标日快照 Mapper 接口
 *
 * 幂等设计：同日重跑先 deleteByMetricDate 后 insertBatch。
 *
 * @author ruoyi
 */
public interface BiMetricSnapshotMapper
{
    /**
     * 删除指定日期的全部快照（幂等重跑前调用）
     */
    public int deleteByMetricDate(@Param("metricDate") Date metricDate);

    /**
     * 批量插入快照
     */
    public int insertBatch(@Param("list") List<BiMetricSnapshot> list);

    /**
     * 查询指定日期快照列表
     */
    public List<BiMetricSnapshot> selectListByDate(@Param("metricDate") Date metricDate);

    /**
     * 查询最新快照日期（决策驾驶舱取数基准；无快照返回 null）
     */
    public Date selectLatestMetricDate();

    /**
     * 查询日期区间内的全部快照（决策驾驶舱趋势图，含 dept_id=0 全部门汇总行）
     *
     * @param startDate 起始日（含，须为当日零点）
     * @param endDate   结束日（含，须为当日零点）
     * @param deptIds   部门维度过滤（保留兼容历史部门粒度行）：null=仅公司汇总行 dept_id=0；
     *                  非 null=仅该部门集合的部门粒度行。现行看板一律传 null（公司级口径）
     */
    public List<BiMetricSnapshot> selectTrendList(@Param("startDate") Date startDate,
            @Param("endDate") Date endDate, @Param("deptIds") java.util.Collection<Long> deptIds);

    /**
     * 查询唯一快照（指标+日期+部门维度）
     */
    public BiMetricSnapshot selectByUnique(@Param("metricCode") String metricCode,
            @Param("metricDate") Date metricDate, @Param("deptId") Long deptId);
}
