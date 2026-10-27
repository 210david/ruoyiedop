package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrPayslip;

public interface HrPayslipMapper {
    public HrPayslip selectHrPayslipById(Long payslipId);
    public List<HrPayslip> selectHrPayslipList(HrPayslip hrPayslip);
    public int insertHrPayslip(HrPayslip hrPayslip);
    public int updateHrPayslip(HrPayslip hrPayslip);
    public int deleteHrPayslipByIds(Long[] payslipIds);
    public int deleteHrPayslipById(Long payslipId);
}
