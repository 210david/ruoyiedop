package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsDemand;
import com.ruoyi.mms.domain.MmsMps;
import com.ruoyi.mms.mapper.MmsDemandMapper;
import com.ruoyi.mms.mapper.MmsMpsMapper;
import com.ruoyi.mms.service.IMmsDemandService;

/**
 * 生产需求 Service实现
 *
 * 需求状态机：
 * 0(草稿) → 1(已确认) → 2(已排产)
 *                ↓
 *           3(已取消)
 *
 * @author ruoyi
 */
@Service
public class MmsDemandServiceImpl implements IMmsDemandService
{
    @Autowired
    private MmsDemandMapper demandMapper;

    @Autowired
    private MmsMpsMapper mpsMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsDemand> selectDemandList(MmsDemand demand)
    {
        return demandMapper.selectDemandList(demand);
    }

    @Override
    public MmsDemand selectDemandById(Long DemandId)
    {
        return demandMapper.selectDemandById(DemandId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDemand(MmsDemand demand)
    {
// 自动生成需求计划编号
if (StringUtils.isEmpty(demand.getDemandNo()))
{
demand.setDemandNo(mkNumberRuleService.generateNumber("mms_demand"));
}
// 状态默认为草稿，避免未传status时数据不可用
if (StringUtils.isEmpty(demand.getStatus()))
{
demand.setStatus("0");
}
        demand.setDelFlag("0");
        demand.setCreateBy(SecurityUtils.getUsername());
        demand.setCreateTime(DateUtils.getNowDate());
        return demandMapper.insertDemand(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDemand(MmsDemand demand)
    {
        return demandMapper.updateDemand(demand);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDemandByIds(Long[] DemandIds)
    {
        return demandMapper.deleteDemandByIds(DemandIds);
    }

    // ========== 业务操作 ==========

    /**
     * 需求确认：0(草稿) → 1(已确认)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmDemand(Long demandId)
    {
        MmsDemand demand = getAndCheckDemand(demandId);
        if (!"0".equals(demand.getStatus()))
        {
            throw new ServiceException("需求[" + demand.getDemandNo() + "]当前状态为" + statusName(demand.getStatus()) + "，只有草稿状态可确认");
        }
        // 校验关键字段
        if (demand.getProductId() == null)
        {
            throw new ServiceException("需求未关联产品，无法确认");
        }
        if (demand.getDemandQty() == null || demand.getDemandQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("需求数量必须大于0");
        }
        demand.setStatus("1");
        demand.setUpdateBy(SecurityUtils.getUsername());
        return demandMapper.updateDemand(demand);
    }

    /**
     * 需求取消确认：1(已确认) → 0(草稿)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unconfirmDemand(Long demandId)
    {
        MmsDemand demand = getAndCheckDemand(demandId);
        if (!"1".equals(demand.getStatus()))
        {
            throw new ServiceException("需求[" + demand.getDemandNo() + "]当前状态为" + statusName(demand.getStatus()) + "，只有已确认状态可取消确认");
        }
        demand.setStatus("0");
        demand.setUpdateBy(SecurityUtils.getUsername());
        return demandMapper.updateDemand(demand);
    }

    /**
     * 需求转计划：根据需求创建MPS草稿，并回写需求状态为已排产(2)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MmsMps convertToMps(Long demandId)
    {
        MmsDemand demand = getAndCheckDemand(demandId);
        // 状态校验：只有已确认(1)可转计划
        if (!"1".equals(demand.getStatus()))
        {
            throw new ServiceException("需求[" + demand.getDemandNo() + "]当前状态为" + statusName(demand.getStatus()) + "，只有已确认状态可转计划");
        }
        // 校验关键字段
        if (demand.getProductId() == null)
        {
            throw new ServiceException("需求未关联产品，无法转计划");
        }
        if (demand.getDemandQty() == null || demand.getDemandQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("需求数量必须大于0");
        }
        if (demand.getRequireDate() == null)
        {
            throw new ServiceException("需求[" + demand.getDemandNo() + "]缺少需求日期，无法转计划");
        }

        // 创建MPS计划（草稿状态）
        MmsMps mps = new MmsMps();
        mps.setMpsNo(mkNumberRuleService.generateNumber("mms_mps"));
        mps.setDemandId(demand.getDemandId());
        mps.setDemandNo(demand.getDemandNo());
        mps.setProductId(demand.getProductId());
        mps.setProductCode(demand.getProductCode());
        mps.setProductName(demand.getProductName());
        mps.setSpecModel(demand.getSpecModel());
        mps.setUnit(demand.getUnit());
        mps.setPlanQty(demand.getDemandQty());
        // 计划期：以需求日期为完工日期，开工日期默认前推7天
        Date requireDate = demand.getRequireDate();
        mps.setPeriodEnd(requireDate);
        mps.setPeriodStart(new Date(requireDate.getTime() - 7L * 24 * 60 * 60 * 1000));
        mps.setPriority(demand.getPriority());
        mps.setStatus("0"); // 草稿
        mps.setDelFlag("0");
        mps.setCreateBy(SecurityUtils.getUsername());
        mps.setCreateTime(DateUtils.getNowDate());
        mps.setRemark("由需求[" + demand.getDemandNo() + "]转计划生成");
        mpsMapper.insertMps(mps);

        // 回写需求状态为已排产
        demand.setStatus("2");
        demand.setUpdateBy(SecurityUtils.getUsername());
        demandMapper.updateDemand(demand);

        return mps;
    }

    // ========== 私有辅助方法 ==========

    private MmsDemand getAndCheckDemand(Long demandId)
    {
        MmsDemand demand = demandMapper.selectDemandById(demandId);
        if (demand == null)
        {
            throw new ServiceException("生产需求不存在或已删除");
        }
        return demand;
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "草稿";
            case "1": return "已确认";
            case "2": return "已排产";
            case "3": return "已取消";
            default: return "未知(" + status + ")";
        }
    }
}
