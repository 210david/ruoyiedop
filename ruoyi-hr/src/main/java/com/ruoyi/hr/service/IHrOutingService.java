package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrOuting;

public interface IHrOutingService {
    public HrOuting selectHrOutingById(Long outingId);
    public List<HrOuting> selectHrOutingList(HrOuting hrOuting);
    public int insertHrOuting(HrOuting hrOuting);
    public int updateHrOuting(HrOuting hrOuting);
    public int deleteHrOutingByIds(Long[] outingIds);
    public int deleteHrOutingById(Long outingId);

    /** 外出审批（auditAction: 1=通过, 2=驳回） */
    public int auditHrOuting(Long outingId, String auditAction, String auditRemark);
}
