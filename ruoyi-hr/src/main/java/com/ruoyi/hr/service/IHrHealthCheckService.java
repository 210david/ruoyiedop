package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrHealthCheck;

public interface IHrHealthCheckService {
    public HrHealthCheck selectHrHealthCheckById(Long healthId);
    public List<HrHealthCheck> selectHrHealthCheckList(HrHealthCheck hrHealthCheck);
    public int insertHrHealthCheck(HrHealthCheck hrHealthCheck);
    public int updateHrHealthCheck(HrHealthCheck hrHealthCheck);
    public int deleteHrHealthCheckByIds(Long[] healthIds);
    public int deleteHrHealthCheckById(Long healthId);
}
