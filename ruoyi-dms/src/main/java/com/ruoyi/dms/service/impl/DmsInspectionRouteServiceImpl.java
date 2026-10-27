package com.ruoyi.dms.service.impl;
import java.util.HashMap;
import java.util.Map;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.dms.domain.DmsInspectionRoute;
import com.ruoyi.dms.mapper.DmsInspectionRouteMapper;
import com.ruoyi.dms.service.IDmsInspectionRouteService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class DmsInspectionRouteServiceImpl implements IDmsInspectionRouteService
{
    @Autowired
    private DmsInspectionRouteMapper dmsInspectionRouteMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<DmsInspectionRoute> selectRouteList(DmsInspectionRoute route) { return dmsInspectionRouteMapper.selectRouteList(route); }
    @Override
    public DmsInspectionRoute selectRouteById(Long routeId) { return dmsInspectionRouteMapper.selectRouteById(routeId); }
    @Override
    public int insertRoute(DmsInspectionRoute route)
    {
        route.setDelFlag("0");
        // 自动生成路线编码
        if (StringUtils.isEmpty(route.getRouteCode()))
        {
            Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(route.getCycleType())) { params.put("cycle", route.getCycleType()); }
        route.setRouteCode(mkNumberRuleService.generateNumber("dms_inspection_route", params));
        }
        return dmsInspectionRouteMapper.insertRoute(route);
    }
    @Override
    public int updateRoute(DmsInspectionRoute route) { return dmsInspectionRouteMapper.updateRoute(route); }
    @Override
    public int deleteRouteByIds(Long[] routeIds) { return dmsInspectionRouteMapper.deleteRouteByIds(routeIds); }

    /**
     * 按状态统计巡检路线数（含全部）
     */
    @Override
    public Map<String, Object> countRouteByStatus()
    {
        Map<String, Object> result = new HashMap<>();
        result.put("all", dmsInspectionRouteMapper.countAllRoutes());
        List<Map<String, Object>> statusCounts = dmsInspectionRouteMapper.countRouteByStatus();
        if (statusCounts != null)
        {
            for (Map<String, Object> item : statusCounts)
            {
                String status = String.valueOf(item.get("status"));
                Object count = item.get("cnt");
                result.put(status, count);
            }
        }
        return result;
    }

    /**
     * 自动生成路线编码（使用编号规则服务）
     */
    private synchronized String generateRouteCode()
    {
        return mkNumberRuleService.generateNumber("dms_inspection_route");
    }
}
