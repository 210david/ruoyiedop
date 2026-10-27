package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
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
    public DmsDataCollectionConfig selectConfigByEquipmentId(Long equipmentId) { return mapper.selectConfigByEquipmentId(equipmentId); }
    @Override
    public int insertConfig(DmsDataCollectionConfig config)
    {
        config.setDelFlag("0");
        // 新增配置时自动生成设备访问密钥
        if (StringUtils.isEmpty(config.getAccessKey()))
        {
            config.setAccessKey(generateAccessKey());
        }
        return mapper.insertConfig(config);
    }
    @Override
    public int updateConfig(DmsDataCollectionConfig config) { return mapper.updateConfig(config); }
    @Override
    public int deleteConfigByIds(Long[] configIds) { return mapper.deleteConfigByIds(configIds); }

    /**
     * 生成设备访问密钥（sk_ + 16位随机字符串）
     */
    public static String generateAccessKey()
    {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder("sk_");
        for (int i = 0; i < 16; i++)
        {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
