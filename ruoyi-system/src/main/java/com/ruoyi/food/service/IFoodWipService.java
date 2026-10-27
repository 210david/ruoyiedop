package com.ruoyi.food.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.food.domain.FoodWip;
import com.ruoyi.food.domain.FoodWipEnv;

/**
 * 在制品台账 Service接口
 *
 * @author ruoyi
 */
public interface IFoodWipService
{
    public List<FoodWip> selectWipList(FoodWip wip);

    public FoodWip selectWipById(Long wipId);

    public int insertWip(FoodWip wip);

    public int updateWip(FoodWip wip);

    public int deleteWipByIds(Long[] wipIds);

    /** 完成（出柜完毕/成品入库后） */
    public int finishWip(Long wipId);

    /** 关闭（作废/返工终止） */
    public int closeWip(Long wipId);

    /** 温湿度/上霉记录列表 */
    public List<FoodWipEnv> selectEnvList(Long wipId);

    /** 新增温湿度/上霉记录（自动带出当前工序） */
    public int addEnv(FoodWipEnv env);

    /** 删除温湿度/上霉记录 */
    public int deleteEnvById(Long envId);

    /** 生产看板：WIP汇总+工序分布 */
    public Map<String, Object> dashboard();
}
