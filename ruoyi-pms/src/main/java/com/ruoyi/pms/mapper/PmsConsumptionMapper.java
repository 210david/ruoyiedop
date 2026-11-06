package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsConsumption;
import com.ruoyi.pms.domain.PmsConsumptionDetail;

/**
 * 零散采购耗用登记Mapper接口
 *
 * @author ruoyi
 */
public interface PmsConsumptionMapper
{
    public List<PmsConsumption> selectConsumptionList(PmsConsumption consumption);

    public PmsConsumption selectConsumptionById(Long consumptionId);

    public int insertConsumption(PmsConsumption consumption);

    public int updateConsumption(PmsConsumption consumption);

    public int deleteConsumptionByIds(Long[] consumptionIds);

    public int insertConsumptionDetail(PmsConsumptionDetail detail);

    public List<PmsConsumptionDetail> selectConsumptionDetailByConsumptionId(Long consumptionId);
}
