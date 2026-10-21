package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsCapaRootCause;

public interface IQmsCapaRootCauseService {
    public List<QmsCapaRootCause> selectRootCauseList(QmsCapaRootCause rootCause);
    public QmsCapaRootCause selectByCapaId(Long capaId);
    public QmsCapaRootCause selectRootCauseById(Long id);
    public int saveRootCause(QmsCapaRootCause rootCause);
    public int deleteRootCauseByIds(Long[] ids);
}
