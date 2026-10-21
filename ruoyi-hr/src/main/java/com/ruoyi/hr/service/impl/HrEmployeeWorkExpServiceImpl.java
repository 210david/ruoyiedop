package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.mapper.HrEmployeeWorkExpMapper;
import com.ruoyi.hr.domain.HrEmployeeWorkExp;
import com.ruoyi.hr.service.IHrEmployeeWorkExpService;

@Service
public class HrEmployeeWorkExpServiceImpl implements IHrEmployeeWorkExpService {
    @Autowired
    private HrEmployeeWorkExpMapper hrEmployeeWorkExpMapper;

    @Override
    public HrEmployeeWorkExp selectHrEmployeeWorkExpByWorkExpId(Long workExpId) {
        return hrEmployeeWorkExpMapper.selectHrEmployeeWorkExpByWorkExpId(workExpId);
    }

    @Override
    public List<HrEmployeeWorkExp> selectHrEmployeeWorkExpList(HrEmployeeWorkExp hrEmployeeWorkExp) {
        return hrEmployeeWorkExpMapper.selectHrEmployeeWorkExpList(hrEmployeeWorkExp);
    }

    @Override
    public int insertHrEmployeeWorkExp(HrEmployeeWorkExp hrEmployeeWorkExp) {
        return hrEmployeeWorkExpMapper.insertHrEmployeeWorkExp(hrEmployeeWorkExp);
    }

    @Override
    public int updateHrEmployeeWorkExp(HrEmployeeWorkExp hrEmployeeWorkExp) {
        return hrEmployeeWorkExpMapper.updateHrEmployeeWorkExp(hrEmployeeWorkExp);
    }

    @Override
    public int deleteHrEmployeeWorkExpByWorkExpId(Long workExpId) {
        return hrEmployeeWorkExpMapper.deleteHrEmployeeWorkExpByWorkExpId(workExpId);
    }

    @Override
    public int deleteHrEmployeeWorkExpByIds(Long[] workExpIds) {
        return hrEmployeeWorkExpMapper.deleteHrEmployeeWorkExpByIds(workExpIds);
    }
}
