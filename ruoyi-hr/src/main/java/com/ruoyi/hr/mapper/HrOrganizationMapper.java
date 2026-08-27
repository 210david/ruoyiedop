package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOrganization;

public interface HrOrganizationMapper {
    public HrOrganization selectHrOrganizationById(Long orgId);
    public List<HrOrganization> selectHrOrganizationList(HrOrganization hrOrganization);
    public int insertHrOrganization(HrOrganization hrOrganization);
    public int updateHrOrganization(HrOrganization hrOrganization);
    public int deleteHrOrganizationByIds(Long[] orgIds);
    public int deleteHrOrganizationById(Long orgId);
}
