package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapaEffectiveness;

public interface IQmsCapaEffectivenessService {
    public List<QmsCapaEffectiveness> selectEffectivenessList(QmsCapaEffectiveness effectiveness);
    public List<QmsCapaEffectiveness> selectByCapaId(Long capaId);
    public QmsCapaEffectiveness selectEffectivenessById(Long id);
    public int insertEffectiveness(QmsCapaEffectiveness effectiveness);
    public int updateEffectiveness(QmsCapaEffectiveness effectiveness);
    public int deleteEffectivenessByIds(Long[] ids);
}
