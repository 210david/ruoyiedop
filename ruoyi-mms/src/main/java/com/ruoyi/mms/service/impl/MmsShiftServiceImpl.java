package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsShift;
import com.ruoyi.mms.mapper.MmsShiftMapper;
import com.ruoyi.mms.service.IMmsShiftService;

/**
 * 班次 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsShiftServiceImpl implements IMmsShiftService
{
    @Autowired
    private MmsShiftMapper shiftMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsShift> selectShiftList(MmsShift shift)
    {
        return shiftMapper.selectShiftList(shift);
    }

    @Override
    public MmsShift selectShiftById(Long ShiftId)
    {
        return shiftMapper.selectShiftById(ShiftId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertShift(MmsShift shift)
    {
        // 班次名称必填校验
        if (StringUtils.isEmpty(shift.getShiftName()))
        {
            throw new ServiceException("班次名称不能为空");
        }
        checkTimeRange(shift);
        computeAvailHours(shift);
        // 班次名称唯一性校验
        MmsShift query = new MmsShift();
        query.setShiftName(shift.getShiftName());
        List<MmsShift> existing = shiftMapper.selectShiftList(query);
        if (existing != null && !existing.isEmpty())
        {
            throw new ServiceException("班次名称'" + shift.getShiftName() + "'已存在");
        }
        // 状态默认已启用
        if (StringUtils.isEmpty(shift.getStatus()))
        {
            shift.setStatus("1");
        }
        // 自动生成班次编号（通过编号规则 mms_shift 生成）
        if (StringUtils.isEmpty(shift.getShiftCode()))
        {
            shift.setShiftCode(mkNumberRuleService.generateNumber("mms_shift"));
        }
        shift.setDelFlag("0");
        shift.setCreateBy(SecurityUtils.getUsername());
        shift.setCreateTime(DateUtils.getNowDate());
        shift.setUpdateBy(SecurityUtils.getUsername());
        shift.setUpdateTime(DateUtils.getNowDate());
        return shiftMapper.insertShift(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateShift(MmsShift shift)
    {
        checkTimeRange(shift);
        computeAvailHours(shift);
        // 班次名称唯一性校验（排除自身）
        if (StringUtils.isNotEmpty(shift.getShiftName()))
        {
            MmsShift query = new MmsShift();
            query.setShiftName(shift.getShiftName());
            List<MmsShift> existing = shiftMapper.selectShiftList(query);
            if (existing != null)
            {
                for (MmsShift item : existing)
                {
                    if (!item.getShiftId().equals(shift.getShiftId()))
                    {
                        throw new ServiceException("班次名称'" + shift.getShiftName() + "'已存在");
                    }
                }
            }
        }
        shift.setUpdateBy(SecurityUtils.getUsername());
        shift.setUpdateTime(DateUtils.getNowDate());
        return shiftMapper.updateShift(shift);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteShiftByIds(Long[] ShiftIds)
    {
        return shiftMapper.deleteShiftByIds(ShiftIds);
    }

    /**
     * 校验结束时间必须晚于开始时间（HH:mm 字符串可直接比较）
     */
    private void checkTimeRange(MmsShift shift)
    {
        if (StringUtils.isNotEmpty(shift.getStartTime()) && StringUtils.isNotEmpty(shift.getEndTime())
                && shift.getEndTime().compareTo(shift.getStartTime()) <= 0)
        {
            throw new ServiceException("结束时间必须晚于开始时间");
        }
    }

    /**
     * 可用工时未填写时自动计算：可用工时 =（结束-开始-休息分钟）/60，保留2位小数；
     * 手动填写的 availHours 不覆盖；休息超过班次时长时不产生负值
     */
    private void computeAvailHours(MmsShift shift)
    {
        if (shift.getAvailHours() != null || StringUtils.isEmpty(shift.getStartTime()) || StringUtils.isEmpty(shift.getEndTime()))
        {
            return;
        }
        try
        {
            String[] sp = shift.getStartTime().split(":");
            String[] ep = shift.getEndTime().split(":");
            int s = Integer.parseInt(sp[0]) * 60 + (sp.length > 1 ? Integer.parseInt(sp[1]) : 0);
            int e = Integer.parseInt(ep[0]) * 60 + (ep.length > 1 ? Integer.parseInt(ep[1]) : 0);
            int rest = shift.getRestMinutes() == null ? 0 : shift.getRestMinutes();
            int net = e - s - rest;
            if (net > 0)
            {
                shift.setAvailHours(java.math.BigDecimal.valueOf(Math.round(net * 100.0 / 60.0) / 100.0));
            }
        }
        catch (NumberFormatException ignored) { }
    }
}
