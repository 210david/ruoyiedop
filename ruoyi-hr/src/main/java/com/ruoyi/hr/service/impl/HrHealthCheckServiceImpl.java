package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrHealthCheck;
import com.ruoyi.hr.mapper.HrHealthCheckMapper;
import com.ruoyi.hr.service.IHrHealthCheckService;

@Service
public class HrHealthCheckServiceImpl implements IHrHealthCheckService
{
    @Autowired
    private HrHealthCheckMapper hrHealthCheckMapper;

    @Override
    public HrHealthCheck selectHrHealthCheckById(Long healthId) { return hrHealthCheckMapper.selectHrHealthCheckById(healthId); }

    @Override
    public List<HrHealthCheck> selectHrHealthCheckList(HrHealthCheck hrHealthCheck) { return hrHealthCheckMapper.selectHrHealthCheckList(hrHealthCheck); }

    @Override
    public int insertHrHealthCheck(HrHealthCheck hrHealthCheck) { return hrHealthCheckMapper.insertHrHealthCheck(hrHealthCheck); }

    @Override
    public int updateHrHealthCheck(HrHealthCheck hrHealthCheck) { return hrHealthCheckMapper.updateHrHealthCheck(hrHealthCheck); }

    @Override
    public int deleteHrHealthCheckByIds(Long[] healthIds) { return hrHealthCheckMapper.deleteHrHealthCheckByIds(healthIds); }

    @Override
    public int deleteHrHealthCheckById(Long healthId) { return hrHealthCheckMapper.deleteHrHealthCheckById(healthId); }
}
