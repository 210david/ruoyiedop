package com.ruoyi.dms.service;

import java.util.List;
import com.ruoyi.dms.domain.DmsEquipmentLog;

public interface IDmsEquipmentLogService
{
    public List<DmsEquipmentLog> selectLogList(DmsEquipmentLog log);
    public int insertLog(DmsEquipmentLog log);
}
