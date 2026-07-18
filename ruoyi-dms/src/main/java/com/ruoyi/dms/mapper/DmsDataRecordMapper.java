package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsDataRecord;

public interface DmsDataRecordMapper
{
    public List<DmsDataRecord> selectRecordList(DmsDataRecord record);
    public DmsDataRecord selectRecordById(Long recordId);
    public int insertRecord(DmsDataRecord record);
    public int updateRecord(DmsDataRecord record);
    public int deleteRecordByIds(Long[] recordIds);
}
