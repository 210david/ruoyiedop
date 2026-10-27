package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsSparePart;

public interface DmsSparePartMapper
{
    public List<DmsSparePart> selectSparePartList(DmsSparePart sparePart);
    public DmsSparePart selectSparePartById(Long partId);
    public int insertSparePart(DmsSparePart sparePart);
    public int updateSparePart(DmsSparePart sparePart);
    public int deleteSparePartByIds(Long[] partIds);

    /** 查询当天最大编号，用于自动生成 */
    public String selectLastPartCode(String prefix);

    /** 查询库存预警列表（当前库存 <= 库存下限） */
    public List<DmsSparePart> selectStockAlertList(DmsSparePart sparePart);

    /** 清除备件安全库存上下限设置（用于删除预警） */
    public int clearStockAlert(Long partId);
}
