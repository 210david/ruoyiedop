package com.ruoyi.mk.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkContact;
import com.ruoyi.mk.mapper.MkContactMapper;
import com.ruoyi.mk.service.IMkContactService;

/**
 * 联系人 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkContactServiceImpl implements IMkContactService
{
    @Autowired
    private MkContactMapper mkContactMapper;

    @Override
    public List<MkContact> selectContactList(MkContact contact)
    {
        return mkContactMapper.selectContactList(contact);
    }

    @Override
    public MkContact selectContactById(Long contactId)
    {
        return mkContactMapper.selectContactById(contactId);
    }

    @Override
    public int insertContact(MkContact contact)
    {
        contact.setDelFlag("0");
        if (contact.getIsKey() == null)
        {
            contact.setIsKey("0");
        }
        if (contact.getIsPrimary() == null)
        {
            contact.setIsPrimary("0");
        }
        // P1-7: 如果设为主要联系人，清除同客户下其他主要联系人
        if ("1".equals(contact.getIsPrimary()) && contact.getCustomerId() != null)
        {
            mkContactMapper.clearOtherPrimary(contact.getCustomerId(), 0L);
        }
        return mkContactMapper.insertContact(contact);
    }

    @Override
    public int updateContact(MkContact contact)
    {
        // P1-7: 如果设为主要联系人，清除同客户下其他主要联系人
        if ("1".equals(contact.getIsPrimary()) && contact.getCustomerId() != null)
        {
            mkContactMapper.clearOtherPrimary(contact.getCustomerId(), contact.getContactId());
        }
        return mkContactMapper.updateContact(contact);
    }

    @Override
    public int deleteContactByIds(Long[] contactIds)
    {
        return mkContactMapper.deleteContactByIds(contactIds);
    }

    /** P1-7: 设为主要联系人 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int setPrimary(Long contactId)
    {
        MkContact contact = mkContactMapper.selectContactById(contactId);
        if (contact == null)
        {
            throw new ServiceException("联系人不存在");
        }
        if (contact.getCustomerId() == null)
        {
            throw new ServiceException("该联系人未关联客户，无法设为主要联系人");
        }
        // 清除同客户下其他主要联系人
        mkContactMapper.clearOtherPrimary(contact.getCustomerId(), contactId);
        // 设为主要联系人
        contact.setIsPrimary("1");
        contact.setUpdateBy(SecurityUtils.getUsername());
        return mkContactMapper.updateContact(contact);
    }

    /** P1-7: 批量设置关键联系人 */
    @Override
    public int batchSetKey(Long[] contactIds, String isKey)
    {
        return mkContactMapper.batchSetKey(contactIds, isKey, SecurityUtils.getUsername());
    }

    /** P2-8: 检查重复联系人 */
    @Override
    public List<MkContact> checkDuplicate(String phone, String email)
    {
        if (StringUtils.isEmpty(phone) && StringUtils.isEmpty(email))
        {
            throw new ServiceException("请至少输入手机号或邮箱");
        }
        return mkContactMapper.selectDuplicateContacts(phone, email);
    }

    /** P2-8: 合并联系人 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int mergeContacts(Long masterId, Long slaveId)
    {
        if (masterId.equals(slaveId))
        {
            throw new ServiceException("不能与自身合并");
        }
        MkContact master = mkContactMapper.selectContactById(masterId);
        MkContact slave = mkContactMapper.selectContactById(slaveId);
        if (master == null || slave == null)
        {
            throw new ServiceException("联系人不存在");
        }
        // 将从记录的互动记录转移到主记录
        mkContactMapper.mergeInteractionsToMaster(masterId, slaveId);
        // 将从记录的活动参与记录转移到主记录
        mkContactMapper.mergeParticipantsToMaster(masterId, slaveId);
        // 软删除从记录
        slave.setDelFlag("2");
        slave.setUpdateBy(SecurityUtils.getUsername());
        mkContactMapper.updateContact(slave);
        return 1;
    }

    /** P2-10: 分配/转移联系人 */
    @Override
    public int assignContact(Long contactId, Long ownerUserId)
    {
        return mkContactMapper.assignContact(contactId, ownerUserId, SecurityUtils.getUsername());
    }

    /** P2-10: 释放到公海 */
    @Override
    public int releaseToPool(Long contactId)
    {
        return mkContactMapper.assignContact(contactId, null, SecurityUtils.getUsername());
    }

    /** P3-14: 查询待跟进联系人 */
    @Override
    public List<MkContact> selectFollowUpReminders(Long userId)
    {
        return mkContactMapper.selectFollowUpReminders(userId);
    }

    /** P3-13: 批量导入联系人 */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importContacts(List<MkContact> contactList, Boolean isUpdateSupport, String operName)
    {
        if (contactList == null || contactList.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MkContact contact : contactList)
        {
            try
            {
                // 验证必填
                if (StringUtils.isEmpty(contact.getName()))
                {
                    throw new ServiceException("姓名不能为空");
                }
                // 检查重复
                List<MkContact> duplicates = mkContactMapper.selectDuplicateContacts(contact.getPhone(), contact.getEmail());
                if (!duplicates.isEmpty())
                {
                    if (isUpdateSupport)
                    {
                        MkContact existing = duplicates.get(0);
                        contact.setContactId(existing.getContactId());
                        contact.setUpdateBy(operName);
                        mkContactMapper.updateContact(contact);
                        successNum++;
                        successMsg.append("<br/>").append(successNum).append("、姓名 ").append(contact.getName()).append(" 更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>").append(failureNum).append("、姓名 ").append(contact.getName()).append(" 已存在");
                    }
                }
                else
                {
                    contact.setDelFlag("0");
                    contact.setIsKey(contact.getIsKey() == null ? "0" : contact.getIsKey());
                    contact.setIsPrimary(contact.getIsPrimary() == null ? "0" : contact.getIsPrimary());
                    contact.setCreateBy(operName);
                    mkContactMapper.insertContact(contact);
                    successNum++;
                    successMsg.append("<br/>").append(successNum).append("、姓名 ").append(contact.getName()).append(" 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>").append(failureNum).append("、姓名 ").append(contact.getName()).append(" 导入失败：").append(e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        return successMsg.toString();
    }
}
