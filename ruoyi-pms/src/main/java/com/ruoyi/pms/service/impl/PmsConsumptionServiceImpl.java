package com.ruoyi.pms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pms.domain.PmsConsumption;
import com.ruoyi.pms.domain.PmsConsumptionDetail;
import com.ruoyi.pms.mapper.PmsConsumptionMapper;
import com.ruoyi.pms.service.IPmsConsumptionService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 零散采购耗用登记Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PmsConsumptionServiceImpl implements IPmsConsumptionService
{
    @Autowired
    private PmsConsumptionMapper pmsConsumptionMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<PmsConsumption> selectConsumptionList(PmsConsumption consumption)
    {
        return pmsConsumptionMapper.selectConsumptionList(consumption);
    }

    @Override
    public PmsConsumption selectConsumptionById(Long consumptionId)
    {
        PmsConsumption consumption = pmsConsumptionMapper.selectConsumptionById(consumptionId);
        if (consumption != null)
        {
            consumption.setDetailList(pmsConsumptionMapper.selectConsumptionDetailByConsumptionId(consumptionId));
        }
        return consumption;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertConsumption(PmsConsumption consumption)
    {
        if (StringUtils.isEmpty(consumption.getConsumptionNo()))
        {
            consumption.setConsumptionNo(mkNumberRuleService.generateNumber("pms_consumption"));
        }
        consumption.setDelFlag("0");
        if (consumption.getStatus() == null)
        {
            consumption.setStatus("0");
        }
        consumption.setCreateBy(SecurityUtils.getUsername());
        computeTotals(consumption);
        int rows = pmsConsumptionMapper.insertConsumption(consumption);
        insertDetails(consumption);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateConsumption(PmsConsumption consumption)
    {
        PmsConsumption existing = pmsConsumptionMapper.selectConsumptionById(consumption.getConsumptionId());
        if (existing == null)
        {
            throw new ServiceException("耗用登记不存在");
        }
        consumption.setUpdateBy(SecurityUtils.getUsername());
        return pmsConsumptionMapper.updateConsumption(consumption);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteConsumptionByIds(Long[] consumptionIds)
    {
        return pmsConsumptionMapper.deleteConsumptionByIds(consumptionIds);
    }

    private void computeTotals(PmsConsumption consumption)
    {
        BigDecimal totalQty = BigDecimal.ZERO;
        BigDecimal totalAmount = BigDecimal.ZERO;
        if (consumption.getDetailList() != null)
        {
            for (PmsConsumptionDetail d : consumption.getDetailList())
            {
                if (d.getQty() != null)
                {
                    totalQty = totalQty.add(d.getQty());
                }
                if (d.getAmount() != null)
                {
                    totalAmount = totalAmount.add(d.getAmount());
                }
                else if (d.getQty() != null && d.getUnitPrice() != null)
                {
                    d.setAmount(d.getQty().multiply(d.getUnitPrice()));
                    totalAmount = totalAmount.add(d.getAmount());
                }
            }
        }
        consumption.setTotalQty(totalQty);
        consumption.setTotalAmount(totalAmount);
    }

    private void insertDetails(PmsConsumption consumption)
    {
        if (consumption.getDetailList() != null)
        {
            for (PmsConsumptionDetail d : consumption.getDetailList())
            {
                d.setConsumptionId(consumption.getConsumptionId());
                d.setDelFlag("0");
                pmsConsumptionMapper.insertConsumptionDetail(d);
            }
        }
    }
}
