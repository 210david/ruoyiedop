package com.ruoyi.bi.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.bi.domain.BiMetricDict;

/**
 * BI指标字典 Mapper 接口
 *
 * @author ruoyi
 */
public interface BiMetricDictMapper
{
    /**
     * 查询启用状态的指标字典列表
     */
    public List<BiMetricDict> selectEnabledList();

    /**
     * 根据指标编码查询
     */
    public BiMetricDict selectByMetricCode(String metricCode);

    /**
     * 条件查询字典列表（管理页分页用）
     */
    public List<BiMetricDict> selectDictList(BiMetricDict biMetricDict);

    /**
     * 根据主键查询
     */
    public BiMetricDict selectDictById(Long id);

    /**
     * 指标编码唯一性校验（排除自身 id）
     */
    public BiMetricDict checkMetricCodeUnique(@Param("metricCode") String metricCode, @Param("id") Long id);

    /**
     * 新增字典
     */
    public int insertDict(BiMetricDict biMetricDict);

    /**
     * 修改字典
     */
    public int updateDict(BiMetricDict biMetricDict);

    /**
     * 逻辑删除字典（del_flag 置 2）
     */
    public int deleteDictByIds(@Param("ids") Long[] ids, @Param("updateBy") String updateBy);
}
