package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkRebatePolicy;
import com.ruoyi.mk.domain.MkRebateSettle;

/**
 * 返利政策/结算 Service接口
 *
 * @author ruoyi
 */
public interface IMkRebateService
{
    /** 政策列表（阶梯组按政策编号聚合展示，tiers 填充在扩展字段） */
    public List<Map<String, Object>> selectPolicyGroupList(MkRebatePolicy policy);

    /** 新增政策（含阶梯组，一次一个 policyNo 多行） */
    public int insertPolicyGroup(List<MkRebatePolicy> tiers);

    /** 修改政策（整组替换阶梯） */
    public int updatePolicyGroup(List<MkRebatePolicy> tiers);

    /** 删除政策（整组） */
    public int deletePolicyGroup(String policyNo);

    public List<MkRebateSettle> selectSettleList(MkRebateSettle settle);

    /** 返利核算：按客户+期间(yyyyMM)命中启用政策阶梯生成结算单 */
    public List<MkRebateSettle> settle(Long customerId, String period);

    /** 返利兑现 */
    public int redeem(Long settleId, String redeemType);
}
