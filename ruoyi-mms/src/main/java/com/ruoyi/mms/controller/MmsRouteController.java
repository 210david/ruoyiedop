package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsRoute;
import com.ruoyi.mms.service.IMmsRouteService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 工艺路线 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/route")
public class MmsRouteController extends BaseController
{
    @Autowired
    private IMmsRouteService mmsRouteService;

    @PreAuthorize("@ss.hasPermi('mms:route:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsRoute route)
    {
        startPage();
        List<MmsRoute> list = mmsRouteService.selectRouteList(route);
        return getDataTable(list);
    }

    @Log(title = "工艺路线", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:route:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsRoute route)
    {
        List<MmsRoute> list = mmsRouteService.selectRouteList(route);
        ExcelUtil<MmsRoute> util = new ExcelUtil<>(MmsRoute.class);
        util.exportExcel(response, list, "工艺路线");
    }

    @PreAuthorize("@ss.hasPermi('mms:route:query')")
    @GetMapping(value = "/{routeId}")
    public AjaxResult getInfo(@PathVariable("routeId") Long routeId)
    {
        return AjaxResult.success(mmsRouteService.selectRouteById(routeId));
    }

    @Log(title = "工艺路线", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:route:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsRoute route)
    {
        // 在事务开始前生成编号（避免编号服务事务失败影响insertRoute事务）
        if (route.getRouteNo() == null || route.getRouteNo().isEmpty())
        {
            route.setRouteNo(mmsRouteService.generateRouteNo());
        }
        mmsRouteService.insertRoute(route);
        return AjaxResult.success(route.getRouteId());
    }

    @Log(title = "工艺路线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:route:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsRoute route)
    {
        return toAjax(mmsRouteService.updateRoute(route));
    }

    @Log(title = "工艺路线", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:route:remove')")
    @DeleteMapping("/{routeIds}")
    public AjaxResult remove(@PathVariable Long[] routeIds)
    {
        return toAjax(mmsRouteService.deleteRouteByIds(routeIds));
    }

    /**
     * 启用路线（草稿→已启用）
     */
    @Log(title = "工艺路线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:route:status')")
    @PutMapping("/enable/{routeId}")
    public AjaxResult enable(@PathVariable("routeId") Long routeId)
    {
        return toAjax(mmsRouteService.enableRoute(routeId));
    }

    /**
     * 审核路线（已启用→已审核/已驳回）
     */
    @Log(title = "工艺路线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:route:status')")
    @PutMapping("/audit/{routeId}")
    public AjaxResult audit(@PathVariable("routeId") Long routeId,
                           @RequestParam String auditAction,
                           @RequestParam(required = false) String auditRemark)
    {
        return toAjax(mmsRouteService.auditRoute(routeId, auditAction, auditRemark));
    }

    /**
     * 停用路线（→已停用）
     */
    @Log(title = "工艺路线", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:route:status')")
    @PutMapping("/disable/{routeId}")
    public AjaxResult disable(@PathVariable("routeId") Long routeId)
    {
        return toAjax(mmsRouteService.disableRoute(routeId));
    }

    /**
     * 复制路线为新版本
     */
    @Log(title = "工艺路线", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:route:copy')")
    @PostMapping("/copy/{routeId}")
    public AjaxResult copy(@PathVariable("routeId") Long routeId)
    {
        return AjaxResult.success(mmsRouteService.copyRoute(routeId));
    }

    /**
     * 版本变更记录
     */
    @PreAuthorize("@ss.hasPermi('mms:route:query')")
    @GetMapping("/log/{routeId}")
    public AjaxResult versionLog(@PathVariable("routeId") Long routeId)
    {
        return AjaxResult.success(mmsRouteService.selectVersionLogByRouteId(routeId));
    }
}
