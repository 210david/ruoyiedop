package com.ruoyi.hr.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.hr.domain.HrLeaveBalance;

public interface HrLeaveBalanceMapper {
    public HrLeaveBalance selectHrLeaveBalanceById(Long balanceId);
    public List<HrLeaveBalance> selectHrLeaveBalanceList(HrLeaveBalance hrLeaveBalance);
    public int insertHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int updateHrLeaveBalance(HrLeaveBalance hrLeaveBalance);
    public int deleteHrLeaveBalanceByIds(Long[] balanceIds);
    public int deleteHrLeaveBalanceById(Long balanceId);

    /** 批量插入假期余额 */
    public int batchInsertHrLeaveBalance(@Param("list") List<HrLeaveBalance> list);

    /** 根据员工ID列表+假别+年度查询已存在的余额记录 */
    public List<HrLeaveBalance> selectExistingBalance(@Param("employeeIds") List<Long> employeeIds,
                                                       @Param("leaveType") String leaveType,
                                                       @Param("year") Integer year);
}
