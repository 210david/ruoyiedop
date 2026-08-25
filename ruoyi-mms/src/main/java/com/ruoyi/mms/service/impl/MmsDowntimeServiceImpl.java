package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
import com.ruoyi.mms.domain.MmsDowntime;
import com.ruoyi.mms.mapper.MmsDowntimeMapper;
import com.ruoyi.mms.service.IMmsDowntimeService;

@Service
public class MmsDowntimeServiceImpl implements IMmsDowntimeService
{
    @Autowired
    private MmsDowntimeMapper downtimeMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsDowntime> selectDowntimeList(MmsDowntime downtime)
    {
        return downtimeMapper.selectDowntimeList(downtime);
    }

    @Override
    public MmsDowntime selectDowntimeById(Long downtimeId)
    {
        return downtimeMapper.selectDowntimeById(downtimeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDowntime(MmsDowntime downtime)
    {
        downtime.setDelFlag("0");
        if (StringUtils.isEmpty(downtime.getDowntimeNo()))
        {
            downtime.setDowntimeNo(mkNumberRuleService.generateNumber("mms_downtime"));
        }
        // 默认上报人为当前用户昵称
        if (StringUtils.isEmpty(downtime.getReportBy()))
        {
            downtime.setReportBy(SecurityUtils.getLoginUser().getUser().getNickName());
        }
        // 填写了结束时间时自动计算停机时长
        if (downtime.getEndTime() != null && downtime.getStartTime() != null)
        {
            long diff = downtime.getEndTime().getTime() - downtime.getStartTime().getTime();
            BigDecimal hours = BigDecimal.valueOf(diff)
                    .divide(BigDecimal.valueOf(1000 * 60 * 60), 2, RoundingMode.HALF_UP);
            if (hours.compareTo(BigDecimal.ZERO) < 0) hours = BigDecimal.ZERO;
            downtime.setHours(hours);
            // 自动设置处理人为当前用户
            if (StringUtils.isEmpty(downtime.getHandleBy()))
            {
                downtime.setHandleBy(SecurityUtils.getUsername());
            }
        }
        downtime.setCreateBy(SecurityUtils.getUsername());
        downtime.setCreateTime(DateUtils.getNowDate());
        return downtimeMapper.insertDowntime(downtime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDowntime(MmsDowntime downtime)
    {
        // 填写了结束时间时自动计算停机时长
        if (downtime.getEndTime() != null)
        {
            // 如果开始时间为空，从数据库取已有值（防止前端传空覆盖）
            if (downtime.getStartTime() == null)
            {
                MmsDowntime existing = downtimeMapper.selectDowntimeById(downtime.getDowntimeId());
                if (existing != null)
                {
                    downtime.setStartTime(existing.getStartTime());
                }
            }
            if (downtime.getStartTime() != null)
            {
                long diff = downtime.getEndTime().getTime() - downtime.getStartTime().getTime();
                // 转换为小时，保留2位小数
                BigDecimal hours = BigDecimal.valueOf(diff)
                        .divide(BigDecimal.valueOf(1000 * 60 * 60), 2, RoundingMode.HALF_UP);
                // 防止负数时长（结束时间早于开始时间时取0）
                if (hours.compareTo(BigDecimal.ZERO) < 0) hours = BigDecimal.ZERO;
                downtime.setHours(hours);
            }
            // 自动设置处理人为当前用户
            if (StringUtils.isEmpty(downtime.getHandleBy()))
            {
                downtime.setHandleBy(SecurityUtils.getUsername());
            }
        }
        downtime.setUpdateBy(SecurityUtils.getUsername());
        return downtimeMapper.updateDowntime(downtime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDowntimeByIds(Long[] downtimeIds)
    {
        return downtimeMapper.deleteDowntimeByIds(downtimeIds);
    }
}
