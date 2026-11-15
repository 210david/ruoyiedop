package com.ruoyi.oa.mapper;

import java.util.List;
import com.ruoyi.oa.domain.OaDriver;

public interface OaDriverMapper
{
    public OaDriver selectOaDriverById(Long driverId);

    public OaDriver selectOaDriverByNo(String driverNo);

    public List<OaDriver> selectOaDriverList(OaDriver oaDriver);

    public int insertOaDriver(OaDriver oaDriver);

    public int updateOaDriver(OaDriver oaDriver);

    public int deleteOaDriverByIds(Long[] driverIds);

    public int deleteOaDriverById(Long driverId);
}
