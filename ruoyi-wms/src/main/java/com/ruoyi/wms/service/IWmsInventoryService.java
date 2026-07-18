package com.ruoyi.wms.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsInventoryLog;

public interface IWmsInventoryService
{
    public List<WmsInventory> selectInventoryList(WmsInventory inventory);
    public WmsInventory selectInventoryById(Long inventoryId);
    public int addInventory(Long materialId, Long warehouseId, Long locationId, String batchNo,
                            BigDecimal qty, String changeType, String refOrderType, String refOrderNo, 
                            String createBy, Date productionDate, Date expiryDate);
    public int reduceInventory(Long materialId, Long warehouseId, Long locationId, String batchNo,
                               BigDecimal qty, String changeType, String refOrderType, String refOrderNo, String createBy);
    public int lockInventory(Long materialId, Long warehouseId, Long locationId, String batchNo, BigDecimal qty, String createBy);
    public int unlockInventory(Long materialId, Long warehouseId, Long locationId, String batchNo, BigDecimal qty, String createBy);
    public List<WmsInventoryLog> selectInventoryLogList(WmsInventoryLog log);

    /**
     * 删除库存记录（软删除）
     */
    public int deleteInventoryByIds(Long[] inventoryIds);

    /**
     * 删除库存流水记录（硬删除）
     */
    public int deleteInventoryLogByIds(Long[] logIds);
}
