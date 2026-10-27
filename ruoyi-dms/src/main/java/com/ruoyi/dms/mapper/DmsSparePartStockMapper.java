package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsSparePartStock;

public interface DmsSparePartStockMapper
{
    public List<DmsSparePartStock> selectStockList(DmsSparePartStock stock);
    public DmsSparePartStock selectStockById(Long stockId);
    public int insertStock(DmsSparePartStock stock);
    public int updateStock(DmsSparePartStock stock);
    public int deleteStockByIds(Long[] stockIds);

    /** 查询库存预警列表（当前库存 <= 库存下限 或 当前库存 >= 库存上限） */
    public List<DmsSparePartStock> selectStockAlertList(DmsSparePartStock stock);

    /** 根据备件ID和仓库名称查询库存 */
    public DmsSparePartStock selectByPartAndWarehouse(Long partId, String warehouseName);

    /** 批量更新库存（乐观锁） */
    public int updateStockWithLock(DmsSparePartStock stock);
}