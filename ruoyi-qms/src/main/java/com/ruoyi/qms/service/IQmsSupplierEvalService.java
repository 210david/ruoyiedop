package com.ruoyi.qms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.qms.domain.QmsSupplierEval;

public interface IQmsSupplierEvalService {
    public List<QmsSupplierEval> selectEvalList(QmsSupplierEval eval);
    public QmsSupplierEval selectEvalById(Long evalId);
    public int insertEval(QmsSupplierEval eval);
    public int updateEval(QmsSupplierEval eval);
    public int deleteEvalByIds(Long[] evalIds);

    /** 根据供应商ID和评价周期统计来料检验批次（从检验任务表自动统计） */
    public Map<String, Object> selectBatchStats(Long supplierId, String period);
}
