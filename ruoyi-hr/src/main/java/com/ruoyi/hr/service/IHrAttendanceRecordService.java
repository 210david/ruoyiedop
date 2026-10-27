package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.hr.domain.HrAttendanceImportDTO;
import com.ruoyi.hr.domain.HrAttendanceRecord;

public interface IHrAttendanceRecordService {
    public HrAttendanceRecord selectHrAttendanceRecordById(Long recordId);
    public List<HrAttendanceRecord> selectHrAttendanceRecordList(HrAttendanceRecord hrAttendanceRecord);
    public int insertHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int updateHrAttendanceRecord(HrAttendanceRecord hrAttendanceRecord);
    public int deleteHrAttendanceRecordByIds(Long[] recordIds);
    public int deleteHrAttendanceRecordById(Long recordId);

    /** 考勤机原始数据导入 */
    public AjaxResult importAttendance(List<HrAttendanceImportDTO> importList, Boolean updateSupport, String operName);

    /** 批量重算考勤状态（根据班次重新计算迟到/早退/状态） */
    public int recalculateAttendanceStatus(HrAttendanceRecord query);
}
