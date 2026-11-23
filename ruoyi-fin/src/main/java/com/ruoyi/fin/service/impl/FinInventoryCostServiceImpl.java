package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinCostCarryForward;
import com.ruoyi.fin.domain.FinInventoryCost;
import com.ruoyi.fin.domain.vo.InboundCostRefVO;
import com.ruoyi.fin.domain.vo.OutboundCostRefVO;
import com.ruoyi.fin.domain.vo.StockTakeDiffRefVO;
import com.ruoyi.fin.engine.FinCostEngine;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.mapper.FinCostCarryForwardMapper;
import com.ruoyi.fin.mapper.FinInventoryCostMapper;
import com.ruoyi.fin.mapper.FinRefMapper;
import com.ruoyi.fin.service.IFinInventoryCostService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存货成本 Service 实现（移动加权平均法）
 *
 * <p>【关键设计 · 无价明细的工程处理】</p>
 * 实测生产库已完成入库的 25 条明细中，仅 9 条有单价，16 条为 0、1 条为 NULL。
 * 这是真实的业务数据质量现状，不是异常。因此计价策略是：
 * <ul>
 *   <li>有单价的明细 → 正常计价</li>
 *   <li>无单价的明细 → <b>跳过</b>，计入 skip 数并记 warn 日志</li>
 *   <li>全部跳过时返回提示，让管理员先补录单价</li>
 * </ul>
 * 若因单条无价而整批回滚，会导致财务完全无法开工，不符合实际。
 *
 * @author ruoyi
 */
@Service
public class FinInventoryCostServiceImpl implements IFinInventoryCostService
{
    private static final Logger log = LoggerFactory.getLogger(FinInventoryCostServiceImpl.class);

    /** 单批处理上限 */
    private static final int BATCH_LIMIT = 200;

    /** 乐观锁冲突重试次数 */
    private static final int MAX_RETRY = 3;

    @Autowired
    private FinInventoryCostMapper costMapper;

    @Autowired
    private FinCostCarryForwardMapper carryMapper;

    @Autowired
    private FinRefMapper refMapper;

    @Autowired
    private FinCostEngine costEngine;

    @Autowired
    private FinNumberGenerator numberGenerator;

    @Override
    public List<FinInventoryCost> selectCostList(FinInventoryCost cost)
    {
        return costMapper.selectCostList(cost);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> priceInbound()
    {
        List<InboundCostRefVO> list = refMapper.selectUnpricedInbound(BATCH_LIMIT);
        int success = 0;
        int skip = 0;
        int fail = 0;
        int fallback = 0;
        StringBuilder skipDetail = new StringBuilder();
        StringBuilder fallbackDetail = new StringBuilder();
        String user = SecurityUtils.getUsername();

        for (InboundCostRefVO in : list)
        {
            BigDecimal qty = nz(in.getReceivedQty());
            BigDecimal price = in.getUnitPrice();
            boolean usedFallback = false;

            // 无价明细：回退到物料「计价参考成本价」兜底；兜底价也没有才跳过，不中断整批
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            {
                BigDecimal costPrice = in.getCostPrice();
                if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) <= 0)
                {
                    skip++;
                    if (skipDetail.length() < 200)
                    {
                        skipDetail.append(in.getInboundOrderNo()).append('/')
                                .append(in.getMaterialCode()).append(' ');
                    }
                    continue;
                }
                price = costPrice;
                usedFallback = true;
                fallback++;
                if (fallbackDetail.length() < 200)
                {
                    fallbackDetail.append(in.getInboundOrderNo()).append('/')
                            .append(in.getMaterialCode()).append(' ');
                }
            }

            try
            {
                // Q4：入库单价为含税价，需除税得不含税成本；
                // 兜底价 cost_price 本身即「不含税」，不能再除一次税，否则会被低估。
                BigDecimal unitCostExcl = usedFallback
                        ? price.setScale(FinCostEngine.UNIT_COST_SCALE, java.math.RoundingMode.HALF_UP)
                        : costEngine.toExclTax(in.getMaterialCode(), null, price);
                String batch = nz(in.getBatchNo());
                long locationId = in.getLocationId() == null ? 0L : in.getLocationId();

                FinCostEngine.CostResult result =
                        inboundOne(in, qty, unitCostExcl, batch, locationId, user);
                log.debug("入库计价成功：{} {} 数量{} 新单位成本{}{}",
                        in.getInboundOrderNo(), in.getMaterialCode(), qty, result.getUnitCost(),
                        usedFallback ? "（兜底价）" : "");
                success++;
            }
            catch (Exception e)
            {
                fail++;
                log.warn("入库计价失败：{} {} —— {}",
                        in.getInboundOrderNo(), in.getMaterialCode(), e.getMessage());
            }
        }

        if (skip > 0)
        {
            log.warn("入库计价跳过 {} 条无单价明细（入库单价与物料参考成本价均为 0），"
                    + "请在 WMS 补录入库单价或维护物料「计价参考成本价」。样例：{}",
                    skip, skipDetail.toString());
        }
        if (fallback > 0)
        {
            log.warn("入库计价命中兜底价 {} 条（入库单价为空，按 wms_material.cost_price 取价），"
                    + "建议尽快补录实际入库单价以保证成本准确。样例：{}",
                    fallback, fallbackDetail.toString());
        }
        Map<String, Object> result = buildResult(success, skip, fail, list.size());
        // 兜底命中数单独返回，供前端提示「其中 N 条使用了参考成本价」
        result.put("fallback", fallback);
        return result;
    }

    /**
     * 单条入库计价（加行锁 + 乐观锁双保险）。
     */
    private FinCostEngine.CostResult inboundOne(InboundCostRefVO in, BigDecimal qty,
                                                BigDecimal unitCostExcl, String batch,
                                                long locationId, String user)
    {
        for (int retry = 0; retry < MAX_RETRY; retry++)
        {
            FinInventoryCost exist = costMapper.selectByDimForUpdate(
                    in.getMaterialId(), in.getWarehouseId(), locationId, batch);

            FinCostEngine.CostResult result;
            if (exist == null)
            {
                // 新建台账：首次入库，成本即入库成本
                result = costEngine.calcInboundWeighted(
                        BigDecimal.ZERO, BigDecimal.ZERO, qty, unitCostExcl);

                FinInventoryCost row = new FinInventoryCost();
                row.setMaterialId(in.getMaterialId());
                row.setMaterialCode(in.getMaterialCode());
                row.setMaterialName(in.getMaterialName());
                row.setWarehouseId(in.getWarehouseId());
                row.setLocationId(locationId);
                row.setBatchNo(batch);
                row.setQty(result.getQty());
                row.setUnitCost(result.getUnitCost());
                row.setTotalCost(result.getTotalCost());
                row.setCostMethod(FinCostEngine.WEIGHTED_AVG);
                row.setSourceModule("wms");
                row.setSourceType(FinCostCarryForward.TYPE_INBOUND);
                row.setSourceId(in.getDetailId());
                row.setInboundOrderId(in.getInboundOrderId());
                row.setLastInDate(in.getInboundDate());
                row.setCreateBy(user);
                costMapper.insertCost(row);
            }
            else
            {
                // 已有台账：加权平均
                result = costEngine.calcInboundWeighted(
                        exist.getQty(), exist.getTotalCost(), qty, unitCostExcl);

                exist.setQty(result.getQty());
                exist.setUnitCost(result.getUnitCost());
                exist.setTotalCost(result.getTotalCost());
                exist.setCostMethod(FinCostEngine.WEIGHTED_AVG);
                exist.setInboundOrderId(in.getInboundOrderId());
                exist.setLastInDate(in.getInboundDate());
                exist.setUpdateBy(user);
                int rows = costMapper.updateCost(exist);
                if (rows == 0)
                {
                    // 乐观锁冲突，重试
                    log.debug("成本台账乐观锁冲突，重试第 {} 次", retry + 1);
                    continue;
                }
            }

            // 记结转流水（入库方向：增加存货）
            FinCostCarryForward cf = new FinCostCarryForward();
            cf.setCfNo(numberGenerator.generate(FinNumberGenerator.RULE_COST_CF));
            cf.setCostDate(in.getInboundDate());
            cf.setPeriod(periodOf(in.getInboundDate()));
            cf.setSourceModule("wms");
            cf.setSourceType(FinCostCarryForward.TYPE_INBOUND);
            cf.setSourceId(in.getDetailId());
            cf.setMaterialId(in.getMaterialId());
            cf.setMaterialCode(in.getMaterialCode());
            cf.setQty(qty);
            cf.setUnitCost(unitCostExcl);
            cf.setTotalCost(result.getAmount());
            cf.setTargetType(FinCostCarryForward.TARGET_SALES_COST);
            cf.setTargetKey(in.getInboundOrderNo());
            cf.setCreateBy(user);
            cf.setRemark("入库计价 " + in.getInboundOrderNo());
            carryMapper.insertCarry(cf);

            return result;
        }
        throw new IllegalStateException("成本台账并发冲突重试 " + MAX_RETRY + " 次仍未成功");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> carryOutbound()
    {
        List<OutboundCostRefVO> list = refMapper.selectUncarriedOutbound(BATCH_LIMIT);
        int success = 0;
        int fail = 0;
        String user = SecurityUtils.getUsername();

        for (OutboundCostRefVO out : list)
        {
            try
            {
                carryOutboundOne(out, user);
                success++;
            }
            catch (Exception e)
            {
                fail++;
                log.warn("出库成本结转失败：{} {} —— {}",
                        out.getOutboundOrderNo(), out.getMaterialCode(), e.getMessage());
            }
        }
        return buildResult(success, 0, fail, list.size());
    }

    /**
     * 单条出库结转。
     */
    private void carryOutboundOne(OutboundCostRefVO out, String user)
    {
        String batch = nz(out.getBatchNo());
        long locationId = out.getLocationId() == null ? 0L : out.getLocationId();
        BigDecimal outQty = nz(out.getActualQty());

        FinInventoryCost exist = costMapper.selectByDimForUpdate(
                out.getMaterialId(), out.getWarehouseId(), locationId, batch);
        if (exist == null)
        {
            // 该维度未计价：跨批次/跨库位兜底，用物料+仓库加权平均
            Map<String, Object> sum = costMapper.sumByMaterial(
                    out.getMaterialId(), out.getWarehouseId());
            if (sum == null || sum.get("avgCost") == null
                    || new BigDecimal(sum.get("avgCost").toString()).compareTo(BigDecimal.ZERO) == 0)
            {
                throw new IllegalStateException(
                        "物料「" + out.getMaterialCode() + "」无成本记录，请先办理入库计价");
            }
            exist = new FinInventoryCost();
            exist.setMaterialId(out.getMaterialId());
            exist.setMaterialCode(out.getMaterialCode());
            exist.setWarehouseId(out.getWarehouseId());
            exist.setLocationId(locationId);
            exist.setBatchNo(batch);
            exist.setQty(new BigDecimal(sum.get("totalQty").toString()));
            exist.setUnitCost(new BigDecimal(sum.get("avgCost").toString()));
            exist.setTotalCost(new BigDecimal(sum.get("totalCost").toString()));
        }

        FinCostEngine.CostResult result =
                costEngine.calcOutboundWeighted(exist.getQty(), exist.getUnitCost(), outQty);

        exist.setQty(result.getQty());
        exist.setTotalCost(result.getTotalCost());
        exist.setUpdateBy(user);
        if (exist.getCostId() != null)
        {
            costMapper.updateCost(exist);
        }

        FinCostCarryForward cf = new FinCostCarryForward();
        cf.setCfNo(numberGenerator.generate(FinNumberGenerator.RULE_COST_CF));
        cf.setCostDate(out.getOutboundDate());
        cf.setPeriod(periodOf(out.getOutboundDate()));
        cf.setSourceModule("wms");
        cf.setSourceType(FinCostCarryForward.TYPE_OUTBOUND);
        cf.setSourceId(out.getDetailId());
        cf.setMaterialId(out.getMaterialId());
        cf.setMaterialCode(out.getMaterialCode());
        cf.setQty(outQty);
        cf.setUnitCost(result.getUnitCost());
        cf.setTotalCost(result.getAmount());
        cf.setTargetType(isSalesOutbound(out.getOrderType())
                ? FinCostCarryForward.TARGET_SALES_COST
                : FinCostCarryForward.TARGET_PROD_COST);
        cf.setTargetKey(out.getOutboundOrderNo());
        cf.setCreateBy(user);
        cf.setRemark("出库成本结转 " + out.getOutboundOrderNo());
        carryMapper.insertCarry(cf);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> adjustStockTakeDiff()
    {
        List<StockTakeDiffRefVO> list = refMapper.selectUnadjustedStockTakeDiff(BATCH_LIMIT);
        int success = 0;
        int skip = 0;
        int fail = 0;
        int gain = 0;
        int loss = 0;
        StringBuilder skipDetail = new StringBuilder();
        String user = SecurityUtils.getUsername();

        for (StockTakeDiffRefVO d : list)
        {
            BigDecimal diff = nz(d.getDiffQty());
            if (diff.compareTo(BigDecimal.ZERO) == 0)
            {
                skip++;
                continue;
            }
            String batch = nz(d.getBatchNo());
            long locationId = d.getLocationId() == null ? 0L : d.getLocationId();

            try
            {
                BigDecimal unitCost = resolveUnitCost(d, batch, locationId);
                if (diff.compareTo(BigDecimal.ZERO) > 0)
                {
                    applyGain(d, diff, unitCost, batch, locationId, user);
                    gain++;
                }
                else
                {
                    boolean applied = applyLoss(d, diff.abs(), unitCost, batch, locationId, user);
                    if (!applied)
                    {
                        skip++;
                        // 台账无结存（该维度未计价），无法冲减，交由补录单价后重跑
                        if (skipDetail.length() < 200)
                        {
                            skipDetail.append(d.getTakeNo()).append('/')
                                    .append(d.getMaterialCode()).append(' ');
                        }
                        continue;
                    }
                    loss++;
                }
                success++;
            }
            catch (Exception e)
            {
                fail++;
                log.warn("盘点成本调整失败：{} {} —— {}",
                        d.getTakeNo(), d.getMaterialCode(), e.getMessage());
            }
        }

        if (skip > 0)
        {
            log.warn("盘点成本调整跳过 {} 条（台账无该维度结存，需先完成入库计价）。样例：{}",
                    skip, skipDetail.toString());
        }
        Map<String, Object> result = buildResult(success, skip, fail, list.size());
        result.put("gain", gain);
        result.put("loss", loss);
        return result;
    }

    /**
     * 解析盘盈/盘亏使用的单位成本（不含税）。
     *
     * <p>优先级：该维度台账 unit_cost → 物料+仓库加权平均 → 入库均价除税。
     * 三级都取不到则抛错，由上层计入 fail，避免用 0 成本污染台账。</p>
     */
    private BigDecimal resolveUnitCost(StockTakeDiffRefVO d, String batch, long locationId)
    {
        FinInventoryCost exist = costMapper.selectByDimForUpdate(
                d.getMaterialId(), d.getWarehouseId(), locationId, batch);
        if (exist != null && exist.getUnitCost() != null
                && exist.getUnitCost().compareTo(BigDecimal.ZERO) > 0)
        {
            return exist.getUnitCost();
        }

        Map<String, Object> sum = costMapper.sumByMaterial(d.getMaterialId(), d.getWarehouseId());
        if (sum != null && sum.get("avgCost") != null) {
            BigDecimal avg = new BigDecimal(sum.get("avgCost").toString());
            if (avg.compareTo(BigDecimal.ZERO) > 0)
            {
                return avg;
            }
        }

        BigDecimal avgPrice = refMapper.selectMaterialAvgPrice(d.getMaterialId());
        if (avgPrice != null && avgPrice.compareTo(BigDecimal.ZERO) > 0)
        {
            // 入库均价为含税，需除税成不含税成本（与入库计价口径一致）
            return costEngine.toExclTax(d.getMaterialCode(), null, avgPrice);
        }

        throw new IllegalStateException(
                "物料「" + d.getMaterialCode() + "」无成本记录且无入库均价，请先办理入库计价");
    }

    /**
     * 盘盈：增加台账数量与金额（金额 = 差异数量 × 单位成本）。
     */
    private void applyGain(StockTakeDiffRefVO d, BigDecimal diff, BigDecimal unitCost,
                           String batch, long locationId, String user)
    {
        BigDecimal amount = diff.multiply(unitCost).setScale(2, java.math.RoundingMode.HALF_UP);
        FinInventoryCost exist = costMapper.selectByDimForUpdate(
                d.getMaterialId(), d.getWarehouseId(), locationId, batch);

        if (exist == null)
        {
            FinInventoryCost row = new FinInventoryCost();
            row.setMaterialId(d.getMaterialId());
            row.setMaterialCode(d.getMaterialCode());
            row.setMaterialName(d.getMaterialName());
            row.setWarehouseId(d.getWarehouseId());
            row.setLocationId(locationId);
            row.setBatchNo(batch);
            row.setQty(diff);
            row.setUnitCost(unitCost);
            row.setTotalCost(amount);
            row.setCostMethod(FinCostEngine.WEIGHTED_AVG);
            row.setSourceModule("wms");
            row.setSourceType(FinCostCarryForward.TYPE_STOCK_TAKE);
            row.setSourceId(d.getDetailId());
            row.setCreateBy(user);
            row.setRemark("盘盈 " + d.getTakeNo());
            costMapper.insertCost(row);
        }
        else
        {
            FinCostEngine.CostResult r = costEngine.calcInboundWeighted(
                    exist.getQty(), exist.getTotalCost(), diff, unitCost);
            exist.setQty(r.getQty());
            exist.setUnitCost(r.getUnitCost());
            exist.setTotalCost(r.getTotalCost());
            exist.setUpdateBy(user);
            exist.setRemark("盘盈 " + d.getTakeNo());
            costMapper.updateCost(exist);
        }

        writeCarry(d, diff, unitCost, amount, true, user, "盘盈 " + d.getTakeNo());
    }

    /**
     * 盘亏：按当前单位成本冲减数量与金额。
     *
     * <p>台账无可冲减结存时返回 false（由上层记为 skip），不写 0 成本流水。</p>
     */
    private boolean applyLoss(StockTakeDiffRefVO d, BigDecimal absDiff, BigDecimal unitCost,
                              String batch, long locationId, String user)
    {
        FinInventoryCost exist = costMapper.selectByDimForUpdate(
                d.getMaterialId(), d.getWarehouseId(), locationId, batch);
        if (exist == null || exist.getQty() == null
                || exist.getQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            return false;
        }

        // 数量不足时截断到实际结存，避免负结存
        BigDecimal fullQty = exist.getQty();
        boolean truncated = absDiff.compareTo(fullQty) > 0;
        BigDecimal qty = truncated ? fullQty : absDiff;
        if (truncated)
        {
            log.warn("盘点 {} 物料 {} 盘亏 {} 超过台账结存 {}，已按结存全额冲减",
                    d.getTakeNo(), d.getMaterialCode(), absDiff, fullQty);
        }

        BigDecimal unit = exist.getUnitCost() == null ? unitCost : exist.getUnitCost();
        BigDecimal amount = qty.multiply(unit).setScale(2, java.math.RoundingMode.HALF_UP);

        if (truncated)
        {
            // 【精度保护】冲减到零时直接清零，不走 calcOutboundWeighted 的逐次舍入，
            // 否则「qty × unit_cost」两次舍入会在末位残留 -0.01 之类误差（已实测复现）。
            // 全额冲减的金额必须等于结存金额，台账与流水才对得上账。
            BigDecimal bookCost = exist.getTotalCost() == null
                    ? BigDecimal.ZERO : exist.getTotalCost();
            amount = bookCost.setScale(2, java.math.RoundingMode.HALF_UP);
            exist.setQty(BigDecimal.ZERO.setScale(2));
            exist.setTotalCost(BigDecimal.ZERO.setScale(2));
            exist.setUpdateBy(user);
            exist.setRemark("盘亏 " + d.getTakeNo());
            costMapper.updateCost(exist);

            writeCarry(d, fullQty.negate(), unit, amount, false, user, "盘亏 " + d.getTakeNo());
            return true;
        }

        FinCostEngine.CostResult r = costEngine.calcOutboundWeighted(
                exist.getQty(), unit, qty);

        exist.setQty(r.getQty());
        exist.setTotalCost(r.getTotalCost());
        exist.setUpdateBy(user);
        exist.setRemark("盘亏 " + d.getTakeNo());
        costMapper.updateCost(exist);

        writeCarry(d, qty.negate(), r.getUnitCost(), amount, false, user, "盘亏 " + d.getTakeNo());
        return true;
    }

    /**
     * 写盘点成本结转流水（幂等键：source_type='stocktake' + detail_id）。
     *
     * <p>盘盈与盘亏使用不同 target_type（STOCK_GAIN / STOCK_LOSS），
     * 否则 sumByPeriod 汇总时会正负抵消，财务无法判断净影响方向。</p>
     */
    private void writeCarry(StockTakeDiffRefVO d, BigDecimal qty, BigDecimal unitCost,
                            BigDecimal amount, boolean isGain, String user, String remark)
    {
        FinCostCarryForward cf = new FinCostCarryForward();
        cf.setCfNo(numberGenerator.generate(FinNumberGenerator.RULE_COST_CF));
        cf.setCostDate(d.getCompleteTime() == null ? new java.util.Date() : d.getCompleteTime());
        cf.setPeriod(periodOf(d.getCompleteTime()));
        cf.setSourceModule("wms");
        cf.setSourceType(FinCostCarryForward.TYPE_STOCK_TAKE);
        cf.setSourceId(d.getDetailId());
        cf.setMaterialId(d.getMaterialId());
        cf.setMaterialCode(d.getMaterialCode());
        cf.setQty(qty);
        cf.setUnitCost(unitCost);
        cf.setTotalCost(amount);
        cf.setTargetType(isGain
                ? FinCostCarryForward.TARGET_STOCK_GAIN
                : FinCostCarryForward.TARGET_STOCK_LOSS);
        cf.setTargetKey(d.getTakeNo());
        cf.setCreateBy(user);
        cf.setRemark(remark);
        carryMapper.insertCarry(cf);
    }

    @Override
    public List<Map<String, Object>> valuation(String period)
    {
        return costMapper.sumValuation(period);
    }

    @Override
    public BigDecimal totalCost()
    {
        return costMapper.sumTotalCost();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initCost(FinInventoryCost cost)
    {
        String batch = nz(cost.getBatchNo());
        long locationId = cost.getLocationId() == null ? 0L : cost.getLocationId();
        FinInventoryCost exist = costMapper.selectByDimForUpdate(
                cost.getMaterialId(), cost.getWarehouseId(), locationId, batch);
        if (exist == null)
        {
            cost.setBatchNo(batch);
            cost.setLocationId(locationId);
            cost.setCostMethod(cost.getCostMethod() == null
                    ? FinCostEngine.WEIGHTED_AVG : cost.getCostMethod());
            cost.setTotalCost(nz(cost.getQty()).multiply(nz(cost.getUnitCost()))
                    .setScale(2, java.math.RoundingMode.HALF_UP));
            cost.setSourceModule("manual");
            cost.setSourceType("opening");
            cost.setCreateBy(SecurityUtils.getUsername());
            return costMapper.insertCost(cost);
        }
        exist.setQty(cost.getQty());
        exist.setUnitCost(cost.getUnitCost());
        exist.setTotalCost(nz(cost.getQty()).multiply(nz(cost.getUnitCost()))
                .setScale(2, java.math.RoundingMode.HALF_UP));
        exist.setUpdateBy(SecurityUtils.getUsername());
        return costMapper.updateCost(exist);
    }

    // ==================== 私有方法 ====================

    /** 出库类型是否属于销售（决定结转到销售成本还是生产成本） */
    private boolean isSalesOutbound(String orderType)
    {
        // wms_outbound_type 字典：0销售出库 1领料出库 2调拨出库
        return "0".equals(orderType);
    }

    private Map<String, Object> buildResult(int success, int skip, int fail, int total)
    {
        Map<String, Object> r = new HashMap<>();
        r.put("total", total);
        r.put("success", success);
        r.put("skip", skip);
        r.put("fail", fail);
        return r;
    }

    private String periodOf(Date date)
    {
        LocalDate d = date == null ? LocalDate.now()
                : date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return String.format("%04d%02d", d.getYear(), d.getMonthValue());
    }

    private static BigDecimal nz(BigDecimal v)
    {
        return v == null ? BigDecimal.ZERO : v;
    }

    private static String nz(String s)
    {
        return s == null ? "" : s;
    }
}