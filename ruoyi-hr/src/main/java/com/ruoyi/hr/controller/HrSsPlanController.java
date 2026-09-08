package com.ruoyi.hr.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrSsPlan;
import com.ruoyi.hr.service.IHrSsPlanService;

/**
 * HR社保方案 Controller
 */
@RestController
@RequestMapping("/hr/ssPlan")
public class HrSsPlanController extends BaseController
{
    @Autowired
    private IHrSsPlanService hrSsPlanService;

    @PreAuthorize("@ss.hasPermi('hr:ssPlan:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSsPlan hrSsPlan)
    {
        startPage();
        List<HrSsPlan> list = hrSsPlanService.selectHrSsPlanList(hrSsPlan);
        return getDataTable(list);
    }

    @Log(title = "社保方案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:ssPlan:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSsPlan hrSsPlan)
    {
        List<HrSsPlan> list = hrSsPlanService.selectHrSsPlanList(hrSsPlan);
        ExcelUtil<HrSsPlan> util = new ExcelUtil<>(HrSsPlan.class);
        util.exportExcel(response, list, "社保方案");
    }

    @PreAuthorize("@ss.hasPermi('hr:ssPlan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(hrSsPlanService.selectHrSsPlanById(planId));
    }

    @PreAuthorize("@ss.hasPermi('hr:ssPlan:list')")
    @GetMapping("/default/{year}")
    public AjaxResult getDefault(@PathVariable("year") Integer year)
    {
        return AjaxResult.success(hrSsPlanService.selectDefaultPlanByYear(year));
    }

    @Log(title = "社保方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:ssPlan:add')")
    @PostMapping
    public AjaxResult add(@RequestBody HrSsPlan hrSsPlan)
    {
        return toAjax(hrSsPlanService.insertHrSsPlan(hrSsPlan));
    }

    @Log(title = "社保方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:ssPlan:add')")
    @PostMapping("/copy/{planId}/{targetYear}")
    public AjaxResult copy(@PathVariable("planId") Long planId, @PathVariable("targetYear") Integer targetYear)
    {
        Long newPlanId = hrSsPlanService.copyPlanToYear(planId, targetYear);
        return AjaxResult.success("复制成功", newPlanId);
    }

    @Log(title = "社保方案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('hr:ssPlan:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody HrSsPlan hrSsPlan)
    {
        return toAjax(hrSsPlanService.updateHrSsPlan(hrSsPlan));
    }

    @Log(title = "社保方案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('hr:ssPlan:remove')")
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(hrSsPlanService.deleteHrSsPlanByIds(planIds));
    }
}
