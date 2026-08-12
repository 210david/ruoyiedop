package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapa;

public interface IQmsCapaService {
    public List<QmsCapa> selectCapaList(QmsCapa capa);
    public QmsCapa selectCapaById(Long capaId);
    public int insertCapa(QmsCapa capa);
    public int updateCapa(QmsCapa capa);
    public int deleteCapaByIds(Long[] capaIds);
    public int closeCapa(Long capaId);
}
