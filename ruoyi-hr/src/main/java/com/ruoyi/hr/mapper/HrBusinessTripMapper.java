package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrBusinessTrip;

public interface HrBusinessTripMapper {
    public HrBusinessTrip selectHrBusinessTripById(Long tripId);
    public List<HrBusinessTrip> selectHrBusinessTripList(HrBusinessTrip hrBusinessTrip);
    public int insertHrBusinessTrip(HrBusinessTrip hrBusinessTrip);
    public int updateHrBusinessTrip(HrBusinessTrip hrBusinessTrip);
    public int deleteHrBusinessTripByIds(Long[] tripIds);
    public int deleteHrBusinessTripById(Long tripId);
}
