package com.ruoyi.mms.service.impl;

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
        if (StringUtils.isEmpty(downtime.getStatus()))
        {
            downtime.setStatus("0");
        }
        downtime.setDelFlag("0");
        if (StringUtils.isEmpty(downtime.getDowntimeNo()))
        {
            downtime.setDowntimeNo(mkNumberRuleService.generateNumber("mms_downtime"));
        }
        // 默认上报人为当前用户
        if (StringUtils.isEmpty(downtime.getReportBy()))
        {
            downtime.setReportBy(SecurityUtils.getUsername());
        }
        downtime.setCreateBy(SecurityUtils.getUsername());
        downtime.setCreateTime(DateUtils.getNowDate());
        return downtimeMapper.insertDowntime(downtime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDowntime(MmsDowntime downtime)
    {
        // 状态变为已恢复时，自动计算停机时长
        if ("1".equals(downtime.getStatus()))
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
            if (downtime.getEndTime() != null && downtime.getStartTime() != null)
            {
                long diff = downtime.getEndTime().getTime() - downtime.getStartTime().getTime();
                int minutes = (int) (diff / (1000 * 60));
                // 防止负数时长（结束时间早于开始时间时取0）
                if (minutes < 0) minutes = 0;
                downtime.setMinutes(minutes);
            }
            // 恢复时自动设置处理人为当前用户
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
