package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSchedule;
import com.ruoyi.hr.domain.dto.HrScheduleBatchDTO;

public interface IHrScheduleService {
    public HrSchedule selectHrScheduleById(Long scheduleId);
    public List<HrSchedule> selectHrScheduleList(HrSchedule hrSchedule);
    public int insertHrSchedule(HrSchedule hrSchedule);
    public int updateHrSchedule(HrSchedule hrSchedule);
    public int deleteHrScheduleByIds(Long[] scheduleIds);
    public int deleteHrScheduleById(Long scheduleId);

    /** 批量排班 */
    public int batchSchedule(HrScheduleBatchDTO dto, String operName);

    /** 月度排班日历 */
    public List<HrSchedule> selectScheduleCalendar(List<Long> employeeIds, Long orgId, String startDate, String endDate);

    /** 清除排班 */
    public int clearSchedule(List<Long> employeeIds, Long orgId, String startDate, String endDate);
}
