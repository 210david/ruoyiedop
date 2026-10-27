package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsDocDistribute;

public interface IQmsDocDistributeService {
    public List<QmsDocDistribute> selectDocDistributeList(QmsDocDistribute distribute);
    public List<QmsDocDistribute> selectByDocId(Long docId);
    public QmsDocDistribute selectDocDistributeById(Long distributeId);
    public int distributeDoc(QmsDocDistribute distribute);
    public int borrowDoc(QmsDocDistribute distribute);
    public int returnDoc(Long distributeId);
    public int receiveDoc(Long distributeId);
    public int deleteDocDistributeByIds(Long[] ids);
    public List<QmsDocDistribute> selectExpiredBorrows();
}
