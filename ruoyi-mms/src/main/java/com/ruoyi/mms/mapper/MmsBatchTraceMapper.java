package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsBatchTrace;

public interface MmsBatchTraceMapper
{
    public List<MmsBatchTrace> selectBatchTraceList(MmsBatchTrace trace);
    public MmsBatchTrace selectBatchTraceById(Long traceId);
}
