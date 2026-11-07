package com.ruoyi.integration.mapper;

import java.util.List;
import com.ruoyi.integration.domain.IntegrationSyncLog;

/**
 * 集成同步日志 Mapper
 *
 * @author ruoyi
 */
public interface IntegrationSyncLogMapper
{
    public List<IntegrationSyncLog> selectIntegrationSyncLogList(IntegrationSyncLog query);

    public int insertIntegrationSyncLog(IntegrationSyncLog log);
}
