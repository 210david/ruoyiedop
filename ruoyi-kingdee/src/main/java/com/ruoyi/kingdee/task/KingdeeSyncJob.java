package com.ruoyi.kingdee.task;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ruoyi.kingdee.config.KingdeeConfigResolver;
import com.ruoyi.kingdee.sync.CustomerSyncService;
import com.ruoyi.kingdee.sync.MaterialSyncService;
import com.ruoyi.kingdee.sync.SupplierSyncService;
import com.ruoyi.kingdee.sync.SyncResult;

/**
 * 金蝶云星辰 定时同步任务
 * <p>
 * 在若依 Quartz 调度器中配置：
 * <pre>
 * 调用目标字符串: kingdeeSyncJob.runAll
 * cron: 0 0/30 * * * ?  （每30分钟）
 * </pre>
 *
 * @author ruoyi
 */
@Component("kingdeeSyncJob")
public class KingdeeSyncJob
{
    private static final Logger log = LoggerFactory.getLogger(KingdeeSyncJob.class);

    @Autowired
    private KingdeeConfigResolver resolver;

    @Autowired
    private CustomerSyncService customerSync;

    @Autowired
    private MaterialSyncService materialSync;

    @Autowired
    private SupplierSyncService supplierSync;

    /**
     * 执行所有已启用模块的同步
     */
    public void runAll()
    {
        if (!resolver.isEnabled())
        {
            log.info("[金蝶定时任务] 对接未启用，跳过本次同步");
            return;
        }

        long start = System.currentTimeMillis();
        log.info("[金蝶定时任务] === 开始全量同步 ===");

        Map<String, SyncResult> all = new HashMap<>();

        try { all.put("customer",  customerSync.syncAll()); }
        catch (Exception e) { log.error("[金蝶定时任务] 客户同步异常: {}", e.getMessage()); }

        try { all.put("material",  materialSync.syncAll()); }
        catch (Exception e) { log.error("[金蝶定时任务] 物料同步异常: {}", e.getMessage()); }

        try { all.put("supplier", supplierSync.syncAll()); }
        catch (Exception e) { log.error("[金蝶定时任务] 供应商同步异常: {}", e.getMessage()); }

        long cost = System.currentTimeMillis() - start;
        log.info("[金蝶定时任务] === 完成  总耗时 {} ms  统计={}", cost, all);
    }

    /** 仅同步客户 */
    public void runCustomer() { if (resolver.isEnabled()) customerSync.syncAll(); }
    /** 仅同步物料 */
    public void runMaterial() { if (resolver.isEnabled()) materialSync.syncAll(); }
    /** 仅同步供应商 */
    public void runSupplier() { if (resolver.isEnabled()) supplierSync.syncAll(); }
}
