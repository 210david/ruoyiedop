package com.ruoyi.pjm.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pjm.domain.PjmInvoice;
import com.ruoyi.pjm.mapper.PjmInvoiceMapper;
import com.ruoyi.pjm.service.IPjmInvoiceService;

/**
 * 项目开票记录Service业务层处理
 */
@Service
public class PjmInvoiceServiceImpl implements IPjmInvoiceService
{
    @Autowired
    private PjmInvoiceMapper pjmInvoiceMapper;

    @Autowired
    private IMkNumberRuleService numberRuleService;

    @Override
    public List<PjmInvoice> selectInvoiceList(PjmInvoice pjmInvoice)
    {
        return pjmInvoiceMapper.selectInvoiceList(pjmInvoice);
    }

    @Override
    public PjmInvoice selectInvoiceById(Long invoiceId)
    {
        return pjmInvoiceMapper.selectInvoiceById(invoiceId);
    }

    @Override
    public int insertInvoice(PjmInvoice pjmInvoice)
    {
        if (pjmInvoice.getInvoiceNo() == null || pjmInvoice.getInvoiceNo().isEmpty())
        {
            pjmInvoice.setInvoiceNo(numberRuleService.generateNumber("PJM_INVOICE"));
        }
        pjmInvoice.setCreateBy(SecurityUtils.getUsername());
        pjmInvoice.setCreateTime(DateUtils.getNowDate());
        return pjmInvoiceMapper.insertInvoice(pjmInvoice);
    }

    @Override
    public int updateInvoice(PjmInvoice pjmInvoice)
    {
        PjmInvoice exist = pjmInvoiceMapper.selectInvoiceById(pjmInvoice.getInvoiceId());
        if (exist == null)
        {
            throw new ServiceException("开票记录不存在");
        }
        if (!"0".equals(exist.getInvoiceStatus()))
        {
            throw new ServiceException("只有待开票状态的发票才能修改，已开票如需调整请先作废后重新登记");
        }
        pjmInvoice.setUpdateBy(SecurityUtils.getUsername());
        pjmInvoice.setUpdateTime(DateUtils.getNowDate());
        return pjmInvoiceMapper.updateInvoice(pjmInvoice);
    }

    @Override
    public int deleteInvoiceByIds(Long[] invoiceIds)
    {
        for (Long invoiceId : invoiceIds)
        {
            PjmInvoice exist = pjmInvoiceMapper.selectInvoiceById(invoiceId);
            if (exist == null)
            {
                continue;
            }
            if (!"0".equals(exist.getInvoiceStatus()))
            {
                throw new ServiceException("只有待开票状态的发票才能删除，已开票请先作废");
            }
        }
        return pjmInvoiceMapper.deleteInvoiceByIds(invoiceIds);
    }

    @Override
    public int issueInvoice(Long invoiceId)
    {
        PjmInvoice exist = pjmInvoiceMapper.selectInvoiceById(invoiceId);
        if (exist == null)
        {
            throw new ServiceException("开票记录不存在");
        }
        if (!"0".equals(exist.getInvoiceStatus()))
        {
            throw new ServiceException("只有待开票状态的发票才能执行开票确认");
        }
        PjmInvoice update = new PjmInvoice();
        update.setInvoiceId(invoiceId);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return pjmInvoiceMapper.issueInvoice(update);
    }

    @Override
    public int voidInvoice(Long invoiceId)
    {
        PjmInvoice exist = pjmInvoiceMapper.selectInvoiceById(invoiceId);
        if (exist == null)
        {
            throw new ServiceException("开票记录不存在");
        }
        if (!"1".equals(exist.getInvoiceStatus()))
        {
            throw new ServiceException("只有已开票状态的发票才能作废");
        }
        PjmInvoice update = new PjmInvoice();
        update.setInvoiceId(invoiceId);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        return pjmInvoiceMapper.voidInvoice(update);
    }
}
