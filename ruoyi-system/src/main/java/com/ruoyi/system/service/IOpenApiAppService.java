package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.OpenApiApp;

/**
 * 开放接口应用 Service接口
 *
 * @author ruoyi
 */
public interface IOpenApiAppService
{
    public OpenApiApp selectOpenApiAppById(Long appId);

    public OpenApiApp selectOpenApiAppByKey(String appKey);

    public List<OpenApiApp> selectOpenApiAppList(OpenApiApp app);

    public int insertOpenApiApp(OpenApiApp app);

    public int updateOpenApiApp(OpenApiApp app);

    public int deleteOpenApiAppByIds(Long[] appIds);

    public boolean checkAppKeyUnique(OpenApiApp app);
}
