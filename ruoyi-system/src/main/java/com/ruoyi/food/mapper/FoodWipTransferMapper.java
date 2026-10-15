package com.ruoyi.food.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.food.domain.FoodWipTransfer;

/**
 * 工序转移单 Mapper接口
 *
 * @author ruoyi
 */
public interface FoodWipTransferMapper
{
    public List<FoodWipTransfer> selectTransferList(FoodWipTransfer transfer);

    public FoodWipTransfer selectTransferById(Long transferId);

    public int insertTransfer(FoodWipTransfer transfer);

    public int deleteTransferByIds(Long[] transferIds);

    /** 该垛是否存在转移记录（有记录的垛不允许删除台账） */
    public int countTransferByWip(Long wipId);

    /** 转移单号取号：GZ+yyyyMMdd+4位流水 */
    public String selectNextTransferNo();

    /** 按工单与损耗类型汇总损耗（产出率核算用） */
    public List<Map<String, Object>> sumLossByType(@Param("workOrderId") Long workOrderId);
}
