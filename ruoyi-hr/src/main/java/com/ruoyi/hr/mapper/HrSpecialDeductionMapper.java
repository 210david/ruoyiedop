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
}
