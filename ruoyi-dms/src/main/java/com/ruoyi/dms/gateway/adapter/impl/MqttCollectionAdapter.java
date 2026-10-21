package com.ruoyi.dms.gateway.adapter.impl;

import com.alibaba.fastjson2.JSON;
import com.ruoyi.dms.gateway.adapter.DataCollectionAdapter;
import com.ruoyi.dms.gateway.dto.DataPacket;
import com.ruoyi.dms.gateway.service.DataGatewayService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * MQTT数据采集适配器
 * 订阅MQTT主题接收设备数据
 */
@Slf4j
@Component
public class MqttCollectionAdapter implements DataCollectionAdapter {

    @Autowired
    private DataGatewayService dataGatewayService;

    @Value("${mqtt.broker:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:dms-gateway-client}")
    private String clientId;

    @Value("${mqtt.topic.prefix:dms/device/}")
    private String topicPrefix;

    @Value("${mqtt.username:}")
    private String username;

    @Value("${mqtt.password:}")
    private String password;

    private MqttClient mqttClient;
    private volatile boolean running = false;

    @PostConstruct
    public void init() {
        // 默认不自动启动，可通过配置控制
        // start();
    }

    @PreDestroy
    public void destroy() {
        stop();
    }

    @Override
    public String getAdapterType() {
        return "MQTT_ADAPTER";
    }

    @Override
    public String getProtocolType() {
        return "MQTT";
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        try {
            mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            options.setAutomaticReconnect(true);

            if (username != null && !username.isEmpty()) {
                options.setUserName(username);
                options.setPassword(password.toCharArray());
            }

            mqttClient.connect(options);

            // 订阅通配主题: dms/device/+/data
            String subscribeTopic = topicPrefix + "+/data";
            mqttClient.subscribe(subscribeTopic, 1);

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT连接断开: {}", cause.getMessage());
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    handleMqttMessage(topic, message);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 发布完成回调（订阅模式不需要处理）
                }
            });

            running = true;
            log.info("MQTT适配器启动成功，订阅主题: {}", subscribeTopic);

        } catch (Exception e) {
            log.error("MQTT适配器启动失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
            }
            running = false;
            log.info("MQTT适配器已停止");
        } catch (Exception e) {
            log.error("MQTT适配器停止失败: {}", e.getMessage(), e);
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public DataPacket receive(Object rawData) {
        // MQTT使用回调方式接收数据，此方法不直接调用
        return null;
    }

    @Override
    public boolean validate(DataPacket packet) {
        return packet != null &&
               packet.getDeviceCode() != null &&
               packet.getCollectTime() != null;
    }

    /**
     * 处理MQTT消息
     */
    private void handleMqttMessage(String topic, MqttMessage message) {
        try {
            String payload = new String(message.getPayload());
            log.debug("收到MQTT消息: topic={}, payload={}", topic, payload);

            // 从topic提取设备编码: dms/device/{deviceCode}/data
            String deviceCode = extractDeviceCode(topic);
            if (deviceCode == null) {
                log.warn("无法从topic提取设备编码: {}", topic);
                return;
            }

            // 解析JSON数据
            Map<String, Object> data = JSON.parseObject(payload, Map.class);

            // 转换为标准数据包
            DataPacket packet = parseMqttData(deviceCode, data);

            // 验证并处理
            if (validate(packet)) {
                dataGatewayService.processDataPacket(packet);
            } else {
                log.warn("数据包验证失败: {}", packet);
            }

        } catch (Exception e) {
            log.error("处理MQTT消息失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 从topic提取设备编码
     */
    private String extractDeviceCode(String topic) {
        // topic格式: dms/device/{deviceCode}/data
        String[] parts = topic.split("/");
        if (parts.length >= 4) {
            return parts[2];
        }
        return null;
    }

    /**
     * 解析MQTT数据为标准数据包
     */
    private DataPacket parseMqttData(String deviceCode, Map<String, Object> data) {
        DataPacket packet = new DataPacket();
        packet.setDeviceCode(deviceCode);
        packet.setSourceProtocol("MQTT");

        // 采集时间
        Object timeObj = data.get("collectTime");
        if (timeObj != null) {
            packet.setCollectTime(LocalDateTime.parse(timeObj.toString(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        } else {
            packet.setCollectTime(LocalDateTime.now());
        }

        // 运行状态
        packet.setRunStatus(String.valueOf(data.getOrDefault("runStatus", "0")));

        // 运行小时
        Object hoursObj = data.get("runHours");
        if (hoursObj != null) {
            packet.setRunHours(new BigDecimal(hoursObj.toString()));
        }

        // 加工件数
        Object countObj = data.get("productCount");
        if (countObj != null) {
            packet.setProductCount(new BigDecimal(countObj.toString()));
        }

        // 自定义参数
        @SuppressWarnings("unchecked")
        Map<String, BigDecimal> params = (Map<String, BigDecimal>) data.get("params");
        packet.setParams(params);

        return packet;
    }
}
