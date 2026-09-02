package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.hr.domain.HrLaborWorker;
import com.ruoyi.hr.mapper.HrLaborWorkerMapper;
import com.ruoyi.hr.service.IHrLaborWorkerService;

@Service
public class HrLaborWorkerServiceImpl implements IHrLaborWorkerService
{
    @Autowired
    private HrLaborWorkerMapper hrLaborWorkerMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrLaborWorker selectHrLaborWorkerById(Long laborId) { return hrLaborWorkerMapper.selectHrLaborWorkerById(laborId); }

    @Override
    public List<HrLaborWorker> selectHrLaborWorkerList(HrLaborWorker hrLaborWorker) { return hrLaborWorkerMapper.selectHrLaborWorkerList(hrLaborWorker); }

    @Override
    public int insertHrLaborWorker(HrLaborWorker hrLaborWorker)
    {
        if (StringUtils.isEmpty(hrLaborWorker.getAgreementNo()))
        {
            hrLaborWorker.setAgreementNo(mkNumberRuleService.generateNumber("hr_labor_agreement"));
        }
        return hrLaborWorkerMapper.insertHrLaborWorker(hrLaborWorker);
    }

    @Override
    public int updateHrLaborWorker(HrLaborWorker hrLaborWorker) { return hrLaborWorkerMapper.updateHrLaborWorker(hrLaborWorker); }

    @Override
    public int deleteHrLaborWorkerByIds(Long[] laborIds) { return hrLaborWorkerMapper.deleteHrLaborWorkerByIds(laborIds); }

    @Override
    public int deleteHrLaborWorkerById(Long laborId) { return hrLaborWorkerMapper.deleteHrLaborWorkerById(laborId); }
}
