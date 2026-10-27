package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrPayrollBatch;

public interface HrPayrollBatchMapper {
    public HrPayrollBatch selectHrPayrollBatchById(Long batchId);
    public List<HrPayrollBatch> selectHrPayrollBatchList(HrPayrollBatch hrPayrollBatch);
    public int insertHrPayrollBatch(HrPayrollBatch hrPayrollBatch);
    public int updateHrPayrollBatch(HrPayrollBatch hrPayrollBatch);
    public int deleteHrPayrollBatchByIds(Long[] batchIds);
    public int deleteHrPayrollBatchById(Long batchId);
}
