package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrSocialSecurity;

public interface HrSocialSecurityMapper {
    public HrSocialSecurity selectHrSocialSecurityById(Long ssId);
    public List<HrSocialSecurity> selectHrSocialSecurityList(HrSocialSecurity hrSocialSecurity);
    public int insertHrSocialSecurity(HrSocialSecurity hrSocialSecurity);
    public int updateHrSocialSecurity(HrSocialSecurity hrSocialSecurity);
    public int deleteHrSocialSecurityByIds(Long[] ssIds);
    public int deleteHrSocialSecurityById(Long ssId);

    /** 统计引用某社保方案的员工记录数（方案删除/改年度前校验） */
    public Long countByPlanId(Long planId);
    public Long countByPlanIdAndYear(Long planId, Integer year);

    /** 批量初始化：为未配置指定年度社保的在职员工按方案生成记录，返回插入行数 */
    public int batchInitByPlan(com.ruoyi.hr.domain.HrSsPlan plan);
}
