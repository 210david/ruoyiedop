package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinBankStatement;
import com.ruoyi.fin.domain.FinBankStatementItem;

import java.util.List;
import java.util.Map;

/**
 * 银行对账单 Service 接口
 *
 * <p><b>对账的核心逻辑</b>：</p>
 * <pre>
 *   1. 导入银行流水 → fin_bank_statement + item
 *   2. 自动匹配：金额严格相等 + 日期 ±3 天 + 同账户 → 匹配账面资金流水
 *   3. 余额核对：银行期末 = 银行期初 + Σ银行流水
 *        账面期末 = 账面期初 + Σ账面流水
 *        差异 = 银行期末 - 账面期末
 *   4. 未匹配项即为未达账项：
 *        银行有账面无（银行已扣款 / 已收款，我方未入账）
 *        账面有银行无（我方已入账，银行未清算）
 * </pre>
 *
 * @author ruoyi
 */
public interface IFinBankStatementService
{
    /**
     * 查询对账单列表
     *
     * @param statement 查询条件
     * @return 列表
     */
    List<FinBankStatement> selectStatementList(FinBankStatement statement);

    /**
     * 查询对账单详情（含明细与勾稽统计）
     *
     * @param statementId 对账单ID
     * @return 详情
     */
    Map<String, Object> selectStatementDetail(Long statementId);

    /**
     * 导入对账单（含明细）
     *
     * @param statement 对账单
     * @param items     银行流水明细
     * @return 对账单ID
     */
    Long importStatement(FinBankStatement statement, List<FinBankStatementItem> items);

    /**
     * 执行自动匹配
     *
     * @param statementId 对账单ID
     * @return 匹配统计
     */
    Map<String, Object> autoMatch(Long statementId);

    /**
     * 手动匹配（人工确认）
     *
     * @param itemId 明细ID
     * @param flowId 账面流水ID
     * @return 影响行数
     */
    int manualMatch(Long itemId, Long flowId);

    /**
     * 解除匹配
     *
     * @param itemId 明细ID
     * @return 影响行数
     */
    int unmatch(Long itemId);

    /**
     * 查询候选匹配流水
     *
     * @param accountId 账户ID
     * @param amount    金额（带符号）
     * @param date      交易日期
     * @return 候选列表
     */
    List<Map<String, Object>> matchCandidates(Long accountId,
                                              java.math.BigDecimal amount,
                                              java.util.Date date);

    /**
     * 执行余额核对
     *
     * @param statementId 对账单ID
     * @return 勾稽结果
     */
    Map<String, Object> reconcile(Long statementId);

    /**
     * 未达账项清单（双向）
     *
     * @param statementId 对账单ID
     * @return 含 bankOnly（银行有账面无）、bookOnly（账面有银行无）
     */
    Map<String, Object> unreconciled(Long statementId);

    /**
     * 删除对账单
     *
     * @param statementIds 对账单ID数组
     * @return 影响行数
     */
    int deleteStatementByIds(Long[] statementIds);
}