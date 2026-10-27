package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrOvertime;

public interface IHrOvertimeService {
    public HrOvertime selectHrOvertimeById(Long overtimeId);
    public List<HrOvertime> selectHrOvertimeList(HrOvertime hrOvertime);
    public int insertHrOvertime(HrOvertime hrOvertime);
    public int updateHrOvertime(HrOvertime hrOvertime);
    public int deleteHrOvertimeByIds(Long[] overtimeIds);
    public int deleteHrOvertimeById(Long overtimeId);

    /** 加班审批（auditAction: 1=通过, 2=驳回） */
    public int auditHrOvertime(Long overtimeId, String auditAction, String auditRemark);
}
