package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinApOrder;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 应付单 Mapper
 *
 * @author ruoyi
 */
public interface FinApOrderMapper
{
    /**
     * 查询应付单列表
     *
     * @param ap 查询条件
     * @return 应付单列表
     */
    List<FinApOrder> selectApList(FinApOrder ap);

    /**
     * 按ID查询
     *
     * @param apId 应付单ID
     * @return 应付单
     */
    FinApOrder selectApById(Long apId);

    /**
     * 按来源查询（幂等回查）
     *
     * @param sourceModule 来源模块
     * @param sourceType   来源类型
     * @param sourceId     来源ID
     * @return 应付单，不存在返回 null
     */
    FinApOrder selectBySource(@Param("sourceModule") String sourceModule,
                              @Param("sourceType") String sourceType,
                              @Param("sourceId") Long sourceId);

    /**
     * 新增应付单
     *
     * @param ap 应付单
     * @return 影响行数
     */
    int insertAp(FinApOrder ap);

    /**
     * 修改应付单
     *
     * @param ap 应付单
     * @return 影响行数
     */
    int updateAp(FinApOrder ap);

    /**
     * 更新已付金额并按金额自动推导状态
     *
     * <p>状态规则：未付=OPEN，部分付=PARTIAL，付清=SETTLED。</p>
     *
     * @param apId        应付单ID
     * @param paidAmount  已付金额
     * @param updateBy    操作人
     * @return 影响行数
     */
    int updatePaidAmount(@Param("apId") Long apId,
                         @Param("paidAmount") BigDecimal paidAmount,
                         @Param("updateBy") String updateBy);

    /**
     * 锁定应付单（核销时用 FOR UPDATE）
     *
     * @param apId 应付单ID
     * @return 应付单
     */
    FinApOrder selectApForUpdate(Long apId);

    /**
     * 逻辑删除
     *
     * @param apIds 应付单ID数组
     * @return 影响行数
     */
    int deleteApByIds(Long[] apIds);

    /**
     * 账龄分析（按到期日与未付金额分桶）
     *
     * @param today 今日
     * @return 账龄分桶统计
     */
    List<Map<String, Object>> selectAging(@Param("today") Date today);

    /**
     * 应付汇总（总账取数）
     *
     * @param period 会计期间
     * @return 汇总
     */
    Map<String, Object> sumByPeriod(@Param("period") String period);

    /**
     * 逾期未付列表（账龄预警）
     *
     * @param today 今日
     * @param limit  上限
     * @return 逾期应付单
     */
    List<FinApOrder> selectOverdue(@Param("today") Date today, @Param("limit") int limit);
}