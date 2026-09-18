package com.ruoyi.system.service.impl;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.ruoyi.common.constant.CacheConstants;
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.mapper.SysConfigMapper;
import com.ruoyi.system.service.ISysConfigService;

/**
 * 参数配置 服务层实现
 * 
 * @author ruoyi
 */
@Service
public class SysConfigServiceImpl implements ISysConfigService
{
    @Autowired
    private SysConfigMapper configMapper;

    @Autowired
    private RedisCache redisCache;

    /**
     * 项目启动时，初始化参数到缓存
     */
    @PostConstruct
    public void init()
    {
        loadingConfigCache();
    }

    /**
     * 查询参数配置信息
     * 
     * @param configId 参数配置ID
     * @return 参数配置信息
     */
    @Override
    public SysConfig selectConfigById(Long configId)
    {
        SysConfig config = new SysConfig();
        config.setConfigId(configId);
        return configMapper.selectConfig(config);
    }

    /**
     * 根据键名查询参数配置信息（带Spring缓存，30分钟过期）
     * 
     * @param configKey 参数key
     * @return 参数键值
     */
    @Override
    @Cacheable(value = "sysConfig", key = "'config:' + #configKey")
    public String selectConfigByKey(String configKey)
    {
        String configValue = Convert.toStr(redisCache.getCacheObject(getCacheKey(configKey)));
        if (StringUtils.isNotEmpty(configValue))
        {
            return configValue;
        }
        SysConfig config = new SysConfig();
        config.setConfigKey(configKey);
        SysConfig retConfig = configMapper.selectConfig(config);
        if (StringUtils.isNotNull(retConfig))
        {
            redisCache.setCacheObject(getCacheKey(configKey), retConfig.getConfigValue(), 30, TimeUnit.MINUTES);
            return retConfig.getConfigValue();
        }
        return StringUtils.EMPTY;
    }

    /**
     * 获取验证码开关
     * 
     * @return true开启，false关闭
     */
    @Override
    public boolean selectCaptchaEnabled()
    {
        String captchaEnabled = selectConfigByKey("sys.account.captchaEnabled");
        if (StringUtils.isEmpty(captchaEnabled))
        {
            return true;
        }
        return Convert.toBool(captchaEnabled);
    }

    /**
     * 查询参数配置列表
     * 
     * @param config 参数配置信息
     * @return 参数配置集合
     */
    @Override
    public List<SysConfig> selectConfigList(SysConfig config)
    {
        // 前台业务页面已托管管理的配置键（企业信息查询、Embedding、NL2Query开关等）
        // 不在参数设置列表展示，仅作为数据存储，避免与业务设置页重复展示误导用户。
        // 精确按configKey查询时不过滤，保证业务代码按键读写的兼容性。
        if (StringUtils.isEmpty(config.getConfigKey()))
        {
            config.getParams().put("hideBizKeys", "1");
        }
        return configMapper.selectConfigList(config);
    }

    /**
     * 新增参数配置（清除对应缓存）
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    @CacheEvict(value = "sysConfig", key = "'config:' + #config.configKey")
    public int insertConfig(SysConfig config)
    {
        int row = configMapper.insertConfig(config);
        if (row > 0)
        {
            // 先删除可能存在的旧缓存（虽然新增时不太可能有，但安全起见）
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue(), 30, TimeUnit.MINUTES);
        }
        return row;
    }

    /**
     * 按键名强制更新配置值（用于内部程序化写入，如敏感密钥加密迁移）
     * 键不存在时插入；自动清缓存（Redis + Spring Cache）
     */
    @Override
    @CacheEvict(value = "sysConfig", key = "'config:' + #configKey")
    public int updateConfigByKeyForced(String configKey, String configValue)
    {
        SysConfig query = new SysConfig();
        query.setConfigKey(configKey);
        SysConfig existing = configMapper.selectConfig(query);
        int row;
        if (existing != null)
        {
            existing.setConfigValue(configValue);
            existing.setUpdateBy("system");
            row = configMapper.updateConfig(existing);
        }
        else
        {
            SysConfig insert = new SysConfig();
            insert.setConfigName(configKey);
            insert.setConfigKey(configKey);
            insert.setConfigValue(configValue);
            insert.setConfigType("Y");
            insert.setCreateBy("system");
            row = configMapper.insertConfig(insert);
        }
        redisCache.deleteObject(getCacheKey(configKey));
        redisCache.setCacheObject(getCacheKey(configKey), configValue, 30, TimeUnit.MINUTES);
        return row;
    }

    /**
     * 修改参数配置（清除对应缓存）
     * 系统内置参数允许修改参数键值，但不允许修改参数键名和内置类型
     * 修改后立即生效：先清除Redis手动缓存，再写入新值，Spring @CacheEvict 自动清除 Spring Cache 层
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    @CacheEvict(value = "sysConfig", key = "'config:' + #config.configKey")
    public int updateConfig(SysConfig config)
    {
        SysConfig temp = configMapper.selectConfigById(config.getConfigId());
        if (!StringUtils.equals(temp.getConfigKey(), config.getConfigKey()))
        {
            redisCache.deleteObject(getCacheKey(temp.getConfigKey()));
        }

        int row = configMapper.updateConfig(config);
        if (row > 0)
        {
            // 先删除旧缓存，再写入新值，确保不会残留旧数据
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue(), 30, TimeUnit.MINUTES);
        }
        return row;
    }

    /**
     * 批量删除参数信息
     * 
     * @param configIds 需要删除的参数ID
     */
    @Override
    public void deleteConfigByIds(Long[] configIds)
    {
        for (Long configId : configIds)
        {
            SysConfig config = selectConfigById(configId);
            if (StringUtils.equals(UserConstants.YES, config.getConfigType()))
            {
                throw new ServiceException(String.format("内置参数【%1$s】不能删除 ", config.getConfigKey()));
            }
            configMapper.deleteConfigById(configId);
            redisCache.deleteObject(getCacheKey(config.getConfigKey()));
        }
    }

    /**
     * 加载参数缓存数据
     */
    @Override
    public void loadingConfigCache()
    {
        List<SysConfig> configsList = configMapper.selectConfigList(new SysConfig());
        for (SysConfig config : configsList)
        {
            redisCache.setCacheObject(getCacheKey(config.getConfigKey()), config.getConfigValue());
        }
    }

    /**
     * 清空参数缓存数据
     */
    @Override
    public void clearConfigCache()
    {
        Collection<String> keys = redisCache.keys(CacheConstants.SYS_CONFIG_KEY + "*");
        redisCache.deleteObject(keys);
    }

    /**
     * 重置参数缓存数据
     */
    @Override
    public void resetConfigCache()
    {
        clearConfigCache();
        loadingConfigCache();
    }

    /**
     * 校验参数键名是否唯一
     * 
     * @param config 参数配置信息
     * @return 结果
     */
    @Override
    public boolean checkConfigKeyUnique(SysConfig config)
    {
        Long configId = StringUtils.isNull(config.getConfigId()) ? -1L : config.getConfigId();
        SysConfig info = configMapper.checkConfigKeyUnique(config.getConfigKey());
        if (StringUtils.isNotNull(info) && info.getConfigId().longValue() != configId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }

    /**
     * 设置cache key
     * 
     * @param configKey 参数键
     * @return 缓存键key
     */
    private String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_CONFIG_KEY + configKey;
    }
}
