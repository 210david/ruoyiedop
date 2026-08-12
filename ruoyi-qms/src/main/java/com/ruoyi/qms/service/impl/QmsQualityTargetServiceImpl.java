package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.qms.domain.QmsQualityTarget;
import com.ruoyi.qms.mapper.QmsQualityTargetMapper;
import com.ruoyi.qms.service.IQmsQualityTargetService;

@Service
public class QmsQualityTargetServiceImpl implements IQmsQualityTargetService {
    @Autowired
    private QmsQualityTargetMapper mapper;

    @Override
    public List<QmsQualityTarget> selectTargetList(QmsQualityTarget target) { return mapper.selectTargetList(target); }
    @Override
    public QmsQualityTarget selectTargetById(Long targetId) { return mapper.selectTargetById(targetId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTarget(QmsQualityTarget target) {
        target.setDelFlag("0"); target.setStatus("0");
        // 自动判断达成
        checkAchievement(target);
        return mapper.insertTarget(target);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTarget(QmsQualityTarget target) {
        checkAchievement(target);
        return mapper.updateTarget(target);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTargetByIds(Long[] targetIds) { return mapper.deleteTargetByIds(targetIds); }

    private void checkAchievement(QmsQualityTarget target) {
        boolean achieved = true;
        if (target.getActualPassRate() != null && target.getTargetPassRate() != null
            && target.getActualPassRate().compareTo(target.getTargetPassRate()) < 0) achieved = false;
        if (target.getActualPpm() != null && target.getTargetPpm() != null
            && target.getActualPpm() > target.getTargetPpm()) achieved = false;
        if (target.getActualComplaintRate() != null && target.getTargetComplaintRate() != null
            && target.getActualComplaintRate().compareTo(target.getTargetComplaintRate()) > 0) achieved = false;
        if (target.getActualCapaCloseRate() != null && target.getTargetCapaCloseRate() != null
            && target.getActualCapaCloseRate().compareTo(target.getTargetCapaCloseRate()) < 0) achieved = false;
        target.setAchievement(achieved ? "Y" : "N");
    }
}
