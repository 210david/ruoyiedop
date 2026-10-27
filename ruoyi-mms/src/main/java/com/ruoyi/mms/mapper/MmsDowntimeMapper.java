package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsDowntime;

public interface MmsDowntimeMapper
{
    public List<MmsDowntime> selectDowntimeList(MmsDowntime downtime);
    public MmsDowntime selectDowntimeById(Long downtimeId);
    public int insertDowntime(MmsDowntime downtime);
    public int updateDowntime(MmsDowntime downtime);
    public int deleteDowntimeByIds(Long[] downtimeIds);
}
