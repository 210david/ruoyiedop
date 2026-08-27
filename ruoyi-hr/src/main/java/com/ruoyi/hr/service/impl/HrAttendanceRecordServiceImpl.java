package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import com.ruoyi.hr.mapper.HrAttendanceRecordMapper;
import com.ruoyi.hr.service.IHrAttendanceRecordService;

@Service
public class HrAttendanceRecordServiceImpl implements IHrAttendanceRecordService
{
    @Autowired
    private HrAttendanceRecordMapper hrAttendanceRecordMapper;

    @Override
    public HrAttendanceRecord selectHrAttendanceRecordById(Long recordId) { return hrAttendanceRecordMapper.selectHrAttendanceRecordById(recordId); }

    @Override
    public List<HrAttendanceRecord> selectHrAttendanceRecordList(HrAttendanceRecord hrAttendanceRecord) { return hrAttendanceRecordMapper.selectHrAttendanceRecordList(hrAttendanceRecord); }

    @Override
    public int insertHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord) { return hrAttendanceRecordMapper.insertHrAttendanceRecord(hrAttendanceRecord); }

    @Override
    public int updateHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord) { return hrAttendanceRecordMapper.updateHrAttendanceRecord(hrAttendanceRecord); }

    @Override
    public int deleteHrAttendanceRecordByIds(Long[] recordIds) { return hrAttendanceRecordMapper.deleteHrAttendanceRecordByIds(recordIds); }

    @Override
    public int deleteHrAttendanceRecordById(Long recordId) { return hrAttendanceRecordMapper.deleteHrAttendanceRecordById(recordId); }
}
