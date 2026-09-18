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

    /**
     * 达成判定（通用标准：一票否决 + 有效性门槛）：
     * 1. 仅当至少存在一组"目标值与实际值均已填写"的指标时才进行判定；
     * 2. 任一已对比指标未达标（合格率/CAPA关闭率低于目标，PPM/客诉率高于目标）即未达成(N)；
     * 3. 全部已对比指标达标为达成(Y)；
     * 4. 无任何有效对比（目标或实际全为空）时为待定(P)，不默认达成。
     */
    private void checkAchievement(QmsQualityTarget target) {
        boolean achieved = true;
        boolean hasComparison = false;
        if (target.getActualPassRate() != null && target.getTargetPassRate() != null) {
            hasComparison = true;
            if (target.getActualPassRate().compareTo(target.getTargetPassRate()) < 0) achieved = false;
        }
        if (target.getActualPpm() != null && target.getTargetPpm() != null) {
            hasComparison = true;
            if (target.getActualPpm() > target.getTargetPpm()) achieved = false;
        }
        if (target.getActualComplaintRate() != null && target.getTargetComplaintRate() != null) {
            hasComparison = true;
            if (target.getActualComplaintRate().compareTo(target.getTargetComplaintRate()) > 0) achieved = false;
        }
        if (target.getActualCapaCloseRate() != null && target.getTargetCapaCloseRate() != null) {
            hasComparison = true;
            if (target.getActualCapaCloseRate().compareTo(target.getTargetCapaCloseRate()) < 0) achieved = false;
        }
        // 无任何有效对比时为待定，不默认达成
        target.setAchievement(hasComparison ? (achieved ? "Y" : "N") : "P");
    }
}
