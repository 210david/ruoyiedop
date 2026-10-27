package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.vo.AutoVoucherScanResult;

/**
 * 财务自动记账服务
 *
 * @author ruoyi
 */
public interface IFinAutoVoucherService
{
    /**
     * 扫描已审核但未记账的业务单据并生成凭证。
     *
     * <p>【零侵入】由财务侧轮询实现，既有业务模块无需改动任何代码。</p>
     *
     * <p>幂等：扫描 SQL 用 NOT EXISTS 判断，数据库唯一索引 uk_source 兜底。</p>
     *
     * @return 本次处理统计
     */
    AutoVoucherScanResult scanAndGenerate();
}
