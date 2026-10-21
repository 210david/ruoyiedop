package com.ruoyi.dms.gateway.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据推送响应DTO
 */
@Data
public class PushResponse {

    /** 是否成功 */
    private boolean success;

    /** 响应码: 0成功 1参数错误 2认证失败 3设备不存在 4数据处理失败 */
    private int code;

    /** 响应消息 */
    private String message;

    /** 服务端接收时间 */
    private LocalDateTime serverTime;

    /** 数据记录ID（成功时返回） */
    private Long recordId;

    /** 告警信息（如果有） */
    private String alarmInfo;

    public static PushResponse ok(Long recordId) {
        PushResponse resp = new PushResponse();
        resp.setSuccess(true);
        resp.setCode(0);
        resp.setMessage("数据接收成功");
        resp.setServerTime(LocalDateTime.now());
        resp.setRecordId(recordId);
        return resp;
    }

    public static PushResponse error(int code, String message) {
        PushResponse resp = new PushResponse();
        resp.setSuccess(false);
        resp.setCode(code);
        resp.setMessage(message);
        resp.setServerTime(LocalDateTime.now());
        return resp;
    }
}
