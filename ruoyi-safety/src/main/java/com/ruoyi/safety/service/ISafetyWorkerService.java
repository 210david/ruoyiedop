package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyWorker;

public interface ISafetyWorkerService {
    public SafetyWorker selectSafetyWorkerById(Long workerId);
    public List<SafetyWorker> selectSafetyWorkerList(SafetyWorker safetyWorker);
    public int insertSafetyWorker(SafetyWorker safetyWorker);
    public int updateSafetyWorker(SafetyWorker safetyWorker);
    public int deleteSafetyWorkerByIds(Long[] workerIds);
    public int deleteSafetyWorkerById(Long workerId);
}