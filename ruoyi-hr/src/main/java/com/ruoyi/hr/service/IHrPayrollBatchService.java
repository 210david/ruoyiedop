package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrPayrollBatch;

public interface IHrPayrollBatchService {
    public HrPayrollBatch selectHrPayrollBatchById(Long batchId);
    public List<HrPayrollBatch> selectHrPayrollBatchList(HrPayrollBatch hrPayrollBatch);
    public int insertHrPayrollBatch(HrPayrollBatch hrPayrollBatch);
    public int updateHrPayrollBatch(HrPayrollBatch hrPayrollBatch);
    public int deleteHrPayrollBatchByIds(Long[] batchIds);
    public int deleteHrPayrollBatchById(Long batchId);
}
