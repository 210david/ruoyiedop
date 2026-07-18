package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsEquipmentLog;
import com.ruoyi.dms.mapper.DmsEquipmentLogMapper;
import com.ruoyi.dms.service.IDmsEquipmentLogService;

@Service
public class DmsEquipmentLogServiceImpl implements IDmsEquipmentLogService
{
    @Autowired
    private DmsEquipmentLogMapper dmsEquipmentLogMapper;

    @Override
    public List<DmsEquipmentLog> selectLogList(DmsEquipmentLog log)
    {
        return dmsEquipmentLogMapper.selectLogList(log);
    }

    @Override
    public int insertLog(DmsEquipmentLog log)
    {
        return dmsEquipmentLogMapper.insertLog(log);
    }
}
