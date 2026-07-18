package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsSparePartRecord;

public interface DmsSparePartRecordMapper
{
    public List<DmsSparePartRecord> selectRecordList(DmsSparePartRecord record);
    public int insertRecord(DmsSparePartRecord record);

    /** 查询当天最大单号，用于自动生成 */
    public String selectLastDocumentCode(String prefix);

    /** 批量删除出入库记录 */
    public int deleteRecordByIds(Long[] recordIds);
}
