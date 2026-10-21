package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.mapper.HrEmployeeEducationMapper;
import com.ruoyi.hr.domain.HrEmployeeEducation;
import com.ruoyi.hr.service.IHrEmployeeEducationService;

@Service
public class HrEmployeeEducationServiceImpl implements IHrEmployeeEducationService {
    @Autowired
    private HrEmployeeEducationMapper hrEmployeeEducationMapper;

    @Override
    public HrEmployeeEducation selectHrEmployeeEducationByEducationId(Long educationId) {
        return hrEmployeeEducationMapper.selectHrEmployeeEducationByEducationId(educationId);
    }

    @Override
    public List<HrEmployeeEducation> selectHrEmployeeEducationList(HrEmployeeEducation hrEmployeeEducation) {
        return hrEmployeeEducationMapper.selectHrEmployeeEducationList(hrEmployeeEducation);
    }

    @Override
    public int insertHrEmployeeEducation(HrEmployeeEducation hrEmployeeEducation) {
        return hrEmployeeEducationMapper.insertHrEmployeeEducation(hrEmployeeEducation);
    }

    @Override
    public int updateHrEmployeeEducation(HrEmployeeEducation hrEmployeeEducation) {
        return hrEmployeeEducationMapper.updateHrEmployeeEducation(hrEmployeeEducation);
    }

    @Override
    public int deleteHrEmployeeEducationByEducationId(Long educationId) {
        return hrEmployeeEducationMapper.deleteHrEmployeeEducationByEducationId(educationId);
    }

    @Override
    public int deleteHrEmployeeEducationByIds(Long[] educationIds) {
        return hrEmployeeEducationMapper.deleteHrEmployeeEducationByIds(educationIds);
    }
}
