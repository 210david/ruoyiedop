package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmChangeAuditLog;

/**
 * 技术变更审批记录 Mapper
 *
 * @author ruoyi
 */
public interface PdmChangeAuditLogMapper
{
    /** 根据变更单ID查询审批记录（按时间正序） */
    public List<PdmChangeAuditLog> selectAuditLogByChangeId(Long changeId);

    /** 新增审批记录 */
    public int insertAuditLog(PdmChangeAuditLog log);
}
