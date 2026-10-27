package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.mms.domain.MmsBatchTrace;
import com.ruoyi.mms.mapper.MmsBatchTraceMapper;
import com.ruoyi.mms.service.IMmsBatchTraceService;

@Service
public class MmsBatchTraceServiceImpl implements IMmsBatchTraceService
{
    @Autowired
    private MmsBatchTraceMapper batchTraceMapper;

    @Override
    public List<MmsBatchTrace> selectBatchTraceList(MmsBatchTrace trace)
    {
        return batchTraceMapper.selectBatchTraceList(trace);
    }

    @Override
    public MmsBatchTrace selectBatchTraceById(Long traceId)
    {
        return batchTraceMapper.selectBatchTraceById(traceId);
    }
}
