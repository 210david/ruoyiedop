package com.ruoyi.pjm.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.pjm.domain.PjmProjectProcurement;
import com.ruoyi.pjm.mapper.PjmProjectProcurementMapper;
import com.ruoyi.pjm.service.IPjmProjectProcurementService;

/**
 * 项目立项采购清单 Service 实现
 */
@Service
public class PjmProjectProcurementServiceImpl implements IPjmProjectProcurementService
{
    @Autowired
    private PjmProjectProcurementMapper procurementMapper;

    @Override
    public PjmProjectProcurement selectPjmProjectProcurementById(Long procurementId)
    {
        return procurementMapper.selectPjmProjectProcurementById(procurementId);
    }

    @Override
    public List<PjmProjectProcurement> selectPjmProjectProcurementList(PjmProjectProcurement procurement)
    {
        return procurementMapper.selectPjmProjectProcurementList(procurement);
    }

    @Override
    public List<PjmProjectProcurement> selectByProjectId(Long projectId)
    {
        return procurementMapper.selectByProjectId(projectId);
    }

    @Override
    public int insertPjmProjectProcurement(PjmProjectProcurement procurement)
    {
        if (procurement.getDelFlag() == null) procurement.setDelFlag("0");
        // 自动计算金额
        computeAmount(procurement);
        return procurementMapper.insertPjmProjectProcurement(procurement);
    }

    @Override
    public int updatePjmProjectProcurement(PjmProjectProcurement procurement)
    {
        computeAmount(procurement);
        return procurementMapper.updatePjmProjectProcurement(procurement);
    }

    @Override
    public int deletePjmProjectProcurementByIds(Long[] procurementIds)
    {
        return procurementMapper.deletePjmProjectProcurementByIds(procurementIds);
    }

    @Override
    public int deletePjmProjectProcurementById(Long procurementId)
    {
        return procurementMapper.deletePjmProjectProcurementById(procurementId);
    }

    @Override
    public int deleteByProjectId(Long projectId)
    {
        return procurementMapper.deleteByProjectId(projectId);
    }

    private void computeAmount(PjmProjectProcurement p) {
        if (p.getAmount() == null || BigDecimal.ZERO.compareTo(p.getAmount()) == 0) {
            if (p.getQty() != null && p.getUnitPrice() != null) {
                p.setAmount(p.getQty().multiply(p.getUnitPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
            }
        }
    }
}
