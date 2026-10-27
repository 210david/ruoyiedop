package com.ruoyi.food.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.food.domain.FoodWip;
import com.ruoyi.food.domain.FoodWipEnv;

/**
 * 在制品台账 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodWipMapper
{
    public List<FoodWip> selectWipList(FoodWip wip);

    public FoodWip selectWipById(Long wipId);

    public int insertWip(FoodWip wip);

    public int updateWip(FoodWip wip);

    public int deleteWipByIds(Long[] wipIds);

    /** 温湿度/上霉记录 */
    public List<FoodWipEnv> selectEnvList(Long wipId);

    public int insertEnv(FoodWipEnv env);

    public int deleteEnvById(Long envId);

    /** 垛号取号：DP+yyyyMMdd+4位流水（当日最大值+1，唯一索引兜底） */
    public String selectNextWipNo();

    /** 工单基本信息（编号/产品/路线/单位），建垛时带出 */
    public Map<String, Object> selectWorkOrderInfo(@Param("workOrderId") Long workOrderId);

    /**
     * 工序参数信息（标准时长天/温湿度范围/损耗类型/路线最大工序序号）
     * resultType map：避免 ruoyi-system 对 ruoyi-mms 的反向依赖
     */
    public Map<String, Object> selectStepInfo(@Param("routeId") Long routeId, @Param("stepSeq") Integer stepSeq);

    /** 生产看板：工序分布（在制垛数按工序分组） */
    public List<Map<String, Object>> selectStepDistribution();

    /** 生产看板：汇总（在制总数/平均在制天数/超期垛数） */
    public Map<String, Object> selectWipSummary();
}
