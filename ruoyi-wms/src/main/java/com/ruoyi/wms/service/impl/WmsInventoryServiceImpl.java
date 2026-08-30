package com.ruoyi.wms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsInventoryLog;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.mapper.WmsInventoryLogMapper;
import com.ruoyi.wms.mapper.WmsInventoryMapper;
import com.ruoyi.wms.mapper.WmsMaterialMapper;
import com.ruoyi.wms.service.IWmsInventoryService;

@Service
public class WmsInventoryServiceImpl implements IWmsInventoryService
{
    @Autowired
    private WmsInventoryMapper wmsInventoryMapper;

    @Autowired
    private WmsInventoryLogMapper wmsInventoryLogMapper;

    @Autowired
    private WmsMaterialMapper wmsMaterialMapper;

    @Override
    public List<WmsInventory> selectInventoryList(WmsInventory inventory)
    {
        return wmsInventoryMapper.selectInventoryList(inventory);
    }

    @Override
    public WmsInventory selectInventoryById(Long inventoryId)
    {
        return wmsInventoryMapper.selectInventoryById(inventoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addInventory(Long materialId, Long warehouseId, Long locationId, String batchNo,
                            BigDecimal qty, String changeType, String refOrderType, String refOrderNo, 
                            String createBy, Date productionDate, Date expiryDate)
    {
        WmsMaterial material = wmsMaterialMapper.selectMaterialById(materialId);
        if (material != null && "1".equals(material.getIsExpiryManage()) && productionDate != null && expiryDate == null)
        {
            if (material.getShelfLifeDays() != null && material.getShelfLifeDays() > 0)
            {
                expiryDate = new Date(productionDate.getTime() + (long) material.getShelfLifeDays() * 24 * 60 * 60 * 1000);
            }
        }

        WmsInventory inv = wmsInventoryMapper.selectInventoryByUnique(materialId, warehouseId, locationId,
                batchNo == null ? "" : batchNo);
        BigDecimal beforeQty = BigDecimal.ZERO;
        if (inv == null)
        {
            inv = new WmsInventory();
            inv.setMaterialId(materialId);
            inv.setWarehouseId(warehouseId);
            inv.setLocationId(locationId);
            inv.setBatchNo(batchNo == null ? "" : batchNo);
            inv.setProductionDate(productionDate);
            inv.setExpiryDate(expiryDate);
            inv.setQty(qty);
            inv.setLockQty(BigDecimal.ZERO);
            inv.setCreateBy(createBy);
            inv.setDelFlag("0");
            wmsInventoryMapper.insertInventory(inv);
            beforeQty = BigDecimal.ZERO;
        }
        else
        {
            inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
            if ("2".equals(inv.getDelFlag()))
            {
                inv.setQty(BigDecimal.ZERO);
            }
            beforeQty = inv.getQty();
            inv.setQty(inv.getQty().add(qty));
            if (productionDate != null) inv.setProductionDate(productionDate);
            if (expiryDate != null) inv.setExpiryDate(expiryDate);
            inv.setDelFlag("0");
            inv.setUpdateBy(createBy);
            wmsInventoryMapper.updateInventory(inv);
        }

        // log
        WmsInventoryLog log = new WmsInventoryLog();
        log.setMaterialId(materialId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(locationId);
        log.setBatchNo(batchNo);
        log.setChangeType(changeType);
        log.setChangeQty(qty);
        log.setBeforeQty(beforeQty);
        log.setAfterQty(beforeQty.add(qty));
        log.setRefOrderType(refOrderType);
        log.setRefOrderNo(refOrderNo);
        log.setCreateBy(createBy);
        wmsInventoryLogMapper.insertInventoryLog(log);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int reduceInventory(Long materialId, Long warehouseId, Long locationId, String batchNo,
                               BigDecimal qty, String changeType, String refOrderType, String refOrderNo, String createBy)
    {
        // 如果指定了库位，走精确匹配
        if (locationId != null)
        {
            return reduceInventoryExact(materialId, warehouseId, locationId, batchNo, qty, changeType, refOrderType, refOrderNo, createBy);
        }
        // 未指定库位，按 FIFO 从所有可用库存中扣减
        String effectiveBatch = batchNo == null ? "" : batchNo;
        List<WmsInventory> invList = wmsInventoryMapper.selectAvailableInventory(materialId, warehouseId, effectiveBatch);
        if (invList == null || invList.isEmpty())
        {
            throw new ServiceException("库存不足，无法出库");
        }
        // 计算总可用量
        BigDecimal totalAvailable = BigDecimal.ZERO;
        for (WmsInventory inv : invList)
        {
            totalAvailable = totalAvailable.add(inv.getQty());
        }
        if (totalAvailable.compareTo(qty) < 0)
        {
            throw new ServiceException("库存不足，可用数量：" + totalAvailable + "，需要数量：" + qty);
        }
        // 按顺序逐条扣减
        BigDecimal remaining = qty;
        for (WmsInventory inv : invList)
        {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
            BigDecimal beforeQty = inv.getQty();
            BigDecimal deductQty = beforeQty.min(remaining);
            inv.setQty(beforeQty.subtract(deductQty));
            inv.setUpdateBy(createBy);
            wmsInventoryMapper.updateInventory(inv);
            // log
            WmsInventoryLog log = new WmsInventoryLog();
            log.setMaterialId(materialId);
            log.setWarehouseId(warehouseId);
            log.setLocationId(inv.getLocationId());
            log.setBatchNo(inv.getBatchNo());
            log.setChangeType(changeType);
            log.setChangeQty(deductQty.negate());
            log.setBeforeQty(beforeQty);
            log.setAfterQty(beforeQty.subtract(deductQty));
            log.setRefOrderType(refOrderType);
            log.setRefOrderNo(refOrderNo);
            log.setCreateBy(createBy);
            wmsInventoryLogMapper.insertInventoryLog(log);
            remaining = remaining.subtract(deductQty);
        }
        return 1;
    }

    /** 精确匹配库位+批次扣减库存（原有逻辑） */
    private int reduceInventoryExact(Long materialId, Long warehouseId, Long locationId, String batchNo,
                                     BigDecimal qty, String changeType, String refOrderType, String refOrderNo, String createBy)
    {
        WmsInventory inv = wmsInventoryMapper.selectInventoryByUnique(materialId, warehouseId, locationId,
                batchNo == null ? "" : batchNo);
        if (inv == null)
        {
            throw new ServiceException("库存不足，无法出库");
        }
        // pessimistic lock
        inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
        BigDecimal beforeQty = inv.getQty();
        if (beforeQty.compareTo(qty) < 0)
        {
            throw new ServiceException("库存不足，可用数量：" + beforeQty + "，需要数量：" + qty);
        }
        inv.setQty(inv.getQty().subtract(qty));
        inv.setUpdateBy(createBy);
        wmsInventoryMapper.updateInventory(inv);

        // log
        WmsInventoryLog log = new WmsInventoryLog();
        log.setMaterialId(materialId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(locationId);
        log.setBatchNo(batchNo);
        log.setChangeType(changeType);
        log.setChangeQty(qty.negate());
        log.setBeforeQty(beforeQty);
        log.setAfterQty(beforeQty.subtract(qty));
        log.setRefOrderType(refOrderType);
        log.setRefOrderNo(refOrderNo);
        log.setCreateBy(createBy);
        wmsInventoryLogMapper.insertInventoryLog(log);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int lockInventory(Long materialId, Long warehouseId, Long locationId, String batchNo, BigDecimal qty, String refOrderType, String refOrderNo, String createBy)
    {
        // 如果指定了库位，走精确匹配
        if (locationId != null)
        {
            return lockInventoryExact(materialId, warehouseId, locationId, batchNo, qty, refOrderType, refOrderNo, createBy);
        }
        // 未指定库位，按 FIFO 从所有可用库存中锁定
        String effectiveBatch = batchNo == null ? "" : batchNo;
        List<WmsInventory> invList = wmsInventoryMapper.selectAvailableInventory(materialId, warehouseId, effectiveBatch);
        if (invList == null || invList.isEmpty())
        {
            throw new ServiceException("库存不存在，无法锁定");
        }
        // 计算总可用量
        BigDecimal totalAvailable = BigDecimal.ZERO;
        for (WmsInventory inv : invList)
        {
            totalAvailable = totalAvailable.add(inv.getQty());
        }
        if (totalAvailable.compareTo(qty) < 0)
        {
            throw new ServiceException("可用库存不足，无法锁定");
        }
        // 按顺序逐条锁定
        BigDecimal remaining = qty;
        for (WmsInventory inv : invList)
        {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
            BigDecimal lockQty = inv.getQty().min(remaining);
            inv.setQty(inv.getQty().subtract(lockQty));
            inv.setLockQty(inv.getLockQty().add(lockQty));
            inv.setUpdateBy(createBy);
            wmsInventoryMapper.updateInventory(inv);
            // log
            WmsInventoryLog log = new WmsInventoryLog();
            log.setMaterialId(materialId);
            log.setWarehouseId(warehouseId);
            log.setLocationId(inv.getLocationId());
            log.setBatchNo(inv.getBatchNo());
            log.setChangeType("6");
            log.setChangeQty(lockQty);
            log.setBeforeQty(inv.getQty().add(lockQty));
            log.setAfterQty(inv.getQty());
            log.setRefOrderType(refOrderType);
            log.setRefOrderNo(refOrderNo);
            log.setCreateBy(createBy);
            wmsInventoryLogMapper.insertInventoryLog(log);
            remaining = remaining.subtract(lockQty);
        }
        return 1;
    }

    /** 精确匹配库位+批次锁定库存（原有逻辑） */
    private int lockInventoryExact(Long materialId, Long warehouseId, Long locationId, String batchNo, BigDecimal qty, String refOrderType, String refOrderNo, String createBy)
    {
        WmsInventory inv = wmsInventoryMapper.selectInventoryByUnique(materialId, warehouseId, locationId,
                batchNo == null ? "" : batchNo);
        if (inv == null)
        {
            throw new ServiceException("库存不存在，无法锁定");
        }
        inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
        if (inv.getQty().compareTo(qty) < 0)
        {
            throw new ServiceException("可用库存不足，无法锁定");
        }
        inv.setQty(inv.getQty().subtract(qty));
        inv.setLockQty(inv.getLockQty().add(qty));
        inv.setUpdateBy(createBy);
        wmsInventoryMapper.updateInventory(inv);

        // log
        WmsInventoryLog log = new WmsInventoryLog();
        log.setMaterialId(materialId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(locationId);
        log.setBatchNo(batchNo);
        log.setChangeType("6");
        log.setChangeQty(qty);
        log.setBeforeQty(inv.getQty().add(qty));
        log.setAfterQty(inv.getQty());
        log.setRefOrderType(refOrderType);
        log.setRefOrderNo(refOrderNo);
        log.setCreateBy(createBy);
        wmsInventoryLogMapper.insertInventoryLog(log);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int unlockInventory(Long materialId, Long warehouseId, Long locationId, String batchNo, BigDecimal qty, String refOrderType, String refOrderNo, String createBy)
    {
        WmsInventory inv = wmsInventoryMapper.selectInventoryByUnique(materialId, warehouseId, locationId,
                batchNo == null ? "" : batchNo);
        if (inv == null)
        {
            throw new ServiceException("库存不存在，无法解锁");
        }
        inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
        if (inv.getLockQty().compareTo(qty) < 0)
        {
            throw new ServiceException("锁定库存不足，无法解锁");
        }
        inv.setLockQty(inv.getLockQty().subtract(qty));
        inv.setQty(inv.getQty().add(qty));
        inv.setUpdateBy(createBy);
        wmsInventoryMapper.updateInventory(inv);

        // log
        WmsInventoryLog log = new WmsInventoryLog();
        log.setMaterialId(materialId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(locationId);
        log.setBatchNo(batchNo);
        log.setChangeType("7");
        log.setChangeQty(qty);
        log.setBeforeQty(inv.getQty().subtract(qty));
        log.setAfterQty(inv.getQty());
        log.setRefOrderType(refOrderType);
        log.setRefOrderNo(refOrderNo);
        log.setCreateBy(createBy);
        wmsInventoryLogMapper.insertInventoryLog(log);

        return 1;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseLockedInventory(Long materialId, Long warehouseId, Long locationId, String batchNo,
                               BigDecimal qty, String refOrderType, String refOrderNo, String createBy)
    {
        WmsInventory inv = wmsInventoryMapper.selectInventoryByUnique(materialId, warehouseId, locationId,
                batchNo == null ? "" : batchNo);
        if (inv == null)
        {
            return 0;
        }
        inv = wmsInventoryMapper.selectInventoryForUpdate(inv.getInventoryId());
        inv.setLockQty(inv.getLockQty().subtract(qty));
        inv.setDelFlag("0");
        inv.setUpdateBy(createBy);
        wmsInventoryMapper.updateInventory(inv);

        // log
        WmsInventoryLog log = new WmsInventoryLog();
        log.setMaterialId(materialId);
        log.setWarehouseId(warehouseId);
        log.setLocationId(locationId);
        log.setBatchNo(batchNo);
        log.setChangeType("1");
        log.setChangeQty(qty.negate());
        log.setBeforeQty(inv.getQty());
        log.setAfterQty(inv.getQty());
        log.setRefOrderType(refOrderType);
        log.setRefOrderNo(refOrderNo);
        log.setCreateBy(createBy);
        wmsInventoryLogMapper.insertInventoryLog(log);

        return 1;
    }

    @Override
    public List<WmsInventoryLog> selectInventoryLogList(WmsInventoryLog log)
    {
        return wmsInventoryLogMapper.selectInventoryLogList(log);
    }

    @Override
    public int deleteInventoryByIds(Long[] inventoryIds)
    {
        return wmsInventoryMapper.deleteInventoryByIds(inventoryIds);
    }

    @Override
    public int deleteInventoryLogByIds(Long[] logIds)
    {
        return wmsInventoryLogMapper.deleteInventoryLogByIds(logIds);
    }
}
