package com.ruoyi.pms.mapper;

import java.util.List;
import com.ruoyi.pms.domain.PmsSupplierEval;

/**
 * 供应商评估 Mapper
 *
 * @author ruoyi
 */
public interface PmsSupplierEvalMapper
{
    public List<PmsSupplierEval> selectEvalList(PmsSupplierEval eval);
    public PmsSupplierEval selectEvalById(Long evalId);
    public int insertEval(PmsSupplierEval eval);
    public int updateEval(PmsSupplierEval eval);
    public int deleteEvalByIds(Long[] evalIds);
}
