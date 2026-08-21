package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.domain.MmsRouteProcess;
import com.ruoyi.mms.domain.MmsRouteVersionLog;

/**
 * 工艺路线 Service接口
 *
 * @author ruoyi
 */
public interface IMmsRouteService
{
    public List<MmsRoute> selectRouteList(MmsRoute route);
    public MmsRoute selectRouteById(Long RouteId);

    /** 自动生成路线编号（非事务方法，独立于insertRoute事务） */
    public String generateRouteNo();
    public int insertRoute(MmsRoute route);
    public int updateRoute(MmsRoute route);
    public int deleteRouteByIds(Long[] RouteIds);
    public List<MmsRouteProcess> selectRouteProcessByRouteId(Long RouteId);

    /** 启用路线（草稿→已启用） */
    public int enableRoute(Long routeId);
    /** 审核路线（已启用→已审核/已驳回） */
    public int auditRoute(Long routeId, String auditAction, String auditRemark);
    /** 停用路线（草稿/已启用/已审核→已停用） */
    public int disableRoute(Long routeId);
    /** 复制路线为新版本 */
    public MmsRoute copyRoute(Long routeId);
    /** 版本变更记录 */
    public List<MmsRouteVersionLog> selectVersionLogByRouteId(Long routeId);
}
