package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrClockRepair;

public interface IHrClockRepairService {
    public HrClockRepair selectHrClockRepairById(Long clockRepairId);
    public List<HrClockRepair> selectHrClockRepairList(HrClockRepair hrClockRepair);
    public int insertHrClockRepair(HrClockRepair hrClockRepair);
    public int updateHrClockRepair(HrClockRepair hrClockRepair);
    public int deleteHrClockRepairByIds(Long[] clockRepairIds);
    public int deleteHrClockRepairById(Long clockRepairId);

    /** 补卡审批（auditAction: 1=通过, 2=驳回） */
    public int auditHrClockRepair(Long clockRepairId, String auditAction, String auditRemark);
}
