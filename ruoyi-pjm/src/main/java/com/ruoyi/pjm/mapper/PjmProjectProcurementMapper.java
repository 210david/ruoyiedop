package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectProcurement;

/**
 * 项目立项采购清单 Mapper
 */
public interface PjmProjectProcurementMapper
{
    public PjmProjectProcurement selectPjmProjectProcurementById(Long procurementId);

    public List<PjmProjectProcurement> selectPjmProjectProcurementList(PjmProjectProcurement procurement);

    /** 按项目ID查询 */
    public List<PjmProjectProcurement> selectByProjectId(Long projectId);

    public int insertPjmProjectProcurement(PjmProjectProcurement procurement);

    public int updatePjmProjectProcurement(PjmProjectProcurement procurement);

    public int deletePjmProjectProcurementByIds(Long[] procurementIds);

    public int deletePjmProjectProcurementById(Long procurementId);

    /** 按项目ID全部删除 */
    public int deleteByProjectId(Long projectId);
}
