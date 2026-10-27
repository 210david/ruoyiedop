package com.ruoyi.qms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.qms.domain.QmsSupplierEval;
import com.ruoyi.qms.mapper.QmsSupplierEvalMapper;
import com.ruoyi.qms.service.IQmsSupplierEvalService;
import com.ruoyi.qms.service.ISqmScorePushService;

@Service
public class QmsSupplierEvalServiceImpl implements IQmsSupplierEvalService {
    @Autowired
    private QmsSupplierEvalMapper mapper;

    @Autowired
    private ISqmScorePushService pushService;

    @Override
    public List<QmsSupplierEval> selectEvalList(QmsSupplierEval eval) { return mapper.selectEvalList(eval); }

    @Override
    public QmsSupplierEval selectEvalById(Long evalId) { return mapper.selectEvalById(evalId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertEval(QmsSupplierEval eval) {
        calcScore(eval);
        eval.setDelFlag("0");
        eval.setStatus("0");
        if (eval.getEvalStatus() == null) eval.setEvalStatus("0");
        int rows = mapper.insertEval(eval);
        // 评分完成后推送至 PMS（FR-SQM-06）
        if (rows > 0 && eval.getTotalScore() != null) {
            pushService.pushScoreToPms(eval);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateEval(QmsSupplierEval eval) {
        calcScore(eval);
        int rows = mapper.updateEval(eval);
        // 评分更新后推送至 PMS（FR-SQM-06）
        if (rows > 0 && eval.getTotalScore() != null) {
            pushService.pushScoreToPms(eval);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteEvalByIds(Long[] evalIds) { return mapper.deleteEvalByIds(evalIds); }

    @Override
    public Map<String, Object> selectBatchStats(Long supplierId, String period) {
        return mapper.selectBatchStats(supplierId, period);
    }

    private void calcScore(QmsSupplierEval eval) {
        // 自动计算合格率
        if (eval.getIncomingTotal() != null && eval.getIncomingTotal() > 0 && eval.getIncomingPass() != null) {
            eval.setPassRate(new BigDecimal(eval.getIncomingPass() * 100).divide(new BigDecimal(eval.getIncomingTotal()), 2, RoundingMode.HALF_UP));
            // PPM = (total - pass) / total * 1000000
            int defect = eval.getIncomingTotal() - eval.getIncomingPass();
            eval.setPpm(new BigDecimal(defect * 1000000L).divide(new BigDecimal(eval.getIncomingTotal()), 2, RoundingMode.HALF_UP));
        }
        // 自动计算综合评分
        if (eval.getQualityScore() != null && eval.getDeliveryScore() != null && eval.getServiceScore() != null) {
            BigDecimal total = eval.getQualityScore().multiply(new BigDecimal("0.5"))
                .add(eval.getDeliveryScore().multiply(new BigDecimal("0.3"))
                .add(eval.getServiceScore().multiply(new BigDecimal("0.2"))));
            eval.setTotalScore(total.setScale(2, RoundingMode.HALF_UP));
            // 自动评级
            if (total.compareTo(new BigDecimal("90")) >= 0) eval.setGrade("A");
            else if (total.compareTo(new BigDecimal("80")) >= 0) eval.setGrade("B");
            else if (total.compareTo(new BigDecimal("70")) >= 0) eval.setGrade("C");
            else eval.setGrade("D");
        }
    }
}
