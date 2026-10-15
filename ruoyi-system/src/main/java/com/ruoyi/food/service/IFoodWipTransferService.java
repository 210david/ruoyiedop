package com.ruoyi.food.service;

import java.util.List;
import com.ruoyi.food.domain.FoodWipTransfer;

/**
 * 工序转移单 Service接口
 *
 * @author ruoyi
 */
public interface IFoodWipTransferService
{
    public List<FoodWipTransfer> selectTransferList(FoodWipTransfer transfer);

    public FoodWipTransfer selectTransferById(Long transferId);

    /**
     * 新增工序转移（即确认生效）：
     * 校验垛在制、目标工序必须大于当前工序且在路线内；
     * 转移后更新垛的当前工序/入柜时间/预计出柜/数量；
     * 转移到最后一道工序自动完成；损耗率超工序阈值推送告警。
     */
    public int insertTransfer(FoodWipTransfer transfer);
}
