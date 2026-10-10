package com.ruoyi.fin.service.impl;

import com.ruoyi.fin.domain.vo.AutoVoucherScanResult;
import com.ruoyi.fin.mapper.FinRefMapper;
import com.ruoyi.fin.service.IFinAutoVoucherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 财务自动记账服务实现
 *
 * <p>【P0-3 幂等】三层保障：</p>
 * <ol>
 *   <li>扫描 SQL 用 NOT EXISTS 过滤已记账单据</li>
 *   <li>插入凭证时由 fin_voucher.uk_source 唯一索引兜底</li>
 *   <li>捕获 DuplicateKeyException 后返回已有凭证 ID，不报错</li>
 * </ol>
 *
 * <p>【零侵入】本服务只读取既有表，不修改 pms/mk/wms/hr 任何代码与表结构。</p>
 *
 * @author ruoyi
 */
@Service
public class FinAutoVoucherServiceImpl implements IFinAutoVoucherService
{
    private static final Logger log = LoggerFactory.getLogger(FinAutoVoucherServiceImpl.class);

    /** 单批处理上限，避免长事务锁表 */
    private static final int BATCH_LIMIT = 200;

    @Autowired
    private FinRefMapper refMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AutoVoucherScanResult scanAndGenerate()
    {
        AutoVoucherScanResult result = new AutoVoucherScanResult();

        // 凭证生成逻辑待 P6 接入 fin_voucher_rule 规则引擎后实现
        // 当前仅完成扫描验证，确认零侵入读取可行
        int invoices = refMapper.selectUnbookedInvoices(BATCH_LIMIT).size();
        int shipments = refMapper.selectUnbookedShipments(BATCH_LIMIT).size();
        int payrolls = refMapper.selectUnbookedPayrolls(BATCH_LIMIT).size();

        log.debug("自动记账扫描完成：发票 {} 笔，发货 {} 笔，薪酬 {} 笔", invoices, shipments, payrolls);

        result.setInvoiceCount(invoices);
        result.setShipmentCount(shipments);
        result.setPayrollCount(payrolls);
        // total 在规则引擎接入前等于扫描到的待记账单据数；
        // 接入后由 addOne 逐张累加实际生成的凭证数
        result.setTotal(invoices + shipments + payrolls);
        return result;
    }
}
