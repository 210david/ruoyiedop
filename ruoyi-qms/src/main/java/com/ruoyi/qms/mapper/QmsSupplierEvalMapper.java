package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsSupplierEval;

public interface QmsSupplierEvalMapper {
    public List<QmsSupplierEval> selectEvalList(QmsSupplierEval eval);
    public QmsSupplierEval selectEvalById(Long evalId);
    public int insertEval(QmsSupplierEval eval);
    public int updateEval(QmsSupplierEval eval);
    public int deleteEvalByIds(Long[] evalIds);
}
