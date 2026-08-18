package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.mms.domain.MmsFactory;
import com.ruoyi.mms.domain.MmsWorkshop;
import com.ruoyi.mms.domain.MmsLine;
import com.ruoyi.mms.domain.MmsStation;
import com.ruoyi.mms.mapper.MmsFactoryMapper;
import com.ruoyi.mms.mapper.MmsWorkshopMapper;
import com.ruoyi.mms.mapper.MmsLineMapper;
import com.ruoyi.mms.mapper.MmsStationMapper;
import com.ruoyi.mms.service.IMmsFactoryService;

/**
 * 工厂建模 Service实现
 */
@Service
public class MmsFactoryServiceImpl implements IMmsFactoryService
{
    @Autowired
    private MmsFactoryMapper factoryMapper;

    @Autowired
    private MmsWorkshopMapper workshopMapper;

    @Autowired
    private MmsLineMapper lineMapper;

    @Autowired
    private MmsStationMapper stationMapper;

    // ---- 工厂 ----
    @Override
    public List<MmsFactory> selectFactoryList(MmsFactory factory) { return factoryMapper.selectFactoryList(factory); }
    @Override
    public MmsFactory selectFactoryById(Long factoryId) { return factoryMapper.selectFactoryById(factoryId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFactory(MmsFactory factory) { factory.setDelFlag("0"); return factoryMapper.insertFactory(factory); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFactory(MmsFactory factory) { return factoryMapper.updateFactory(factory); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFactoryByIds(Long[] factoryIds) { return factoryMapper.deleteFactoryByIds(factoryIds); }

    // ---- 车间 ----
    @Override
    public List<MmsWorkshop> selectWorkshopList(MmsWorkshop workshop) { return workshopMapper.selectWorkshopList(workshop); }
    @Override
    public MmsWorkshop selectWorkshopById(Long workshopId) { return workshopMapper.selectWorkshopById(workshopId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWorkshop(MmsWorkshop workshop) { workshop.setDelFlag("0"); return workshopMapper.insertWorkshop(workshop); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkshop(MmsWorkshop workshop) { return workshopMapper.updateWorkshop(workshop); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWorkshopByIds(Long[] workshopIds) { return workshopMapper.deleteWorkshopByIds(workshopIds); }

    // ---- 产线 ----
    @Override
    public List<MmsLine> selectLineList(MmsLine line) { return lineMapper.selectLineList(line); }
    @Override
    public MmsLine selectLineById(Long lineId) { return lineMapper.selectLineById(lineId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertLine(MmsLine line) { line.setDelFlag("0"); return lineMapper.insertLine(line); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateLine(MmsLine line) { return lineMapper.updateLine(line); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteLineByIds(Long[] lineIds) { return lineMapper.deleteLineByIds(lineIds); }

    // ---- 工位 ----
    @Override
    public List<MmsStation> selectStationList(MmsStation station) { return stationMapper.selectStationList(station); }
    @Override
    public MmsStation selectStationById(Long stationId) { return stationMapper.selectStationById(stationId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertStation(MmsStation station) { station.setDelFlag("0"); return stationMapper.insertStation(station); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateStation(MmsStation station) { return stationMapper.updateStation(station); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteStationByIds(Long[] stationIds) { return stationMapper.deleteStationByIds(stationIds); }
}
