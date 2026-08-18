package com.ruoyi.mms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
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
            downtime.setDowntimeNo(mkNumberRuleService.generateNumber("DT"));
        }
        downtime.setCreateBy(SecurityUtils.getUsername());
        return downtimeMapper.insertDowntime(downtime);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDowntime(MmsDowntime downtime)
    {
        if ("1".equals(downtime.getStatus()) && downtime.getEndTime() != null && downtime.getStartTime() != null)
        {
            long diff = downtime.getEndTime().getTime() - downtime.getStartTime().getTime();
            downtime.setMinutes((int) (diff / (1000 * 60)));
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
