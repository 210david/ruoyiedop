package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkPaymentRecord;

/**
 * 回款记录 Mapper
 *
 * @author ruoyi
 */
public interface MkPaymentRecordMapper
{
    List<MkPaymentRecord> selectPaymentRecordList(MkPaymentRecord record);

    List<MkPaymentRecord> selectPaymentRecordByPlanId(Long planId);

    MkPaymentRecord selectPaymentRecordById(Long recordId);

    int insertPaymentRecord(MkPaymentRecord record);

    int updatePaymentRecord(MkPaymentRecord record);

    int deletePaymentRecordByIds(Long[] recordIds);

    int deletePaymentRecordByPlanId(Long planId);

    /** 查询某计划的已确认回款总额 */
    java.math.BigDecimal selectConfirmedAmountByPlanId(Long planId);
}
