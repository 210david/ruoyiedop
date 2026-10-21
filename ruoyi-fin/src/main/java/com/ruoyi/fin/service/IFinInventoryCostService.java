package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinInventoryCost;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 存货成本 Service 接口
 *
 * @author ruoyi
 */
public interface IFinInventoryCostService
{
    /**
     * 查询成本台账列表
     *
     * @param cost 查询条件
     * @return 成本台账
     */
    List<FinInventoryCost> selectCostList(FinInventoryCost cost);

    /**
     * 入库计价（扫描已完成入库单）
     *
     * <p>移动加权平均法：先按含税单价除税得不含税成本，
     * 再与原结存加权平均。</p>
     *
     * <p><b>无价明细策略</b>：实测生产库已完成入库的明细中存在大量
     * unit_price=0 的记录。这类明细<b>跳过并计入警告数</b>，
     * 不中断整批处理；全部无价时返回失败数并给出明确提示。</p>
     *
     * @return 处理结果，含 success/skip/fail
     */
    Map<String, Object> priceInbound();

    /**
     * 出库成本结转（扫描已完成出库单）
     *
     * <p>出库成本 = 出库数量 × 当前不含税单位成本。</p>
     *
     * @return 处理结果
     */
    Map<String, Object> carryOutbound();

    /**
     * 期末存货计价汇总（资产负债表取数）
     *
     * @param period 会计期间
     * @return 汇总列表
     */
    List<Map<String, Object>> valuation(String period);

    /**
     * 成本台账总成本
     *
     * @return 总成本
     */
    BigDecimal totalCost();

    /**
     * 手动设置某物料的期初成本（上线期初数录入）
     *
     * @param cost 成本记录
     * @return 影响行数
     */
    int initCost(FinInventoryCost cost);
}