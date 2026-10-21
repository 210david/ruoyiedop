package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContact;
import org.apache.ibatis.annotations.Param;

public interface MkContactMapper
{
    public List<MkContact> selectContactList(MkContact contact);
    public MkContact selectContactById(Long contactId);
    public int insertContact(MkContact contact);
    public int updateContact(MkContact contact);
    public int deleteContactByIds(Long[] contactIds);

    /** 按手机号/邮箱查重复联系人 (P2-8) */
    public List<MkContact> selectDuplicateContacts(@Param("phone") String phone, @Param("email") String email);

    /** 按客户ID查询疑似重复联系人 (P2-8) */
    public List<MkContact> selectContactsByCustomerForDedup(@Param("customerId") Long customerId);

    /** 合并联系人：将从记录的互动/参与人关联更新到主记录 (P2-8) */
    public int mergeInteractionsToMaster(@Param("masterId") Long masterId, @Param("slaveId") Long slaveId);
    public int mergeParticipantsToMaster(@Param("masterId") Long masterId, @Param("slaveId") Long slaveId);

    /** 清除同客户下其他联系人的主要联系人标识 (P1-7) */
    public int clearOtherPrimary(@Param("customerId") Long customerId, @Param("contactId") Long contactId);

    /** 批量设置关键联系人 (P1-7) */
    public int batchSetKey(@Param("contactIds") Long[] contactIds, @Param("isKey") String isKey, @Param("updateBy") String updateBy);

    /** 分配/转移联系人 (P2-10) */
    public int assignContact(@Param("contactId") Long contactId, @Param("ownerUserId") Long ownerUserId, @Param("updateBy") String updateBy);

    /** 查询待跟进联系人 (P3-14) */
    public List<MkContact> selectFollowUpReminders(@Param("userId") Long userId);
}
