package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapaEffectiveness;

public interface QmsCapaEffectivenessMapper {
    public List<QmsCapaEffectiveness> selectEffectivenessList(QmsCapaEffectiveness effectiveness);
    public List<QmsCapaEffectiveness> selectByCapaId(Long capaId);
    public QmsCapaEffectiveness selectEffectivenessById(Long effectivenessId);
    public int insertEffectiveness(QmsCapaEffectiveness effectiveness);
    public int updateEffectiveness(QmsCapaEffectiveness effectiveness);
    public int deleteEffectivenessByIds(Long[] ids);
}
