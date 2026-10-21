package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinVoucher;
import com.ruoyi.fin.domain.FinVoucherEntry;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 记账凭证 Mapper
 *
 * @author ruoyi
 */
public interface FinVoucherMapper
{
    /**
     * 查询凭证列表
     *
     * @param voucher 查询条件
     * @return 凭证列表
     */
    List<FinVoucher> selectVoucherList(FinVoucher voucher);

    /**
     * 按凭证ID查询主表（含明细，供详情与修改）
     *
     * @param voucherId 凭证ID
     * @return 凭证
     */
    FinVoucher selectVoucherById(Long voucherId);

    /**
     * 按凭证ID查询分录
     *
     * @param voucherId 凭证ID
     * @return 分录列表
     */
    List<FinVoucherEntry> selectEntriesByVoucherId(Long voucherId);

    /**
     * 按五元组查询凭证（幂等兜底：唯一索引冲突后回查）
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceId     来源ID
     * @param ruleKey      规则键
     * @return 凭证，不存在返回 null
     */
    FinVoucher selectBySource(@Param("sourceModule") String sourceModule,
                              @Param("sourceType") String sourceType,
                              @Param("sourceId") Long sourceId,
                              @Param("ruleKey") String ruleKey);

    /**
     * 试算平衡：按科目汇总借贷发生额
     *
     * @param period 会计期间，空则查全部
     * @return 科目汇总列表
     */
    List<Map<String, Object>> selectTrialBalance(@Param("period") String period);

    /**
     * 科目余额表：含期初、本期发生、期末
     *
     * @param period 会计期间
     * @return 余额列表
     */
    List<Map<String, Object>> selectSubjectBalance(@Param("period") String period);

    /**
     * 总借方合计（试算平衡快速校验）
     *
     * @param period 会计期间
     * @return 借方合计
     */
    BigDecimal sumDebit(@Param("period") String period);

    /**
     * 总贷方合计
     *
     * @param period 会计期间
     * @return 贷方合计
     */
    BigDecimal sumCredit(@Param("period") String period);

    /**
     * 新增凭证主表
     *
     * @param voucher 凭证
     * @return 影响行数
     */
    int insertVoucher(FinVoucher voucher);

    /**
     * 批量新增分录
     *
     * @param entries 分录列表
     * @return 影响行数
     */
    int insertEntries(List<FinVoucherEntry> entries);

    /**
     * 逻辑删除分录（按凭证ID）
 *
     * @param voucherId 凭证ID
     * @return 影响行数
     */
    int deleteEntriesByVoucherId(Long voucherId);

    /**
     * 修改凭证主表（仅草稿状态可改，状态不变更）
     *
     * <p>带 status='DRAFT' 前置条件，并发下若有他人已审核则影响行数为 0。</p>
     *
     * @param voucher 凭证
     * @return 影响行数
     */
    int updateVoucher(FinVoucher voucher);

/**
 * 逻辑删除凭证
 *
 * @param voucherIds 凭证ID数组
 * @return 影响行数
 */
int deleteVoucherByIds(Long[] voucherIds);

    /**
     * 按来源逻辑删除凭证（反结账清理结转凭证用）
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceKey    来源标识（结转损益传期间年月）
     * @return 影响行数
     */
    int deleteVoucherBySource(@Param("sourceModule") String sourceModule,
                              @Param("sourceType") String sourceType,
                              @Param("sourceKey") String sourceKey);

    /**
     * 审核凭证（状态机：草稿 → 已审核）
     *
     * <p>带状态前置条件，防并发重复审核。</p>
     *
     * @param voucherId    凭证ID
     * @param auditBy      审核人
     * @param auditOpinion 审核意见
     * @return 影响行数，0 表示状态已变更
     */
    int auditVoucher(@Param("voucherId") Long voucherId,
                     @Param("auditBy") String auditBy,
                     @Param("auditOpinion") String auditOpinion);

    /**
     * 反审核（状态机：已审核 → 草稿）
     *
     * @param voucherId 凭证ID
     * @param updateBy  操作人
     * @return 影响行数
     */
    int cancelAudit(@Param("voucherId") Long voucherId,
                    @Param("updateBy") String updateBy);

    /**
     * 锁定凭证行（审核/反审核时用 FOR UPDATE 防并发）
     *
     * @param voucherId 凭证ID
     * @return 凭证
     */
    FinVoucher selectVoucherForUpdate(@Param("voucherId") Long voucherId);

    /**
     * 统计某科目下的凭证分录数（科目删除前置校验复用）
     *
     * @param accountCode 科目编码
     * @return 分录数
     */
    int countEntryByAccount(@Param("accountCode") String accountCode);
}