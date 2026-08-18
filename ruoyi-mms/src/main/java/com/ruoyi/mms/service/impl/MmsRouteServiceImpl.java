package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.domain.MmsRouteProcess;
import com.ruoyi.mms.domain.MmsRouteVersionLog;
import com.ruoyi.mms.mapper.MmsRouteMapper;
import com.ruoyi.mms.service.IMmsRouteService;

/**
 * 工艺路线 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsRouteServiceImpl implements IMmsRouteService
{
    @Autowired
    private MmsRouteMapper routeMapper;

    @Override
    public List<MmsRoute> selectRouteList(MmsRoute route)
    {
        return routeMapper.selectRouteList(route);
    }

    @Override
    public MmsRoute selectRouteById(Long RouteId)
    {
        MmsRoute route = routeMapper.selectRouteById(RouteId);
        if (route != null)
        {
            List<MmsRouteProcess> processList = routeMapper.selectRouteProcessByRouteId(RouteId);
            route.setProcessList(processList);
            if (processList != null)
            {
                route.setProcessCount(processList.size());
            }
        }
        return route;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRoute(MmsRoute route)
    {
        route.setDelFlag("0");
        if (route.getStatus() == null)
        {
            route.setStatus("0"); // 草稿
        }
        // 计算总标准工时
        calculateTotalStdTime(route);
        // 插入主表
        int rows = routeMapper.insertRoute(route);
        // 插入工序明细
        insertRouteProcesses(route);
        // 记录变更日志
        insertVersionLog(route, null, route.getVersion(), null, route.getStatus(), "create", "创建工艺路线");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRoute(MmsRoute route)
    {
        MmsRoute existing = routeMapper.selectRouteById(route.getRouteId());
        if (existing == null)
        {
            throw new ServiceException("工艺路线不存在");
        }
        // 已审核或已停用的路线不允许直接修改
        if ("2".equals(existing.getStatus()) || "3".equals(existing.getStatus()))
        {
            throw new ServiceException("已审核/已停用的工艺路线不允许修改，请复制新版本后编辑");
        }
        // 计算总标准工时
        calculateTotalStdTime(route);
        // 删除旧明细
        routeMapper.deleteRouteProcessByRouteId(route.getRouteId());
        // 更新主表
        int rows = routeMapper.updateRoute(route);
        // 插入新明细
        insertRouteProcesses(route);
        // 记录变更日志
        insertVersionLog(route, existing.getVersion(), route.getVersion(), existing.getStatus(), route.getStatus(), "update", "修改工艺路线");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRouteByIds(Long[] RouteIds)
    {
        return routeMapper.deleteRouteByIds(RouteIds);
    }

    @Override
    public List<MmsRouteProcess> selectRouteProcessByRouteId(Long RouteId)
    {
        return routeMapper.selectRouteProcessByRouteId(RouteId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int enableRoute(Long routeId)
    {
        MmsRoute route = routeMapper.selectRouteById(routeId);
        if (route == null)
        {
            throw new ServiceException("工艺路线不存在");
        }
        if (!"0".equals(route.getStatus()) && !"3".equals(route.getStatus()))
        {
            throw new ServiceException("只有草稿或已停用的路线才能启用");
        }
        if (route.getProcessList() == null)
        {
            route.setProcessList(routeMapper.selectRouteProcessByRouteId(routeId));
        }
        if (route.getProcessList() == null || route.getProcessList().isEmpty())
        {
            throw new ServiceException("工艺路线没有工序，无法启用");
        }
        MmsRoute update = new MmsRoute();
        update.setRouteId(routeId);
        update.setStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = routeMapper.updateRouteStatus(update);
        // 记录变更日志
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), "1", "enable", "启用工艺路线");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRoute(Long routeId)
    {
        MmsRoute route = routeMapper.selectRouteById(routeId);
        if (route == null)
        {
            throw new ServiceException("工艺路线不存在");
        }
        if (!"1".equals(route.getStatus()))
        {
            throw new ServiceException("只有已启用的路线才能审核");
        }
        MmsRoute update = new MmsRoute();
        update.setRouteId(routeId);
        update.setStatus("2");
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(DateUtils.getNowDate());
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = routeMapper.updateRouteStatus(update);
        // 记录变更日志
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), "2", "audit", "审核通过");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int disableRoute(Long routeId)
    {
        MmsRoute route = routeMapper.selectRouteById(routeId);
        if (route == null)
        {
            throw new ServiceException("工艺路线不存在");
        }
        if ("3".equals(route.getStatus()))
        {
            throw new ServiceException("路线已经是停用状态");
        }
        MmsRoute update = new MmsRoute();
        update.setRouteId(routeId);
        update.setStatus("3");
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = routeMapper.updateRouteStatus(update);
        // 记录变更日志
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), "3", "disable", "停用工艺路线");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MmsRoute copyRoute(Long routeId)
    {
        MmsRoute source = routeMapper.selectRouteById(routeId);
        if (source == null)
        {
            throw new ServiceException("源工艺路线不存在");
        }
        List<MmsRouteProcess> sourceProcesses = routeMapper.selectRouteProcessByRouteId(routeId);

        // 复制主表
        MmsRoute newRoute = new MmsRoute();
        newRoute.setRouteNo(source.getRouteNo() + "-COPY");
        newRoute.setRouteName(source.getRouteName());
        newRoute.setProductId(source.getProductId());
        newRoute.setProductCode(source.getProductCode());
        newRoute.setProductName(source.getProductName());
        newRoute.setVersion(incrementVersion(source.getVersion()));
        newRoute.setStatus("0"); // 草稿
        newRoute.setDefaultWorkshop(source.getDefaultWorkshop());
        newRoute.setDefaultLine(source.getDefaultLine());
        newRoute.setTotalStdTime(source.getTotalStdTime());
        newRoute.setDelFlag("0");
        newRoute.setCreateBy(SecurityUtils.getUsername());
        newRoute.setCreateTime(DateUtils.getNowDate());
        newRoute.setRemark(source.getRemark());
        routeMapper.insertRoute(newRoute);

        // 复制工序明细
        if (sourceProcesses != null)
        {
            for (MmsRouteProcess rp : sourceProcesses)
            {
                rp.setId(null);
                rp.setRouteId(newRoute.getRouteId());
                rp.setDelFlag("0");
                rp.setCreateBy(SecurityUtils.getUsername());
                rp.setCreateTime(DateUtils.getNowDate());
                routeMapper.insertRouteProcess(rp);
            }
            newRoute.setProcessList(sourceProcesses);
        }

        // 记录变更日志
        insertVersionLog(source, source.getVersion(), newRoute.getVersion(), source.getStatus(), "0", "copy", "复制为新版本：" + newRoute.getVersion());
        return newRoute;
    }

    @Override
    public List<MmsRouteVersionLog> selectVersionLogByRouteId(Long routeId)
    {
        return routeMapper.selectVersionLogByRouteId(routeId);
    }

    // ====== 私有辅助方法 ======

    /**
     * 计算总标准工时
     */
    private void calculateTotalStdTime(MmsRoute route)
    {
        BigDecimal total = BigDecimal.ZERO;
        if (route.getProcessList() != null)
        {
            for (MmsRouteProcess rp : route.getProcessList())
            {
                if (rp.getStdTime() != null)
                {
                    total = total.add(rp.getStdTime());
                }
            }
        }
        route.setTotalStdTime(total);
    }

    /**
     * 批量插入工序明细
     */
    private void insertRouteProcesses(MmsRoute route)
    {
        if (route.getProcessList() != null && !route.getProcessList().isEmpty())
        {
            int seq = 10;
            for (MmsRouteProcess rp : route.getProcessList())
            {
                rp.setRouteId(route.getRouteId());
                rp.setDelFlag("0");
                if (rp.getStepSeq() == null)
                {
                    rp.setStepSeq(seq);
                    seq += 10;
                }
                if (rp.getIsKeyProcess() == null)
                {
                    rp.setIsKeyProcess("0");
                }
                if (rp.getIsOutsource() == null)
                {
                    rp.setIsOutsource("0");
                }
                if (rp.getPrepTime() == null)
                {
                    rp.setPrepTime(BigDecimal.ZERO);
                }
                rp.setCreateBy(SecurityUtils.getUsername());
                rp.setCreateTime(DateUtils.getNowDate());
                routeMapper.insertRouteProcess(rp);
            }
        }
    }

    /**
     * 插入版本变更记录
     */
    private void insertVersionLog(MmsRoute route, String oldVersion, String newVersion, String oldStatus, String newStatus, String changeType, String desc)
    {
        MmsRouteVersionLog log = new MmsRouteVersionLog();
        log.setRouteId(route.getRouteId());
        log.setRouteNo(route.getRouteNo());
        log.setOldVersion(oldVersion);
        log.setNewVersion(newVersion);
        log.setOldStatus(oldStatus);
        log.setNewStatus(newStatus);
        log.setChangeType(changeType);
        log.setChangeDesc(desc);
        log.setChangeBy(SecurityUtils.getUsername());
        log.setChangeTime(DateUtils.getNowDate());
        routeMapper.insertVersionLog(log);
    }

    /**
     * 版本号递增（1.0 → 1.1, 1.9 → 2.0, 2.0 → 2.1 ...）
     */
    private String incrementVersion(String version)
    {
        if (version == null || version.isEmpty())
        {
            return "1.0";
        }
        try
        {
            String[] parts = version.split("\\.");
            int major = Integer.parseInt(parts[0]);
            int minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            minor++;
            if (minor >= 10)
            {
                major++;
                minor = 0;
            }
            return major + "." + minor;
        }
        catch (Exception e)
        {
            return version + "-v2";
        }
    }
}
