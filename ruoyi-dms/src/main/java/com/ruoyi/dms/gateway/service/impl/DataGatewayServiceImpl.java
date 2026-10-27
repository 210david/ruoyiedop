package com.ruoyi.dms.gateway.service.impl;

import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.dms.domain.DmsDataCollectionConfig;
import com.ruoyi.dms.domain.DmsDataRecord;
import com.ruoyi.dms.domain.DmsEquipment;
import com.ruoyi.dms.gateway.dto.DataPacket;
import com.ruoyi.dms.gateway.dto.PushResponse;
import com.ruoyi.dms.gateway.service.DataGatewayService;
import com.ruoyi.dms.mapper.DmsDataRecordMapper;
import com.ruoyi.dms.mapper.DmsEquipmentMapper;
import com.ruoyi.dms.service.IDmsDataCollectionConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 数据网关服务实现
 */
@Slf4j
@Service
public class DataGatewayServiceImpl implements DataGatewayService {

    @Autowired
    private DmsEquipmentMapper equipmentMapper;

    @Autowired
    private DmsDataRecordMapper dataRecordMapper;

    @Autowired
    private IDmsDataCollectionConfigService configService;

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /** 访问密钥缓存前缀 */
    private static final String ACCESS_KEY_PREFIX = "dms:access:key:";

    /** 最新数据缓存前缀 */
    private static final String LATEST_DATA_PREFIX = "dms:latest:data:";

    /** 本地内存缓存（当Redis不可用时） */
    private final Map<String, Object> localCache = new ConcurrentHashMap<>();

    @Override
    public PushResponse processDataPacket(DataPacket packet) {
        try {
            // 1. 获取设备信息
            Long equipmentId = getEquipmentIdByCode(packet.getDeviceCode());
            if (equipmentId == null) {
                log.warn("设备不存在: {}", packet.getDeviceCode());
                return PushResponse.error(3, "设备不存在或未启用采集");
            }
            packet.setEquipmentId(equipmentId);

            // 2. 保存数据到数据库
            Long recordId = saveDataRecord(packet);

            // 3. 缓存最新数据（用于实时监控）
            cacheLatestData(equipmentId, packet);

            // 4. 检查告警
            String alarmInfo = checkAlarm(equipmentId, packet);

            // 5. 构建响应
            PushResponse response = PushResponse.ok(recordId);
            if (alarmInfo != null) {
                response.setAlarmInfo(alarmInfo);
            }

            log.info("数据接收成功: device={}, recordId={}", packet.getDeviceCode(), recordId);
            return response;

        } catch (Exception e) {
            log.error("数据处理失败: {}", e.getMessage(), e);
            return PushResponse.error(4, "数据处理失败: " + e.getMessage());
        }
    }

    @Override
    public boolean validateAccessKey(String deviceCode, String accessKey) {
        if (deviceCode == null || accessKey == null || accessKey.isEmpty()) {
            return false;
        }
        // 先从缓存查数据库中存储的密钥
        String cacheKey = ACCESS_KEY_PREFIX + deviceCode;
        String cachedKey = (String) getCache(cacheKey);
        String storedKey = cachedKey;

        if (storedKey == null) {
            // 从数据库验证：按设备编码找到设备，再从采集配置表读取密钥比对
            DmsEquipment equipment = equipmentMapper.selectEquipmentByCode(deviceCode);
            if (equipment == null || "2".equals(equipment.getDelFlag())) {
                return false;
            }
            DmsDataCollectionConfig config = configService.selectConfigByEquipmentId(equipment.getEquipmentId());
            if (config == null || config.getAccessKey() == null || config.getAccessKey().isEmpty()) {
                // 未配置密钥的设备拒绝访问
                return false;
            }
            storedKey = config.getAccessKey();
            // 缓存数据库中存储的密钥1小时
            setCache(cacheKey, storedKey, 1, TimeUnit.HOURS);
        }

        return storedKey.equals(accessKey);
    }

    @Override
    public void refreshAccessKeyCache(String deviceCode) {
        if (deviceCode == null) {
            return;
        }
        String cacheKey = ACCESS_KEY_PREFIX + deviceCode;
        if (redisTemplate != null) {
            try {
                redisTemplate.delete(cacheKey);
            } catch (Exception e) {
                log.warn("Redis删除密钥缓存失败: {}", e.getMessage());
            }
        }
        localCache.remove(cacheKey);
    }

    @Override
    public String checkAlarm(Long equipmentId, DataPacket packet) {
        // TODO: 实现告警检测逻辑
        // 1. 查询设备采集配置的告警阈值
        // 2. 对比当前数据是否超出阈值
        // 3. 触发告警通知
        return null;
    }

    @Override
    public Long saveDataRecord(DataPacket packet) {
        DmsDataRecord record = new DmsDataRecord();
        record.setEquipmentId(packet.getEquipmentId());
        record.setEquipmentCode(packet.getDeviceCode());
        record.setRunStatus(packet.getRunStatus());
        record.setRunHours(packet.getRunHours());
        record.setProductCount(packet.getProductCount());
        record.setCollectMode("1"); // 自动采集

        // 设置自定义参数值
        Map<String, BigDecimal> params = packet.getParams();
        if (params != null) {
            record.setParam1Value(params.get("param1"));
            record.setParam2Value(params.get("param2"));
            record.setParam3Value(params.get("param3"));
        }

        // 设置采集时间
        if (packet.getCollectTime() != null) {
            record.setCollectTime(Date.from(packet.getCollectTime()
                .atZone(ZoneId.systemDefault()).toInstant()));
        } else {
            record.setCollectTime(new Date());
        }

        dataRecordMapper.insertRecord(record);
        return record.getRecordId();
    }

    @Override
    public Long getEquipmentIdByCode(String deviceCode) {
        // 先从缓存查
        String cacheKey = "dms:equipment:code:" + deviceCode;
        Long equipmentId = (Long) getCache(cacheKey);

        if (equipmentId != null) {
            return equipmentId;
        }

        // 从数据库查
        DmsEquipment equipment = equipmentMapper.selectEquipmentByCode(deviceCode);
        if (equipment == null || "2".equals(equipment.getDelFlag())) {
            return null;
        }

        // 缓存24小时
        setCache(cacheKey, equipment.getEquipmentId(), 24, TimeUnit.HOURS);
        return equipment.getEquipmentId();
    }

    /**
     * 缓存最新数据
     */
    private void cacheLatestData(Long equipmentId, DataPacket packet) {
        String cacheKey = LATEST_DATA_PREFIX + equipmentId;
        setCache(cacheKey, packet, 24, TimeUnit.HOURS);
    }

    /**
     * 获取缓存（优先Redis，降级到本地缓存）
     */
    private Object getCache(String key) {
        if (redisTemplate != null) {
            try {
                return redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("Redis获取缓存失败，使用本地缓存: {}", e.getMessage());
            }
        }
        return localCache.get(key);
    }

    /**
     * 设置缓存（优先Redis，降级到本地缓存）
     */
    private void setCache(String key, Object value, long timeout, TimeUnit unit) {
        if (redisTemplate != null) {
            try {
                redisTemplate.opsForValue().set(key, value, timeout, unit);
                return;
            } catch (Exception e) {
                log.warn("Redis设置缓存失败，使用本地缓存: {}", e.getMessage());
            }
        }
        localCache.put(key, value);
    }
}
