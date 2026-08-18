package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mms.domain.MmsStatDaily;
import com.ruoyi.mms.mapper.MmsStatDailyMapper;
import com.ruoyi.mms.service.IMmsStatDailyService;

@Service
public class MmsStatDailyServiceImpl implements IMmsStatDailyService
{
    @Autowired
    private MmsStatDailyMapper statDailyMapper;

    @Override
    public List<MmsStatDaily> selectStatDailyList(MmsStatDaily statDaily)
    {
        return statDailyMapper.selectStatDailyList(statDaily);
    }

    @Override
    public MmsStatDaily selectStatDailyById(Long statId)
    {
        return statDailyMapper.selectStatDailyById(statId);
    }
}
