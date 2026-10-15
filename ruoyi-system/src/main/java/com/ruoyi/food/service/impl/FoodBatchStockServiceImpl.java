package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchStock;
import com.ruoyi.food.domain.FoodExpiryPolicy;
import com.ruoyi.food.domain.FoodFefoPlan;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.service.IFoodBatchStockService;
import com.ruoyi.food.service.IFoodExpiryPolicyService;

/**
 * 食品批次库存Service实现
 *
 * 与 wms_inventory 双写同步 + FEFO三重拦截分配
 *
 * @author ruoyi
 */
@Service
public class FoodBatchStockServiceImpl implements IFoodBatchStockService
{
    private static final Logger log = LoggerFactory.getLogger(FoodBatchStockServiceImpl.class);

    @Autowired
    private FoodBatchMapper batchMapper;

    @Autowired
    private IFoodExpiryPolicyService policyService;

    // ---------------- 双写同步 ----------------

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long syncAdd(Long materialId, String batchNo, Long warehouseId, Long locationId,
                        BigDecimal qty, String operator, Date productionDate, Date expiryDate)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return null;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, operator, productionDate, expiryDate);
        if (batch == null)
        {
            // 非效期物料或无批次号：跳过批次同步
            return null;
        }
        upsertStock(batch.getBatchId(), materialId, warehouseId, locationId, qty, BigDecimal.ZERO, operator, true);
        refreshBatchQty(batch.getBatchId(), operator);
        return batch.getBatchId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDeduct(Long materialId, String batchNo, Long warehouseId, Long locationId,
                           BigDecimal qty, String operator)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, null, null, null);
        if (batch == null)
        {
            return;
        }
        FoodBatchStock stock = getOrThrowStock(batch.getBatchId(), warehouseId, locationId);
        BigDecimal available = stock.getQty().subtract(stock.getQtyLocked());
        if (available.compareTo(qty) < 0)
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】可用库存不足，可用：" + available
                    + "，需扣减：" + qty);
        }
        stock.setQty(stock.getQty().subtract(qty));
        updateStockWithRetry(stock, operator);
        refreshBatchQty(batch.getBatchId(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncLock(Long materialId, String batchNo, Long warehouseId, Long locationId,
                         BigDecimal qty, String operator)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, null, null, null);
        if (batch == null)
        {
            return;
        }
        FoodBatchStock stock = getOrThrowStock(batch.getBatchId(), warehouseId, locationId);
        BigDecimal available = stock.getQty().subtract(stock.getQtyLocked());
        if (available.compareTo(qty) < 0)
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】可用库存不足，无法锁定");
        }
        stock.setQty(stock.getQty().subtract(qty));
        stock.setQtyLocked(stock.getQtyLocked().add(qty));
        updateStockWithRetry(stock, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncUnlock(Long materialId, String batchNo, Long warehouseId, Long locationId,
                           BigDecimal qty, String operator)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, null, null, null);
        if (batch == null)
        {
            return;
        }
        FoodBatchStock stock = getOrThrowStock(batch.getBatchId(), warehouseId, locationId);
        BigDecimal unlock = stock.getQtyLocked().min(qty);
        stock.setQtyLocked(stock.getQtyLocked().subtract(unlock));
        stock.setQty(stock.getQty().add(unlock));
        updateStockWithRetry(stock, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncDeductLocked(Long materialId, String batchNo, Long warehouseId, Long locationId,
                                 BigDecimal qty, String operator)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, null, null, null);
        if (batch == null)
        {
            return;
        }
        FoodBatchStock stock = getOrThrowStock(batch.getBatchId(), warehouseId, locationId);
        // 与 wms releaseLockedInventory 一致：锁定量不足时按实际锁定量释放
        BigDecimal deduct = stock.getQtyLocked().min(qty);
        stock.setQtyLocked(stock.getQtyLocked().subtract(deduct));
        updateStockWithRetry(stock, operator);
        refreshBatchQty(batch.getBatchId(), operator);
    }

    // ---------------- FEFO 分配 ----------------

    @Override
    public FoodFefoPlan planFefo(Long materialId, Long warehouseId, BigDecimal qty)
    {
        FoodFefoPlan plan = new FoodFefoPlan();
        plan.setRequestedQty(qty);
        Map<String, Object> material = batchMapper.selectMaterialInfo(materialId);
        if (material == null)
        {
            throw new ServiceException("物料不存在或已删除");
        }
        String isExpiryManage = material.get("is_expiry_manage") == null ? "0" : String.valueOf(material.get("is_expiry_manage"));
        if (!"1".equals(isExpiryManage))
        {
            plan.setSuccess(true);
            plan.getSkipped().add("物料未启用效期管理，按普通物料出库");
            return plan;
        }
        FoodExpiryPolicy policy = policyService.matchPolicy(materialId,
                material.get("material_type") == null ? null : String.valueOf(material.get("material_type")));
        int minShipDays = policy != null && policy.getMinShipDays() != null ? policy.getMinShipDays() : 0;
        boolean fefoEnabled = policy == null || !"0".equals(policy.getFefoEnabled());

        List<FoodBatchStock> candidates = batchMapper.selectFefoStockCandidates(materialId, warehouseId);
        LocalDate today = LocalDate.now();
        BigDecimal remaining = qty;
        BigDecimal allocatable = BigDecimal.ZERO;
        for (FoodBatchStock c : candidates)
        {
            long daysRemain = c.getExpireDate() == null ? Long.MAX_VALUE
                    : java.time.temporal.ChronoUnit.DAYS.between(today,
                        c.getExpireDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            c.setDaysRemain(daysRemain == Long.MAX_VALUE ? null : daysRemain);
            // 拦截一：仅合格批次可发（待检/冻结/报废禁发）
            if (!"1".equals(c.getBatchStatus()))
            {
                plan.getSkipped().add("批次【" + c.getBatchCode() + "】" + statusText(c.getBatchStatus()) + "，禁止发运");
                continue;
            }
            // 拦截一补充：过期批次禁发
            if (c.getExpireDate() != null && daysRemain < 0)
            {
                plan.getSkipped().add("批次【" + c.getBatchCode() + "】已过期（到期日 " + fmtDate(c.getExpireDate()) + "），禁止发运");
                continue;
            }
            // 拦截二：最小剩余效期不足禁发
            if (c.getExpireDate() != null && minShipDays > 0 && daysRemain < minShipDays)
            {
                plan.getSkipped().add("批次【" + c.getBatchCode() + "】剩余效期 " + daysRemain
                        + " 天，低于最小可发运剩余天数 " + minShipDays + " 天");
                continue;
            }
            allocatable = allocatable.add(c.getQty().subtract(c.getQtyLocked()));
            if (remaining.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal take = c.getQty().subtract(c.getQtyLocked()).min(remaining);
                if (take.compareTo(BigDecimal.ZERO) > 0)
                {
                    FoodBatchStock alloc = new FoodBatchStock();
                    alloc.setBatchId(c.getBatchId());
                    alloc.setBatchCode(c.getBatchCode());
                    alloc.setMaterialId(c.getMaterialId());
                    alloc.setWarehouseId(c.getWarehouseId());
                    alloc.setWarehouseName(c.getWarehouseName());
                    alloc.setLocationId(c.getLocationId());
                    alloc.setLocationName(c.getLocationName());
                    alloc.setQty(take);
                    alloc.setExpireDate(c.getExpireDate());
                    alloc.setDaysRemain(c.getDaysRemain());
                    alloc.setBatchStatus(c.getBatchStatus());
                    plan.getAllocations().add(alloc);
                    remaining = remaining.subtract(take);
                }
            }
        }
        plan.setAvailableQty(allocatable);
        plan.setSuccess(remaining.compareTo(BigDecimal.ZERO) <= 0);
        if (!plan.isSuccess())
        {
            // 拦截三：可分配量不足
            plan.getSkipped().add(0, "可发批次量合计 " + allocatable.stripTrailingZeros().toPlainString()
                    + "，需求 " + qty.stripTrailingZeros().toPlainString() + "，不足");
        }
        return plan;
    }

    @Override
    public Long validateBatchShippable(Long materialId, String batchCode)
    {
        if (StringUtils.isEmpty(batchCode))
        {
            throw new ServiceException("批次号不能为空");
        }
        FoodBatch batch = batchMapper.selectBatchByCode(batchCode);
        if (batch == null || !materialId.equals(batch.getMaterialId()))
        {
            throw new ServiceException("批次【" + batchCode + "】不存在或不属于当前物料");
        }
        Map<String, Object> material = batchMapper.selectMaterialInfo(materialId);
        FoodExpiryPolicy policy = policyService.matchPolicy(materialId,
                material == null || material.get("material_type") == null ? null : String.valueOf(material.get("material_type")));
        int minShipDays = policy != null && policy.getMinShipDays() != null ? policy.getMinShipDays() : 0;
        if (!"1".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("批次【" + batchCode + "】" + statusText(batch.getBatchStatus()) + "，禁止发运（三重拦截）");
        }
        if (batch.getExpireDate() != null)
        {
            long daysRemain = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(),
                    batch.getExpireDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            if (daysRemain < 0)
            {
                throw new ServiceException("批次【" + batchCode + "】已过期，禁止发运（三重拦截）");
            }
            if (minShipDays > 0 && daysRemain < minShipDays)
            {
                throw new ServiceException("批次【" + batchCode + "】剩余效期 " + daysRemain
                        + " 天，低于最小可发运剩余天数 " + minShipDays + " 天，禁止发运（三重拦截）");
            }
        }
        return batch.getBatchId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordAllocLog(Long materialId, String batchCode, String operateType, String reason,
                               String refBillType, Long refBillId, String operator)
    {
        FoodBatch batch = batchMapper.selectBatchByCode(batchCode);
        if (batch == null)
        {
            return;
        }
        com.ruoyi.food.domain.FoodBatchStatusLog log = new com.ruoyi.food.domain.FoodBatchStatusLog();
        log.setBatchId(batch.getBatchId());
        log.setBatchCode(batch.getBatchCode());
        log.setOperateType(operateType);
        log.setReason(reason);
        log.setRefBillType(refBillType);
        log.setRefBillId(refBillId);
        log.setOperateBy(operator);
        batchMapper.insertStatusLog(log);
    }

    @Override
    public List<FoodBatchStock> listShippableBatches(Long materialId, Long warehouseId)
    {
        FoodFefoPlan plan = planFefo(materialId, warehouseId, BigDecimal.ZERO);
        // qty=0 时不做分配，仅返回通过三重拦截的候选列表
        List<FoodBatchStock> result = new java.util.ArrayList<>();
        List<FoodBatchStock> candidates = batchMapper.selectFefoStockCandidates(materialId, warehouseId);
        Map<String, Object> material = batchMapper.selectMaterialInfo(materialId);
        FoodExpiryPolicy policy = policyService.matchPolicy(materialId,
                material == null || material.get("material_type") == null ? null : String.valueOf(material.get("material_type")));
        int minShipDays = policy != null && policy.getMinShipDays() != null ? policy.getMinShipDays() : 0;
        LocalDate today = LocalDate.now();
        for (FoodBatchStock c : candidates)
        {
            long daysRemain = c.getExpireDate() == null ? Long.MAX_VALUE
                    : java.time.temporal.ChronoUnit.DAYS.between(today,
                        c.getExpireDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            c.setDaysRemain(daysRemain == Long.MAX_VALUE ? null : daysRemain);
            if (!"1".equals(c.getBatchStatus()))
            {
                continue;
            }
            if (c.getExpireDate() != null && daysRemain < 0)
            {
                continue;
            }
            if (c.getExpireDate() != null && minShipDays > 0 && daysRemain < minShipDays)
            {
                continue;
            }
            result.add(c);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductAcrossWarehouse(Long materialId, String batchNo, BigDecimal qty, String operator)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return;
        }
        // 非效期管理物料不参与批次库存体系
        Map<String, Object> material = batchMapper.selectMaterialInfo(materialId);
        if (material == null || !"1".equals(String.valueOf(material.get("is_expiry_manage"))))
        {
            return;
        }
        FoodBatch batch = batchMapper.selectBatchByCode(batchNo);
        if (batch == null)
        {
            throw new ServiceException("批次【" + batchNo + "】未登记，请先完成批次入库");
        }
        if (!materialId.equals(batch.getMaterialId()))
        {
            throw new ServiceException("批次【" + batchNo + "】归属物料与当前物料不一致，请核对");
        }
        // 领用拦截：仅合格批次可领（待检/冻结/报废禁止投料）
        if (!"1".equals(batch.getBatchStatus()))
        {
            throw new ServiceException("批次【" + batchNo + "】" + statusText(batch.getBatchStatus()) + "，禁止领用");
        }
        FoodBatchStock query = new FoodBatchStock();
        query.setBatchId(batch.getBatchId());
        List<FoodBatchStock> stocks = batchMapper.selectBatchStockList(query);
        BigDecimal available = BigDecimal.ZERO;
        for (FoodBatchStock s : stocks)
        {
            available = available.add(s.getQty().subtract(s.getQtyLocked()));
        }
        if (available.compareTo(qty) < 0)
        {
            throw new ServiceException("批次【" + batchNo + "】可用库存不足，可用：" + available.stripTrailingZeros().toPlainString()
                    + "，需领用：" + qty.stripTrailingZeros().toPlainString());
        }
        // 按库存行可用量从大到小逐行扣
        stocks.sort((a, b) -> b.getQty().subtract(b.getQtyLocked()).compareTo(a.getQty().subtract(a.getQtyLocked())));
        BigDecimal remaining = qty;
        for (FoodBatchStock s : stocks)
        {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0)
            {
                break;
            }
            BigDecimal lineAvail = s.getQty().subtract(s.getQtyLocked());
            if (lineAvail.compareTo(BigDecimal.ZERO) <= 0)
            {
                continue;
            }
            BigDecimal take = lineAvail.min(remaining);
            s.setQty(s.getQty().subtract(take));
            updateStockWithRetry(s, operator);
            remaining = remaining.subtract(take);
        }
        refreshBatchQty(batch.getBatchId(), operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long addAcrossWarehouse(Long materialId, String batchNo, BigDecimal qty, String operator,
                                   Long warehouseId, Long locationId, Date productionDate, Date expiryDate)
    {
        if (qty == null || qty.compareTo(BigDecimal.ZERO) == 0)
        {
            return null;
        }
        FoodBatch batch = locateBatch(materialId, batchNo, operator, productionDate, expiryDate);
        if (batch == null)
        {
            return null;
        }
        // 仓库定位：优先调用方指定；否则复用批次现有库存行仓库；最后取系统默认仓库（与现有数据惯例 location=warehouse）
        Long whId = warehouseId;
        Long locId = locationId;
        if (whId == null)
        {
            FoodBatchStock query = new FoodBatchStock();
            query.setBatchId(batch.getBatchId());
            List<FoodBatchStock> stocks = batchMapper.selectBatchStockList(query);
            if (!stocks.isEmpty())
            {
                whId = stocks.get(0).getWarehouseId();
                locId = stocks.get(0).getLocationId();
            }
            else
            {
                Map<String, Object> defWh = batchMapper.selectDefaultWarehouse();
                if (defWh == null)
                {
                    throw new ServiceException("系统未配置可用仓库，无法登记批次库存，请先维护仓库");
                }
                whId = ((Number) defWh.get("warehouse_id")).longValue();
                locId = whId;
            }
        }
        upsertStock(batch.getBatchId(), materialId, whId, locId, qty, BigDecimal.ZERO, operator, true);
        refreshBatchQty(batch.getBatchId(), operator);
        return batch.getBatchId();
    }

    @Override
    public List<FoodBatchStock> listBatchStocksByMaterial(Long materialId)
    {
        return batchMapper.selectBatchStocksWithBatch(materialId);
    }

    // ---------------- 私有方法 ----------------

    /**
     * 按批次号定位批次；效期管理物料且批次不存在时自动创建初始化批次（存量/盘盈兜底）
     */
    private FoodBatch locateBatch(Long materialId, String batchNo, String operator, Date productionDate, Date expiryDate)
    {
        if (StringUtils.isEmpty(batchNo))
        {
            return null;
        }
        Map<String, Object> material = batchMapper.selectMaterialInfo(materialId);
        if (material == null)
        {
            return null;
        }
        String isExpiryManage = material.get("is_expiry_manage") == null ? "0" : String.valueOf(material.get("is_expiry_manage"));
        if (!"1".equals(isExpiryManage))
        {
            return null;
        }
        FoodBatch batch = batchMapper.selectBatchByCode(batchNo);
        if (batch != null && !materialId.equals(batch.getMaterialId()))
        {
            throw new ServiceException("批次【" + batchNo + "】归属物料与当前物料不一致，请核对");
        }
        if (batch != null)
        {
            return batch;
        }
        // 自动创建初始化批次（盘点盘盈/期初/其他链路带入的未登记批次）
        if (operator == null)
        {
            log.warn("批次【{}】未登记且非人工操作链路，跳过批次库存同步", batchNo);
            return null;
        }
        batch = new FoodBatch();
        batch.setBatchCode(batchNo);
        batch.setMaterialId(materialId);
        batch.setMaterialCode((String) material.get("material_code"));
        batch.setMaterialName((String) material.get("material_name"));
        batch.setSourceType("6");
        batch.setSourceBillType("auto_init");
        batch.setProduceDate(productionDate);
        batch.setReceiveDate(DateUtils.getNowDate());
        Integer shelfLifeDays = material.get("shelf_life_days") == null ? null : ((Number) material.get("shelf_life_days")).intValue();
        batch.setShelfLifeDays(shelfLifeDays);
        batch.setExpireDate(expiryDate);
        batch.setQtyUnit((String) material.get("unit"));
        batch.setBatchStatus("0");
        batch.setCreateBy(operator);
        batchMapper.insertBatch(batch);
        batchMapper.refreshBatchExpireStatus(batch.getBatchId());
        log.info("自动创建初始化批次：{}（物料 {}）", batchNo, material.get("material_code"));
        return batch;
    }

    private FoodBatchStock getOrThrowStock(Long batchId, Long warehouseId, Long locationId)
    {
        FoodBatchStock stock = batchMapper.selectBatchStock(batchId, warehouseId, locationId);
        if (stock == null)
        {
            throw new ServiceException("批次库存行不存在（批次" + batchId + "），请先完成入库上架");
        }
        return stock;
    }

    private void upsertStock(Long batchId, Long materialId, Long warehouseId, Long locationId,
                             BigDecimal qtyAdd, BigDecimal lockAdd, String operator, boolean addQty)
    {
        FoodBatchStock stock = batchMapper.selectBatchStock(batchId, warehouseId, locationId);
        if (stock == null)
        {
            FoodBatchStock ns = new FoodBatchStock();
            ns.setBatchId(batchId);
            ns.setMaterialId(materialId);
            ns.setWarehouseId(warehouseId);
            ns.setLocationId(locationId);
            ns.setQty(addQty ? qtyAdd : BigDecimal.ZERO);
            ns.setQtyLocked(lockAdd);
            ns.setCreateBy(operator);
            batchMapper.insertBatchStock(ns);
            return;
        }
        if (addQty)
        {
            stock.setQty(stock.getQty().add(qtyAdd));
        }
        stock.setQtyLocked(stock.getQtyLocked().add(lockAdd));
        updateStockWithRetry(stock, operator);
    }

    /**
     * 乐观锁更新（并发冲突重试3次）
     */
    private void updateStockWithRetry(FoodBatchStock stock, String operator)
    {
        stock.setUpdateBy(operator);
        for (int i = 0; i < 3; i++)
        {
            if (batchMapper.updateBatchStockQty(stock) == 1)
            {
                return;
            }
            FoodBatchStock fresh = batchMapper.selectBatchStock(stock.getBatchId(), stock.getWarehouseId(), stock.getLocationId());
            if (fresh == null)
            {
                throw new ServiceException("批次库存行不存在，同步失败");
            }
            fresh.setQty(stock.getQty());
            fresh.setQtyLocked(stock.getQtyLocked());
            fresh.setUpdateBy(operator);
            stock = fresh;
        }
        throw new ServiceException("批次库存并发冲突，请重试");
    }

    /**
     * 批次总量 = 明细库存行合计
     */
    private void refreshBatchQty(Long batchId, String operator)
    {
        FoodBatchStock q = new FoodBatchStock();
        q.setBatchId(batchId);
        BigDecimal total = BigDecimal.ZERO;
        for (FoodBatchStock s : batchMapper.selectBatchStockList(q))
        {
            total = total.add(s.getQty() == null ? BigDecimal.ZERO : s.getQty());
        }
        batchMapper.updateBatchQty(batchId, total);
    }

    private String statusText(String status)
    {
        switch (status == null ? "" : status)
        {
            case "0": return "待检";
            case "1": return "合格";
            case "2": return "冻结";
            case "3": return "报废";
            default: return "状态" + status;
        }
    }

    private String fmtDate(Date date)
    {
        return DateUtils.parseDateToStr("yyyy-MM-dd", date);
    }
}
