package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsDataCollectionConfig;
import com.ruoyi.dms.mapper.DmsDataCollectionConfigMapper;
import com.ruoyi.dms.service.IDmsDataCollectionConfigService;

@Service
public class DmsDataCollectionConfigServiceImpl implements IDmsDataCollectionConfigService
{
    @Autowired
    private DmsDataCollectionConfigMapper mapper;

    @Override
    public List<DmsDataCollectionConfig> selectConfigList(DmsDataCollectionConfig config) { return mapper.selectConfigList(config); }
    @Override
    public DmsDataCollectionConfig selectConfigById(Long configId) { return mapper.selectConfigById(configId); }
    @Override
    public int insertConfig(DmsDataCollectionConfig config) { config.setDelFlag("0"); return mapper.insertConfig(config); }
    @Override
    public int updateConfig(DmsDataCollectionConfig config) { return mapper.updateConfig(config); }
    @Override
    public int deleteConfigByIds(Long[] configIds) { return mapper.deleteConfigByIds(configIds); }
}
