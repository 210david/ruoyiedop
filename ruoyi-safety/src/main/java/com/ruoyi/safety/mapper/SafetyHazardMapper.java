package com.ruoyi.safety.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyHazard;

public interface SafetyHazardMapper
{
    /**
     * 查询隐患管理
     */
    public SafetyHazard selectSafetyHazardById(Long hazardId);

    /**
     * 查询隐患管理列表
     */
    public List<SafetyHazard> selectSafetyHazardList(SafetyHazard safetyHazard);

    /**
     * 新增隐患管理
     */
    public int insertSafetyHazard(SafetyHazard safetyHazard);

    /**
     * 修改隐患管理
     */
    public int updateSafetyHazard(SafetyHazard safetyHazard);

    /**
     * 删除隐患管理
     */
    public int deleteSafetyHazardById(Long hazardId);

    /**
     * 批量删除隐患管理
     */
    public int deleteSafetyHazardByIds(Long[] hazardIds);

    /**
     * 更新隐患状态
     */
    public int updateHazardStatus(SafetyHazard safetyHazard);

    /**
     * 统计隐患数量
     */
    public int countHazardsByStatus(String hazardStatus);

    /**
     * 隐患按月趋势统计（近6个月）
     */
    public List<Map<String, Object>> selectHazardTrendByMonth();

    /**
     * 隐患按类型统计
     */
    public List<Map<String, Object>> selectHazardCountByType();

    /**
     * 隐患按等级统计
     */
    public List<Map<String, Object>> selectHazardCountByLevel();

    /**
     * 隐患按来源统计
     */
    public List<Map<String, Object>> selectHazardCountBySource();

    /**
     * 隐患按部门统计（Top 5）
     */
    public List<Map<String, Object>> selectHazardCountByDept();
}