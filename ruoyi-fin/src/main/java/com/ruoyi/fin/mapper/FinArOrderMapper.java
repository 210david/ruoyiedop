package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinArOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 应收单 Mapper
 *
 * @author ruoyi
 */
public interface FinArOrderMapper
{
    /**
     * 查询应收单列表
     *
     * @param ar 查询条件
     * @return 应收单列表
     */
    List<FinArOrder> selectArList(FinArOrder ar);

    /**
     * 按ID查询
     *
     * @param arId 应收单ID
     * @return 应收单
     */
    FinArOrder selectArById(Long arId);

    /**
     * 按来源查询（幂等回查）
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceId     来源ID
     * @return 应收单，不存在返回 null
     */
    FinArOrder selectBySource(@Param("sourceModule") String sourceModule,
                              @Param("sourceType") String sourceType,
                              @Param("sourceId") Long sourceId);

    /**
     * 新增应收单
     *
     * @param ar 应收单
     * @return 影响行数
     */
    int insertAr(FinArOrder ar);

    /**
     * 修改应收单
     *
     * @param ar 应收单
     * @return 影响行数
     */
    int updateAr(FinArOrder ar);

    /**
     * 更新已收金额并按金额自动推导状态
     *
     * <p>状态在 SQL 内推导，避免并发下状态与金额不一致。</p>
     *
     * @param arId       应收单ID
     * @param recvAmount 已收金额
     * @param updateBy   操作人
     * @return 影响行数
     */
    int updateReceivedAmount(@Param("arId") Long arId,
                             @Param("recvAmount") BigDecimal recvAmount,
                             @Param("updateBy") String updateBy);

    /**
     * 更新销项发票信息
     *
     * @param arId      应收单ID
     * @param invoiceNo 发票号码
     * @param invoiceCode 发票代码
     * @param invoiceDate 开票日期
     * @return 影响行数
     */
    int updateInvoice(@Param("arId") Long arId,
                      @Param("invoiceNo") String invoiceNo,
                      @Param("invoiceCode") String invoiceCode,
                      @Param("invoiceDate") Date invoiceDate);

    /**
     * 锁定应收单（核销时用 FOR UPDATE）
     *
     * @param arId 应收单ID
     * @return 应收单
     */
    FinArOrder selectArForUpdate(Long arId);

    /**
     * 逻辑删除（已核销不可删）
     *
     * @param arIds 应收单ID数组
     * @return 影响行数
     */
    int deleteArByIds(Long[] arIds);

    /**
     * 账龄分析（按到期日与未收金额分桶）
     *
     * @param today 今日
     * @return 账龄分桶
     */
    List<Map<String, Object>> selectAging(@Param("today") Date today);

    /**
     * 应收汇总（资产负债表应收账款取数）
     *
     * @param period 会计期间
     * @return 汇总
     */
    Map<String, Object> sumByPeriod(@Param("period") String period);

    /**
     * 逾期未收列表
     *
     * @param today 今日
     * @param limit  上限
     * @return 逾期应收单
     */
    List<FinArOrder> selectOverdue(@Param("today") Date today, @Param("limit") int limit);

    /**
     * 客户维度应收余额（坏账计提基数）
     *
     * @return 按客户汇总
     */
    List<Map<String, Object>> selectBalanceByCustomer();

    /**
     * 已计提坏账合计（避免重复计提）
     *
     * @param arId 应收单ID
     * @return 已计提金额
     */
    BigDecimal sumBadDebtByAr(@Param("arId") Long arId);
}