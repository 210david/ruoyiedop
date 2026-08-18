package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsWorkshop;

/**
 * 车间 Mapper
 */
public interface MmsWorkshopMapper
{
    List<MmsWorkshop> selectWorkshopList(MmsWorkshop workshop);
    MmsWorkshop selectWorkshopById(Long workshopId);
    int insertWorkshop(MmsWorkshop workshop);
    int updateWorkshop(MmsWorkshop workshop);
    int deleteWorkshopByIds(Long[] workshopIds);
}
