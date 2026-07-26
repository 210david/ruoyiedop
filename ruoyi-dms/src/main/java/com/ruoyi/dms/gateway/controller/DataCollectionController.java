package com.ruoyi.dms.gateway.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.dms.gateway.dto.DataPacket;
import com.ruoyi.dms.gateway.dto.HttpPushRequest;
import com.ruoyi.dms.gateway.dto.PushResponse;
import com.ruoyi.dms.gateway.service.DataGatewayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据采集网关控制器
 * 提供标准HTTP接口供设备/边缘网关推送数据
 */
@Slf4j
@Api("数据采集网关接口")
@RestController
@RequestMapping("/api/dms/gateway")
public class DataCollectionController extends BaseController {

    @Autowired
    private DataGatewayService dataGatewayService;

    /**
     * 单条数据推送接口（最常用）
     */
    @ApiOperation("单条数据推送")
    @PostMapping("/push")
    public PushResponse pushData(@RequestBody @Validated HttpPushRequest request) {
        log.debug("收到数据推送: device={}", request.getDeviceCode());

        // 1. 验证访问密钥
        if (!dataGatewayService.validateAccessKey(request.getDeviceCode(), request.getAccessKey())) {
            log.warn("访问密钥验证失败: device={}", request.getDeviceCode());
            return PushResponse.error(2, "访问密钥验证失败");
        }

        // 2. 转换为标准数据包
        DataPacket packet = convertToDataPacket(request);

        // 3. 处理数据
        return dataGatewayService.processDataPacket(packet);
    }

    /**
     * 批量数据推送接口
     */
    @ApiOperation("批量数据推送")
    @PostMapping("/push/batch")
    public AjaxResult pushBatchData(@RequestBody @Validated HttpPushRequest[] requests) {
        log.debug("收到批量数据推送: count={}", requests.length);

        int successCount = 0;
        int failCount = 0;

        for (HttpPushRequest request : requests) {
            PushResponse response = pushData(request);
            if (response.isSuccess()) {
                successCount++;
            } else {
                failCount++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("total", requests.length);
        result.put("success", successCount);
        result.put("fail", failCount);

        return AjaxResult.success("批量处理完成", result);
    }

    /**
     * 简化版数据推送（用于简单设备）
     * URL参数方式：?deviceCode=xxx&accessKey=xxx&runStatus=0&runHours=10
     */
    @ApiOperation("简化版数据推送（URL参数）")
    @GetMapping("/push/simple")
    public PushResponse pushSimpleData(
            @RequestParam String deviceCode,
            @RequestParam String accessKey,
            @RequestParam(required = false) String runStatus,
            @RequestParam(required = false) BigDecimal runHours,
            @RequestParam(required = false) BigDecimal productCount,
            @RequestParam(required = false) BigDecimal param1,
            @RequestParam(required = false) BigDecimal param2,
            @RequestParam(required = false) BigDecimal param3) {

        // 验证密钥
        if (!dataGatewayService.validateAccessKey(deviceCode, accessKey)) {
            return PushResponse.error(2, "访问密钥验证失败");
        }

        // 构建数据包
        DataPacket packet = DataPacket.builder()
                .deviceCode(deviceCode)
                .runStatus(runStatus)
                .runHours(runHours)
                .productCount(productCount)
                .sourceProtocol("HTTP-GET")
                .build();

        // 设置自定义参数
        Map<String, BigDecimal> params = new HashMap<>();
        if (param1 != null) params.put("param1", param1);
        if (param2 != null) params.put("param2", param2);
        if (param3 != null) params.put("param3", param3);
        if (!params.isEmpty()) {
            packet.setParams(params);
        }

        return dataGatewayService.processDataPacket(packet);
    }

    /**
     * 设备心跳检测接口
     */
    @ApiOperation("设备心跳检测")
    @GetMapping("/heartbeat/{deviceCode}")
    public AjaxResult heartbeat(@PathVariable String deviceCode,
                                 @RequestParam String accessKey) {
        boolean valid = dataGatewayService.validateAccessKey(deviceCode, accessKey);
        if (!valid) {
            return AjaxResult.error("设备验证失败");
        }
        return AjaxResult.success("心跳正常");
    }

    /**
     * 获取设备最新数据（用于设备端同步）
     */
    @ApiOperation("获取设备最新数据")
    @GetMapping("/latest/{deviceCode}")
    public AjaxResult getLatestData(@PathVariable String deviceCode,
                                     @RequestParam String accessKey) {
        boolean valid = dataGatewayService.validateAccessKey(deviceCode, accessKey);
        if (!valid) {
            return AjaxResult.error("设备验证失败");
        }

        // TODO: 从Redis获取最新数据返回
        return AjaxResult.success("最新数据", null);
    }

    /**
     * 转换请求为数据包
     */
    private DataPacket convertToDataPacket(HttpPushRequest request) {
        DataPacket packet = DataPacket.builder()
                .deviceCode(request.getDeviceCode())
                .collectTime(request.getCollectTime())
                .runStatus(request.getRunStatus())
                .runHours(request.getRunHours())
                .productCount(request.getProductCount())
                .params(request.getParams())
                .sourceProtocol("HTTP-POST")
                .build();

        packet.setSignature(request.getSignature());
        return packet;
    }
}
