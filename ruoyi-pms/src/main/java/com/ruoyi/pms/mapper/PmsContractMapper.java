package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsContract;
import com.ruoyi.pms.domain.PmsContractChange;

/**
 * 采购合同 Mapper
 *
 * @author ruoyi
 */
public interface PmsContractMapper
{
    public List<PmsContract> selectContractList(PmsContract contract);
    public PmsContract selectContractById(Long contractId);
    public int insertContract(PmsContract contract);
    public int updateContract(PmsContract contract);
    public int deleteContractByIds(Long[] contractIds);
    public List<PmsContractChange> selectContractChangeByContractId(Long contractId);
    public PmsContractChange selectContractChangeById(Long changeId);
    public int insertContractChange(PmsContractChange change);
    public int updateContractChange(PmsContractChange change);
    public int deleteContractChangeByContractId(Long contractId);

    /** 查询已到期但状态仍为已签订的合同 */
    public List<PmsContract> selectExpiredContracts();
}
