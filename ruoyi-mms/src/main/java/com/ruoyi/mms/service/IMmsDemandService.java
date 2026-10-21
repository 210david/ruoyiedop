package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsDemand;
import com.ruoyi.mms.domain.MmsMps;

/**
 * 生产需求 Service接口
 *
 * @author ruoyi
 */
public interface IMmsDemandService
{
    public List<MmsDemand> selectDemandList(MmsDemand demand);
    public MmsDemand selectDemandById(Long DemandId);
    public int insertDemand(MmsDemand demand);
    public int updateDemand(MmsDemand demand);
    public int deleteDemandByIds(Long[] DemandIds);

    /** 需求确认：0(草稿) → 1(已确认) */
    public int confirmDemand(Long demandId);

    /** 需求取消确认：1(已确认) → 0(草稿) */
    public int unconfirmDemand(Long demandId);

    /** 需求转计划：根据需求创建MPS草稿，并回写需求状态为已排产(2)，返回创建的MPS */
    public MmsMps convertToMps(Long demandId);
}
