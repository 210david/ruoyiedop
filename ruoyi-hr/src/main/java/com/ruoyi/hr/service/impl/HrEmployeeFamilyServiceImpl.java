package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.mapper.HrEmployeeFamilyMapper;
import com.ruoyi.hr.domain.HrEmployeeFamily;
import com.ruoyi.hr.service.IHrEmployeeFamilyService;

@Service
public class HrEmployeeFamilyServiceImpl implements IHrEmployeeFamilyService {
    @Autowired
    private HrEmployeeFamilyMapper hrEmployeeFamilyMapper;

    @Override
    public HrEmployeeFamily selectHrEmployeeFamilyByFamilyId(Long familyId) {
        return hrEmployeeFamilyMapper.selectHrEmployeeFamilyByFamilyId(familyId);
    }

    @Override
    public List<HrEmployeeFamily> selectHrEmployeeFamilyList(HrEmployeeFamily hrEmployeeFamily) {
        return hrEmployeeFamilyMapper.selectHrEmployeeFamilyList(hrEmployeeFamily);
    }

    @Override
    public int insertHrEmployeeFamily(HrEmployeeFamily hrEmployeeFamily) {
        return hrEmployeeFamilyMapper.insertHrEmployeeFamily(hrEmployeeFamily);
    }

    @Override
    public int updateHrEmployeeFamily(HrEmployeeFamily hrEmployeeFamily) {
        return hrEmployeeFamilyMapper.updateHrEmployeeFamily(hrEmployeeFamily);
    }

    @Override
    public int deleteHrEmployeeFamilyByFamilyId(Long familyId) {
        return hrEmployeeFamilyMapper.deleteHrEmployeeFamilyByFamilyId(familyId);
    }

    @Override
    public int deleteHrEmployeeFamilyByIds(Long[] familyIds) {
        return hrEmployeeFamilyMapper.deleteHrEmployeeFamilyByIds(familyIds);
    }
}
