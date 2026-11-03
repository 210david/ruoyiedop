package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkCustomerChangeLog;

/**
 * 客户变更日志 数据层
 *
 * @author ruoyi
 */
public interface MkCustomerChangeLogMapper
{
    /** 查询客户变更日志列表（按客户ID，时间倒序） */
    public List<MkCustomerChangeLog> selectChangeLogList(MkCustomerChangeLog log);

    /** 新增变更日志 */
    public int insertChangeLog(MkCustomerChangeLog log);
}
