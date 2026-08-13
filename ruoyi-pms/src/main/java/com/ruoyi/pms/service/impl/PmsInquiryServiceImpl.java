package com.ruoyi.pms.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.util.List;
import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.domain.PmsInquiryDetail;
import com.ruoyi.pms.domain.PmsInquiryAuditLog;
import com.ruoyi.pms.domain.PmsQuotation;
import com.ruoyi.pms.domain.PmsQuotationDetail;
import com.ruoyi.pms.mapper.PmsInquiryMapper;
import com.ruoyi.pms.mapper.PmsInquiryAuditLogMapper;
import com.ruoyi.pms.mapper.PmsQuotationMapper;
import com.ruoyi.pms.service.IPmsInquiryService;

/**
 * 询价单Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class PmsInquiryServiceImpl implements IPmsInquiryService
{
    @Autowired
    private PmsInquiryMapper pmsInquiryMapper;

    @Autowired
    private PmsInquiryAuditLogMapper pmsInquiryAuditLogMapper;

    @Autowired
    private PmsQuotationMapper pmsQuotationMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public PmsInquiry selectPmsInquiryById(Long inquiryId)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry != null)
        {
            inquiry.setAuditLogList(pmsInquiryAuditLogMapper.selectAuditLogByInquiryId(inquiryId));
            inquiry.setQuotationList(pmsQuotationMapper.selectQuotationByInquiryId(inquiryId));
        }
        return inquiry;
    }

    @Override
    public List<PmsInquiry> selectPmsInquiryList(PmsInquiry pmsInquiry)
    {
        return pmsInquiryMapper.selectPmsInquiryList(pmsInquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int insertPmsInquiry(PmsInquiry pmsInquiry)
    {
        // 自动生成询价单号
        if (StringUtils.isEmpty(pmsInquiry.getInquiryNo()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(pmsInquiry.getInquiryType())) { params.put("inquiryType", pmsInquiry.getInquiryType()); }
        pmsInquiry.setInquiryNo(mkNumberRuleService.generateNumber("PMS_INQUIRY", params));
        }
        // 自动计算预计金额 = 数量 × 最高限价
        List<PmsInquiryDetail> details = pmsInquiry.getDetailList();
        if (details != null && !details.isEmpty())
        {
            java.math.BigDecimal estimate = java.math.BigDecimal.ZERO;
            for (PmsInquiryDetail detail : details)
            {
                java.math.BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal price = detail.getMaxPrice() != null ? detail.getMaxPrice() : java.math.BigDecimal.ZERO;
                estimate = estimate.add(qty.multiply(price));
            }
            pmsInquiry.setEstimateAmount(estimate);
        }
        pmsInquiry.setCreateTime(DateUtils.getNowDate());
        pmsInquiry.setCreateBy(SecurityUtils.getUsername());
        pmsInquiry.setStatus("0"); // 草稿
        pmsInquiry.setDelFlag("0");
        int rows = pmsInquiryMapper.insertPmsInquiry(pmsInquiry);
        insertPmsInquiryDetail(pmsInquiry);
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int updatePmsInquiry(PmsInquiry pmsInquiry)
    {
        // 驳回状态修改后重置为待审核，清空审核信息（审核日志保留）
        PmsInquiry existing = pmsInquiryMapper.selectPmsInquiryById(pmsInquiry.getInquiryId());
        if (existing != null && "6".equals(existing.getStatus()))
        {
            pmsInquiry.setStatus("5");
            pmsInquiry.setAuditBy(null);
            pmsInquiry.setAuditTime(null);
            pmsInquiry.setAuditOpinion(null);
        }
        // 自动计算预计金额 = 数量 × 最高限价
        List<PmsInquiryDetail> details = pmsInquiry.getDetailList();
        if (details != null && !details.isEmpty())
        {
            java.math.BigDecimal estimate = java.math.BigDecimal.ZERO;
            for (PmsInquiryDetail detail : details)
            {
                java.math.BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal price = detail.getMaxPrice() != null ? detail.getMaxPrice() : java.math.BigDecimal.ZERO;
                estimate = estimate.add(qty.multiply(price));
            }
            pmsInquiry.setEstimateAmount(estimate);
        }
        pmsInquiry.setUpdateTime(DateUtils.getNowDate());
        pmsInquiry.setUpdateBy(SecurityUtils.getUsername());
        pmsInquiryMapper.deletePmsInquiryDetailByInquiryId(pmsInquiry.getInquiryId());
        insertPmsInquiryDetail(pmsInquiry);
        return pmsInquiryMapper.updatePmsInquiry(pmsInquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePmsInquiryByIds(Long[] inquiryIds)
    {
        pmsInquiryMapper.deletePmsInquiryDetailByInquiryIds(inquiryIds);
        return pmsInquiryMapper.deletePmsInquiryByIds(inquiryIds);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deletePmsInquiryById(Long inquiryId)
    {
        pmsInquiryMapper.deletePmsInquiryDetailByInquiryId(inquiryId);
        return pmsInquiryMapper.deletePmsInquiryById(inquiryId);
    }

    private void insertPmsInquiryDetail(PmsInquiry pmsInquiry)
    {
        List<PmsInquiryDetail> detailList = pmsInquiry.getDetailList();
        if (detailList != null && !detailList.isEmpty()) {
            Long inquiryId = pmsInquiry.getInquiryId();
            for (PmsInquiryDetail detail : detailList) {
                detail.setInquiryId(inquiryId);
                detail.setDelFlag("0");
            }
            pmsInquiryMapper.batchPmsInquiryDetail(detailList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int publishInquiry(Long inquiryId)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"0".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有草稿状态的询价单才能发布");
        }
        inquiry.setStatus("1"); // 询价中
        inquiry.setUpdateTime(DateUtils.getNowDate());
        inquiry.setUpdateBy(SecurityUtils.getUsername());
        return pmsInquiryMapper.updatePmsInquiry(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int closeInquiry(Long inquiryId)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"1".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有询价中状态的询价单才能截止");
        }
        inquiry.setStatus("2"); // 已截止
        inquiry.setUpdateTime(DateUtils.getNowDate());
        inquiry.setUpdateBy(SecurityUtils.getUsername());
        return pmsInquiryMapper.updatePmsInquiry(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int compareInquiry(Long inquiryId)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"2".equals(inquiry.getStatus()) && !"1".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有已截止或询价中状态的询价单才能比价");
        }
        // 校验是否有报价
        List<PmsQuotation> quotations = pmsQuotationMapper.selectQuotationByInquiryId(inquiryId);
        if (quotations == null || quotations.isEmpty())
        {
            throw new ServiceException("暂无供应商报价，无法比价");
        }
        inquiry.setStatus("3"); // 已比价
        inquiry.setUpdateTime(DateUtils.getNowDate());
        inquiry.setUpdateBy(SecurityUtils.getUsername());
        return pmsInquiryMapper.updatePmsInquiry(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int awardInquiry(Long inquiryId, Long supplierId, String supplierName, java.math.BigDecimal amount)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"3".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有已比价状态的询价单才能定标");
        }
        inquiry.setStatus("4"); // 已定标
        inquiry.setAwardSupplierId(supplierId);
        inquiry.setAwardSupplierName(supplierName);
        inquiry.setAwardAmount(amount);
        inquiry.setUpdateTime(DateUtils.getNowDate());
        inquiry.setUpdateBy(SecurityUtils.getUsername());
        return pmsInquiryMapper.updatePmsInquiry(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int submitInquiry(Long inquiryId)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"0".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有草稿状态的询价单才能提交审核");
        }
        List<PmsInquiryDetail> details = pmsInquiryMapper.selectPmsInquiryDetailByInquiryId(inquiryId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("询价明细不能为空，无法提交");
        }
        inquiry.setStatus("5"); // 待审核
        inquiry.setUpdateTime(DateUtils.getNowDate());
        inquiry.setUpdateBy(SecurityUtils.getUsername());
        return pmsInquiryMapper.updatePmsInquiry(inquiry);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int auditInquiry(Long inquiryId, String status, String auditOpinion)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(inquiryId);
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if (!"5".equals(inquiry.getStatus()))
        {
            throw new ServiceException("只有待审核状态的询价单才能审核");
        }
        inquiry.setStatus(status);
        inquiry.setAuditBy(SecurityUtils.getUsername());
        inquiry.setAuditTime(new Date());
        inquiry.setAuditOpinion(auditOpinion);
        int rows = pmsInquiryMapper.updatePmsInquiry(inquiry);

        // 写入审核日志（永久留存）
        PmsInquiryAuditLog auditLog = new PmsInquiryAuditLog();
        auditLog.setInquiryId(inquiryId);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditRemark(auditOpinion);
        // auditAction: 1=通过 2=驳回
        if ("1".equals(status))
        {
            auditLog.setAuditAction("1");
        }
        else
        {
            auditLog.setAuditAction("2");
        }
        pmsInquiryAuditLogMapper.insertAuditLog(auditLog);

        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int saveQuotation(PmsQuotation quotation)
    {
        PmsInquiry inquiry = pmsInquiryMapper.selectPmsInquiryById(quotation.getInquiryId());
        if (inquiry == null)
        {
            throw new ServiceException("询价单不存在");
        }
        if ("4".equals(inquiry.getStatus()))
        {
            throw new ServiceException("已定标的询价单不能录入报价");
        }
        // 校验同一供应商同一询价只能报一次价
        if (quotation.getSupplierId() != null)
        {
            int count = pmsQuotationMapper.countByInquiryIdAndSupplierId(quotation.getInquiryId(), quotation.getSupplierId());
            if (count > 0)
            {
                throw new ServiceException("该供应商已对此询价单报过价，不能重复报价");
            }
        }
        quotation.setStatus("1"); // 已报价
        quotation.setDelFlag("0");
        quotation.setCreateBy(SecurityUtils.getUsername());
        quotation.setCreateTime(DateUtils.getNowDate());

        // 计算报价总金额
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        List<PmsQuotationDetail> detailList = quotation.getDetailList();
        if (detailList != null && !detailList.isEmpty())
        {
            for (PmsQuotationDetail detail : detailList)
            {
                java.math.BigDecimal qty = detail.getQuantity() != null ? detail.getQuantity() : java.math.BigDecimal.ZERO;
                java.math.BigDecimal price = detail.getPrice() != null ? detail.getPrice() : java.math.BigDecimal.ZERO;
                detail.setAmount(qty.multiply(price));
                total = total.add(detail.getAmount());
                detail.setDelFlag("0");
            }
        }
        if (quotation.getTotalAmount() == null || quotation.getTotalAmount().compareTo(java.math.BigDecimal.ZERO) == 0)
        {
            quotation.setTotalAmount(total);
        }

        int rows = pmsQuotationMapper.insertQuotation(quotation);
        if (detailList != null && !detailList.isEmpty())
        {
            for (PmsQuotationDetail detail : detailList)
            {
                detail.setQuotationId(quotation.getQuotationId());
            }
            pmsQuotationMapper.batchQuotationDetail(detailList);
        }
        // 录入报价后，如果当前状态为"询价中"或"已截止"，自动变为"比价中"
        if ("1".equals(inquiry.getStatus()) || "2".equals(inquiry.getStatus()))
        {
            inquiry.setStatus("3"); // 比价中
            inquiry.setUpdateTime(DateUtils.getNowDate());
            inquiry.setUpdateBy(SecurityUtils.getUsername());
            pmsInquiryMapper.updatePmsInquiry(inquiry);
        }
        return rows;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public int deleteQuotation(Long quotationId)
    {
        pmsQuotationMapper.deleteQuotationDetailByQuotationId(quotationId);
        return pmsQuotationMapper.deleteQuotationById(quotationId);
    }
}
