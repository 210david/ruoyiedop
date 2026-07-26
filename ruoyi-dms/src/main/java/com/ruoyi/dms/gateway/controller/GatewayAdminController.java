package com.ruoyi.dms.gateway.controller;

import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.dms.gateway.manager.AdapterManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据采集网关管理控制器
 * 用于管理适配器状态、查看接入设备等
 */
@Api("数据采集网关管理")
@RestController
@RequestMapping("/dms/gateway/admin")
public class GatewayAdminController extends BaseController {

    @Autowired
    private AdapterManager adapterManager;

    /**
     * 获取所有适配器状态
     */
    @ApiOperation("获取适配器状态列表")
    @PreAuthorize("@ss.hasPermi('dms:data:config:list')")
    @GetMapping("/adapters")
    public AjaxResult listAdapters() {
        Map<String, Boolean> status = adapterManager.getAdapterStatus();

        List<Map<String, Object>> list = new ArrayList<>();
        for (Map.Entry<String, Boolean> entry : status.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("adapterType", entry.getKey());
            item.put("running", entry.getValue());

            // 添加适配器详细信息
            switch (entry.getKey()) {
                case "MQTT_ADAPTER":
                    item.put("protocol", "MQTT");
                    item.put("description", "MQTT协议订阅适配器");
                    item.put("mode", "被动接收");
                    break;
                case "MODBUS_ADAPTER":
                    item.put("protocol", "MODBUS_TCP");
                    item.put("description", "Modbus TCP轮询适配器");
                    item.put("mode", "主动轮询");
                    break;
                default:
                    item.put("protocol", "UNKNOWN");
                    item.put("description", "未知适配器");
                    item.put("mode", "未知");
            }

            list.add(item);
        }

        return AjaxResult.success(list);
    }

    /**
     * 启动适配器
     */
    @ApiOperation("启动指定适配器")
    @PreAuthorize("@ss.hasPermi('dms:data:config:edit')")
    @PostMapping("/adapter/{adapterType}/start")
    public AjaxResult startAdapter(@PathVariable String adapterType) {
        boolean success = adapterManager.startAdapter(adapterType);
        return success ? AjaxResult.success("启动成功") : AjaxResult.error("启动失败");
    }

    /**
     * 停止适配器
     */
    @ApiOperation("停止指定适配器")
    @PreAuthorize("@ss.hasPermi('dms:data:config:edit')")
    @PostMapping("/adapter/{adapterType}/stop")
    public AjaxResult stopAdapter(@PathVariable String adapterType) {
        boolean success = adapterManager.stopAdapter(adapterType);
        return success ? AjaxResult.success("停止成功") : AjaxResult.error("停止失败");
    }

    /**
     * 获取接入文档
     */
    @ApiOperation("获取设备接入文档")
    @GetMapping("/docs")
    public AjaxResult getIntegrationDocs() {
        Map<String, Object> docs = new HashMap<>();

        // HTTP推送接口文档
        Map<String, Object> httpDoc = new HashMap<>();
        httpDoc.put("name", "HTTP数据推送");
        httpDoc.put("endpoint", "POST /api/dms/gateway/push");
        httpDoc.put("contentType", "application/json");
        httpDoc.put("description", "设备或边缘网关通过HTTP POST推送数据");

        Map<String, String> httpExample = new HashMap<>();
        httpExample.put("request", "{\n" +
                "  \"deviceCode\": \"EQ001\",\n" +
                "  \"accessKey\": \"sk_abc123\",\n" +
                "  \"collectTime\": \"2024-01-15 10:30:00\",\n" +
                "  \"runStatus\": \"0\",\n" +
                "  \"runHours\": 123.5,\n" +
                "  \"productCount\": 1000,\n" +
                "  \"params\": {\n" +
                "    \"param1\": 65.2,\n" +
                "    \"param2\": 0.8\n" +
                "  }\n" +
                "}");
        httpDoc.put("example", httpExample);

        // MQTT接入文档
        Map<String, Object> mqttDoc = new HashMap<>();
        mqttDoc.put("name", "MQTT数据推送");
        mqttDoc.put("broker", "tcp://localhost:1883");
        mqttDoc.put("topic", "dms/device/{deviceCode}/data");
        mqttDoc.put("description", "设备通过MQTT发布数据到指定主题");

        Map<String, String> mqttExample = new HashMap<>();
        mqttExample.put("topic", "dms/device/EQ001/data");
        mqttExample.put("payload", "{\n" +
                "  \"collectTime\": \"2024-01-15 10:30:00\",\n" +
                "  \"runStatus\": \"0\",\n" +
                "  \"runHours\": 123.5,\n" +
                "  \"productCount\": 1000\n" +
                "}");
        mqttDoc.put("example", mqttExample);

        // 简化GET接口
        Map<String, Object> getDoc = new HashMap<>();
        getDoc.put("name", "简化GET推送");
        getDoc.put("endpoint", "GET /api/dms/gateway/push/simple");
        getDoc.put("description", "适用于简单设备，通过URL参数推送");
        getDoc.put("example", "GET /api/dms/gateway/push/simple?" +
                "deviceCode=EQ001&accessKey=sk_abc123&runStatus=0&runHours=123.5");

        List<Map<String, Object>> protocols = new ArrayList<>();
        protocols.add(httpDoc);
        protocols.add(mqttDoc);
        protocols.add(getDoc);

        docs.put("protocols", protocols);
        docs.put("version", "1.0");
        docs.put("updateTime", "2024-01-15");

        return AjaxResult.success(docs);
    }

    /**
     * 生成设备接入密钥
     */
    @ApiOperation("生成设备接入密钥")
    @PreAuthorize("@ss.hasPermi('dms:data:config:edit')")
    @PostMapping("/device/{equipmentId}/key")
    public AjaxResult generateAccessKey(@PathVariable Long equipmentId) {
        // TODO: 生成并保存设备访问密钥
        String accessKey = "sk_" + generateRandomKey();

        Map<String, Object> result = new HashMap<>();
        result.put("equipmentId", equipmentId);
        result.put("accessKey", accessKey);
        result.put("note", "请妥善保管，此密钥仅显示一次");

        return AjaxResult.success("生成成功", result);
    }

    private String generateRandomKey() {
        // 生成16位随机字符串
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(chars.charAt((int) (Math.random() * chars.length())));
        }
        return sb.toString();
    }
}
