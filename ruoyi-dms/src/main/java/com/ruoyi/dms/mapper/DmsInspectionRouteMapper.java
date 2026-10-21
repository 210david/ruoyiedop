package com.ruoyi.dms.mapper;

import java.util.List;
import java.util.Map;
import com.ruoyi.dms.domain.DmsInspectionRoute;

public interface DmsInspectionRouteMapper
{
    public List<DmsInspectionRoute> selectRouteList(DmsInspectionRoute route);
    public DmsInspectionRoute selectRouteById(Long routeId);
    public int insertRoute(DmsInspectionRoute route);
    public int updateRoute(DmsInspectionRoute route);
    public int deleteRouteByIds(Long[] routeIds);

    /** 查询最大的路线编码（用于自动生成编号） */
    public String selectMaxRouteCode();

    /** 统计全部巡检路线数 */
    public int countAllRoutes();

    /** 按状态统计巡检路线数 */
    public List<Map<String, Object>> countRouteByStatus();
}
