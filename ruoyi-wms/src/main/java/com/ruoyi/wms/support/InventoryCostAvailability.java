package com.ruoyi.wms.support;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 存货成本表可用性探测（WMS 侧只读判定）
 *
 * <p>【为什么需要】库存页要显示「结存金额」，权威口径来自财务的 fin_inventory_cost。
 * 但 ruoyi-fin 的 pom 依赖 ruoyi-wms，WMS 若反向依赖财务模块会形成 Maven 循环依赖，
 * 因此 WMS 只能以「只读表」方式取数，且不能假设财务表一定存在。</p>
 *
 * <p>【部署事实】主部署包 deploy/config/sql/EDOP_ALL.sql 共 40 张表，不含任何 fin_* 表；
 * 财务表由独立的 sql/fin/fin_01_schema.sql 安装。因此未上线财务模块的环境里
 * fin_inventory_cost 不存在，直接 JOIN 会导致库存查询整体 SQL 报错。</p>
 *
 * <p>【策略】启动时探测一次并缓存结果；缺表时库存页自动降级到 WMS 自有口径，
 * 不做任何报错，也不影响其他库存功能。</p>
 *
 * @author ruoyi
 */
@Component
public class InventoryCostAvailability
{
    private static final Logger log = LoggerFactory.getLogger(InventoryCostAvailability.class);

    /** 财务存货成本台账表名 */
    private static final String COST_TABLE = "fin_inventory_cost";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /** 财务成本表是否可用（启动时探测一次） */
    private volatile boolean costTableAvailable = false;

    @PostConstruct
    public void probe()
    {
        try
        {
            Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES "
                    + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ?",
                    Integer.class, COST_TABLE);
            costTableAvailable = count != null && count > 0;
        }
        catch (Exception e)
        {
            // 探测失败一律按不可用处理：宁可显示「估算」，也不让库存查询挂掉
            costTableAvailable = false;
            log.warn("存货成本表可用性探测失败，库存金额将降级为 WMS 自有口径：{}", e.getMessage());
        }
        log.info("存货成本表 {} 可用性：{}", COST_TABLE,
                costTableAvailable ? "可用（库存金额取成本台账）" : "不可用（库存金额降级为估算口径）");
    }

    /**
     * 财务成本台账是否可用于取数。
     */
    public boolean isCostTableAvailable()
    {
        return costTableAvailable;
    }
}
