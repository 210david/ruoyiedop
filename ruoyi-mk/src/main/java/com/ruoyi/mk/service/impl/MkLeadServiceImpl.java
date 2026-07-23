package com.ruoyi.mk.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkContact;
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.domain.MkLead;
import com.ruoyi.mk.mapper.MkContactMapper;
import com.ruoyi.mk.mapper.MkCustomerMapper;
import com.ruoyi.mk.mapper.MkLeadMapper;
import com.ruoyi.mk.service.IMkLeadService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 线索 Service实现
 *
 * @author ruoyi
 */
@Service
public class MkLeadServiceImpl implements IMkLeadService
{
    @Autowired
    private MkLeadMapper mkLeadMapper;

    @Autowired
    private MkCustomerMapper mkCustomerMapper;

    @Autowired
    private MkContactMapper mkContactMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MkLead> selectLeadList(MkLead lead)
    {
        return mkLeadMapper.selectLeadList(lead);
    }

    @Override
    public MkLead selectLeadById(Long leadId)
    {
        return mkLeadMapper.selectLeadById(leadId);
    }

    @Override
    public int insertLead(MkLead lead)
    {
        lead.setDelFlag("0");
        if (lead.getLeadStatus() == null)
        {
            lead.setLeadStatus("0");
        }
        if (lead.getLeadGrade() == null)
        {
            lead.setLeadGrade("D");
        }
        if (lead.getIsPublic() == null)
        {
            lead.setIsPublic("1");
        }
        if (lead.getLeadScore() == null)
        {
            lead.setLeadScore(0);
        }
        // 自动生成线索编号
        if (StringUtils.isEmpty(lead.getLeadNo()))
        {
            lead.setLeadNo(mkNumberRuleService.generateNumber("lead"));
        }
        // 自动评分
        autoScoreLead(lead);
        return mkLeadMapper.insertLead(lead);
    }

    @Override
    public int updateLead(MkLead lead)
    {
        return mkLeadMapper.updateLead(lead);
    }

    @Override
    public int deleteLeadByIds(Long[] leadIds)
    {
        return mkLeadMapper.deleteLeadByIds(leadIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveLead(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if (!"1".equals(dbLead.getIsPublic()))
        {
            throw new ServiceException("非公海线索，无法领取");
        }
        if ("1".equals(dbLead.getReceiveStatus()))
        {
            throw new ServiceException("该线索已有领取申请待审批");
        }
        lead.setReceiveStatus("1");
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.receiveLead(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int approveReceive(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if (!"1".equals(dbLead.getReceiveStatus()))
        {
            throw new ServiceException("该线索无待审批的领取申请");
        }
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.approveReceive(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectReceive(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if (!"1".equals(dbLead.getReceiveStatus()))
        {
            throw new ServiceException("该线索无待审批的领取申请");
        }
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.rejectReceive(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int assignLead(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if ("4".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("已转化的线索不能分配");
        }
        if ("5".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("无效线索不能分配");
        }
        lead.setIsPublic("0");
        lead.setReceiveTime(new Date());
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.assignLead(lead);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int convertLead(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if ("4".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("线索已转化，请勿重复操作");
        }
        // 创建客户
        if (lead.getConvertCustomerId() == null)
        {
            MkCustomer customer = new MkCustomer();
            customer.setCustomerName(dbLead.getCompanyName());
            customer.setCustomerNo(mkNumberRuleService.generateNumber("customer"));
            customer.setIndustry(dbLead.getIndustry());
            customer.setCompanySize(dbLead.getCompanySize());
            customer.setAddress(dbLead.getAddress());
            customer.setCustomerLevel("4");
            customer.setCustomerSource(dbLead.getLeadSource());
            customer.setCustomerStatus("0");
            customer.setFirstContactDate(new Date());
            // 如果线索有负责人则继承，否则将当前操作人设为负责人（确保客户在正常列表可见）
            if (dbLead.getUserId() != null)
            {
                customer.setUserId(dbLead.getUserId());
                customer.setDeptId(dbLead.getDeptId());
            }
            else
            {
                customer.setUserId(SecurityUtils.getUserId());
                customer.setDeptId(SecurityUtils.getLoginUser().getDeptId());
            }
            customer.setDelFlag("0");
            customer.setCreateBy(SecurityUtils.getUsername());
            mkCustomerMapper.insertCustomer(customer);
            lead.setConvertCustomerId(customer.getCustomerId());

            // 自动创建联系人（从线索的联系人信息）
            if (StringUtils.isNotEmpty(dbLead.getContactName()))
            {
                MkContact contact = new MkContact();
                contact.setCustomerId(customer.getCustomerId());
                contact.setCustomerName(customer.getCustomerName());
                contact.setName(dbLead.getContactName());
                contact.setPhone(dbLead.getContactPhone());
                contact.setEmail(dbLead.getContactEmail());
                contact.setPosition(dbLead.getPosition());
                contact.setIsKey("1");
                contact.setIsPrimary("1");
                contact.setDelFlag("0");
                contact.setCreateBy(SecurityUtils.getUsername());
                mkContactMapper.insertContact(contact);
            }
        }
        lead.setLeadStatus("4");
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.convertLead(lead);
    }

    // ====== P1: 批量操作 + 退回公海 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAssign(Long[] leadIds, Long userId, Long deptId, String userName, String deptName)
    {
        return mkLeadMapper.batchAssignLeads(leadIds, userId, deptId, userName, deptName, SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchUpdateStatus(Long[] leadIds, String leadStatus)
    {
        return mkLeadMapper.batchUpdateStatus(leadIds, leadStatus, SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseToPool(Long leadId)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(leadId);
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if ("4".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("已转化的线索不能退回公海");
        }
        if ("5".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("无效线索不能退回公海");
        }
        if ("1".equals(dbLead.getIsPublic()))
        {
            throw new ServiceException("线索已在公海，无需重复操作");
        }
        return mkLeadMapper.releaseToPool(leadId, SecurityUtils.getUsername());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importLead(List<MkLead> leadList, Boolean isUpdateSupport, String operName)
    {
        if (StringUtils.isNull(leadList) || leadList.size() == 0)
        {
            throw new ServiceException("导入线索数据不能为空！");
        }
        int successNum = 0;
        int failureNum = 0;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder failureMsg = new StringBuilder();
        for (MkLead row : leadList)
        {
            try
            {
                if (StringUtils.isEmpty(row.getCompanyName()))
                {
                    throw new RuntimeException("企业名称不能为空");
                }
                // 检查是否已存在（按企业名称匹配）
                MkLead query = new MkLead();
                query.setCompanyName(row.getCompanyName());
                List<MkLead> existing = mkLeadMapper.selectLeadList(query);
                if (existing.size() > 0)
                {
                    if (isUpdateSupport)
                    {
                        MkLead existingLead = existing.get(0);
                        row.setLeadId(existingLead.getLeadId());
                        row.setUpdateBy(operName);
                        mkLeadMapper.updateLead(row);
                        successNum++;
                        successMsg.append("<br/>" + successNum + "、企业名称 " + row.getCompanyName() + " 更新成功");
                    }
                    else
                    {
                        failureNum++;
                        failureMsg.append("<br/>" + failureNum + "、企业名称 " + row.getCompanyName() + " 已存在");
                    }
                }
                else
                {
                    row.setDelFlag("0");
                    if (row.getLeadStatus() == null)
                    {
                        row.setLeadStatus("0");
                    }
                    if (row.getLeadGrade() == null)
                    {
                        row.setLeadGrade("D");
                    }
                    if (row.getIsPublic() == null)
                    {
                        row.setIsPublic("1");
                    }
                    if (row.getLeadScore() == null)
                    {
                        row.setLeadScore(0);
                    }
                    if (StringUtils.isEmpty(row.getLeadNo()))
                    {
                        row.setLeadNo(mkNumberRuleService.generateNumber("lead"));
                    }
                    autoScoreLead(row);
                    row.setCreateBy(operName);
                    mkLeadMapper.insertLead(row);
                    successNum++;
                    successMsg.append("<br/>" + successNum + "、企业名称 " + row.getCompanyName() + " 导入成功");
                }
            }
            catch (Exception e)
            {
                failureNum++;
                failureMsg.append("<br/>" + failureNum + "、企业名称 " + row.getCompanyName() + " 导入失败：" + e.getMessage());
            }
        }
        if (failureNum > 0)
        {
            failureMsg.insert(0, "导入失败！共 " + failureNum + " 条数据格式不正确，错误如下：");
            throw new ServiceException(failureMsg.toString());
        }
        else
        {
            successMsg.insert(0, "恭喜您，数据已全部导入成功！共 " + successNum + " 条，数据如下：");
        }
        return successMsg.toString();
    }

    // ====== P2: 无效线索 + 跟进时间 ======

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int invalidateLead(MkLead lead)
    {
        MkLead dbLead = mkLeadMapper.selectLeadById(lead.getLeadId());
        if (dbLead == null)
        {
            throw new ServiceException("线索不存在");
        }
        if ("4".equals(dbLead.getLeadStatus()))
        {
            throw new ServiceException("已转化的线索不能标记为无效");
        }
        lead.setLeadStatus("5");
        lead.setUpdateBy(SecurityUtils.getUsername());
        return mkLeadMapper.invalidateLead(lead);
    }

    @Override
    public int updateLastFollowTime(Long leadId, Date followTime)
    {
        return mkLeadMapper.updateLastFollowTime(leadId, followTime, SecurityUtils.getUsername());
    }

    // ====== P3: 查重 + 自动评分 ======

    @Override
    public List<MkLead> checkDuplicate(String companyName, String contactPhone)
    {
        return mkLeadMapper.selectDuplicateLeads(companyName, contactPhone);
    }

    @Override
    public void autoScoreLead(MkLead lead)
    {
        int score = 0;
        // 企业规模加分
        if (StringUtils.isNotEmpty(lead.getCompanySize()))
        {
            switch (lead.getCompanySize())
            {
                case "5": score += 30; break; // 1000人以上
                case "4": score += 25; break; // 500-1000人
                case "3": score += 20; break; // 100-500人
                case "2": score += 15; break; // 50-100人
                case "1": score += 10; break; // 50人以下
            }
        }
        // 线索来源加分
        if (StringUtils.isNotEmpty(lead.getLeadSource()))
        {
            switch (lead.getLeadSource())
            {
                case "1": score += 20; break; // 客户介绍
                case "2": score += 15; break; // 展会活动
                case "3": score += 10; break; // 网络营销
                case "4": score += 5; break;  // 陌生拜访
                case "5": score += 18; break; // 老客户
                case "6": score += 8; break;  // 广告
            }
        }
        // 有需求描述加分
        if (StringUtils.isNotEmpty(lead.getRequirementDesc()))
        {
            score += 15;
        }
        // 有联系人电话加分
        if (StringUtils.isNotEmpty(lead.getContactPhone()))
        {
            score += 10;
        }
        // 有邮箱加分
        if (StringUtils.isNotEmpty(lead.getContactEmail()))
        {
            score += 5;
        }
        // 手动设置的评分优先，仅在评分为0时自动计算
        if (lead.getLeadScore() == null || lead.getLeadScore() == 0)
        {
            lead.setLeadScore(score);
        }
        else
        {
            score = lead.getLeadScore();
        }
        // 自动分级
        if (StringUtils.isEmpty(lead.getLeadGrade()) || "D".equals(lead.getLeadGrade()))
        {
            if (score >= 70)
            {
                lead.setLeadGrade("A");
            }
            else if (score >= 50)
            {
                lead.setLeadGrade("B");
            }
            else if (score >= 30)
            {
                lead.setLeadGrade("C");
            }
            else
            {
                lead.setLeadGrade("D");
            }
        }
    }
}
