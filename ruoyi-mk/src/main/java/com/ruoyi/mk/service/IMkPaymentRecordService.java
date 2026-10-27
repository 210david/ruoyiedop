package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkPaymentRecord;

/**
 * 回款记录 Service接口
 *
 * @author ruoyi
 */
public interface IMkPaymentRecordService
{
    List<MkPaymentRecord> selectPaymentRecordList(MkPaymentRecord record);

    List<MkPaymentRecord> selectPaymentRecordByPlanId(Long planId);

    MkPaymentRecord selectPaymentRecordById(Long recordId);

    int insertPaymentRecord(MkPaymentRecord record);

    int updatePaymentRecord(MkPaymentRecord record);

    int deletePaymentRecordByIds(Long[] recordIds);

    /** 确认回款记录 */
    int confirmPaymentRecord(Long recordId, String confirmStatus, String remark);
}
