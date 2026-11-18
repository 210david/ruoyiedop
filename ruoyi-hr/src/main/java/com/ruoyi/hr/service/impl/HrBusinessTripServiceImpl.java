package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrBusinessTrip;
import com.ruoyi.hr.domain.HrBusinessTripAuditLog;
import com.ruoyi.hr.mapper.HrBusinessTripMapper;
import com.ruoyi.hr.mapper.HrBusinessTripAuditLogMapper;
import com.ruoyi.hr.service.IHrBusinessTripService;

@Service
public class HrBusinessTripServiceImpl implements IHrBusinessTripService
{
    @Autowired
    private HrBusinessTripMapper hrBusinessTripMapper;
    @Autowired
    private HrBusinessTripAuditLogMapper hrBusinessTripAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrBusinessTrip selectHrBusinessTripById(Long tripId)
    {
        HrBusinessTrip trip = hrBusinessTripMapper.selectHrBusinessTripById(tripId);
        if (trip != null)
        {
            trip.setAuditLogList(hrBusinessTripAuditLogMapper.selectHrBusinessTripAuditLogByTripId(tripId));
        }
        return trip;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_business_trip")
    public List<HrBusinessTrip> selectHrBusinessTripList(HrBusinessTrip hrBusinessTrip) { return hrBusinessTripMapper.selectHrBusinessTripList(hrBusinessTrip); }

    @Override
    public int insertHrBusinessTrip(HrBusinessTrip hrBusinessTrip)
    {
        if (StringUtils.isEmpty(hrBusinessTrip.getTripNo()))
        {
            hrBusinessTrip.setTripNo(mkNumberRuleService.generateNumber("hr_business_trip"));
        }
        computeTripDays(hrBusinessTrip);
        if (StringUtils.isEmpty(hrBusinessTrip.getAuditStatus()))
        {
            hrBusinessTrip.setAuditStatus("0");
        }
        return hrBusinessTripMapper.insertHrBusinessTrip(hrBusinessTrip);
    }

    @Override
    public int updateHrBusinessTrip(HrBusinessTrip hrBusinessTrip)
    {
        if ("2".equals(hrBusinessTrip.getAuditStatus()))
        {
            hrBusinessTrip.setAuditStatus("0");
            hrBusinessTrip.setAuditBy(null);
            hrBusinessTrip.setAuditTime(null);
            hrBusinessTrip.setAuditRemark("");
        }
        computeTripDays(hrBusinessTrip);
        return hrBusinessTripMapper.updateHrBusinessTrip(hrBusinessTrip);
    }

    /** 根据出发/预计返回日期自动计算出差天数（含首尾） */
    private void computeTripDays(HrBusinessTrip trip)
    {
        if (trip.getDepartureDate() == null || trip.getExpectedReturnDate() == null
                || trip.getExpectedReturnDate().before(trip.getDepartureDate()))
        {
            return;
        }
        long millis = trip.getExpectedReturnDate().getTime() - trip.getDepartureDate().getTime();
        long days = millis / 86400000L;
        if (millis % 86400000L != 0)
        {
            days++;
        }
        trip.setTripDays(java.math.BigDecimal.valueOf(days));
    }

    @Override
    public int deleteHrBusinessTripByIds(Long[] tripIds)
    {
        int rows = hrBusinessTripMapper.deleteHrBusinessTripByIds(tripIds);
        for (Long tripId : tripIds)
        {
            hrBusinessTripAuditLogMapper.deleteHrBusinessTripAuditLogByTripId(tripId);
        }
        return rows;
    }

    @Override
    public int deleteHrBusinessTripById(Long tripId)
    {
        hrBusinessTripAuditLogMapper.deleteHrBusinessTripAuditLogByTripId(tripId);
        return hrBusinessTripMapper.deleteHrBusinessTripById(tripId);
    }
}
