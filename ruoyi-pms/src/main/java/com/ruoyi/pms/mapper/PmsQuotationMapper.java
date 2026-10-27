package com.ruoyi.pms.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.pms.domain.PmsQuotation;
import com.ruoyi.pms.domain.PmsQuotationDetail;

/**
 * 供应商报价Mapper接口
 *
 * @author ruoyi
 */
public interface PmsQuotationMapper
{
    /**
     * 根据询价ID查询报价列表（含明细）
     *
     * @param inquiryId 询价ID
     * @return 报价集合
     */
    public List<PmsQuotation> selectQuotationByInquiryId(Long inquiryId);

    /**
     * 根据报价ID查询报价（含明细）
     *
     * @param quotationId 报价ID
     * @return 报价
     */
    public PmsQuotation selectQuotationById(Long quotationId);

    /**
     * 根据询价ID和供应商ID查询报价数量（校验重复报价）
     *
     * @param inquiryId 询价ID
     * @param supplierId 供应商ID
     * @return 数量
     */
    public int countByInquiryIdAndSupplierId(@Param("inquiryId") Long inquiryId, @Param("supplierId") Long supplierId);

    /**
     * 新增报价
     *
     * @param pmsQuotation 报价
     * @return 结果
     */
    public int insertQuotation(PmsQuotation pmsQuotation);

    /**
     * 批量新增报价明细
     *
     * @param detailList 报价明细列表
     * @return 结果
     */
    public int batchQuotationDetail(List<PmsQuotationDetail> detailList);

    /**
     * 删除报价（逻辑删除）
     *
     * @param quotationId 报价ID
     * @return 结果
     */
    public int deleteQuotationById(Long quotationId);

    /**
     * 删除报价明细（逻辑删除）
     *
     * @param quotationId 报价ID
     * @return 结果
     */
    public int deleteQuotationDetailByQuotationId(Long quotationId);
}
