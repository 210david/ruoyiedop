package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsStation;

/**
 * 工位 Mapper
 */
public interface MmsStationMapper
{
    List<MmsStation> selectStationList(MmsStation station);
    MmsStation selectStationById(Long stationId);
    int insertStation(MmsStation station);
    int updateStation(MmsStation station);
    int deleteStationByIds(Long[] stationIds);
}
