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
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.domain.MmsRouteAuditLog;
import com.ruoyi.mms.domain.MmsRouteProcess;
import com.ruoyi.mms.domain.MmsRouteVersionLog;
import com.ruoyi.mms.mapper.MmsRouteAuditLogMapper;
import com.ruoyi.mms.mapper.MmsRouteMapper;
import com.ruoyi.mms.service.IMmsRouteService;
import com.ruoyi.mk.service.IMkNumberRuleService;

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

    @Autowired
    private MmsRouteAuditLogMapper routeAuditLogMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

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
            // 加载审核日志
            List<MmsRouteAuditLog> auditLogList = routeAuditLogMapper.selectAuditLogByRouteId(RouteId);
            route.setAuditLogList(auditLogList);
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
        // 自动生成路线编号（如果Controller未提前生成）
        if (StringUtils.isEmpty(route.getRouteNo()))
        {
            route.setRouteNo(generateRouteNo());
        }
        // 计算总标准工时
        calculateTotalStdTime(route);
        route.setCreateBy(SecurityUtils.getUsername());
        route.setCreateTime(DateUtils.getNowDate());
        // 插入主表
        int rows = routeMapper.insertRoute(route);
        // 插入工序明细
        insertRouteProcesses(route);
        // 记录变更日志
        insertVersionLog(route, null, route.getVersion(), null, route.getStatus(), "create", "创建工艺路线", null);
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
        // 已驳回状态修改后重置为待审核，清空审核信息
        if ("4".equals(existing.getStatus()))
        {
            MmsRoute resetUpdate = new MmsRoute();
            resetUpdate.setRouteId(route.getRouteId());
            resetUpdate.setStatus("1");
            resetUpdate.setAuditBy("");
            resetUpdate.setAuditTime(DateUtils.getNowDate());
            resetUpdate.setAuditRemark("");
            resetUpdate.setUpdateBy(SecurityUtils.getUsername());
            resetUpdate.setUpdateTime(DateUtils.getNowDate());
            routeMapper.updateRouteStatus(resetUpdate);
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
        insertVersionLog(route, existing.getVersion(), route.getVersion(), existing.getStatus(), route.getStatus(), "update", "修改工艺路线", null);
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
        if (!"0".equals(route.getStatus()) && !"3".equals(route.getStatus()) && !"4".equals(route.getStatus()))
        {
            throw new ServiceException("只有草稿、已停用或已驳回的路线才能提交审核");
        }
        if (route.getProcessList() == null)
        {
            route.setProcessList(routeMapper.selectRouteProcessByRouteId(routeId));
        }
        if (route.getProcessList() == null || route.getProcessList().isEmpty())
        {
            throw new ServiceException("工艺路线没有工序，无法提交审核");
        }
        // 唯一性校验：同一产品同一时间只能有一条已审核的工艺路线
        // 在提交审核时就拦截，避免用户准备审核后才发现冲突
        int count = routeMapper.countAuditedByProductId(route.getProductId());
        if (count > 0)
        {
            throw new ServiceException("产品[" + route.getProductCode() + "]已存在已审核的工艺路线，请先停用旧版本再提交审核");
        }
        MmsRoute update = new MmsRoute();
        update.setRouteId(routeId);
        update.setStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = routeMapper.updateRouteStatus(update);
        // 记录变更日志
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), "1", "enable", "提交审核", null);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditRoute(Long routeId, String auditAction, String auditRemark)
    {
        MmsRoute route = routeMapper.selectRouteById(routeId);
        if (route == null)
        {
            throw new ServiceException("工艺路线不存在");
        }
        if (!"1".equals(route.getStatus()))
        {
            throw new ServiceException("只有待审核的路线才能审核");
        }
        if (!"1".equals(auditAction) && !"2".equals(auditAction))
        {
            throw new ServiceException("审核动作不合法");
        }
        // 1=通过→已审核(2), 2=驳回→已驳回(4)
        String newStatus = "1".equals(auditAction) ? "2" : "4";
        String changeDesc = "1".equals(auditAction) ? "审核通过" : "审核驳回";
        MmsRoute update = new MmsRoute();
        update.setRouteId(routeId);
        update.setStatus(newStatus);
        update.setAuditBy(SecurityUtils.getUsername());
        update.setAuditTime(DateUtils.getNowDate());
        update.setAuditRemark(auditRemark);
        update.setUpdateBy(SecurityUtils.getUsername());
        update.setUpdateTime(DateUtils.getNowDate());
        int rows = routeMapper.updateRouteStatus(update);
        // 记录变更日志
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), newStatus, "audit", changeDesc, auditRemark);
        // 写入审核日志
        MmsRouteAuditLog auditLog = new MmsRouteAuditLog();
        auditLog.setRouteId(routeId);
        auditLog.setAuditAction(auditAction);
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditTime(DateUtils.getNowDate());
        auditLog.setAuditRemark(auditRemark);
        routeAuditLogMapper.insertAuditLog(auditLog);
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
        insertVersionLog(route, route.getVersion(), route.getVersion(), route.getStatus(), "3", "disable", "停用工艺路线", null);
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
        newRoute.setRouteNo(generateRouteNo());
        newRoute.setRouteName(source.getRouteName());
        newRoute.setProductId(source.getProductId());
        newRoute.setProductCode(source.getProductCode());
        newRoute.setProductName(source.getProductName());
        newRoute.setVersion(incrementVersion(source.getVersion()));
        newRoute.setStatus("0"); // 草稿
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
        insertVersionLog(source, source.getVersion(), newRoute.getVersion(), source.getStatus(), "0", "copy", "复制为新版本：" + newRoute.getVersion(), null);
        return newRoute;
    }

    @Override
    public List<MmsRouteVersionLog> selectVersionLogByRouteId(Long routeId)
    {
        return routeMapper.selectVersionLogByRouteId(routeId);
    }

    // ====== 私有辅助方法 ======

    /**
     * 自动生成路线编号
     * 通过编码规则服务（mk_number_rule 表中 rule_code='mms_route'）生成标准编号。
     * 编号格式：RT-yyyyMMdd-0001（前缀+日期+流水号）
     * 如果编号规则未配置或生成失败，使用时间戳兜底。
     * 注意：此方法为 public 且无 @Transactional 注解，在 Controller 中独立调用，
     * 不参与 insertRoute 的事务，避免编号生成失败导致事务被标记为 rollback-only。
     */
    @Override
    public String generateRouteNo()
    {
        try
        {
            return mkNumberRuleService.generateNumber("mms_route");
        }
        catch (Exception e)
        {
            // 如果编号规则未配置或生成失败，使用时间戳兜底
            String timestamp = new java.text.SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
            return "RT" + timestamp;
        }
    }

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
     * @param auditRemark 审核意见（仅审核操作时有值，其他传 null）
     */
    private void insertVersionLog(MmsRoute route, String oldVersion, String newVersion, String oldStatus, String newStatus, String changeType, String desc, String auditRemark)
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
        log.setAuditRemark(auditRemark);
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
