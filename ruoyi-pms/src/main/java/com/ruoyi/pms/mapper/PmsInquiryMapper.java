package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.domain.PmsInquiryDetail;

/**
 * 询价单Mapper接口
 *
 * @author ruoyi
 */
public interface PmsInquiryMapper
{
    /**
     * 查询询价单
     *
     * @param inquiryId 询价单ID
     * @return 询价单
     */
    public PmsInquiry selectPmsInquiryById(Long inquiryId);

    /**
     * 查询询价单列表
     *
     * @param pmsInquiry 询价单
     * @return 询价单集合
     */
    public List<PmsInquiry> selectPmsInquiryList(PmsInquiry pmsInquiry);

    /**
     * 新增询价单
     *
     * @param pmsInquiry 询价单
     * @return 结果
     */
    public int insertPmsInquiry(PmsInquiry pmsInquiry);

    /**
     * 修改询价单
     *
     * @param pmsInquiry 询价单
     * @return 结果
     */
    public int updatePmsInquiry(PmsInquiry pmsInquiry);

    /**
     * 删除询价单
     *
     * @param inquiryId 询价单ID
     * @return 结果
     */
    public int deletePmsInquiryById(Long inquiryId);

    /**
     * 批量删除询价单
     *
     * @param inquiryIds 需要删除的数据ID
     * @return 结果
     */
    public int deletePmsInquiryByIds(Long[] inquiryIds);

    /**
     * 批量删除询价明细
     *
     * @param inquiryIds 需要删除的数据ID
     * @return 结果
     */
    public int deletePmsInquiryDetailByInquiryIds(Long[] inquiryIds);

    /**
     * 查询询价明细列表
     *
     * @param inquiryId 询价单ID
     * @return 询价明细集合
     */
    public List<PmsInquiryDetail> selectPmsInquiryDetailByInquiryId(Long inquiryId);

    /**
     * 批量新增询价明细
     *
     * @param pmsInquiryDetailList 询价明细列表
     * @return 结果
     */
    public int batchPmsInquiryDetail(List<PmsInquiryDetail> pmsInquiryDetailList);

    /**
     * 删除询价明细
     *
     * @param inquiryId 询价单ID
     * @return 结果
     */
    public int deletePmsInquiryDetailByInquiryId(Long inquiryId);
}
