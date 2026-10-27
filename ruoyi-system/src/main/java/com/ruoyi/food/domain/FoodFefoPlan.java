package com.ruoyi.food.domain;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * FEFO分配计划对象
 *
 * 承载一次出库需求的批次分配结果：分配明细（拆行依据）+ 被拦截批次原因（三重拦截提示）
 *
 * @author ruoyi
 */
public class FoodFefoPlan implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 需求数量 */
    private BigDecimal requestedQty;

    /** 通过三重拦截后的可分配总量 */
    private BigDecimal availableQty;

    /** 分配明细（batchId/batchCode/locationId/qty...），合计=requestedQty 时成功 */
    private List<FoodBatchStock> allocations = new ArrayList<>();

    /** 被拦截批次及原因（三重拦截提示语） */
    private List<String> skipped = new ArrayList<>();

    /** 是否满足全部需求 */
    private boolean success;

    public BigDecimal getRequestedQty()
    {
        return requestedQty;
    }

    public void setRequestedQty(BigDecimal requestedQty)
    {
        this.requestedQty = requestedQty;
    }

    public BigDecimal getAvailableQty()
    {
        return availableQty;
    }

    public void setAvailableQty(BigDecimal availableQty)
    {
        this.availableQty = availableQty;
    }

    public List<FoodBatchStock> getAllocations()
    {
        return allocations;
    }

    public void setAllocations(List<FoodBatchStock> allocations)
    {
        this.allocations = allocations;
    }

    public List<String> getSkipped()
    {
        return skipped;
    }

    public void setSkipped(List<String> skipped)
    {
        this.skipped = skipped;
    }

    public boolean isSuccess()
    {
        return success;
    }

    public void setSuccess(boolean success)
    {
        this.success = success;
    }
}
