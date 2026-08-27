package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrAttendanceRecord;

public interface HrAttendanceRecordMapper {
    public HrAttendanceRecord selectHrAttendanceRecordById(Long recordId);
    public List<HrAttendanceRecord> selectHrAttendanceRecordList(HrAttendanceRecord hrAttendanceRecord);
    public int insertHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int updateHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int deleteHrAttendanceRecordByIds(Long[] recordIds);
    public int deleteHrAttendanceRecordById(Long recordId);
}
