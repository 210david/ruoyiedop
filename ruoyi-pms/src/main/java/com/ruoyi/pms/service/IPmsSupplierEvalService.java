package com.ruoyi.pms.service;

import java.util.List;
import com.ruoyi.pms.domain.PmsSupplierEval;

/**
 * 供应商评估 Service接口
 *
 * @author ruoyi
 */
public interface IPmsSupplierEvalService
{
    public List<PmsSupplierEval> selectEvalList(PmsSupplierEval eval);
    public PmsSupplierEval selectEvalById(Long evalId);
    public int insertEval(PmsSupplierEval eval);
    public int updateEval(PmsSupplierEval eval);
    public int deleteEvalByIds(Long[] evalIds);

    /** 审批 */
    public int auditEval(Long evalId, String status, String auditRemark);
}
