package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsWorkshop;

/**
 * 车间/产线/工位 Service接口
 *
 * @author ruoyi
 */
public interface IQmsWorkshopService
{
    public List<QmsWorkshop> selectWorkshopList(QmsWorkshop workshop);

    /** 构建树形结构 */
    public List<QmsWorkshop> buildWorkshopTree(List<QmsWorkshop> list);

    public QmsWorkshop selectWorkshopById(Long workshopId);

    public int insertWorkshop(QmsWorkshop workshop);

    public int updateWorkshop(QmsWorkshop workshop);

    public int deleteWorkshopByIds(Long[] workshopIds);
}
