package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsMr;

public interface IQmsMrService {
    public List<QmsMr> selectMrList(QmsMr mr);
    public QmsMr selectMrById(Long mrId);
    public int insertMr(QmsMr mr);
    public int updateMr(QmsMr mr);
    public int deleteMrByIds(Long[] mrIds);
}
