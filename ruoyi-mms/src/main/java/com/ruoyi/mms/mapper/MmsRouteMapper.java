package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.domain.MmsRouteProcess;
import com.ruoyi.mms.domain.MmsRouteVersionLog;

public interface MmsRouteMapper {
    public List<MmsRoute> selectRouteList(MmsRoute route);
    public MmsRoute selectRouteById(Long routeId);
    public int insertRoute(MmsRoute route);
    public int updateRoute(MmsRoute route);
    public int deleteRouteByIds(Long[] routeIds);
    public List<MmsRouteProcess> selectRouteProcessByRouteId(Long routeId);
    public int insertRouteProcess(MmsRouteProcess rp);
    public int deleteRouteProcessByRouteId(Long routeId);
    public int updateRouteStatus(MmsRoute route);
    public List<MmsRouteVersionLog> selectVersionLogByRouteId(Long routeId);
    public int insertVersionLog(MmsRouteVersionLog log);
}
