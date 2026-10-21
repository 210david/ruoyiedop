package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsDataRecord;
import com.ruoyi.dms.mapper.DmsDataRecordMapper;
import com.ruoyi.dms.service.IDmsDataRecordService;

@Service
public class DmsDataRecordServiceImpl implements IDmsDataRecordService
{
    @Autowired
    private DmsDataRecordMapper mapper;

    @Override
    public List<DmsDataRecord> selectRecordList(DmsDataRecord record) { return mapper.selectRecordList(record); }
    @Override
    public DmsDataRecord selectRecordById(Long recordId) { return mapper.selectRecordById(recordId); }
    @Override
    public int insertRecord(DmsDataRecord record) { return mapper.insertRecord(record); }
    @Override
    public int updateRecord(DmsDataRecord record) { return mapper.updateRecord(record); }
    @Override
    public int deleteRecordByIds(Long[] recordIds) { return mapper.deleteRecordByIds(recordIds); }
}
