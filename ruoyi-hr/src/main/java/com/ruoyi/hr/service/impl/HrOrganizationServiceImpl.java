package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrOrganization;
import com.ruoyi.hr.mapper.HrOrganizationMapper;
import com.ruoyi.hr.service.IHrOrganizationService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrOrganizationServiceImpl implements IHrOrganizationService
{
    @Autowired
    private HrOrganizationMapper hrOrganizationMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrOrganization selectHrOrganizationById(Long orgId) { return hrOrganizationMapper.selectHrOrganizationById(orgId); }

    @Override
    public List<HrOrganization> selectHrOrganizationList(HrOrganization hrOrganization) { return hrOrganizationMapper.selectHrOrganizationList(hrOrganization); }

    @Override
    public int insertHrOrganization(HrOrganization hrOrganization)
    {
        if (StringUtils.isEmpty(hrOrganization.getOrgCode()))
        {
            hrOrganization.setOrgCode(mkNumberRuleService.generateNumber("hr_organization"));
        }
        return hrOrganizationMapper.insertHrOrganization(hrOrganization);
    }

    @Override
    public int updateHrOrganization(HrOrganization hrOrganization) { return hrOrganizationMapper.updateHrOrganization(hrOrganization); }

    @Override
    public int deleteHrOrganizationByIds(Long[] orgIds) { return hrOrganizationMapper.deleteHrOrganizationByIds(orgIds); }

    @Override
    public int deleteHrOrganizationById(Long orgId) { return hrOrganizationMapper.deleteHrOrganizationById(orgId); }
}
