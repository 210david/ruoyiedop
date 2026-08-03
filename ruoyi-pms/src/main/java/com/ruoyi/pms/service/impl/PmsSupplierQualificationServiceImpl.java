package com.ruoyi.pms.service.impl;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pms.domain.PmsSupplierQualification;
import com.ruoyi.pms.mapper.PmsSupplierQualificationMapper;
import com.ruoyi.pms.service.IPmsSupplierQualificationService;

/**
 * 供应商资质 Service实现
 *
 * @author ruoyi
 */
@Service
public class PmsSupplierQualificationServiceImpl implements IPmsSupplierQualificationService
{
    @Autowired
    private PmsSupplierQualificationMapper pmsSupplierQualificationMapper;

    @Override
    public List<PmsSupplierQualification> selectQualificationList(PmsSupplierQualification qualification)
    {
        return pmsSupplierQualificationMapper.selectQualificationList(qualification);
    }

    @Override
    public PmsSupplierQualification selectQualificationById(Long qualId)
    {
        return pmsSupplierQualificationMapper.selectQualificationById(qualId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertQualification(PmsSupplierQualification qualification)
    {
        qualification.setDelFlag("0");
        if (qualification.getStatus() == null)
        {
            qualification.setStatus("1");
        }
        return pmsSupplierQualificationMapper.insertQualification(qualification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateQualification(PmsSupplierQualification qualification)
    {
        return pmsSupplierQualificationMapper.updateQualification(qualification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteQualificationByIds(Long[] qualIds)
    {
        return pmsSupplierQualificationMapper.deleteQualificationByIds(qualIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditQualification(Long qualId, String status, String auditRemark)
    {
        PmsSupplierQualification qualification = pmsSupplierQualificationMapper.selectQualificationById(qualId);
        if (qualification == null)
        {
            throw new ServiceException("资质信息不存在");
        }
        if (!"0".equals(qualification.getStatus()))
        {
            throw new ServiceException("只有待审核状态的资质才能审核");
        }
        qualification.setStatus(status);
        qualification.setAuditBy(SecurityUtils.getUsername());
        qualification.setAuditTime(new Date());
        qualification.setAuditRemark(auditRemark);
        return pmsSupplierQualificationMapper.auditQualification(qualification);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int checkExpiredQualifications()
    {
        List<PmsSupplierQualification> expiredList = pmsSupplierQualificationMapper.selectExpiredQualifications();
        if (expiredList == null || expiredList.isEmpty())
        {
            return 0;
        }
        List<Long> qualIds = expiredList.stream()
                .map(PmsSupplierQualification::getQualId)
                .collect(Collectors.toList());
        return pmsSupplierQualificationMapper.updateExpiredStatus(qualIds);
    }
}
