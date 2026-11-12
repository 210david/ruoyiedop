package com.ruoyi.kingdee.controller;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.kingdee.adapter.AppSecretRefresher;
import com.ruoyi.kingdee.adapter.KingdeeHttpClient;
import com.ruoyi.kingdee.config.KingdeeConfigResolver;
import com.ruoyi.kingdee.sync.CustomerSyncService;
import com.ruoyi.kingdee.sync.MaterialSyncService;
import com.ruoyi.kingdee.sync.SupplierSyncService;
import com.ruoyi.kingdee.sync.SyncResult;

/**
 * 金蝶云星辰 管理入口
 */
@RestController
@RequestMapping("/kingdee")
public class KingdeeSyncController extends BaseController
{
    private static final Logger log = LoggerFactory.getLogger(KingdeeSyncController.class);

    @Autowired private KingdeeConfigResolver resolver;
    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private AppSecretRefresher appSecretRefresher;
    @Autowired private CustomerSyncService customerSyncService;
    @Autowired private MaterialSyncService materialSyncService;
    @Autowired private SupplierSyncService supplierSyncService;

    // ==================== 配置管理 ====================

    @PreAuthorize("@ss.hasPermi('kingdee:config:list')")
    @GetMapping("/config")
    public AjaxResult getConfig(@RequestParam(value = "showSecret", required = false) String showSecret)
    {
        boolean mask = !"true".equalsIgnoreCase(showSecret);
        return AjaxResult.success(resolver.snapshot(mask));
    }

    @PreAuthorize("@ss.hasPermi('kingdee:config:edit')")
    @PutMapping("/config")
    public AjaxResult updateConfig(@RequestBody Map<String, Object> body)
    {
        String op = getUsername();
        resolver.save(KingdeeConfigResolver.K_ENABLED,     boolStr(body.get("enabled")), op);
        String baseUrl = str(body.get("baseUrl"));
        if (StringUtils.isNotEmpty(baseUrl) && !baseUrl.startsWith("http"))
            return AjaxResult.error("API根地址必须以 http:// 或 https:// 开头");
        resolver.save(KingdeeConfigResolver.K_BASE_URL,    baseUrl, op);
        resolver.save(KingdeeConfigResolver.K_CLIENT_ID,   str(body.get("clientId")), op);
        resolver.save(KingdeeConfigResolver.K_CLIENT_SECRET, str(body.get("clientSecret")), op);
        resolver.save(KingdeeConfigResolver.K_APP_KEY,      str(body.get("appKey")), op);
        String secret = str(body.get("appSecret"));
        if (StringUtils.isNotEmpty(secret)) resolver.save(KingdeeConfigResolver.K_APP_SECRET, secret, op);
        resolver.save(KingdeeConfigResolver.K_ROUTER_ADDR, str(body.get("routerAddr")), op);
        resolver.save(KingdeeConfigResolver.K_TIMEOUT,     str(body.get("timeout")), op);
        resolver.save(KingdeeConfigResolver.K_RETRY,       str(body.get("retryTimes")), op);
        resolver.save(KingdeeConfigResolver.K_SYNC_CRON,   str(body.get("syncCron")), op);
        resolver.save(KingdeeConfigResolver.K_OUTER_INSTANCE_ID, str(body.get("outerInstanceId")), op);
        resolver.save(KingdeeConfigResolver.K_SECRET_REFRESH_CRON, str(body.get("secretRefreshCron")), op);

        kingdeeClient.syncConfigToSdk();
        return AjaxResult.success("配置保存成功，SDK 已即时同步");
    }

    @PreAuthorize("@ss.hasPermi('kingdee:config:edit')")
    @PostMapping("/config/test")
    public AjaxResult testConfig()
    {
        Map<String, Object> result = new HashMap<>();
        long start = System.currentTimeMillis();
        try
        {
            kingdeeClient.invalidateTokenCache();
            kingdeeClient.post("/bd/customer", Map.of("page", 1, "pagesize", 1));
            result.put("success", true);
            result.put("latencyMs", System.currentTimeMillis() - start);
            result.put("message", "配置正确，连通性测试通过");
        }
        catch (Exception e)
        {
            result.put("success", false);
            result.put("latencyMs", System.currentTimeMillis() - start);
            result.put("error", e.getMessage());
        }
        return AjaxResult.success(result);
    }

    // ==================== 同步操作 ====================

    @Log(title = "金蝶客户同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('kingdee:sync:customer')")
    @PostMapping("/sync/customer")
    public AjaxResult syncCustomer()
    {
        if (!resolver.isEnabled()) return AjaxResult.error("金蝶对接未启用");
        SyncResult r = customerSyncService.syncAll();
        AjaxResult ajax = AjaxResult.success("客户同步完成");
        ajax.put("statistics", r.getStatistics());
        ajax.put("elapsedMs", r.getElapsedMs());
        ajax.put("samples", r.getSamples());
        ajax.put("skipReasons", r.getSkipReasons());
        return ajax;
    }

    @Log(title = "金蝶物料同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('kingdee:sync:material')")
    @PostMapping("/sync/material")
    public AjaxResult syncMaterial()
    {
        if (!resolver.isEnabled()) return AjaxResult.error("金蝶对接未启用");
        SyncResult r = materialSyncService.syncAll();
        AjaxResult ajax = AjaxResult.success("物料同步完成");
        ajax.put("statistics", r.getStatistics());
        ajax.put("elapsedMs", r.getElapsedMs());
        ajax.put("samples", r.getSamples());
        ajax.put("skipReasons", r.getSkipReasons());
        return ajax;
    }

    @Log(title = "金蝶供应商同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('kingdee:sync:supplier')")
    @PostMapping("/sync/supplier")
    public AjaxResult syncSupplier()
    {
        if (!resolver.isEnabled()) return AjaxResult.error("金蝶对接未启用");
        SyncResult r = supplierSyncService.syncAll();
        AjaxResult ajax = AjaxResult.success("供应商同步完成");
        ajax.put("statistics", r.getStatistics());
        ajax.put("elapsedMs", r.getElapsedMs());
        ajax.put("samples", r.getSamples());
        ajax.put("skipReasons", r.getSkipReasons());
        return ajax;
    }

    @Log(title = "金蝶全模块同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('kingdee:sync:all')")
    @PostMapping("/sync/all")
    public AjaxResult syncAll()
    {
        if (!resolver.isEnabled()) return AjaxResult.error("金蝶对接未启用");
        Map<String, Object> all = new HashMap<>();
        try { all.put("customer",  customerSyncService.syncAll()); }  catch (Exception e) { log.error("客户同步异常", e); }
        try { all.put("material",  materialSyncService.syncAll()); }  catch (Exception e) { log.error("物料同步异常", e); }
        try { all.put("supplier", supplierSyncService.syncAll()); } catch (Exception e) { log.error("供应商同步异常", e); }
        AjaxResult ajax = AjaxResult.success("全模块同步完成");
        ajax.put("modules", all);
        return ajax;
    }

    // ==================== appSecret 自动刷新 ====================

    /** 手动触发 appSecret 刷新（从金蝶主动拉取最新授权信息） */
    @Log(title = "金蝶appSecret刷新", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('kingdee:config:edit')")
    @PostMapping("/secret/refresh")
    public AjaxResult refreshAppSecret()
    {
        Map<String, Object> r = appSecretRefresher.refreshNow();
        if (Boolean.TRUE.equals(r.get("ok")))
        {
            return AjaxResult.success(r);
        }
        return AjaxResult.error(String.valueOf(r.get("error")), r);
    }

    /** 查看 appSecret 刷新状态 */
    @GetMapping("/secret/status")
    public AjaxResult appSecretStatus()
    {
        Map<String, Object> s = new HashMap<>();
        s.put("outerInstanceId", resolver.getOuterInstanceId());
        s.put("secretRefreshCron", resolver.getSecretRefreshCron());
        s.put("lastSuccessAt", appSecretRefresher.getLastSuccessAt());
        s.put("configured", StringUtils.isNotEmpty(resolver.getOuterInstanceId()));
        return AjaxResult.success(s);
    }

    // ==================== 健康检查 ====================

    @GetMapping("/sync/health")
    public AjaxResult health() { return AjaxResult.success(resolver.snapshot(true)); }

    // ==================== 工具 ====================

    private static String str(Object o) { return o == null ? null : String.valueOf(o).trim(); }
    private static String boolStr(Object o)
    {
        if (o == null) return "false";
        if (o instanceof Boolean) return ((Boolean) o) ? "true" : "false";
        return String.valueOf(o).trim();
    }
}
