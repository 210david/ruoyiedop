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
     * 盘点差异成本调整（扫描已完成盘点单的盘盈/盘亏）
     *
     * <p>【解决什么问题】WMS 盘点审批只调整库存数量，不联动成本台账，
     * 导致盘亏后 fin_inventory_cost.qty 不减、total_cost 虚高，
     * 期末存货计价与总账对不上。</p>
     *
     * <p>【口径】</p>
     * <ul>
     *   <li>盘盈：按该维度当前单位成本（无台账则按物料+仓库加权平均，
     *       再退化为入库均价）增加数量与金额</li>
     *   <li>盘亏：按当前单位成本减少数量与金额；数量不足时截断到 0 并记 warn，
     *       不允许负结存</li>
     *   <li>盘盈盘亏均记入 fin_cost_carry_forward（source_type='stocktake'），
     *       保证幂等且可供总账取数</li>
     * </ul>
     *
     * @return 处理结果，含 success/skip/fail
     */
    Map<String, Object> adjustStockTakeDiff();

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