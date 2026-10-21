package com.ruoyi.pms.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
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

    /** 复评到期提醒：最近已审核评估超过复评周期的供应商 */
    public List<Map<String, Object>> selectReevalDueList();

    /** 供应商最近一次已审核评估（等级/日期） */
    public Map<String, Object> selectLatestAuditedEval(@Param("supplierId") Long supplierId);

    /** 供应商复评周期（月） */
    public Integer selectSupplierReevalCycle(@Param("supplierId") Long supplierId);
}
