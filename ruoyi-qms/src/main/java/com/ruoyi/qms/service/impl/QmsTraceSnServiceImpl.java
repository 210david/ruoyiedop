package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qms.domain.QmsTraceSn;
import com.ruoyi.qms.mapper.QmsTraceSnMapper;
import com.ruoyi.qms.service.IQmsTraceSnService;

@Service
public class QmsTraceSnServiceImpl implements IQmsTraceSnService {
    @Autowired
    private QmsTraceSnMapper mapper;

    @Override
    public List<QmsTraceSn> selectSnList(QmsTraceSn sn) { return mapper.selectSnList(sn); }

    @Override
    public QmsTraceSn selectSnById(Long id) { return mapper.selectSnById(id); }

    @Override
    public QmsTraceSn selectBySnCode(String snCode) { return mapper.selectBySnCode(snCode); }

    @Override
    public List<QmsTraceSn> selectByBatchNo(String batchNo) { return mapper.selectByBatchNo(batchNo); }

    @Override
    public int insertSn(QmsTraceSn sn) {
        if (sn.getStatus() == null) sn.setStatus("0");
        return mapper.insertSn(sn);
    }

    @Override
    public int updateSn(QmsTraceSn sn) { return mapper.updateSn(sn); }

    @Override
    public int deleteSnByIds(Long[] ids) { return mapper.deleteSnByIds(ids); }
}
