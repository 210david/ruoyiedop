package com.ruoyi.wms.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.wms.domain.WmsInventory;

public interface WmsInventoryMapper
{
    public List<WmsInventory> selectInventoryList(WmsInventory inventory);
    public WmsInventory selectInventoryById(Long inventoryId);
    public WmsInventory selectInventoryByUnique(Long materialId, Long warehouseId, Long locationId, String batchNo);
    /** 按物料+仓库查询可用库存（qty > 0 且未删除），按创建时间升序（FIFO） */
    public List<WmsInventory> selectAvailableInventory(Long materialId, Long warehouseId, String batchNo);
    public int insertInventory(WmsInventory inventory);
    public int updateInventory(WmsInventory inventory);
    public int updateInventoryWithVersion(WmsInventory inventory);
    public WmsInventory selectInventoryForUpdate(Long inventoryId);
    public int deleteInventoryByIds(Long[] inventoryIds);
}
