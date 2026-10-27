package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinCostCarryForward;
import com.ruoyi.fin.domain.FinInventoryCost;
import com.ruoyi.fin.domain.vo.InboundCostRefVO;
import com.ruoyi.fin.domain.vo.OutboundCostRefVO;
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
        StringBuilder skipDetail = new StringBuilder();
        String user = SecurityUtils.getUsername();

        for (InboundCostRefVO in : list)
        {
            BigDecimal qty = nz(in.getReceivedQty());
            BigDecimal price = in.getUnitPrice();

            // 无价明细：跳过，不中断整批
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            {
                skip++;
                if (skipDetail.length() < 200)
                {
                    skipDetail.append(in.getInboundOrderNo()).append('/')
                            .append(in.getMaterialCode()).append(' ');
                }
                continue;
            }

            try
            {
                // Q4：含税单价除税得不含税成本
                BigDecimal unitCostExcl = costEngine.toExclTax(in.getMaterialCode(), null, price);
                String batch = nz(in.getBatchNo());
                long locationId = in.getLocationId() == null ? 0L : in.getLocationId();

                FinCostEngine.CostResult result =
                        inboundOne(in, qty, unitCostExcl, batch, locationId, user);
                log.debug("入库计价成功：{} {} 数量{} 新单位成本{}",
                        in.getInboundOrderNo(), in.getMaterialCode(), qty, result.getUnitCost());
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
            log.warn("入库计价跳过 {} 条无单价明细（入库单价为 0），请先在 WMS 补录单价或设置默认成本价。样例：{}",
                    skip, skipDetail.toString());
        }
        return buildResult(success, skip, fail, list.size());
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