package com.ruoyi.wms.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 按「物料+仓库+库位+批次」四维批量取成本台账结存金额（只读财务 fin_inventory_cost）。
     *
     * <p>【调用前提】必须先由 {@code InventoryCostAvailability#isCostTableAvailable()} 判定财务表存在，
     * 缺表时调用会抛 SQL 异常。WMS 与财务表之间只读、不建立 Maven 依赖，
     * 避免 ruoyi-fin → ruoyi-wms 已有依赖造成的循环依赖。</p>
     *
     * @param inventoryList 待取数的库存记录（需含 materialId/warehouseId/locationId/batchNo）
     * @return 四维键 → 台账记录（qty、unitCost、totalCost）
     */
    public List<com.ruoyi.wms.domain.vo.WmsInventoryCostVO> selectCostByDimension(
            @Param("list") List<WmsInventory> inventoryList);

    /**
     * 批量取物料取价依据：入库加权均价 + 物料兜底成本价。
     *
     * <p>用于财务成本台账不可用（或该记录未计价）时的降级估算。</p>
     *
     * @param materialIds 物料ID集合
     */
    public List<com.ruoyi.wms.domain.vo.WmsMaterialPriceVO> selectMaterialPrice(
            @Param("materialIds") List<Long> materialIds);
}
