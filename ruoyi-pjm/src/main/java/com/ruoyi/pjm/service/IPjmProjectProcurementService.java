package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectProcurement;

/**
 * 项目立项采购清单 Service 接口
 */
public interface IPjmProjectProcurementService
{
    public PjmProjectProcurement selectPjmProjectProcurementById(Long procurementId);

    public List<PjmProjectProcurement> selectPjmProjectProcurementList(PjmProjectProcurement procurement);

    public List<PjmProjectProcurement> selectByProjectId(Long projectId);

    public int insertPjmProjectProcurement(PjmProjectProcurement procurement);

    public int updatePjmProjectProcurement(PjmProjectProcurement procurement);

    public int deletePjmProjectProcurementByIds(Long[] procurementIds);

    public int deletePjmProjectProcurementById(Long procurementId);

    public int deleteByProjectId(Long projectId);
}
