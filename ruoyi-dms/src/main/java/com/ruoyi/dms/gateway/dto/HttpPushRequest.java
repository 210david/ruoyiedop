package com.ruoyi.dms.gateway.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * HTTP推送请求DTO
 * 设备或边缘网关通过HTTP POST推送数据时使用
 */
@Data
public class HttpPushRequest {

    /** 设备编码（必填） */
    @NotBlank(message = "设备编码不能为空")
    private String deviceCode;

    /** 访问密钥（用于身份验证） */
    @NotBlank(message = "访问密钥不能为空")
    private String accessKey;

    /** 采集时间（必填，ISO格式） */
    @NotNull(message = "采集时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectTime;

    /** 运行状态: 0运行 1停机 2故障 */
    private String runStatus;

    /** 运行小时数 */
    private BigDecimal runHours;

    /** 加工件数 */
    private BigDecimal productCount;

    /** 自定义参数值（key-value形式） */
    private Map<String, BigDecimal> params;

    /** 数据签名（可选，用于高级安全验证） */
    private String signature;

    /** 数据批次号（用于批量推送时关联） */
    private String batchNo;
}
