package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrContract;

public interface HrContractMapper {
    public HrContract selectHrContractById(Long contractId);
    public List<HrContract> selectHrContractList(HrContract hrContract);
    public int insertHrContract(HrContract hrContract);
    public int updateHrContract(HrContract hrContract);
    public int deleteHrContractByIds(Long[] contractIds);
    public int deleteHrContractById(Long contractId);
}
