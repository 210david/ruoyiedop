package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrSpecialDeduction;

public interface HrSpecialDeductionMapper {
    public HrSpecialDeduction selectHrSpecialDeductionById(Long sdId);
    public List<HrSpecialDeduction> selectHrSpecialDeductionList(HrSpecialDeduction hrSpecialDeduction);
    public int insertHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction);
    public int updateHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction);
    public int deleteHrSpecialDeductionByIds(Long[] sdIds);
    public int deleteHrSpecialDeductionById(Long sdId);
    /** 汇总员工在指定日期已生效的月度专项附加扣除合计 */
    public java.math.BigDecimal sumMonthlyDeduction(@org.apache.ibatis.annotations.Param("employeeId") Long employeeId, @org.apache.ibatis.annotations.Param("onDate") java.util.Date onDate);
}
