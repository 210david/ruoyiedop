package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsDocDistribute;
import com.ruoyi.qms.mapper.QmsDocDistributeMapper;
import com.ruoyi.qms.service.IQmsDocDistributeService;

@Service
public class QmsDocDistributeServiceImpl implements IQmsDocDistributeService {
    @Autowired
    private QmsDocDistributeMapper mapper;

    @Override
    public List<QmsDocDistribute> selectDocDistributeList(QmsDocDistribute distribute) { return mapper.selectDocDistributeList(distribute); }

    @Override
    public List<QmsDocDistribute> selectByDocId(Long docId) { return mapper.selectByDocId(docId); }

    @Override
    public QmsDocDistribute selectDocDistributeById(Long distributeId) { return mapper.selectDocDistributeById(distributeId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int distributeDoc(QmsDocDistribute distribute) {
        distribute.setDistributeType("distribute");
        distribute.setDistStatus("0");
        distribute.setWatermark("DISTRIBUTED-" + System.currentTimeMillis());
        return mapper.insertDocDistribute(distribute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int borrowDoc(QmsDocDistribute distribute) {
        distribute.setDistributeType("borrow");
        distribute.setDistStatus("0");
        return mapper.insertDocDistribute(distribute);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int returnDoc(Long distributeId) {
        QmsDocDistribute existing = mapper.selectDocDistributeById(distributeId);
        if (existing == null) throw new ServiceException("借阅记录不存在");
        if (!"borrow".equals(existing.getDistributeType())) throw new ServiceException("非借阅记录不支持归还操作");
        QmsDocDistribute update = new QmsDocDistribute();
        update.setDistributeId(distributeId);
        update.setDistStatus("2");
        update.setReturnTime(new Date());
        return mapper.updateDocDistribute(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int receiveDoc(Long distributeId) {
        QmsDocDistribute existing = mapper.selectDocDistributeById(distributeId);
        if (existing == null) throw new ServiceException("分发记录不存在");
        if (!"0".equals(existing.getDistStatus())) throw new ServiceException("当前状态不支持签收操作");
        QmsDocDistribute update = new QmsDocDistribute();
        update.setDistributeId(distributeId);
        update.setDistStatus("1");
        update.setReceiveTime(new Date());
        return mapper.updateDocDistribute(update);
    }

    @Override
    public int deleteDocDistributeByIds(Long[] ids) { return mapper.deleteDocDistributeByIds(ids); }

    @Override
    public List<QmsDocDistribute> selectExpiredBorrows() { return mapper.selectExpiredBorrows(); }
}
