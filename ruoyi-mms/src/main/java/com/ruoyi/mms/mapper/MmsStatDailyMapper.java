package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsStatDaily;

public interface MmsStatDailyMapper
{
    public List<MmsStatDaily> selectStatDailyList(MmsStatDaily statDaily);

    public MmsStatDaily selectStatDailyByDate(String statDate);

    public MmsStatDaily selectStatDailyById(Long statId);
}
