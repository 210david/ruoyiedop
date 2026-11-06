package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsConsumption;

/**
 * 零散采购耗用登记Service接口
 *
 * @author ruoyi
 */
public interface IPmsConsumptionService
{
    public List<PmsConsumption> selectConsumptionList(PmsConsumption consumption);

    public PmsConsumption selectConsumptionById(Long consumptionId);

    public int insertConsumption(PmsConsumption consumption);

    public int updateConsumption(PmsConsumption consumption);

    public int deleteConsumptionByIds(Long[] consumptionIds);
}
