package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrSocialSecurity;

public interface IHrSocialSecurityService {
    public HrSocialSecurity selectHrSocialSecurityById(Long ssId);
    public List<HrSocialSecurity> selectHrSocialSecurityList(HrSocialSecurity hrSocialSecurity);
    public int insertHrSocialSecurity(HrSocialSecurity hrSocialSecurity);
    public int updateHrSocialSecurity(HrSocialSecurity hrSocialSecurity);
    public int deleteHrSocialSecurityByIds(Long[] ssIds);
    public int deleteHrSocialSecurityById(Long ssId);
}
