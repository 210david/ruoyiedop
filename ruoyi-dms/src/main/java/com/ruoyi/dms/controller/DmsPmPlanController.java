package com.ruoyi.dms.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.dms.domain.DmsPmPlan;
import com.ruoyi.dms.service.IDmsPmPlanService;

@RestController
@RequestMapping("/dms/pmplan")
public class DmsPmPlanController extends BaseController
{
    @Autowired
    private IDmsPmPlanService dmsPmPlanService;

    @PreAuthorize("@ss.hasPermi('dms:pmplan:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsPmPlan pmPlan)
    {
        startPage();
        List<DmsPmPlan> list = dmsPmPlanService.selectPmPlanList(pmPlan);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('dms:pmplan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(dmsPmPlanService.selectPmPlanById(planId));
    }

    @Log(title = "PM维护计划", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:pmplan:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsPmPlan pmPlan)
    {
        return toAjax(dmsPmPlanService.insertPmPlan(pmPlan));
    }

    @Log(title = "PM维护计划", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:pmplan:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsPmPlan pmPlan)
    {
        return toAjax(dmsPmPlanService.updatePmPlan(pmPlan));
    }

    @Log(title = "PM维护计划", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:pmplan:remove')")
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(dmsPmPlanService.deletePmPlanByIds(planIds));
    }

    /** 根据PM计划生成工单 */
    @Log(title = "PM生成工单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:pmplan:edit')")
    @PostMapping("/generate/{planId}")
    public AjaxResult generateWorkOrder(@PathVariable("planId") Long planId)
    {
        return toAjax(dmsPmPlanService.generateWorkOrder(planId));
    }

    /** 手动触发PM自动生成（用于测试或补执行） */
    @Log(title = "PM自动生成", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:pmplan:edit')")
    @PostMapping("/autoGenerate")
    public AjaxResult autoGenerate()
    {
        int count = dmsPmPlanService.autoGeneratePmWorkOrders();
        return AjaxResult.success("共自动生成 " + count + " 个工单");
    }

    /** 日历视图数据 */
    @PreAuthorize("@ss.hasPermi('dms:pmplan:list')")
    @GetMapping("/calendar")
    public AjaxResult calendar(@RequestParam(required = false) String month)
    {
        List<Map<String, Object>> data = dmsPmPlanService.selectPmPlanCalendar(month);
        return AjaxResult.success(data);
    }
}
