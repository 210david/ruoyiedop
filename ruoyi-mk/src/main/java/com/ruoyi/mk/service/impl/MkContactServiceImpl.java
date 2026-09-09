package com.ruoyi.mk.service.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
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
    public AjaxResult importContacts(List<MkContact> contactList, Boolean isUpdateSupport, String operName)
    {
        if (contactList == null || contactList.isEmpty())
        {
            return AjaxResult.error("导入数据不能为空");
        }
        int successNum = 0;
        int failureNum = 0;
        List<Map<String, Object>> successList = new ArrayList<>();
        List<Map<String, Object>> failureList = new ArrayList<>();
        for (MkContact contact : contactList)
        {
            try
            {
                // 验证必填
                if (StringUtils.isEmpty(contact.getName()))
                {
                    throw new RuntimeException("姓名不能为空");
                }
                // 字典类字段合法性校验
                validateDictFields(contact);
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
                        successList.add(buildSuccessItem(contact, "更新成功"));
                    }
                    else
                    {
                        failureNum++;
                        failureList.add(buildFailureItem(contact, "联系人已存在，未勾选更新选项"));
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
                    successList.add(buildSuccessItem(contact, "导入成功"));
                }
            }
            catch (Exception e)
            {
                failureNum++;
                String errMsg = e.getMessage();
                if (errMsg != null && errMsg.length() > 200) { errMsg = errMsg.substring(0, 200) + "..."; }
                failureList.add(buildFailureItem(contact, errMsg != null ? errMsg : "未知错误"));
            }
        }
        // 构建返回结果（结构化明细）
        AjaxResult ajax = AjaxResult.success();
        ajax.put("successNum", successNum);
        ajax.put("failureNum", failureNum);
        ajax.put("successList", successList);
        ajax.put("failureList", failureList);
        if (failureNum > 0 && successNum == 0)
        {
            ajax.put("code", 500);
            ajax.put("msg", "导入失败，共 " + failureNum + " 条数据异常");
        }
        else if (failureNum > 0)
        {
            ajax.put("msg", "部分导入成功：成功 " + successNum + " 条，失败 " + failureNum + " 条");
        }
        else
        {
            ajax.put("msg", "导入成功，共 " + successNum + " 条");
        }
        return ajax;
    }

    /**
     * 校验字典类字段：性别、关键联系人
     */
    private void validateDictFields(MkContact row)
    {
        // 性别（dictType: sys_user_sex）
        validateDictTypeField("性别", row.getGender(), "sys_user_sex");
        // 关键联系人（readConverterExp: 0=否,1=是）
        validateConverterField("关键联系人", row.getIsKey(), "0=否,1=是");
    }

    /**
     * 校验dictType字段
     */
    private void validateDictTypeField(String fieldLabel, String value, String dictType)
    {
        if (StringUtils.isNotEmpty(value))
        {
            String label = DictUtils.getDictLabel(dictType, value);
            if (StringUtils.isEmpty(label))
            {
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述");
            }
        }
    }

    /**
     * 校验readConverterExp字段
     */
    private void validateConverterField(String fieldLabel, String value, String converterExp)
    {
        if (StringUtils.isNotEmpty(value))
        {
            String label = ExcelUtil.convertByExp(value, converterExp, ",");
            if (StringUtils.isEmpty(label))
            {
                throw new RuntimeException(fieldLabel + "值不合法，请填写文字描述");
            }
        }
    }

    /**
     * 构建成功明细项
     */
    private Map<String, Object> buildSuccessItem(MkContact row, String result)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("name", row.getName() != null ? row.getName() : "");
        item.put("gender", dictText(row.getGender(), "sys_user_sex"));
        item.put("position", row.getPosition() != null ? row.getPosition() : "");
        item.put("roleTag", row.getRoleTag() != null ? row.getRoleTag() : "");
        item.put("phone", row.getPhone() != null ? row.getPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("isKey", dictText(row.getIsKey(), "0=否,1=是"));
        item.put("result", result);
        return item;
    }

    /**
     * 构建失败明细项（列与导入模板对齐）
     */
    private Map<String, Object> buildFailureItem(MkContact row, String errorReason)
    {
        Map<String, Object> item = new LinkedHashMap<>();
        item.put("customerName", row.getCustomerName() != null ? row.getCustomerName() : "");
        item.put("name", row.getName() != null ? row.getName() : "");
        item.put("gender", dictText(row.getGender(), "sys_user_sex"));
        item.put("position", row.getPosition() != null ? row.getPosition() : "");
        item.put("roleTag", row.getRoleTag() != null ? row.getRoleTag() : "");
        item.put("phone", row.getPhone() != null ? row.getPhone() : "");
        item.put("email", row.getEmail() != null ? row.getEmail() : "");
        item.put("isKey", dictText(row.getIsKey(), "0=否,1=是"));
        item.put("errorReason", errorReason);
        return item;
    }

    /**
     * 将编码转换为文字标签（用于明细展示）
     */
    private String dictText(String code, String converterExp)
    {
        if (StringUtils.isEmpty(code)) { return ""; }
        String label = ExcelUtil.convertByExp(code, converterExp, ",");
        return StringUtils.isNotEmpty(label) ? label : code;
    }
}
