package com.ruoyi.wms.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.wms.domain.WmsInventory;
import com.ruoyi.wms.domain.WmsInventorySummary;

public interface WmsInventoryMapper
{
    public List<WmsInventory> selectInventoryList(WmsInventory inventory);
    /** 按物料+仓库汇总查询（管理视角） */
    public List<WmsInventorySummary> selectInventorySummaryList(WmsInventory inventory);
    public WmsInventory selectInventoryById(Long inventoryId);
    public WmsInventory selectInventoryByUnique(Long materialId, Long warehouseId, Long locationId, String batchNo);
    /** 按物料+仓库查询可用库存（qty > 0 且未删除），按创建时间升序（FIFO） */
    public List<WmsInventory> selectAvailableInventory(Long materialId, Long warehouseId, String batchNo);
    public int insertInventory(WmsInventory inventory);
    public int updateInventory(WmsInventory inventory);
    public int updateInventoryWithVersion(WmsInventory inventory);
    public WmsInventory selectInventoryForUpdate(Long inventoryId);
    public int deleteInventoryByIds(Long[] inventoryIds);

    /**
     * 按「物料+仓库+库位+批次」四维批量取成本台账结存金额（只读财务 fin_inventory_cost）。
     *
     * <p>【调用前提】必须先由 InventoryCostAvailability 判定财务表存在，缺表时会抛 SQL 异常。</p>
     */
    public List<com.ruoyi.wms.domain.vo.WmsInventoryCostVO> selectCostByDimension(
            @org.apache.ibatis.annotations.Param("list") List<WmsInventory> inventoryList);

    /**
     * 批量取物料取价依据：入库加权均价 + 物料兜底成本价。
     */
    public List<com.ruoyi.wms.domain.vo.WmsMaterialPriceVO> selectMaterialPrice(
            @org.apache.ibatis.annotations.Param("materialIds") List<Long> materialIds);
}
