package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsDemand;

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
}
