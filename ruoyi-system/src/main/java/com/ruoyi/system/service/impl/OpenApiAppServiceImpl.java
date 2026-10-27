package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.OpenApiApp;
import com.ruoyi.system.mapper.OpenApiAppMapper;
import com.ruoyi.system.service.IOpenApiAppService;

/**
 * 开放接口应用 Service实现
 *
 * @author ruoyi
 */
@Service
public class OpenApiAppServiceImpl implements IOpenApiAppService
{
    @Autowired
    private OpenApiAppMapper openApiAppMapper;

    @Override
    public OpenApiApp selectOpenApiAppById(Long appId)
    {
        return openApiAppMapper.selectOpenApiAppById(appId);
    }

    @Override
    public OpenApiApp selectOpenApiAppByKey(String appKey)
    {
        return openApiAppMapper.selectOpenApiAppByKey(appKey);
    }

    @Override
    public List<OpenApiApp> selectOpenApiAppList(OpenApiApp app)
    {
        return openApiAppMapper.selectOpenApiAppList(app);
    }

    @Override
    public int insertOpenApiApp(OpenApiApp app)
    {
        if (StringUtils.isEmpty(app.getAppKey()))
        {
            app.setAppKey(generateAppKey());
        }
        if (!checkAppKeyUnique(app))
        {
            throw new ServiceException("API Key已存在：" + app.getAppKey());
        }
        app.setCreateTime(DateUtils.getNowDate());
        if (StringUtils.isEmpty(app.getStatus()))
        {
            app.setStatus("0");
        }
        if (StringUtils.isEmpty(app.getAllowedModules()))
        {
            app.setAllowedModules("*");
        }
        return openApiAppMapper.insertOpenApiApp(app);
    }

    @Override
    public int updateOpenApiApp(OpenApiApp app)
    {
        app.setUpdateTime(DateUtils.getNowDate());
        return openApiAppMapper.updateOpenApiApp(app);
    }

    @Override
    public int deleteOpenApiAppByIds(Long[] appIds)
    {
        return openApiAppMapper.deleteOpenApiAppByIds(appIds);
    }

    @Override
    public boolean checkAppKeyUnique(OpenApiApp app)
    {
        Long appId = app.getAppId() == null ? -1L : app.getAppId();
        OpenApiApp info = openApiAppMapper.selectOpenApiAppByKey(app.getAppKey());
        return info == null || info.getAppId().equals(appId);
    }

    /**
     * 自动生成API Key
     */
    public static String generateAppKey()
    {
        return "ak-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 10000);
    }
}
