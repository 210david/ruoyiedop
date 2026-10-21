package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrAttendanceRecord;
import org.apache.ibatis.annotations.Param;

public interface HrAttendanceRecordMapper {
    public HrAttendanceRecord selectHrAttendanceRecordById(Long recordId);
    public List<HrAttendanceRecord> selectHrAttendanceRecordList(HrAttendanceRecord hrAttendanceRecord);
    public int insertHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int updateHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int deleteHrAttendanceRecordByIds(Long[] recordIds);
    public int deleteHrAttendanceRecordById(Long recordId);

    /** 根据工号+日期查询已有考勤记录（导入时判重） */
    public HrAttendanceRecord selectByEmployeeNoAndDate(@Param("employeeNo") String employeeNo, @Param("recordDate") String recordDate);
}
