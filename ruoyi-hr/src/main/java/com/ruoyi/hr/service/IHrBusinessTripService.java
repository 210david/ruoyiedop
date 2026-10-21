package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrBusinessTrip;

public interface IHrBusinessTripService {
    public HrBusinessTrip selectHrBusinessTripById(Long tripId);
    public List<HrBusinessTrip> selectHrBusinessTripList(HrBusinessTrip hrBusinessTrip);
    public int insertHrBusinessTrip(HrBusinessTrip hrBusinessTrip);
    public int updateHrBusinessTrip(HrBusinessTrip hrBusinessTrip);
    public int deleteHrBusinessTripByIds(Long[] tripIds);
    public int deleteHrBusinessTripById(Long tripId);

    /** 出差审批（auditAction: 1=通过, 2=驳回） */
    public int auditHrBusinessTrip(Long tripId, String auditAction, String auditRemark);
}
