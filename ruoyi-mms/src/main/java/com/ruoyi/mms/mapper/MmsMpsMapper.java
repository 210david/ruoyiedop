package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsMps;

public interface MmsMpsMapper {
    public List<MmsMps> selectMpsList(MmsMps mps);
    public MmsMps selectMpsById(Long mpsId);
    public int insertMps(MmsMps mps);
    public int updateMps(MmsMps mps);
    public int deleteMpsByIds(Long[] mpsIds);
}