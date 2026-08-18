package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsAbnormal;

public interface MmsAbnormalMapper {
    public List<MmsAbnormal> selectAbnormalList(MmsAbnormal abnormal);
    public MmsAbnormal selectAbnormalById(Long abnormalId);
    public int insertAbnormal(MmsAbnormal abnormal);
    public int updateAbnormal(MmsAbnormal abnormal);
    public int deleteAbnormalByIds(Long[] abnormalIds);
}