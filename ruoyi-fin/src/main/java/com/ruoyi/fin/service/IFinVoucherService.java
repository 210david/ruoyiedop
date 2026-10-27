package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.dto.VoucherDTO;

import java.util.List;
import java.util.Map;

/**
 * 记账凭证 Service 接口
 *
 * @author ruoyi
 */
public interface IFinVoucherService
{
    /**
     * 查询凭证列表
     *
     * @param voucher 查询条件
     * @return 凭证列表
     */
    List<FinVoucher> selectVoucherList(FinVoucher voucher);

    /**
     * 查询凭证详情（含分录，已回填借贷合计）
     *
     * @param voucherId 凭证ID
     * @return 凭证聚合体
     */
    VoucherDTO selectVoucherDetail(Long voucherId);

    /**
     * 新增凭证（手工录入）
     *
     * <p>借贷必平、科目须为明细、期间须未结账，三项均不满足则拒绝。</p>
     *
     * @param dto 凭证聚合体
     * @return 新增的凭证ID
     */
    Long insertVoucher(VoucherDTO dto);

    /**
     * 修改凭证（仅草稿状态可改）
     *
     * @param dto 凭证聚合体
     * @return 影响行数
     */
    int updateVoucher(VoucherDTO dto);

    /**
     * 删除凭证（仅草稿状态可删）
     *
     * @param voucherIds 凭证ID数组
     * @return 影响行数
     */
    int deleteVoucherByIds(Long[] voucherIds);

    /**
     * 审核凭证（草稿 → 已审核）
     *
     * @param voucherId    凭证ID
     * @param auditOpinion 审核意见
     * @return 影响行数
     */
    int auditVoucher(Long voucherId, String auditOpinion);

    /**
     * 反审核（已审核 → 草稿）
     *
     * @param voucherId 凭证ID
     * @return 影响行数
     */
    int cancelAudit(Long voucherId);

    /**
     * 试算平衡表
     *
     * @param period 会计期间，空则查全部
     * @return 汇总结果，含 totalDebit/totalCredit/diff/balanced
     */
    Map<String, Object> trialBalance(String period);

    /**
     * 科目余额表
     *
     * @param period 会计期间
     * @return 余额列表
     */
    List<Map<String, Object>> subjectBalance(String period);
    /**
     * 红字冲销：对已审核凭证生成负数冲销凭证（草稿，须人工审核）。
     *
     * <p>同科目同方向、金额取负——不虚增发生额，符合红字冲销会计惯例。
     * 每张凭证只能被冲销一次（uk_source 唯一索引兜底）。</p>
     *
     * @param voucherId 原凭证ID
     * @return 冲销凭证ID
     */
    Long redFlush(Long voucherId);
}