package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodWip;
import com.ruoyi.food.domain.FoodWipEnv;
import com.ruoyi.food.mapper.FoodWipMapper;
import com.ruoyi.food.mapper.FoodWipTransferMapper;
import com.ruoyi.food.service.IFoodWipService;

/**
 * 在制品台账 Service实现
 *
 * @author ruoyi
 */
@Service
public class FoodWipServiceImpl implements IFoodWipService
{
    @Autowired
    private FoodWipMapper wipMapper;

    @Autowired
    private FoodWipTransferMapper transferMapper;

    @Override
    public List<FoodWip> selectWipList(FoodWip wip)
    {
        return wipMapper.selectWipList(wip);
    }

    @Override
    public FoodWip selectWipById(Long wipId)
    {
        FoodWip wip = wipMapper.selectWipById(wipId);
        if (wip != null)
        {
            wip.setEnvList(wipMapper.selectEnvList(wipId));
        }
        return wip;
    }

    /**
     * 建垛：挂工单带出产品/路线，按工序标准时长推算预计出柜时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWip(FoodWip wip)
    {
        if (wip.getWorkOrderId() == null && wip.getMaterialId() == null)
        {
            throw new ServiceException("工单与物料至少填写一项");
        }
        // 挂工单：带出产品与路线信息（垛跟产品走）
        if (wip.getWorkOrderId() != null)
        {
            Map<String, Object> wo = wipMapper.selectWorkOrderInfo(wip.getWorkOrderId());
            if (wo == null)
            {
                throw new ServiceException("工单不存在或已删除");
            }
            wip.setWorkOrderNo((String) wo.get("work_order_no"));
            if (wip.getMaterialId() == null)
            {
                wip.setMaterialId(((Number) wo.get("product_id")).longValue());
                wip.setMaterialCode((String) wo.get("product_code"));
                wip.setMaterialName((String) wo.get("product_name"));
                wip.setQtyUnit((String) wo.get("unit"));
            }
            if (wip.getRouteId() == null)
            {
                Object routeId = wo.get("route_id");
                wip.setRouteId(routeId != null ? ((Number) routeId).longValue() : null);
            }
        }
        if (wip.getMaterialId() == null)
        {
            throw new ServiceException("物料不能为空");
        }
        if (wip.getQty() == null || wip.getQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("在制数量必须大于0");
        }
        if (wip.getCurrentStepSeq() == null || wip.getCurrentStepSeq() <= 0)
        {
            throw new ServiceException("当前工序序号不能为空");
        }
        if (wip.getStepStartTime() == null)
        {
            wip.setStepStartTime(DateUtils.getNowDate());
        }
        applyStepPlan(wip, wip.getStepStartTime());
        wip.setWipNo(wipMapper.selectNextWipNo());
        wip.setWipStatus("0");
        wip.setDelFlag("0");
        wip.setCreateBy(SecurityUtils.getUsername());
        wip.setCreateTime(DateUtils.getNowDate());
        return wipMapper.insertWip(wip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWip(FoodWip wip)
    {
        FoodWip existing = wipMapper.selectWipById(wip.getWipId());
        if (existing == null)
        {
            throw new ServiceException("垛不存在或已删除");
        }
        if (wip.getQty() != null && wip.getQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("在制数量必须大于0");
        }
        // 工序位置只能通过工序转移单推进，不允许直接修改
        wip.setCurrentStepSeq(null);
        wip.setCurrentProcessName(null);
        wip.setStepStartTime(null);
        wip.setPlanStepEnd(null);
        wip.setWipStatus(null);
        wip.setUpdateBy(SecurityUtils.getUsername());
        wip.setUpdateTime(DateUtils.getNowDate());
        return wipMapper.updateWip(wip);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWipByIds(Long[] wipIds)
    {
        for (Long wipId : wipIds)
        {
            if (transferMapper.countTransferByWip(wipId) > 0)
            {
                FoodWip wip = wipMapper.selectWipById(wipId);
                throw new ServiceException("垛[" + (wip != null ? wip.getWipNo() : wipId) + "]存在工序转移记录，不允许删除");
            }
        }
        return wipMapper.deleteWipByIds(wipIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishWip(Long wipId)
    {
        FoodWip existing = wipMapper.selectWipById(wipId);
        if (existing == null)
        {
            throw new ServiceException("垛不存在或已删除");
        }
        if (!"0".equals(existing.getWipStatus()))
        {
            throw new ServiceException("垛[" + existing.getWipNo() + "]当前状态不允许完成（仅在制可完成）");
        }
        FoodWip updater = new FoodWip();
        updater.setWipId(wipId);
        updater.setWipStatus("1");
        updater.setFinishTime(DateUtils.getNowDate());
        updater.setUpdateBy(SecurityUtils.getUsername());
        updater.setUpdateTime(DateUtils.getNowDate());
        return wipMapper.updateWip(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeWip(Long wipId)
    {
        FoodWip existing = wipMapper.selectWipById(wipId);
        if (existing == null)
        {
            throw new ServiceException("垛不存在或已删除");
        }
        if (!"0".equals(existing.getWipStatus()))
        {
            throw new ServiceException("垛[" + existing.getWipNo() + "]当前状态不允许关闭");
        }
        FoodWip updater = new FoodWip();
        updater.setWipId(wipId);
        updater.setWipStatus("9");
        updater.setUpdateBy(SecurityUtils.getUsername());
        updater.setUpdateTime(DateUtils.getNowDate());
        return wipMapper.updateWip(updater);
    }

    @Override
    public List<FoodWipEnv> selectEnvList(Long wipId)
    {
        return wipMapper.selectEnvList(wipId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addEnv(FoodWipEnv env)
    {
        FoodWip wip = wipMapper.selectWipById(env.getWipId());
        if (wip == null)
        {
            throw new ServiceException("垛不存在或已删除");
        }
        if (env.getRecordTime() == null)
        {
            env.setRecordTime(DateUtils.getNowDate());
        }
        // 未指定工序时记录垛的当前工序
        if (env.getStepSeq() == null)
        {
            env.setStepSeq(wip.getCurrentStepSeq());
            env.setProcessName(wip.getCurrentProcessName());
        }
        env.setWipNo(wip.getWipNo());
        try
        {
            env.setRecorder(SecurityUtils.getUsername());
        }
        catch (Exception e)
        {
            env.setRecorder("system");
        }
        env.setCreateBy(env.getRecorder());
        env.setCreateTime(DateUtils.getNowDate());
        return wipMapper.insertEnv(env);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEnvById(Long envId)
    {
        return wipMapper.deleteEnvById(envId);
    }

    @Override
    public Map<String, Object> dashboard()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("summary", wipMapper.selectWipSummary());
        result.put("stepDistribution", wipMapper.selectStepDistribution());
        return result;
    }

    /**
     * 按路线工序参数回填当前工序名并推算预计出柜时间（入柜时间+标准时长天）
     */
    private void applyStepPlan(FoodWip wip, Date stepStartTime)
    {
        wip.setCurrentProcessName(null);
        wip.setPlanStepEnd(null);
        if (wip.getRouteId() == null)
        {
            return;
        }
        Map<String, Object> step = wipMapper.selectStepInfo(wip.getRouteId(), wip.getCurrentStepSeq());
        if (step == null)
        {
            throw new ServiceException("路线[" + wip.getRouteId() + "]下不存在序号" + wip.getCurrentStepSeq() + "的工序，请核对工艺路线");
        }
        wip.setCurrentProcessName((String) step.get("process_name"));
        Object stdDaysObj = step.get("std_days");
        if (stdDaysObj != null)
        {
            BigDecimal stdDays = new BigDecimal(stdDaysObj.toString());
            if (stdDays.compareTo(BigDecimal.ZERO) > 0)
            {
                wip.setPlanStepEnd(DateUtils.addHours(stepStartTime, stdDays.multiply(new BigDecimal(24)).intValue()));
            }
        }
    }
}
