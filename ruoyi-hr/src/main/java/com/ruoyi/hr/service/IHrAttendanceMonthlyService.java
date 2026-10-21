package com.ruoyi.hr.service;

import java.util.List;
import java.util.Map;
import java.util.Map;
import com.ruoyi.hr.domain.HrAttendanceMonthly;

public interface IHrAttendanceMonthlyService
{
    public HrAttendanceMonthly selectHrAttendanceMonthlyById(Long monthlyId);

    public List<HrAttendanceMonthly> selectHrAttendanceMonthlyList(HrAttendanceMonthly hrAttendanceMonthly);

    public int insertHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly);

    public int updateHrAttendanceMonthly(HrAttendanceMonthly hrAttendanceMonthly);

    public int deleteHrAttendanceMonthlyByIds(Long[] monthlyIds);

    public int deleteHrAttendanceMonthlyById(Long monthlyId);

    /**
     * 按年月自动汇总生成考勤月报
     * 数据来源：日考勤打卡记录 + 当月已审批请假单；覆盖未封账旧数据
     *
     * @return total 生成人数 / recordCount 日考勤记录数 / leaveCount 审批请假单数
     */
    public Map<String, Object> generate(String yearMonth, String operator);


    /**
     * 下钻明细：某员工某月的日考勤明细 + 已审批请假/外出/出差单
     */
    public Map<String, Object> detail(Long employeeId, String yearMonth);
}
