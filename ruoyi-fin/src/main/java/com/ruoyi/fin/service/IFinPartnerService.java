package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinPartner;

import java.util.List;

/**
 * 财务往来单位 Service 接口
 *
 * @author ruoyi
 */
public interface IFinPartnerService
{
    List<FinPartner> selectPartnerList(FinPartner partner);

    FinPartner selectPartnerById(Long partnerId);

    int insertPartner(FinPartner partner);

    int updatePartner(FinPartner partner);

    int deletePartnerByIds(Long[] partnerIds);
}