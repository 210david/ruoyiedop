package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsBatchTrace;

public interface IMmsBatchTraceService
{
    public List<MmsBatchTrace> selectBatchTraceList(MmsBatchTrace trace);
    public MmsBatchTrace selectBatchTraceById(Long traceId);
}
