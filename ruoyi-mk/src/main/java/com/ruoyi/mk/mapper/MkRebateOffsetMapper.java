package com.ruoyi.mk.mapper;

import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.mk.domain.MkRebateOffset;

/**
 * 返利冲抵台账 数据层
 *
 * @author ruoyi
 */
public interface MkRebateOffsetMapper
{
    /** 查询冲抵台账列表（关联结算单） */
    public List<MkRebateOffset> selectOffsetList(MkRebateOffset offset);

    /** 按冲抵ID查询 */
    public MkRebateOffset selectOffsetById(Long offsetId);

    /** 按结算单查冲抵明细（含全部审批状态） */
    public List<MkRebateOffset> selectOffsetListBySettleId(Long settleId);

    /** 按结算单汇总已审核通过的冲抵金额 */
    public BigDecimal selectOffsetSumBySettleId(@Param("settleId") Long settleId);

    /** 新增冲抵记录 */
    public int insertOffset(MkRebateOffset offset);

    /** 审核更新冲抵记录 */
    public int updateOffset(MkRebateOffset offset);
}
