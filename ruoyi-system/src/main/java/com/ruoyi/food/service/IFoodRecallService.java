package com.ruoyi.food.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.food.domain.FoodRecall;
import com.ruoyi.food.domain.FoodRecallBatch;

/**
 * 食品批次召回Service接口
 *
 * 召回隔离：从触发批次出发按批次DAG（food谱系 + qms生产谱系桥接）正/反向圈批，
 * 一键冻结全部关联批次（GB/T 22005 追溯合规·召回演练核心动作）
 *
 * @author ruoyi
 */
public interface IFoodRecallService
{
    public FoodRecall selectRecallById(Long recallId);

    public List<FoodRecall> selectRecallList(FoodRecall recall);

    public List<FoodRecallBatch> selectRecallBatchList(Long recallId);

    /**
     * 圈批预览：按谱系正/反向遍历，返回关联批次清单（不落库不冻结）
     *
     * @param triggerBatchId 触发批次ID
     * @param scope          1正向 2反向 3双向
     * @return 圈批结果（batchId/batchCode/materialName/depth/relationPath/batchStatus/qty/expireDate）
     */
    public List<Map<String, Object>> circles(Long triggerBatchId, String scope);

    /**
     * 新建召回单（待执行）
     */
    public int insertRecall(FoodRecall recall);

    /**
     * 执行召回：按谱系圈批 → 冻结全部关联批次 → 留痕 → 消息通知
     */
    public int executeRecall(Long recallId);

    /**
     * 取消召回（仅待执行可取消；已执行的隔离解除走批次操作解冻留痕）
     */
    public int cancelRecall(Long recallId);

    public int deleteRecallByIds(Long[] recallIds);
}
