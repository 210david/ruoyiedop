package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.qms.domain.QmsCapaEffectiveness;
import com.ruoyi.qms.mapper.QmsCapaEffectivenessMapper;
import com.ruoyi.qms.service.IQmsCapaEffectivenessService;

@Service
public class QmsCapaEffectivenessServiceImpl implements IQmsCapaEffectivenessService {
    @Autowired
    private QmsCapaEffectivenessMapper mapper;

    @Override
    public List<QmsCapaEffectiveness> selectEffectivenessList(QmsCapaEffectiveness effectiveness) { return mapper.selectEffectivenessList(effectiveness); }

    @Override
    public List<QmsCapaEffectiveness> selectByCapaId(Long capaId) { return mapper.selectByCapaId(capaId); }

    @Override
    public QmsCapaEffectiveness selectEffectivenessById(Long id) { return mapper.selectEffectivenessById(id); }

    @Override
    public int insertEffectiveness(QmsCapaEffectiveness effectiveness) {
        if (effectiveness.getStatus() == null) effectiveness.setStatus("0");
        return mapper.insertEffectiveness(effectiveness);
    }

    @Override
    public int updateEffectiveness(QmsCapaEffectiveness effectiveness) { return mapper.updateEffectiveness(effectiveness); }

    @Override
    public int deleteEffectivenessByIds(Long[] ids) { return mapper.deleteEffectivenessByIds(ids); }
}
