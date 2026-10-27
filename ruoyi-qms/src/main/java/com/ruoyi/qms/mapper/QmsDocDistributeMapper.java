package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsDocDistribute;

public interface QmsDocDistributeMapper {
    public List<QmsDocDistribute> selectDocDistributeList(QmsDocDistribute distribute);
    public List<QmsDocDistribute> selectByDocId(Long docId);
    public QmsDocDistribute selectDocDistributeById(Long distributeId);
    public int insertDocDistribute(QmsDocDistribute distribute);
    public int updateDocDistribute(QmsDocDistribute distribute);
    public int deleteDocDistributeByIds(Long[] distributeIds);
    public List<QmsDocDistribute> selectExpiredBorrows();
}
