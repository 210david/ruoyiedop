package com.ruoyi.bid.mapper;

import java.util.List;
import com.ruoyi.bid.domain.BidQualification;

/**
 * 资质资料库 Mapper 接口
 */
public interface BidQualificationMapper
{
    public List<BidQualification> selectQualificationList(BidQualification qualification);

    public BidQualification selectQualificationById(Long qualificationId);

    public List<BidQualification> selectExpiringQualifications();

    public int insertQualification(BidQualification qualification);

    public int updateQualification(BidQualification qualification);

    public int deleteQualificationByIds(Long[] qualificationIds);
}
