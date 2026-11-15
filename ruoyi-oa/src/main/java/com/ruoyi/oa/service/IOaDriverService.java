package com.ruoyi.oa.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.oa.domain.OaDriver;

public interface IOaDriverService
{
    public OaDriver selectOaDriverById(Long driverId);

    public List<OaDriver> selectOaDriverList(OaDriver oaDriver);

    public AjaxResult importDriver(List<OaDriver> driverList, boolean updateSupport, String operName);

    public int insertOaDriver(OaDriver oaDriver);

    public int updateOaDriver(OaDriver oaDriver);

    public int deleteOaDriverByIds(Long[] driverIds);

    public int deleteOaDriverById(Long driverId);
}
