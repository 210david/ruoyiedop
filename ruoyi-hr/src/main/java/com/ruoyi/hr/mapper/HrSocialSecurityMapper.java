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
}
