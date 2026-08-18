package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsStatDaily;

public interface IMmsStatDailyService
{
    public List<MmsStatDaily> selectStatDailyList(MmsStatDaily statDaily);
    public MmsStatDaily selectStatDailyById(Long statId);
}
