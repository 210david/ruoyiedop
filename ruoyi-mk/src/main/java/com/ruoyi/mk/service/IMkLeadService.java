package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkLead;

/**
 * 线索 Service接口
 *
 * @author ruoyi
 */
public interface IMkLeadService
{
    public List<MkLead> selectLeadList(MkLead lead);
    public MkLead selectLeadById(Long leadId);
    public int insertLead(MkLead lead);
    public int updateLead(MkLead lead);
    public int deleteLeadByIds(Long[] leadIds);
    /** 领取线索 */
    public int receiveLead(MkLead lead);
    /** 分配线索 */
    public int assignLead(MkLead lead);
    /** 审批通过领取申请 */
    public int approveReceive(MkLead lead);
    /** 拒绝领取申请 */
    public int rejectReceive(MkLead lead);
    /** 线索转化为客户 */
    public int convertLead(MkLead lead);

    // ====== P1: 批量操作 + 退回公海 + 导入 ======
    /** 批量分配线索 */
    public int batchAssign(Long[] leadIds, Long userId, Long deptId, String userName, String deptName);
    /** 批量变更线索状态 */
    public int batchUpdateStatus(Long[] leadIds, String leadStatus);
    /** 释放线索到公海 */
    public int releaseToPool(Long leadId);
    /** 导入线索 */
    public String importLead(List<MkLead> leadList, Boolean isUpdateSupport, String operName);

    // ====== P2: 无效线索 + 跟进时间 ======
    /** 标记线索无效 */
    public int invalidateLead(MkLead lead);
    /** 更新最后跟进时间 */
    public int updateLastFollowTime(Long leadId, java.util.Date followTime);

    // ====== P3: 查重 + 自动评分 ======
    /** 线索查重 */
    public List<MkLead> checkDuplicate(String companyName, String contactPhone);
    /** 自动评分 */
    public void autoScoreLead(MkLead lead);
}
