package com.ruoyi.fin.service.impl;

import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinPartner;
import com.ruoyi.fin.engine.FinNumberGenerator;
import com.ruoyi.fin.mapper.FinPartnerMapper;
import com.ruoyi.fin.service.IFinPartnerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 财务往来单位 Service 实现
 *
 * <p>【删除约束】已被凭证辅助核算引用的单位不可删除，只能停用，
 * 否则历史凭证的辅助核算将失去指向。</p>
 *
 * @author ruoyi
 */
@Service
public class FinPartnerServiceImpl implements IFinPartnerService
{
    @Autowired
    private FinPartnerMapper partnerMapper;

    @Autowired
    private FinNumberGenerator numberGenerator;

    @Override
    @DataScope(deptAlias = "p", userAlias = "p")
    public List<FinPartner> selectPartnerList(FinPartner partner)
    {
        return partnerMapper.selectPartnerList(partner);
    }

    @Override
    public FinPartner selectPartnerById(Long partnerId)
    {
        return partnerMapper.selectPartnerById(partnerId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertPartner(FinPartner partner)
    {
        if (partnerMapper.checkNameUnique(partner.getPartnerName(),
                partner.getPartnerType(), null) > 0)
        {
            throw new ServiceException("同类型下已存在同名往来单位：" + partner.getPartnerName());
        }
        if (partner.getPartnerCode() == null || partner.getPartnerCode().trim().isEmpty())
        {
            partner.setPartnerCode(numberGenerator.generate(FinNumberGenerator.RULE_PARTNER));
        }
        partner.setCreditUsed(partner.getCreditUsed() == null ? BigDecimal.ZERO : partner.getCreditUsed());
        partner.setCreditLimit(partner.getCreditLimit() == null ? BigDecimal.ZERO : partner.getCreditLimit());
        partner.setCurrency(partner.getCurrency() == null ? "CNY" : partner.getCurrency());
        partner.setStatus(partner.getStatus() == null ? "0" : partner.getStatus());
        partner.setCreateBy(SecurityUtils.getUsername());
        return partnerMapper.insertPartner(partner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updatePartner(FinPartner partner)
    {
        FinPartner exist = partnerMapper.selectPartnerById(partner.getPartnerId());
        if (exist == null)
        {
            throw new ServiceException("往来单位不存在或已删除");
        }
        if (partnerMapper.checkNameUnique(partner.getPartnerName(),
                partner.getPartnerType(), partner.getPartnerId()) > 0)
        {
            throw new ServiceException("同类型下已存在同名往来单位：" + partner.getPartnerName());
        }
        partner.setUpdateBy(SecurityUtils.getUsername());
        return partnerMapper.updatePartner(partner);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deletePartnerByIds(Long[] partnerIds)
    {
        if (partnerIds == null || partnerIds.length == 0)
        {
            return 0;
        }
        for (Long id : partnerIds)
        {
            if (partnerMapper.countVoucherUsage(id) > 0)
            {
                FinPartner p = partnerMapper.selectPartnerById(id);
                String name = p == null ? String.valueOf(id) : p.getPartnerName();
                throw new ServiceException("往来单位「" + name + "」已有凭证引用，不能删除；如不再使用请改为停用");
            }
        }
        return partnerMapper.deletePartnerByIds(partnerIds);
    }
}