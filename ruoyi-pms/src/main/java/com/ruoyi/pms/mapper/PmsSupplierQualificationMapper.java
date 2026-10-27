package com.ruoyi.pms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.pms.domain.PmsSupplierQualification;

/**
 * 供应商资质 Mapper
 *
 * @author ruoyi
 */
public interface PmsSupplierQualificationMapper
{
    public List<PmsSupplierQualification> selectQualificationList(PmsSupplierQualification qualification);
    public PmsSupplierQualification selectQualificationById(Long qualId);
    public int insertQualification(PmsSupplierQualification qualification);
    public int updateQualification(PmsSupplierQualification qualification);
    public int deleteQualificationByIds(Long[] qualIds);
    public int auditQualification(PmsSupplierQualification qualification);

    /** 查询已过期但状态仍为有效的资质 */
    public List<PmsSupplierQualification> selectExpiredQualifications();

    /** 批量将过期资质状态更新为已过期 */
    public int updateExpiredStatus(@Param("qualIds") List<Long> qualIds);
}
