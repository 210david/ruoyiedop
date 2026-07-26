package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkLeadLog;

public interface MkLeadLogMapper
{
    /** 查询线索操作日志列表 */
    public List<MkLeadLog> selectLeadLogList(Long leadId);

    /** 插入线索操作日志 */
    public int insertLeadLog(MkLeadLog log);
}
