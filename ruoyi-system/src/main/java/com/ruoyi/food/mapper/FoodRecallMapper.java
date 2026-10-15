package com.ruoyi.food.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.food.domain.FoodRecall;
import com.ruoyi.food.domain.FoodRecallBatch;

/**
 * 食品批次召回Mapper接口
 *
 * @author ruoyi
 */
public interface FoodRecallMapper
{
    public FoodRecall selectRecallById(Long recallId);

    public List<FoodRecall> selectRecallList(FoodRecall recall);

    public int insertRecall(FoodRecall recall);

    public int updateRecall(FoodRecall recall);

    public int deleteRecallByIds(Long[] recallIds);

    public List<FoodRecallBatch> selectRecallBatchList(Long recallId);

    public int insertRecallBatch(FoodRecallBatch rb);

    public int deleteRecallBatchByRecallId(Long recallId);

    /**
     * qms生产谱系边（batchNo维度，按工单桥接原料→产出的跨体系边），del_flag=0 且 parent/child 均非空
     */
    public List<java.util.Map<String, Object>> selectQmsGenealogyEdges();

    /**
     * 按批次号集合查批次（圈批结果批量定位）
     */
    public List<com.ruoyi.food.domain.FoodBatch> selectBatchesByCodes(@Param("codes") List<String> codes);
}
