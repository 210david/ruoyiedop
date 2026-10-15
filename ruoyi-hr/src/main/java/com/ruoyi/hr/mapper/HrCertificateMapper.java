package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrCertificate;

public interface HrCertificateMapper {
    public HrCertificate selectHrCertificateById(Long certId);
    public List<HrCertificate> selectHrCertificateList(HrCertificate hrCertificate);
    public int insertHrCertificate(HrCertificate hrCertificate);
    public int updateHrCertificate(HrCertificate hrCertificate);
    public int deleteHrCertificateByIds(Long[] certIds);
    public int deleteHrCertificateById(Long certId);
    /** 刷新健康证预警状态（口径同calcRemindStatus：有效期至/复审日期较早者，60天临期） */
    public int refreshHealthCertRemindStatus();
    /** 查询临期/过期的健康证（remind_status 1临期 2过期） */
    public List<HrCertificate> selectExpiringHealthCerts();
}
