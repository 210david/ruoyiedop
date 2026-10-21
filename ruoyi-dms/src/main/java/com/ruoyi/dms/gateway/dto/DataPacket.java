package com.ruoyi.dms.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 标准数据包格式
 * 所有采集方式最终都转换为这个统一格式
 */
@Data
public class DataPacket {

    /** 设备唯一标识 */
    private String deviceCode;

    /** 设备ID（系统内部） */
    private Long equipmentId;

    /** 采集时间（设备端时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;

    /** 数据接收时间（服务端时间） */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime receiveTime;

    /** 运行状态: 0运行 1停机 2故障 */
    private String runStatus;

    /** 运行小时数 */
    private BigDecimal runHours;

    /** 加工件数 */
    private BigDecimal productCount;

    /** 自定义参数值集合 */
    private Map<String, BigDecimal> params;

    /** 原始数据（用于追溯） */
    private String rawData;

    /** 数据来源协议 */
    private String sourceProtocol;

    /** 数据签名（用于安全验证） */
    private String signature;

    /** 数据包版本 */
    private String version = "1.0";

    // 便捷方法
    public static DataPacketBuilder builder() {
        return new DataPacketBuilder();
    }

    public static class DataPacketBuilder {
        private DataPacket packet = new DataPacket();

        public DataPacketBuilder deviceCode(String deviceCode) {
            packet.setDeviceCode(deviceCode);
            return this;
        }

        public DataPacketBuilder equipmentId(Long equipmentId) {
            packet.setEquipmentId(equipmentId);
            return this;
        }

        public DataPacketBuilder collectTime(LocalDateTime collectTime) {
            packet.setCollectTime(collectTime);
            return this;
        }

        public DataPacketBuilder runStatus(String runStatus) {
            packet.setRunStatus(runStatus);
            return this;
        }

        public DataPacketBuilder runHours(BigDecimal runHours) {
            packet.setRunHours(runHours);
            return this;
        }

        public DataPacketBuilder productCount(BigDecimal productCount) {
            packet.setProductCount(productCount);
            return this;
        }

        public DataPacketBuilder params(Map<String, BigDecimal> params) {
            packet.setParams(params);
            return this;
        }

        public DataPacketBuilder sourceProtocol(String protocol) {
            packet.setSourceProtocol(protocol);
            return this;
        }

        public DataPacket build() {
            packet.setReceiveTime(LocalDateTime.now());
            return packet;
        }
    }
}
