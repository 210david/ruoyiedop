package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinArOrder;
import com.ruoyi.fin.domain.FinBadDebt;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 应收单 Service 接口
 *
 * @author ruoyi
 */
public interface IFinArOrderService
{
    List<FinArOrder> selectArList(FinArOrder ar);

    FinArOrder selectArById(Long arId);

    /**
     * 从已审核发货单生成应收单（扫描）
     *
     * @return 处理结果，含 success/skip/fail
     */
    Map<String, Object> generateFromShipment();

    /**
     * 同步已收金额（从 mk_payment_record）
     *
     * @return 更新条数
     */
    int syncReceivedAmount();

    /**
     * 收款核销
     *
     * @param arId           应收单ID
     * @param writeoffAmount 核销金额
     * @param remark         核销备注
     * @return 影响行数
     */
    int writeoff(Long arId, BigDecimal writeoffAmount, String remark);

    /**
     * 登记销项发票
     *
     * @param arId        应收单ID
     * @param invoiceNo   发票号码
     * @param invoiceCode 发票代码
     * @param invoiceDate 开票日期
     * @return 影响行数
     */
    int invoice(Long arId, String invoiceNo, String invoiceCode, Date invoiceDate);

    /**
     * 作废应收单
     *
     * @param arIds  应收单ID数组
     * @param reason 作废原因
     * @return 影响行数
     */
    int voidAr(Long[] arIds, String reason);

    /** 账龄分析 */
    List<Map<String, Object>> aging(Date today);

    /** 逾期未收预警 */
    List<FinArOrder> overdue(Date today);

    /** 应收汇总 */
    Map<String, Object> summary(String period);

    /** 坏账计提（按账龄区间比例） */
    List<FinBadDebt> provisionBadDebt(String period, Date today);

    /** 客户维度应收余额 */
    List<Map<String, Object>> balanceByCustomer();
}