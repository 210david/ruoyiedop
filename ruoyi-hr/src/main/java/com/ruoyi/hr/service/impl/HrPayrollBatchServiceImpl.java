package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrPayrollBatch;
import com.ruoyi.hr.mapper.HrPayrollBatchMapper;
import com.ruoyi.hr.service.IHrPayrollBatchService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class HrPayrollBatchServiceImpl implements IHrPayrollBatchService
{
    @Autowired
    private HrPayrollBatchMapper hrPayrollBatchMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public HrPayrollBatch selectHrPayrollBatchById(Long batchId) { return hrPayrollBatchMapper.selectHrPayrollBatchById(batchId); }

    @Override
    public List<HrPayrollBatch> selectHrPayrollBatchList(HrPayrollBatch hrPayrollBatch) { return hrPayrollBatchMapper.selectHrPayrollBatchList(hrPayrollBatch); }

    @Override
    public int insertHrPayrollBatch(HrPayrollBatch hrPayrollBatch)
    {
        if (StringUtils.isEmpty(hrPayrollBatch.getBatchNo()))
        {
            hrPayrollBatch.setBatchNo(mkNumberRuleService.generateNumber("hr_payroll_batch"));
        }
        return hrPayrollBatchMapper.insertHrPayrollBatch(hrPayrollBatch);
    }

    @Override
    public int updateHrPayrollBatch(HrPayrollBatch hrPayrollBatch) { return hrPayrollBatchMapper.updateHrPayrollBatch(hrPayrollBatch); }

    @Override
    public int deleteHrPayrollBatchByIds(Long[] batchIds) { return hrPayrollBatchMapper.deleteHrPayrollBatchByIds(batchIds); }

    @Override
    public int deleteHrPayrollBatchById(Long batchId) { return hrPayrollBatchMapper.deleteHrPayrollBatchById(batchId); }
}
