package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsSupplierQualification;

/**
 * 供应商资质 Service接口
 *
 * @author ruoyi
 */
public interface IPmsSupplierQualificationService
{
    public List<PmsSupplierQualification> selectQualificationList(PmsSupplierQualification qualification);
    public PmsSupplierQualification selectQualificationById(Long qualId);
    public int insertQualification(PmsSupplierQualification qualification);
    public int updateQualification(PmsSupplierQualification qualification);
    public int deleteQualificationByIds(Long[] qualIds);
    public int auditQualification(Long qualId, String status, String auditRemark);

    /** 检查过期资质并更新状态，返回过期数量 */
    public int checkExpiredQualifications();
}
