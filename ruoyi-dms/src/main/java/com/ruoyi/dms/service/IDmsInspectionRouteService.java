package com.ruoyi.dms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.dms.domain.DmsInspectionRoute;

public interface IDmsInspectionRouteService
{
    public List<DmsInspectionRoute> selectRouteList(DmsInspectionRoute route);
    public DmsInspectionRoute selectRouteById(Long routeId);
    public int insertRoute(DmsInspectionRoute route);
    public int updateRoute(DmsInspectionRoute route);
    public int deleteRouteByIds(Long[] routeIds);

    /** 按状态统计巡检路线数（含全部） */
    public Map<String, Object> countRouteByStatus();
}
