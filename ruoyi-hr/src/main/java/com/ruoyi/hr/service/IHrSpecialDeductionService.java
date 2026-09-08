package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSpecialDeduction;

public interface IHrSpecialDeductionService {
    public HrSpecialDeduction selectHrSpecialDeductionById(Long sdId);
    public List<HrSpecialDeduction> selectHrSpecialDeductionList(HrSpecialDeduction hrSpecialDeduction);
    public int insertHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction);
    public int updateHrSpecialDeduction(HrSpecialDeduction hrSpecialDeduction);
    public int deleteHrSpecialDeductionByIds(Long[] sdIds);
    public int deleteHrSpecialDeductionById(Long sdId);
    /** 批量导入：按工号匹配员工，同员工+项目+生效日期更新或新增 */
    public com.ruoyi.common.core.domain.AjaxResult importSpecialDeduction(java.util.List<HrSpecialDeduction> list, boolean updateSupport, String operName);
}
