package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrSocialSecurity;
import com.ruoyi.hr.mapper.HrSocialSecurityMapper;
import com.ruoyi.hr.service.IHrSocialSecurityService;

@Service
public class HrSocialSecurityServiceImpl implements IHrSocialSecurityService
{
    @Autowired
    private HrSocialSecurityMapper hrSocialSecurityMapper;

    @Override
    public HrSocialSecurity selectHrSocialSecurityById(Long ssId) { return hrSocialSecurityMapper.selectHrSocialSecurityById(ssId); }

    @Override
    public List<HrSocialSecurity> selectHrSocialSecurityList(HrSocialSecurity hrSocialSecurity) { return hrSocialSecurityMapper.selectHrSocialSecurityList(hrSocialSecurity); }

    @Override
    public int insertHrSocialSecurity(HrSocialSecurity hrSocialSecurity) { return hrSocialSecurityMapper.insertHrSocialSecurity(hrSocialSecurity); }

    @Override
    public int updateHrSocialSecurity(HrSocialSecurity hrSocialSecurity) { return hrSocialSecurityMapper.updateHrSocialSecurity(hrSocialSecurity); }

    @Override
    public int deleteHrSocialSecurityByIds(Long[] ssIds) { return hrSocialSecurityMapper.deleteHrSocialSecurityByIds(ssIds); }

    @Override
    public int deleteHrSocialSecurityById(Long ssId) { return hrSocialSecurityMapper.deleteHrSocialSecurityById(ssId); }
}
