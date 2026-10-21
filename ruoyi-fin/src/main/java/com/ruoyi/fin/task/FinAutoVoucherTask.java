package com.ruoyi.fin.task;

import com.ruoyi.fin.domain.vo.AutoVoucherScanResult;
import com.ruoyi.fin.service.IFinAutoVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 财务自动记账定时任务
 *
 * <p>【设计约束 · 零侵入】既有业务模块零改动，不在 pms/mk/wms/hr 的 Service 中插入
 * 任何财务调用。本任务由财务侧主动轮询已审核单据并生成凭证。</p>
 *
 * <p>Quartz 配置：</p>
 * <pre>
 *   Bean 名称： finAutoVoucherTask
 *   方法    ： scanAndGenerate
 *   cron    ： 0 0/2 * * * ? （每 2 分钟）
 * </pre>
 *
 * <p>幂等保障：扫描 SQL 用 NOT EXISTS 判断是否已生成凭证，
 * 数据库唯一索引 uk_source 兜底，重复触发不会产生重复凭证。</p>
 *
 * @author ruoyi
 */
@Component("finAutoVoucherTask")
public class FinAutoVoucherTask
{
    private static final Logger log = LoggerFactory.getLogger(FinAutoVoucherTask.class);

    @Autowired
    private IFinAutoVoucherService autoVoucherService;

    /**
     * 扫描已审核单据并自动生成记账凭证。
     *
     * <p>调度频率：每 2 分钟。财务场景对实时性要求不高，
     * 最多 2 分钟延迟可接受，换取既有模块零改动。</p>
     */
    public void scanAndGenerate()
    {
        try
        {
            AutoVoucherScanResult result = autoVoucherService.scanAndGenerate();
            if (result.getTotal() > 0)
            {
                log.warn("财务自动记账完成，生成凭证 {} 张（发票 {} 笔，发货 {} 笔，出库 {} 笔，薪酬 {} 笔）",
                        result.getTotal(),
                        result.getInvoiceCount(),
                        result.getShipmentCount(),
                        result.getOutboundCount(),
                        result.getPayrollCount());
            }
            else
            {
                log.info("财务自动记账完成，无待生成凭证的单据");
            }
        }
        catch (Exception e)
        {
            // 记账失败不得抛出到 Quartz，避免任务被标记为失败后停止调度
            log.error("财务自动记账任务异常", e);
        }
    }
}
