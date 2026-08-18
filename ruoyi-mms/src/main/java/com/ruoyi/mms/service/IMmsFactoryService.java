package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsFactory;
import com.ruoyi.mms.domain.MmsWorkshop;
import com.ruoyi.mms.domain.MmsLine;
import com.ruoyi.mms.domain.MmsStation;

/**
 * 工厂建模 Service接口（工厂/车间/产线/工位四级）
 */
public interface IMmsFactoryService
{
    // ---- 工厂 ----
    List<MmsFactory> selectFactoryList(MmsFactory factory);
    MmsFactory selectFactoryById(Long factoryId);
    int insertFactory(MmsFactory factory);
    int updateFactory(MmsFactory factory);
    int deleteFactoryByIds(Long[] factoryIds);

    // ---- 车间 ----
    List<MmsWorkshop> selectWorkshopList(MmsWorkshop workshop);
    MmsWorkshop selectWorkshopById(Long workshopId);
    int insertWorkshop(MmsWorkshop workshop);
    int updateWorkshop(MmsWorkshop workshop);
    int deleteWorkshopByIds(Long[] workshopIds);

    // ---- 产线 ----
    List<MmsLine> selectLineList(MmsLine line);
    MmsLine selectLineById(Long lineId);
    int insertLine(MmsLine line);
    int updateLine(MmsLine line);
    int deleteLineByIds(Long[] lineIds);

    // ---- 工位 ----
    List<MmsStation> selectStationList(MmsStation station);
    MmsStation selectStationById(Long stationId);
    int insertStation(MmsStation station);
    int updateStation(MmsStation station);
    int deleteStationByIds(Long[] stationIds);
}
