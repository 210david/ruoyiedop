package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
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
    @DataScope(deptAlias = "biz_hr_certificate")
    public List<HrCertificate> selectHrCertificateList(HrCertificate hrCertificate) { return hrCertificateMapper.selectHrCertificateList(hrCertificate); }

    @Override
    public int insertHrCertificate(HrCertificate hrCertificate)
    {
        // 证书编号由用户必填输入，不再自动生成；重复编号拦截
        if (StringUtils.isEmpty(hrCertificate.getCertNo()))
        {
            throw new com.ruoyi.common.exception.ServiceException("证书编号不能为空");
        }
        HrCertificate dup = new HrCertificate();
        dup.setCertNo(hrCertificate.getCertNo());
        List<HrCertificate> exist = hrCertificateMapper.selectHrCertificateList(dup);
        if (exist != null && !exist.isEmpty())
        {
            throw new com.ruoyi.common.exception.ServiceException("证书编号已存在：" + hrCertificate.getCertNo());
        }
        // 预警状态只读：服务端根据有效期/复审日期自动判断（0正常 1临期 2过期）
        hrCertificate.setRemindStatus(calcRemindStatus(hrCertificate));
        return hrCertificateMapper.insertHrCertificate(hrCertificate);
    }

    @Override
    public int updateHrCertificate(HrCertificate hrCertificate) { hrCertificate.setRemindStatus(calcRemindStatus(hrCertificate)); return hrCertificateMapper.updateHrCertificate(hrCertificate); }

    /** 预警状态自动判断：有效期至/复审日期 60天内为临期，已过为过期，无日期为正常 */
    private String calcRemindStatus(HrCertificate c)
    {
        java.util.Date now = new java.util.Date();
        java.util.Date warnDate = new java.util.Date(now.getTime() + 60L * 24 * 3600 * 1000);
        // 取有效期至与复审日期中较早者判断
        java.util.Date validTo = c.getValidTo();
        java.util.Date review = c.getReviewDate();
        java.util.Date check = null;
        if (validTo != null && (review == null || validTo.before(review))) { check = validTo; }
        else if (review != null) { check = review; }
        if (check == null) { return "0"; }
        if (check.before(now)) { return "2"; }
        if (check.before(warnDate)) { return "1"; }
        return "0";
    }

    @Override
    public int deleteHrCertificateByIds(Long[] certIds) { return hrCertificateMapper.deleteHrCertificateByIds(certIds); }

    @Override
    public int deleteHrCertificateById(Long certId) { return hrCertificateMapper.deleteHrCertificateById(certId); }
}
