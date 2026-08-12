package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsWorkshop;

/**
 * 车间/产线/工位 Mapper接口
 *
 * @author ruoyi
 */
public interface QmsWorkshopMapper
{
    public List<QmsWorkshop> selectWorkshopList(QmsWorkshop workshop);

    public QmsWorkshop selectWorkshopById(Long workshopId);

    public int insertWorkshop(QmsWorkshop workshop);

    public int updateWorkshop(QmsWorkshop workshop);

    public int deleteWorkshopByIds(Long[] workshopIds);

    public int hasChildByWorkshopId(Long workshopId);
}
