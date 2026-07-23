package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.OpenApiApp;
import com.ruoyi.system.service.IOpenApiAppService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 开放接口应用管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/openapi")
public class OpenApiAppController extends BaseController
{
    @Autowired
    private IOpenApiAppService openApiAppService;

    /**
     * 查询开放接口应用列表
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpenApiApp app)
    {
        startPage();
        List<OpenApiApp> list = openApiAppService.selectOpenApiAppList(app);
        return getDataTable(list);
    }

    /**
     * 导出开放接口应用列表
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:list')")
    @Log(title = "开放接口应用", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpenApiApp app)
    {
        List<OpenApiApp> list = openApiAppService.selectOpenApiAppList(app);
        ExcelUtil<OpenApiApp> util = new ExcelUtil<>(OpenApiApp.class);
        util.exportExcel(response, list, "开放接口应用数据");
    }

    /**
     * 获取开放接口应用详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:query')")
    @GetMapping(value = "/{appId}")
    public AjaxResult getInfo(@PathVariable("appId") Long appId)
    {
        return AjaxResult.success(openApiAppService.selectOpenApiAppById(appId));
    }

    /**
     * 新增开放接口应用
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:add')")
    @Log(title = "开放接口应用", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OpenApiApp app)
    {
        app.setCreateBy(getUsername());
        return toAjax(openApiAppService.insertOpenApiApp(app));
    }

    /**
     * 修改开放接口应用
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:edit')")
    @Log(title = "开放接口应用", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OpenApiApp app)
    {
        app.setUpdateBy(getUsername());
        return toAjax(openApiAppService.updateOpenApiApp(app));
    }

    /**
     * 删除开放接口应用
     */
    @PreAuthorize("@ss.hasPermi('system:openapi:remove')")
    @Log(title = "开放接口应用", businessType = BusinessType.DELETE)
    @DeleteMapping("/{appIds}")
    public AjaxResult remove(@PathVariable Long[] appIds)
    {
        return toAjax(openApiAppService.deleteOpenApiAppByIds(appIds));
    }
}
