package com.ruoyi.food.domain;

/**
 * CCP监控值超限事件
 *
 * food域发布，qms域监听（Spring同步事件，同一线程同一事务）：
 * 监听器创建不合格品报告(NCR)、回写记录联动NCR编号、冻结关联批次。
 * 通过事件解耦，避免 food 反向依赖 qms。
 *
 * @author ruoyi
 */
public class FoodCcpOverLimitEvent
{
    /** 超限的CCP监控记录 */
    private final FoodCcpRecord record;

    public FoodCcpOverLimitEvent(FoodCcpRecord record)
    {
        this.record = record;
    }

    public FoodCcpRecord getRecord()
    {
        return record;
    }
}
