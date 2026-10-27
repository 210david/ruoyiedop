package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsSqmPushLog;

public interface QmsSqmPushLogMapper {
    public List<QmsSqmPushLog> selectPushLogList(QmsSqmPushLog log);
    public QmsSqmPushLog selectPushLogById(Long logId);
    public int insertPushLog(QmsSqmPushLog log);
    public int updatePushLog(QmsSqmPushLog log);
    public int deletePushLogByIds(Long[] logIds);
    /** 查询待推送的记录（push_status='0' 且重试次数<3） */
    public List<QmsSqmPushLog> selectPendingPushList();
}
