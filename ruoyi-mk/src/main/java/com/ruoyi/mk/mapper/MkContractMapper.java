package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContract;

public interface MkContractMapper
{
    public List<MkContract> selectContractList(MkContract contract);
    public MkContract selectContractById(Long contractId);
    public int insertContract(MkContract contract);
    public int updateContract(MkContract contract);
    public int deleteContractByIds(Long[] contractIds);
    /** 更新合同状态 */
    public int updateContractStatus(@org.apache.ibatis.annotations.Param("contractId") Long contractId, @org.apache.ibatis.annotations.Param("contractStatus") String contractStatus);
    /** 查询已生效但已过期的合同（用于定时任务） */
    public List<MkContract> selectExpiredContracts();
    /** 查询即将到期的合同（指定天数内到期） */
    public List<MkContract> selectExpiringContracts(@org.apache.ibatis.annotations.Param("days") int days);
}
