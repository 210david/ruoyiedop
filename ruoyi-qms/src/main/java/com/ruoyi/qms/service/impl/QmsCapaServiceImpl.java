package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsCapa;
import com.ruoyi.qms.mapper.QmsCapaMapper;
import com.ruoyi.qms.service.IQmsCapaService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsCapaServiceImpl implements IQmsCapaService {
    @Autowired
    private QmsCapaMapper qmsCapaMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsCapa> selectCapaList(QmsCapa capa) { return qmsCapaMapper.selectCapaList(capa); }

    @Override
    public QmsCapa selectCapaById(Long capaId) { return qmsCapaMapper.selectCapaById(capaId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCapa(QmsCapa capa) {
        if (StringUtils.isEmpty(capa.getCapaNo())) {
            capa.setCapaNo(mkNumberRuleService.generateNumber("qms_capa"));
        }
        capa.setDelFlag("0");
        capa.setStatus("0");
        if (capa.getCapaStatus() == null) capa.setCapaStatus("0");
        return qmsCapaMapper.insertCapa(capa);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCapa(QmsCapa capa) { return qmsCapaMapper.updateCapa(capa); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCapaByIds(Long[] capaIds) { return qmsCapaMapper.deleteCapaByIds(capaIds); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeCapa(Long capaId) {
        QmsCapa capa = qmsCapaMapper.selectCapaById(capaId);
        if (capa == null) throw new ServiceException("CAPA不存在");
        QmsCapa update = new QmsCapa();
        update.setCapaId(capaId);
        update.setCapaStatus("3");
        update.setCloseTime(new Date());
        return qmsCapaMapper.updateCapa(update);
    }
}
