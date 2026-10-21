package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkContractChangeLog;

/**
 * 合同变更记录 Mapper
 *
 * @author ruoyi
 */
public interface MkContractChangeLogMapper
{
    List<MkContractChangeLog> selectChangeLogList(MkContractChangeLog changeLog);
    List<MkContractChangeLog> selectChangeLogByContractId(Long contractId);
    MkContractChangeLog selectChangeLogById(Long logId);
    int insertChangeLog(MkContractChangeLog changeLog);
    int updateChangeLog(MkContractChangeLog changeLog);
}
