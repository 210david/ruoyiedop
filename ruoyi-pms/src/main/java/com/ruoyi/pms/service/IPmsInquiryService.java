package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.domain.PmsQuotation;

/**
 * 询价单Service接口
 *
 * @author ruoyi
 */
public interface IPmsInquiryService
{
    /**
     * 查询询价单
     */
    public PmsInquiry selectPmsInquiryById(Long inquiryId);

    /**
     * 查询询价单列表
     */
    public List<PmsInquiry> selectPmsInquiryList(PmsInquiry pmsInquiry);

    /**
     * 新增询价单
     */
    public int insertPmsInquiry(PmsInquiry pmsInquiry);

    /**
     * 修改询价单
     */
    public int updatePmsInquiry(PmsInquiry pmsInquiry);

    /**
     * 批量删除询价单
     */
    public int deletePmsInquiryByIds(Long[] inquiryIds);

    /**
     * 删除询价单信息
     */
    public int deletePmsInquiryById(Long inquiryId);

    /**
     * 发布询价单
     */
    public int publishInquiry(Long inquiryId);

    /**
     * 截止询价单
     */
    public int closeInquiry(Long inquiryId);

    /**
     * 比价（进入比价定标环节）
     */
    public int compareInquiry(Long inquiryId);

    /**
     * 定标
     */
    public int awardInquiry(Long inquiryId, Long supplierId, String supplierName, java.math.BigDecimal amount);

    /**
     * 提交审核
     */
    public int submitInquiry(Long inquiryId);

    /**
     * 审核询价单
     */
    public int auditInquiry(Long inquiryId, String status, String auditOpinion);

    /**
     * 保存供应商报价（含明细）
     */
    public int saveQuotation(PmsQuotation quotation);

    /**
     * 删除供应商报价
     */
    public int deleteQuotation(Long quotationId);
}
