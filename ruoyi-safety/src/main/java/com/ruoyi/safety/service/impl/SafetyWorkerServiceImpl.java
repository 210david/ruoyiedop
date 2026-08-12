package com.ruoyi.safety.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.safety.domain.SafetyWorker;
import com.ruoyi.safety.mapper.SafetyWorkerMapper;
import com.ruoyi.safety.service.ISafetyWorkerService;

@Service
public class SafetyWorkerServiceImpl implements ISafetyWorkerService
{
    @Autowired
    private SafetyWorkerMapper safetyWorkerMapper;

    @Override
    public SafetyWorker selectSafetyWorkerById(Long workerId) { return safetyWorkerMapper.selectSafetyWorkerById(workerId); }

    @Override
    public List<SafetyWorker> selectSafetyWorkerList(SafetyWorker safetyWorker) { return safetyWorkerMapper.selectSafetyWorkerList(safetyWorker); }

    @Override
    public int insertSafetyWorker(SafetyWorker safetyWorker)
    {
        return safetyWorkerMapper.insertSafetyWorker(safetyWorker);
    }

    @Override
    public int updateSafetyWorker(SafetyWorker safetyWorker) { return safetyWorkerMapper.updateSafetyWorker(safetyWorker); }

    @Override
    public int deleteSafetyWorkerByIds(Long[] workerIds) { return safetyWorkerMapper.deleteSafetyWorkerByIds(workerIds); }

    @Override
    public int deleteSafetyWorkerById(Long workerId) { return safetyWorkerMapper.deleteSafetyWorkerById(workerId); }
}