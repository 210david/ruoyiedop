package com.ruoyi.safety.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.safety.domain.SafetyHazard;

public interface ISafetyHazardService
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
     * 批量删除隐患管理
     */
    public int deleteSafetyHazardByIds(Long[] hazardIds);

    /**
     * 删除隐患管理信息
     */
    public int deleteSafetyHazardById(Long hazardId);

    /**
     * 提交隐患
     */
    public int submitHazard(Long hazardId);

    /**
     * 审批隐患
     */
    public int approveHazard(SafetyHazard safetyHazard);

    /**
     * 开始整改
     */
    public int startRectify(Long hazardId);

    /**
     * 提交整改
     */
    public int submitRectify(SafetyHazard safetyHazard);

    /**
     * 验收隐患
     */
    public int verifyHazard(SafetyHazard safetyHazard);

    /**
     * 统计隐患数量
     */
    public int countHazardsByStatus(String hazardStatus);

    /**
     * 隐患按月趋势统计（近6个月）
     */
    public List<Map<String, Object>> getHazardTrendByMonth();

    /**
     * 隐患按类型统计
     */
    public List<Map<String, Object>> getHazardCountByType();

    /**
     * 隐患按等级统计
     */
    public List<Map<String, Object>> getHazardCountByLevel();

    /**
     * 隐患按来源统计
     */
    public List<Map<String, Object>> getHazardCountBySource();

    /**
     * 隐患按部门统计（Top 5）
     */
    public List<Map<String, Object>> getHazardCountByDept();
}