package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsMps;

/**
 * 主生产计划 Service接口
 *
 * @author ruoyi
 */
public interface IMmsMpsService
{
    /** 标准CRUD */
    public List<MmsMps> selectMpsList(MmsMps mps);
    public MmsMps selectMpsById(Long mpsId);
    public int insertMps(MmsMps mps);
    public int updateMps(MmsMps mps);
    public int deleteMpsByIds(Long[] mpsIds);

    /** 计划提交审批：0(草稿) → 1(待审批) */
    public int submitMps(Long mpsId);

    /** 计划审批：1(待审批) → 2(已审批) 或 驳回→0(草稿) */
    public int auditMps(Long mpsId, String status, String auditOpinion);

    /** 计划下达（生成工单）：2(已审批) → 3(已下达)，返回生成的工单ID */
    public Long releaseMps(Long mpsId);

    /** 计划取消：非3(已下达) → 4(已取消) */
    public int cancelMps(Long mpsId, String cancelReason);
}
