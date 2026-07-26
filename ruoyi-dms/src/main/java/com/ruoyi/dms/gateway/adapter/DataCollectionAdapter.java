package com.ruoyi.dms.gateway.adapter;

import com.ruoyi.dms.gateway.dto.DataPacket;

/**
 * 数据采集适配器接口
 * 所有采集方式（HTTP/MQTT/Modbus等）都实现此接口
 */
public interface DataCollectionAdapter {

    /**
     * 适配器类型标识
     */
    String getAdapterType();

    /**
     * 协议类型: HTTP/MQTT/MODBUS/OPCUA等
     */
    String getProtocolType();

    /**
     * 接收原始数据并转换为标准数据包
     */
    DataPacket receive(Object rawData);

    /**
     * 验证数据合法性
     */
    boolean validate(DataPacket packet);

    /**
     * 启动适配器
     */
    void start();

    /**
     * 停止适配器
     */
    void stop();

    /**
     * 是否正在运行
     */
    boolean isRunning();
}
