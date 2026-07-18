package com.ruoyi.dms.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.dms.domain.DmsInspectionRoute;
import com.ruoyi.dms.mapper.DmsInspectionRouteMapper;
import com.ruoyi.dms.service.IDmsInspectionRouteService;

@Service
public class DmsInspectionRouteServiceImpl implements IDmsInspectionRouteService
{
    @Autowired
    private DmsInspectionRouteMapper dmsInspectionRouteMapper;

    @Override
    public List<DmsInspectionRoute> selectRouteList(DmsInspectionRoute route) { return dmsInspectionRouteMapper.selectRouteList(route); }
    @Override
    public DmsInspectionRoute selectRouteById(Long routeId) { return dmsInspectionRouteMapper.selectRouteById(routeId); }
    @Override
    public int insertRoute(DmsInspectionRoute route)
    {
        route.setDelFlag("0");
        // 自动生成路线编码：RT + yyyyMMdd + 001
        if (route.getRouteCode() == null || route.getRouteCode().isEmpty())
        {
            route.setRouteCode(generateRouteCode());
        }
        return dmsInspectionRouteMapper.insertRoute(route);
    }
    @Override
    public int updateRoute(DmsInspectionRoute route) { return dmsInspectionRouteMapper.updateRoute(route); }
    @Override
    public int deleteRouteByIds(Long[] routeIds) { return dmsInspectionRouteMapper.deleteRouteByIds(routeIds); }

    /**
     * 自动生成路线编码
     * 格式：RT + yyyyMMdd + 001
     */
    private synchronized String generateRouteCode()
    {
        String dateStr = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String maxCode = dmsInspectionRouteMapper.selectMaxRouteCode();
        int seq = 1;
        if (maxCode != null && maxCode.length() > 10 && maxCode.contains(dateStr))
        {
            try { seq = Integer.parseInt(maxCode.substring(10)) + 1; } catch (Exception e) { seq = 1; }
        }
        return "RT" + dateStr + String.format("%03d", seq);
    }
}
