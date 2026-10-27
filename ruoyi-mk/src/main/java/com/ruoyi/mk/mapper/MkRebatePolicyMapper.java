package com.ruoyi.mk.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkRebatePolicy;
import com.ruoyi.mk.domain.MkRebateSettle;

/**
 * 返利政策/结算 Mapper
 *
 * @author ruoyi
 */
public interface MkRebatePolicyMapper
{
    public List<MkRebatePolicy> selectPolicyList(MkRebatePolicy policy);

    public MkRebatePolicy selectPolicyById(Long policyId);

    public List<MkRebatePolicy> selectTiersByNo(String policyNo);

    public int insertPolicy(MkRebatePolicy policy);

    public int updatePolicy(MkRebatePolicy policy);

    public int deletePolicyByIds(Long[] policyIds);

    public int deleteTiersByNo(String policyNo);

    /** 期间已审核订单达成额（金额口径）：状态2已审核/3部分发货/4已完成，按订单创建时间归属期间 */
    public BigDecimal selectOrderAchieveAmount(@Param("customerId") Long customerId,
            @Param("beginTime") String beginTime, @Param("endTime") String endTime);

    /** 结算单是否存在（同客户+期间+政策） */
    public MkRebateSettle selectSettleExists(@Param("customerId") Long customerId,
            @Param("period") String period, @Param("policyNo") String policyNo);

    public List<MkRebateSettle> selectSettleList(MkRebateSettle settle);

    public MkRebateSettle selectSettleById(Long settleId);

    public int insertSettle(MkRebateSettle settle);

    public int updateSettle(MkRebateSettle settle);

    public int deleteSettleByIds(Long[] settleIds);
}
