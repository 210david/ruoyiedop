package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinBankStatement;
import com.ruoyi.fin.domain.FinBankStatementItem;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 银行对账单 Mapper
 *
 * @author ruoyi
 */
public interface FinBankStatementMapper
{
    /**
     * 查询对账单列表
     *
     * @param statement 查询条件
     * @return 列表
     */
    List<FinBankStatement> selectStatementList(FinBankStatement statement);

    /**
     * 按ID查询
     *
     * @param statementId 对账单ID
     * @return 对账单
     */
    FinBankStatement selectStatementById(Long statementId);

    /**
     * 同账户同期是否已有对账单（幂等）
     *
     * @param accountId     账户ID
     * @param statementDate 对账日期
     * @return 对账单ID，不存在返回 null
     */
    FinBankStatement selectByAccountDate(@Param("accountId") Long accountId,
                                         @Param("statementDate") Date statementDate);

    /**
     * 新增对账单
     *
     * @param statement 对账单
     * @return 影响行数
     */
    int insertStatement(FinBankStatement statement);

    /**
     * 更新对账结果（余额与状态）
     *
     * @param statement 对账单
     * @return 影响行数
     */
    int updateStatement(FinBankStatement statement);

    /**
     * 逻辑删除对账单
     *
     * @param statementIds 对账单ID数组
     * @return 影响行数
     */
    int deleteStatementByIds(Long[] statementIds);

    // ==================== 明细 ====================

    /**
     * 批量新增对账明细
     *
     * @param items 明细列表
     * @return 影响行数
     */
    int insertItems(List<FinBankStatementItem> items);

    /**
     * 查询对账明细
     *
     * @param statementId 对账单ID
     * @param matchStatus 匹配状态（空查全部）
     * @return 明细列表
     */
    List<FinBankStatementItem> selectItems(@Param("statementId") Long statementId,
                                           @Param("matchStatus") String matchStatus);

    /**
     * 逻辑删除对账明细
     *
     * @param statementId 对账单ID
     * @return 影响行数
     */
    int deleteItemsByStatement(Long statementId);

    /**
     * 标记明细已匹配
     *
     * @param itemId 明细ID
     * @param flowId 账面流水ID
     * @return 影响行数
     */
    int markMatched(@Param("itemId") Long itemId, @Param("flowId") Long flowId);

    /**
     * 解除匹配
     *
     * @param itemId 明细ID
     * @return 影响行数
     */
    int unmarkMatch(@Param("itemId") Long itemId);

    // ==================== 勾稽核对 ====================

    /**
     * 银行流水合计（按对账单汇总）
     *
     * @param statementId 对账单ID
     * @return 含 totalAmount（收入-支出净额）、inAmount、outAmount、count
     */
    Map<String, Object> sumBankFlow(@Param("statementId") Long statementId);

    /**
     * 账面资金流水合计（同期同账户）
     *
     * @param accountId 账户ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 含 inAmount、outAmount、netAmount、count
     */
    Map<String, Object> sumBookFlow(@Param("accountId") Long accountId,
                                    @Param("beginDate") Date beginDate,
                                    @Param("endDate") Date endDate);

    /**
     * 候选匹配：同期同账户且金额绝对值一致的账面流水。
     *
     * <p>自动匹配策略：金额 + 日期接近 + 对方单位摘要相似 三条件，
     * 金额必须严格相等（避免误配）。</p>
     *
     * @param accountId 账户ID
     * @param amount    金额（带符号）
     * @param date      交易日期
     * @return 候选流水列表
     */
    List<Map<String, Object>> selectMatchCandidates(@Param("accountId") Long accountId,
                                                    @Param("amount") BigDecimal amount,
                                                    @Param("date") Date date);

    /**
     * 未匹配的对账明细（未达账项）
     *
     * @param statementId 对账单ID
     * @return 明细列表
     */
    List<FinBankStatementItem> selectUnmatched(@Param("statementId") Long statementId);

    /**
     * 账面有但银行无的流水（另一类未达账项）
     *
     * @param accountId 账户ID
     * @param beginDate 开始日期
     * @param endDate   结束日期
     * @return 账面流水列表
     */
    List<Map<String, Object>> selectBookOnlyFlow(@Param("accountId") Long accountId,
                                                 @Param("beginDate") Date beginDate,
                                                 @Param("endDate") Date endDate);

    /**
     * 对账统计
     *
     * @param statementId 对账单ID
     * @return 含 total/matched/unmatched
     */
    Map<String, Object> matchStat(@Param("statementId") Long statementId);
}