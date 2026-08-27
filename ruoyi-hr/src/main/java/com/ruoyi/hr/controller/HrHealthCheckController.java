package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrHealthCheck;
import com.ruoyi.hr.service.IHrHealthCheckService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/healthCheck")
public class HrHealthCheckController extends BaseController
{
    @Autowired
    private IHrHealthCheckService hrHealthCheckService;

    @PreAuthorize("@ss.hasPermi('hr:healthCheck:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrHealthCheck hrHealthCheck)
    {
        startPage();
        List<HrHealthCheck> list = hrHealthCheckService.selectHrHealthCheckList(hrHealthCheck);
        return getDataTable(list);
    }

    @Log(title = "职业健康体检", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:healthCheck:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrHealthCheck hrHealthCheck)
    {
        List<HrHealthCheck> list = hrHealthCheckService.selectHrHealthCheckList(hrHealthCheck);
        ExcelUtil<HrHealthCheck> util = new ExcelUtil<>(HrHealthCheck.class);
        util.exportExcel(response, list, "职业健康体检");
    }

    @PreAuthorize("@ss.hasPermi('hr:healthCheck:query')")
    @GetMapping(value = "/{healthId}")
    public AjaxResult getInfo(@PathVariable("healthId") Long healthId)
    {
        return AjaxResult.success(hrHealthCheckService.selectHrHealthCheckById(healthId));
    }

    @PreAuthorize("@ss.hasPermi('hr:healthCheck:add')")
    @Log(title = "职业健康体检", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrHealthCheck hrHealthCheck)
    {
        return toAjax(hrHealthCheckService.insertHrHealthCheck(hrHealthCheck));
    }

    @PreAuthorize("@ss.hasPermi('hr:healthCheck:edit')")
    @Log(title = "职业健康体检", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrHealthCheck hrHealthCheck)
    {
        return toAjax(hrHealthCheckService.updateHrHealthCheck(hrHealthCheck));
    }

    @PreAuthorize("@ss.hasPermi('hr:healthCheck:remove')")
    @Log(title = "职业健康体检", businessType = BusinessType.DELETE)
    @DeleteMapping("/{healthIds}")
    public AjaxResult remove(@PathVariable Long[] healthIds)
    {
        return toAjax(hrHealthCheckService.deleteHrHealthCheckByIds(healthIds));
    }
}
