package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.mapper.HrEmployeeQualificationMapper;
import com.ruoyi.hr.domain.HrEmployeeQualification;
import com.ruoyi.hr.service.IHrEmployeeQualificationService;

@Service
public class HrEmployeeQualificationServiceImpl implements IHrEmployeeQualificationService {
    @Autowired
    private HrEmployeeQualificationMapper hrEmployeeQualificationMapper;

    @Override
    public HrEmployeeQualification selectHrEmployeeQualificationByQualificationId(Long qualificationId) {
        return hrEmployeeQualificationMapper.selectHrEmployeeQualificationByQualificationId(qualificationId);
    }

    @Override
    public List<HrEmployeeQualification> selectHrEmployeeQualificationList(HrEmployeeQualification hrEmployeeQualification) {
        return hrEmployeeQualificationMapper.selectHrEmployeeQualificationList(hrEmployeeQualification);
    }

    @Override
    public int insertHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification) {
        return hrEmployeeQualificationMapper.insertHrEmployeeQualification(hrEmployeeQualification);
    }

    @Override
    public int updateHrEmployeeQualification(HrEmployeeQualification hrEmployeeQualification) {
        return hrEmployeeQualificationMapper.updateHrEmployeeQualification(hrEmployeeQualification);
    }

    @Override
    public int deleteHrEmployeeQualificationByQualificationId(Long qualificationId) {
        return hrEmployeeQualificationMapper.deleteHrEmployeeQualificationByQualificationId(qualificationId);
    }

    @Override
    public int deleteHrEmployeeQualificationByIds(Long[] qualificationIds) {
        return hrEmployeeQualificationMapper.deleteHrEmployeeQualificationByIds(qualificationIds);
    }
}
