package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsWarehouse;

public interface IWmsWarehouseService
{
    /** 查询仓库结构列表（全部节点） */
    public List<WmsWarehouse> selectWarehouseList(WmsWarehouse warehouse);

    /** 按ID查询 */
    public WmsWarehouse selectWarehouseById(Long warehouseId);

    /** 新增（自动生成编码） */
    public int insertWarehouse(WmsWarehouse warehouse);

    /** 修改 */
    public int updateWarehouse(WmsWarehouse warehouse);

    /** 删除（含子节点校验） */
    public int deleteWarehouseByIds(Long[] warehouseIds);

    /** 生成下一个编码 */
    public String generateNextCode(WmsWarehouse warehouse);
}
