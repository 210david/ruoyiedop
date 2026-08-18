package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsFactory;

/**
 * 工厂 Mapper
 */
public interface MmsFactoryMapper
{
    List<MmsFactory> selectFactoryList(MmsFactory factory);
    MmsFactory selectFactoryById(Long factoryId);
    int insertFactory(MmsFactory factory);
    int updateFactory(MmsFactory factory);
    int deleteFactoryByIds(Long[] factoryIds);
}
