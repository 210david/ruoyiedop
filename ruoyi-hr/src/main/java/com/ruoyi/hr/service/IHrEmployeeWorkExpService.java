package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrEmployeeWorkExp;

public interface IHrEmployeeWorkExpService {
    public HrEmployeeWorkExp selectHrEmployeeWorkExpByWorkExpId(Long workExpId);
    public List<HrEmployeeWorkExp> selectHrEmployeeWorkExpList(HrEmployeeWorkExp hrEmployeeWorkExp);
    public int insertHrEmployeeWorkExp(HrEmployeeWorkExp hrEmployeeWorkExp);
    public int updateHrEmployeeWorkExp(HrEmployeeWorkExp hrEmployeeWorkExp);
    public int deleteHrEmployeeWorkExpByWorkExpId(Long workExpId);
    public int deleteHrEmployeeWorkExpByIds(Long[] workExpIds);
}
