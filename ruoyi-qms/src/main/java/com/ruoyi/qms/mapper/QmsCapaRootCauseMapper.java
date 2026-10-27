package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapaRootCause;

public interface QmsCapaRootCauseMapper {
    public List<QmsCapaRootCause> selectRootCauseList(QmsCapaRootCause rootCause);
    public QmsCapaRootCause selectByCapaId(Long capaId);
    public QmsCapaRootCause selectRootCauseById(Long rootCauseId);
    public int insertRootCause(QmsCapaRootCause rootCause);
    public int updateRootCause(QmsCapaRootCause rootCause);
    public int deleteRootCauseByIds(Long[] ids);
}
