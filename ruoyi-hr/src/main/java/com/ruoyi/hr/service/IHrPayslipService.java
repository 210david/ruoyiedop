package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrPayslip;

public interface IHrPayslipService {
    public HrPayslip selectHrPayslipById(Long payslipId);
    public List<HrPayslip> selectHrPayslipList(HrPayslip hrPayslip);
    public int insertHrPayslip(HrPayslip hrPayslip);
    /**
     * 保存工资条并按「累计预扣法」计算个税：
     * 累计应纳税所得额 = 年初至本月 累计应发 − 累计5000 − 累计社保公积金 − 累计专项附加扣除
     * 本月税额 = 累计应预扣税额(新) − 累计已预扣税额(上月)
     */
    public int saveWithCumulativeTax(HrPayslip payslip);
    public int updateHrPayslip(HrPayslip hrPayslip);
    public int deleteHrPayslipByIds(Long[] payslipIds);
    public int deleteHrPayslipById(Long payslipId);
}
