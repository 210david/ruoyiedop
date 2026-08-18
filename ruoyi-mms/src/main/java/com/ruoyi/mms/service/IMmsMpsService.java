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

    /** 计划确认/提交审批：0(草稿) → 1(已确认) */
    public int confirmMps(Long mpsId);

    /** 计划审批：1(已确认) → 2(已发布) 或 驳回→0(草稿) */
    public int auditMps(Long mpsId, String status, String auditOpinion);

    /** 计划发布（下达生成工单）：2(已发布) → 3(已下达)，返回生成的工单ID */
    public Long releaseMps(Long mpsId);

    /** 计划取消：非3(已下达) → 4(已取消) */
    public int cancelMps(Long mpsId, String cancelReason);
}
