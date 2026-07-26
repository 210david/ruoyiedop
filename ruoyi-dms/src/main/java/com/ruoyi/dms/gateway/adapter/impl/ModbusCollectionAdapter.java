package com.ruoyi.dms.gateway.adapter.impl;

import com.ruoyi.dms.gateway.adapter.DataCollectionAdapter;
import com.ruoyi.dms.gateway.dto.DataPacket;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Modbus TCP/RTU 数据采集适配器
 * 主动轮询方式采集设备数据
 * 
 * 注意：此适配器为框架实现，实际Modbus通信需要依赖库如 jamod 或 modbus4j
 */
@Slf4j
@Component
public class ModbusCollectionAdapter implements DataCollectionAdapter {

    /** 设备连接配置缓存 */
    private final Map<String, DeviceConnection> deviceConnections = new ConcurrentHashMap<>();

    /** 轮询调度器 */
    private ScheduledExecutorService scheduler;

    /** 运行状态 */
    private volatile boolean running = false;

    @Override
    public String getAdapterType() {
        return "MODBUS_ADAPTER";
    }

    @Override
    public String getProtocolType() {
        return "MODBUS_TCP";
    }

    @Override
    public void start() {
        if (running) {
            return;
        }

        scheduler = Executors.newScheduledThreadPool(10);
        running = true;

        log.info("Modbus适配器启动成功");
    }

    @Override
    public void stop() {
        if (!running) {
            return;
        }

        running = false;

        if (scheduler != null) {
            scheduler.shutdown();
            try {
                scheduler.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        // 断开所有设备连接
        deviceConnections.values().forEach(DeviceConnection::disconnect);
        deviceConnections.clear();

        log.info("Modbus适配器已停止");
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public DataPacket receive(Object rawData) {
        // Modbus为主动轮询模式，不通过receive接收
        return null;
    }

    @Override
    public boolean validate(DataPacket packet) {
        return packet != null && packet.getDeviceCode() != null;
    }

    /**
     * 注册设备并开始轮询采集
     * 
     * @param deviceCode 设备编码
     * @param host 设备IP地址
     * @param port 设备端口（默认502）
     * @param slaveId 从站ID
     * @param registerMap 寄存器映射配置
     * @param pollInterval 轮询间隔（秒）
     */
    public void registerDevice(String deviceCode, String host, int port, int slaveId,
                               Map<String, RegisterMapping> registerMap, int pollInterval) {

        DeviceConnection connection = new DeviceConnection(deviceCode, host, port, slaveId, registerMap);
        deviceConnections.put(deviceCode, connection);

        // 启动定时轮询
        scheduler.scheduleAtFixedRate(() -> {
            try {
                pollDevice(connection);
            } catch (Exception e) {
                log.error("轮询设备失败: device={}, error={}", deviceCode, e.getMessage());
            }
        }, 0, pollInterval, TimeUnit.SECONDS);

        log.info("Modbus设备注册成功: device={}, {}:{}, slaveId={}", deviceCode, host, port, slaveId);
    }

    /**
     * 移除设备
     */
    public void unregisterDevice(String deviceCode) {
        DeviceConnection connection = deviceConnections.remove(deviceCode);
        if (connection != null) {
            connection.disconnect();
            log.info("Modbus设备已移除: {}", deviceCode);
        }
    }

    /**
     * 轮询采集设备数据
     */
    private void pollDevice(DeviceConnection connection) {
        // TODO: 实际实现需要使用Modbus库读取寄存器
        // 以下为模拟实现

        try {
            if (!connection.isConnected()) {
                connection.connect();
            }

            // 读取各个寄存器值
            DataPacket packet = new DataPacket();
            packet.setDeviceCode(connection.getDeviceCode());
            packet.setCollectTime(LocalDateTime.now());
            packet.setSourceProtocol("MODBUS_TCP");

            Map<String, BigDecimal> params = new HashMap<>();

            for (Map.Entry<String, RegisterMapping> entry : connection.getRegisterMap().entrySet()) {
                String paramName = entry.getKey();
                RegisterMapping mapping = entry.getValue();

                // 实际实现：读取Modbus寄存器
                // int value = modbusMaster.readInputRegisters(slaveId, address, count);
                // double realValue = value * mapping.getScale() + mapping.getOffset();

                // 模拟数据
                double mockValue = Math.random() * 100;
                params.put(paramName, BigDecimal.valueOf(mockValue));
            }

            packet.setParams(params);
            packet.setRunStatus("0"); // 假设运行中

            // 发送到处理服务
            // dataGatewayService.processDataPacket(packet);

            log.debug("Modbus轮询完成: device={}, params={}", connection.getDeviceCode(), params);

        } catch (Exception e) {
            log.error("Modbus轮询异常: device={}, error={}", connection.getDeviceCode(), e.getMessage());
            connection.setConnected(false);
        }
    }

    /**
     * 设备连接信息
     */
    private static class DeviceConnection {
        private final String deviceCode;
        private final String host;
        private final int port;
        private final int slaveId;
        private final Map<String, RegisterMapping> registerMap;
        private volatile boolean connected = false;

        // TODO: 实际的Modbus连接对象
        // private ModbusTCPMaster modbusMaster;

        public DeviceConnection(String deviceCode, String host, int port, int slaveId,
                               Map<String, RegisterMapping> registerMap) {
            this.deviceCode = deviceCode;
            this.host = host;
            this.port = port;
            this.slaveId = slaveId;
            this.registerMap = registerMap;
        }

        public void connect() {
            // TODO: 建立Modbus连接
            // modbusMaster = new ModbusTCPMaster(host, port);
            // modbusMaster.connect();
            this.connected = true;
        }

        public void disconnect() {
            // TODO: 断开Modbus连接
            // if (modbusMaster != null) {
            //     modbusMaster.disconnect();
            // }
            this.connected = false;
        }

        public boolean isConnected() {
            return connected;
        }

        public void setConnected(boolean connected) {
            this.connected = connected;
        }

        public String getDeviceCode() {
            return deviceCode;
        }

        public Map<String, RegisterMapping> getRegisterMap() {
            return registerMap;
        }
    }

    /**
     * 寄存器映射配置
     */
    public static class RegisterMapping {
        /** 寄存器地址 */
        private int address;
        /** 数据类型: INT16/INT32/FLOAT32 */
        private String dataType;
        /** 缩放系数 */
        private double scale = 1.0;
        /** 偏移量 */
        private double offset = 0.0;

        public RegisterMapping(int address, String dataType) {
            this.address = address;
            this.dataType = dataType;
        }

        public RegisterMapping(int address, String dataType, double scale, double offset) {
            this.address = address;
            this.dataType = dataType;
            this.scale = scale;
            this.offset = offset;
        }

        // Getters
        public int getAddress() { return address; }
        public String getDataType() { return dataType; }
        public double getScale() { return scale; }
        public double getOffset() { return offset; }
    }
}
