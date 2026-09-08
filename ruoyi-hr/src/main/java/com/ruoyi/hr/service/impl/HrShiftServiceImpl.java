package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrShift;
import com.ruoyi.hr.mapper.HrShiftMapper;
import com.ruoyi.hr.service.IHrShiftService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrShiftServiceImpl implements IHrShiftService
{
    @Autowired
    private HrShiftMapper hrShiftMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrShift selectHrShiftById(Long shiftId) { return hrShiftMapper.selectHrShiftById(shiftId); }

    @Override
    public List<HrShift> selectHrShiftList(HrShift hrShift) { return hrShiftMapper.selectHrShiftList(hrShift); }

    @Override
    public int insertHrShift(HrShift hrShift)
    {
        if (StringUtils.isEmpty(hrShift.getShiftCode()))
        {
            hrShift.setShiftCode(mkNumberRuleService.generateNumber("hr_shift"));
        }
        computeShiftFields(hrShift);
        // 如果设为默认班次，清除其他班次的默认标记
        if (hrShift.getIsDefault() != null && hrShift.getIsDefault() == 1)
        {
            hrShiftMapper.clearDefaultFlag(hrShift.getShiftId());
        }
        return hrShiftMapper.insertHrShift(hrShift);
    }

    @Override
    public int updateHrShift(HrShift hrShift)
    {
        computeShiftFields(hrShift);
        // 如果设为默认班次，清除其他班次的默认标记
        if (hrShift.getIsDefault() != null && hrShift.getIsDefault() == 1)
        {
            hrShiftMapper.clearDefaultFlag(hrShift.getShiftId());
        }
        return hrShiftMapper.updateHrShift(hrShift);
    }

    /** 根据上下班时间自动计算跨零点；工作时长仅在未手动填写时自动计算 */
    private void computeShiftFields(HrShift shift)
    {
        if (shift.getStartTime() == null || shift.getEndTime() == null || !shift.getStartTime().contains(":") || !shift.getEndTime().contains(":"))
        {
            return;
        }
        try
        {
            String[] sp = shift.getStartTime().split(":");
            String[] ep = shift.getEndTime().split(":");
            int s = Integer.parseInt(sp[0]) * 60 + (sp.length > 1 ? Integer.parseInt(sp[1]) : 0);
            int e = Integer.parseInt(ep[0]) * 60 + (ep.length > 1 ? Integer.parseInt(ep[1]) : 0);
            if (e == 0) { e = 1440; }
            shift.setCrossMidnight(e < s ? 1 : 0);
            if (e < s) { e += 1440; }
            // 工作时长：用户手动填写了则保留，否则自动计算
            if (shift.getWorkHours() == null)
            {
                shift.setWorkHours(java.math.BigDecimal.valueOf(Math.round((e - s) * 100.0 / 60.0) / 100.0));
            }
        }
        catch (NumberFormatException ignored) { }
    }

    @Override
    public int deleteHrShiftByIds(Long[] shiftIds) { return hrShiftMapper.deleteHrShiftByIds(shiftIds); }

    @Override
    public int deleteHrShiftById(Long shiftId) { return hrShiftMapper.deleteHrShiftById(shiftId); }

    @Override
    public HrShift selectDefaultShift() { return hrShiftMapper.selectDefaultShift(); }
}
