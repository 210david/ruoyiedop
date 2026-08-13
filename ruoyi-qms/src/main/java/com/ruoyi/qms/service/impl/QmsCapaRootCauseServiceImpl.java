package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.qms.domain.QmsCapaRootCause;
import com.ruoyi.qms.mapper.QmsCapaRootCauseMapper;
import com.ruoyi.qms.mapper.QmsCapaMapper;
import com.ruoyi.qms.service.IQmsCapaRootCauseService;

@Service
public class QmsCapaRootCauseServiceImpl implements IQmsCapaRootCauseService {
    @Autowired
    private QmsCapaRootCauseMapper mapper;
    @Autowired
    private QmsCapaMapper capaMapper;

    @Override
    public List<QmsCapaRootCause> selectRootCauseList(QmsCapaRootCause rootCause) { return mapper.selectRootCauseList(rootCause); }

    @Override
    public QmsCapaRootCause selectByCapaId(Long capaId) { return mapper.selectByCapaId(capaId); }

    @Override
    public QmsCapaRootCause selectRootCauseById(Long id) { return mapper.selectRootCauseById(id); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveRootCause(QmsCapaRootCause rootCause) {
        // 同步更新CAPA主表的root_cause字段
        if (rootCause.getRootCause() != null && rootCause.getCapaId() != null) {
            com.ruoyi.qms.domain.QmsCapa capaUpdate = new com.ruoyi.qms.domain.QmsCapa();
            capaUpdate.setCapaId(rootCause.getCapaId());
            capaUpdate.setRootCause(rootCause.getRootCause());
            capaMapper.updateCapa(capaUpdate);
        }
        if (rootCause.getRootCauseId() != null) {
            return mapper.updateRootCause(rootCause);
        } else {
            // 检查是否已有记录
            QmsCapaRootCause existing = mapper.selectByCapaId(rootCause.getCapaId());
            if (existing != null) {
                rootCause.setRootCauseId(existing.getRootCauseId());
                return mapper.updateRootCause(rootCause);
            }
            return mapper.insertRootCause(rootCause);
        }
    }

    @Override
    public int deleteRootCauseByIds(Long[] ids) { return mapper.deleteRootCauseByIds(ids); }
}
