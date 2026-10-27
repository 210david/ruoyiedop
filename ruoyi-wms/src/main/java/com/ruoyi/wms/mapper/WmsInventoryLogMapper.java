package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsInventoryLog;

public interface WmsInventoryLogMapper
{
    public List<WmsInventoryLog> selectInventoryLogList(WmsInventoryLog log);
    public int insertInventoryLog(WmsInventoryLog log);

    /**
     * 批量删除库存流水记录（硬删除）
     */
    public int deleteInventoryLogByIds(Long[] logIds);
}
