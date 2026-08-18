package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsDemand;

public interface MmsDemandMapper {
    public List<MmsDemand> selectDemandList(MmsDemand demand);
    public MmsDemand selectDemandById(Long demandId);
    public int insertDemand(MmsDemand demand);
    public int updateDemand(MmsDemand demand);
    public int deleteDemandByIds(Long[] demandIds);
}