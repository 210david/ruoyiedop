package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapa;

public interface QmsCapaMapper {
    public List<QmsCapa> selectCapaList(QmsCapa capa);
    public QmsCapa selectCapaById(Long capaId);
    public int insertCapa(QmsCapa capa);
    public int updateCapa(QmsCapa capa);
    public int deleteCapaByIds(Long[] capaIds);
}
