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

    /**
     * 考勤原始数据导入（可指定打卡来源与备注，供考勤机导入与钉钉等外部数据源共用）
     *
     * @param clockSource 打卡来源（hr_clock_source 字典值，如 0=考勤机导入 3=钉钉同步）
     * @param sourceRemark 来源备注写入 remark 字段
     */
    public AjaxResult importAttendance(List<HrAttendanceImportDTO> importList, Boolean updateSupport, String operName, String clockSource, String sourceRemark);

    /** 批量重算考勤状态（根据班次重新计算迟到/早退/状态） */
    public int recalculateAttendanceStatus(HrAttendanceRecord query);
}
