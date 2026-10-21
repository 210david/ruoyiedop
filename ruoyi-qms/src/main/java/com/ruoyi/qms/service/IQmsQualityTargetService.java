package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsQualityTarget;

public interface IQmsQualityTargetService {
    public List<QmsQualityTarget> selectTargetList(QmsQualityTarget target);
    public QmsQualityTarget selectTargetById(Long targetId);
    public int insertTarget(QmsQualityTarget target);
    public int updateTarget(QmsQualityTarget target);
    public int deleteTargetByIds(Long[] targetIds);
}
