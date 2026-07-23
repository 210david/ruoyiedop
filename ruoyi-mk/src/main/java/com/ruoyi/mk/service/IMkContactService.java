package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkContact;

/**
 * 联系人 Service接口
 *
 * @author ruoyi
 */
public interface IMkContactService
{
    public List<MkContact> selectContactList(MkContact contact);
    public MkContact selectContactById(Long contactId);
    public int insertContact(MkContact contact);
    public int updateContact(MkContact contact);
    public int deleteContactByIds(Long[] contactIds);

    /** P1-7: 设为主要联系人（同客户唯一） */
    public int setPrimary(Long contactId);

    /** P1-7: 批量设置关键联系人 */
    public int batchSetKey(Long[] contactIds, String isKey);

    /** P2-8: 检查重复联系人 */
    public List<MkContact> checkDuplicate(String phone, String email);

    /** P2-8: 合并联系人 */
    public int mergeContacts(Long masterId, Long slaveId);

    /** P2-10: 分配/转移联系人 */
    public int assignContact(Long contactId, Long ownerUserId);

    /** P2-10: 释放到公海 */
    public int releaseToPool(Long contactId);

    /** P3-14: 查询待跟进联系人 */
    public List<MkContact> selectFollowUpReminders(Long userId);

    /** P3-13: 批量导入联系人 */
    public String importContacts(List<MkContact> contactList, Boolean isUpdateSupport, String operName);
}
