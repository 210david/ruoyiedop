package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrLaborWorker;

public interface IHrLaborWorkerService {
    public HrLaborWorker selectHrLaborWorkerById(Long laborId);
    public List<HrLaborWorker> selectHrLaborWorkerList(HrLaborWorker hrLaborWorker);
    public int insertHrLaborWorker(HrLaborWorker hrLaborWorker);
    public int updateHrLaborWorker(HrLaborWorker hrLaborWorker);
    public int deleteHrLaborWorkerByIds(Long[] laborIds);
    public int deleteHrLaborWorkerById(Long laborId);
}
