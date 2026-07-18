package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsSparePart;
import com.ruoyi.dms.domain.DmsSparePartRecord;

public interface IDmsSparePartService
{
    public List<DmsSparePart> selectSparePartList(DmsSparePart sparePart);
    public DmsSparePart selectSparePartById(Long partId);
    public int insertSparePart(DmsSparePart sparePart);
    public int updateSparePart(DmsSparePart sparePart);
    public int deleteSparePartByIds(Long[] partIds);

    /** 自动生成备件编号 */
    public String generatePartCode();

    /** 出入库操作 */
    public int stockMove(DmsSparePartRecord record);
    /** 查询出入库记录 */
    public List<DmsSparePartRecord> selectRecordList(DmsSparePartRecord record);

    /** 删除出入库记录 */
    public int deleteRecordByIds(Long[] recordIds);

    /** 清除备件安全库存上下限设置（用于库存预警） */
    public int clearSparePartAlert(Long partId);

    /** 生成入库单号 */
    public String generateInCode();
    /** 生成出库单号 */
    public String generateOutCode();

    /** 库存预警查询 */
    public List<DmsSparePart> selectStockAlertList(DmsSparePart sparePart);
}
