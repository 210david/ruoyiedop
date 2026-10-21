package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrCertificate;

public interface IHrCertificateService {
    public HrCertificate selectHrCertificateById(Long certId);
    public List<HrCertificate> selectHrCertificateList(HrCertificate hrCertificate);
    public int insertHrCertificate(HrCertificate hrCertificate);
    public int updateHrCertificate(HrCertificate hrCertificate);
    public int deleteHrCertificateByIds(Long[] certIds);
    public int deleteHrCertificateById(Long certId);
}
