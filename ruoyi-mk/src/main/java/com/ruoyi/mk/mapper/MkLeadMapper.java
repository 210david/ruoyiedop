package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkLead;
import org.apache.ibatis.annotations.Param;

public interface MkLeadMapper
{
    public List<MkLead> selectLeadList(MkLead lead);
    public MkLead selectLeadById(Long leadId);
    public int insertLead(MkLead lead);
    public int updateLead(MkLead lead);
    public int deleteLeadByIds(Long[] leadIds);
    /** 领取线索 */
    public int receiveLead(MkLead lead);
    /** 审批通过领取申请 */
    public int approveReceive(MkLead lead);
    /** 拒绝领取申请 */
    public int rejectReceive(MkLead lead);
    /** 直接分配线索 */
    public int assignLead(MkLead lead);
    /** 转化为客户 */
    public int convertLead(MkLead lead);

    // ====== P1: 批量操作 + 退回公海 ======
    /** 批量分配线索 */
    public int batchAssignLeads(@Param("leadIds") Long[] leadIds, @Param("userId") Long userId, @Param("deptId") Long deptId, @Param("userName") String userName, @Param("deptName") String deptName, @Param("updateBy") String updateBy);
    /** 批量变更线索状态 */
    public int batchUpdateStatus(@Param("leadIds") Long[] leadIds, @Param("leadStatus") String leadStatus, @Param("updateBy") String updateBy);
    /** 释放线索到公海 */
    public int releaseToPool(@Param("leadId") Long leadId, @Param("updateBy") String updateBy);

    // ====== P2: 无效线索 + 更新跟进时间 ======
    /** 标记线索无效 */
    public int invalidateLead(MkLead lead);
    /** 更新最后跟进时间 */
    public int updateLastFollowTime(@Param("leadId") Long leadId, @Param("followTime") java.util.Date followTime, @Param("updateBy") String updateBy);

    // ====== P3: 查重 ======
    /** 按企业名称/手机号查重 */
    public List<MkLead> selectDuplicateLeads(@Param("companyName") String companyName, @Param("contactPhone") String contactPhone);
}
