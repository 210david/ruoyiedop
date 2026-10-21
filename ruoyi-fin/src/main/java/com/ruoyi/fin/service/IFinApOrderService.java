package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinApOrder;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 应付单 Service 接口
 *
 * @author ruoyi
 */
public interface IFinApOrderService
{
    /**
     * 查询应付单列表
     *
     * @param ap 查询条件
     * @return 应付单列表
     */
    List<FinApOrder> selectApList(FinApOrder ap);

    /**
     * 按ID查询
     *
     * @param apId 应付单ID
     * @return 应付单
     */
    FinApOrder selectApById(Long apId);

    /**
     * 从已审核发票生成应付单（扫描）
     *
     * @return 处理结果，含 success/skip/fail
     */
    Map<String, Object> generateFromInvoice();

    /**
     * 同步已付金额（从 pms_invoice / pms_invoice_payment）
     *
     * @return 更新条数
     */
    int syncPaidAmount();

    /**
     * 付款核销
     *
     * @param apId          应付单ID
     * @param writeoffAmount 核销金额
     * @param remark        核销备注
     * @return 影响行数
     */
    int writeoff(Long apId, java.math.BigDecimal writeoffAmount, String remark);

    /**
     * 作废应付单
     *
     * @param apIds  应付单ID数组
     * @param reason 作废原因
     * @return 影响行数
     */
    int voidAp(Long[] apIds, String reason);

    /**
     * 账龄分析
     *
     * @param today 今日
     * @return 账龄分桶
     */
    List<Map<String, Object>> aging(Date today);

    /**
     * 逾期未付预警
     *
     * @param today 今日
     * @return 逾期应付单
     */
    List<FinApOrder> overdue(Date today);

    /**
     * 应付汇总
     *
     * @param period 会计期间
     * @return 汇总
     */
    Map<String, Object> summary(String period);
}