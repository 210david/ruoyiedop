package com.ruoyi.dms.gateway.service;

import com.ruoyi.dms.gateway.dto.DataPacket;
import com.ruoyi.dms.gateway.dto.PushResponse;

/**
 * 数据网关服务接口
 * 统一处理所有采集方式的数据
 */
public interface DataGatewayService {

    /**
     * 接收标准数据包并处理
     */
    PushResponse processDataPacket(DataPacket packet);

    /**
     * 验证设备访问密钥
     */
    boolean validateAccessKey(String deviceCode, String accessKey);

    /**
     * 检查参数告警
     */
    String checkAlarm(Long equipmentId, DataPacket packet);

    /**
     * 存储数据到数据库
     */
    Long saveDataRecord(DataPacket packet);

    /**
     * 获取设备ID通过设备编码
     */
    Long getEquipmentIdByCode(String deviceCode);

    /**
     * 设备密钥变更后刷新缓存（新密钥生效）
     */
    void refreshAccessKeyCache(String deviceCode);
}
