package com.ruoyi.qms.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.qms.domain.QmsSupplierEval;

public interface QmsSupplierEvalMapper {
    public List<QmsSupplierEval> selectEvalList(QmsSupplierEval eval);
    public QmsSupplierEval selectEvalById(Long evalId);
    public int insertEval(QmsSupplierEval eval);
    public int updateEval(QmsSupplierEval eval);
    public int deleteEvalByIds(Long[] evalIds);

    /** 根据供应商ID和评价周期统计来料检验批次（从 qms_insp_task 表） */
    public Map<String, Object> selectBatchStats(@Param("supplierId") Long supplierId, @Param("period") String period);
}
