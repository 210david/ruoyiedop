package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsWarehouse;

public interface WmsWarehouseMapper
{
    /** 查询所有节点（树形列表） */
    public List<WmsWarehouse> selectWarehouseList(WmsWarehouse warehouse);

    /** 按ID查询 */
    public WmsWarehouse selectWarehouseById(Long warehouseId);

    /** 查询子节点数量 */
    public int countChildByParentId(Long parentId);

    /** 新增 */
    public int insertWarehouse(WmsWarehouse warehouse);

    /** 修改 */
    public int updateWarehouse(WmsWarehouse warehouse);

    /** 逻辑删除（含子节点） */
    public int deleteWarehouseByIds(Long[] warehouseIds);

    /** 查询某父节点下同类型最大编码后缀 */
    public String selectMaxCodeByParent(Long parentId, String nodeType);
}
