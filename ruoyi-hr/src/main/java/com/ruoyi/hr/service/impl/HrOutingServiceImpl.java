package com.ruoyi.hr.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrOuting;
import com.ruoyi.hr.domain.HrOutingAuditLog;
import com.ruoyi.hr.mapper.HrOutingMapper;
import com.ruoyi.hr.mapper.HrOutingAuditLogMapper;
import com.ruoyi.hr.service.IHrOutingService;

@Service
public class HrOutingServiceImpl implements IHrOutingService
{
    @Autowired
    private HrOutingMapper hrOutingMapper;
    @Autowired
    private HrOutingAuditLogMapper hrOutingAuditLogMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrOuting selectHrOutingById(Long outingId)
    {
        HrOuting outing = hrOutingMapper.selectHrOutingById(outingId);
        if (outing != null)
        {
            outing.setAuditLogList(hrOutingAuditLogMapper.selectHrOutingAuditLogByOutingId(outingId));
        }
        return outing;
    }

    @Override
    @DataScope(deptAlias = "biz_hr_outing")
    public List<HrOuting> selectHrOutingList(HrOuting hrOuting) { return hrOutingMapper.selectHrOutingList(hrOuting); }

    @Override
    public int insertHrOuting(HrOuting hrOuting)
    {
        if (StringUtils.isEmpty(hrOuting.getOutingNo()))
        {
            hrOuting.setOutingNo(mkNumberRuleService.generateNumber("hr_outing"));
        }
        if (StringUtils.isEmpty(hrOuting.getAuditStatus()))
        {
            hrOuting.setAuditStatus("0");
        }
        return hrOutingMapper.insertHrOuting(hrOuting);
    }

    @Override
    public int updateHrOuting(HrOuting hrOuting)
    {
        if ("2".equals(hrOuting.getAuditStatus()))
        {
            hrOuting.setAuditStatus("0");
            hrOuting.setAuditBy(null);
            hrOuting.setAuditTime(null);
            hrOuting.setAuditRemark("");
        }
        return hrOutingMapper.updateHrOuting(hrOuting);
    }

    @Override
    public int deleteHrOutingByIds(Long[] outingIds)
    {
        int rows = hrOutingMapper.deleteHrOutingByIds(outingIds);
        for (Long outingId : outingIds)
        {
            hrOutingAuditLogMapper.deleteHrOutingAuditLogByOutingId(outingId);
        }
        return rows;
    }

    @Override
    public int deleteHrOutingById(Long outingId)
    {
        hrOutingAuditLogMapper.deleteHrOutingAuditLogByOutingId(outingId);
        return hrOutingMapper.deleteHrOutingById(outingId);
    }
}
