package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsDataCollectionConfig;

public interface DmsDataCollectionConfigMapper
{
    public List<DmsDataCollectionConfig> selectConfigList(DmsDataCollectionConfig config);
    public DmsDataCollectionConfig selectConfigById(Long configId);
    public int insertConfig(DmsDataCollectionConfig config);
    public int updateConfig(DmsDataCollectionConfig config);
    public int deleteConfigByIds(Long[] configIds);
}
