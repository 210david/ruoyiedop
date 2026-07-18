package com.ruoyi.dms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsWorkOrderLog;
import com.ruoyi.dms.mapper.DmsWorkOrderLogMapper;
import com.ruoyi.dms.service.IDmsWorkOrderLogService;

@Service
public class DmsWorkOrderLogServiceImpl implements IDmsWorkOrderLogService
{
    @Autowired
    private DmsWorkOrderLogMapper dmsWorkOrderLogMapper;

    @Override
    public void recordLog(Long orderId, String orderNo, String fromStatus, String toStatus,
                           String action, String operatorName, String remark)
    {
        DmsWorkOrderLog log = new DmsWorkOrderLog();
        log.setOrderId(orderId);
        log.setOrderNo(orderNo);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setAction(action);
        log.setOperatorName(operatorName);
        log.setRemark(remark);
        dmsWorkOrderLogMapper.insertLog(log);
    }

    @Override
    public List<DmsWorkOrderLog> selectLogByOrderId(Long orderId)
    {
        return dmsWorkOrderLogMapper.selectLogByOrderId(orderId);
    }
}
