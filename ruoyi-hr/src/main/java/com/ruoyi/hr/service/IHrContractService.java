package com.ruoyi.hr.service;

import java.util.List;
import com.ruoyi.hr.domain.HrContract;

public interface IHrContractService {
    public HrContract selectHrContractById(Long contractId);
    public List<HrContract> selectHrContractList(HrContract hrContract);
    public int insertHrContract(HrContract hrContract);
    public int updateHrContract(HrContract hrContract);
    public int deleteHrContractByIds(Long[] contractIds);
    public int deleteHrContractById(Long contractId);
}
