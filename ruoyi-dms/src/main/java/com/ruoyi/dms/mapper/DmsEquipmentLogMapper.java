package com.ruoyi.dms.mapper;

import java.util.List;
import com.ruoyi.dms.domain.DmsEquipmentLog;

public interface DmsEquipmentLogMapper
{
    public List<DmsEquipmentLog> selectLogList(DmsEquipmentLog log);
    public int insertLog(DmsEquipmentLog log);
}
