package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrCertificate;
import com.ruoyi.hr.mapper.HrCertificateMapper;
import com.ruoyi.hr.service.IHrCertificateService;

@Service
public class HrCertificateServiceImpl implements IHrCertificateService
{
    @Autowired
    private HrCertificateMapper hrCertificateMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrCertificate selectHrCertificateById(Long certId) { return hrCertificateMapper.selectHrCertificateById(certId); }

    @Override
    public List<HrCertificate> selectHrCertificateList(HrCertificate hrCertificate) { return hrCertificateMapper.selectHrCertificateList(hrCertificate); }

    @Override
    public int insertHrCertificate(HrCertificate hrCertificate)
    {
        if (StringUtils.isEmpty(hrCertificate.getCertNo()))
        {
            hrCertificate.setCertNo(mkNumberRuleService.generateNumber("hr_certificate"));
        }
        return hrCertificateMapper.insertHrCertificate(hrCertificate);
    }

    @Override
    public int updateHrCertificate(HrCertificate hrCertificate) { return hrCertificateMapper.updateHrCertificate(hrCertificate); }

    @Override
    public int deleteHrCertificateByIds(Long[] certIds) { return hrCertificateMapper.deleteHrCertificateByIds(certIds); }

    @Override
    public int deleteHrCertificateById(Long certId) { return hrCertificateMapper.deleteHrCertificateById(certId); }
}
