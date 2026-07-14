package com.ruoyi.wms.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.wms.domain.WmsInventory;

public interface WmsInventoryMapper
{
    public List<WmsInventory> selectInventoryList(WmsInventory inventory);
    public WmsInventory selectInventoryById(Long inventoryId);
    public WmsInventory selectInventoryByUnique(Long materialId, Long warehouseId, Long locationId, String batchNo);
    public int insertInventory(WmsInventory inventory);
    public int updateInventory(WmsInventory inventory);
    public int updateInventoryWithVersion(WmsInventory inventory);
    public WmsInventory selectInventoryForUpdate(Long inventoryId);
    public int deleteInventoryByIds(Long[] inventoryIds);
}
