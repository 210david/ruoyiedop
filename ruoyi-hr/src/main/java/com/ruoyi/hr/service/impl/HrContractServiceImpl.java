package com.ruoyi.hr.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.hr.domain.HrContract;
import com.ruoyi.hr.mapper.HrContractMapper;
import com.ruoyi.hr.service.IHrContractService;

@Service
public class HrContractServiceImpl implements IHrContractService
{
    @Autowired
    private HrContractMapper hrContractMapper;

    @Override
    public HrContract selectHrContractById(Long contractId) { return hrContractMapper.selectHrContractById(contractId); }

    @Override
    public List<HrContract> selectHrContractList(HrContract hrContract) { return hrContractMapper.selectHrContractList(hrContract); }

    @Override
    public int insertHrContract(HrContract hrContract) { return hrContractMapper.insertHrContract(hrContract); }

    @Override
    public int updateHrContract(HrContract hrContract) { return hrContractMapper.updateHrContract(hrContract); }

    @Override
    public int deleteHrContractByIds(Long[] contractIds) { return hrContractMapper.deleteHrContractByIds(contractIds); }

    @Override
    public int deleteHrContractById(Long contractId) { return hrContractMapper.deleteHrContractById(contractId); }
}
