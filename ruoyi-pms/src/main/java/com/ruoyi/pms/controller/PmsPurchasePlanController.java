package com.ruoyi.pms.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pms.domain.PmsPurchasePlan;
import com.ruoyi.pms.service.IPmsPurchasePlanService;

/**
 * 采购计划Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/plan")
public class PmsPurchasePlanController extends BaseController
{
    @Autowired
    private IPmsPurchasePlanService pmsPurchasePlanService;

    /**
     * 查询采购计划列表
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsPurchasePlan pmsPurchasePlan)
    {
        startPage();
        List<PmsPurchasePlan> list = pmsPurchasePlanService.selectPmsPurchasePlanList(pmsPurchasePlan);
        return getDataTable(list);
    }

    /**
     * 导出采购计划列表
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:export')")
    @Log(title = "采购计划", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsPurchasePlan pmsPurchasePlan)
    {
        List<PmsPurchasePlan> list = pmsPurchasePlanService.selectPmsPurchasePlanList(pmsPurchasePlan);
        ExcelUtil<PmsPurchasePlan> util = new ExcelUtil<PmsPurchasePlan>(PmsPurchasePlan.class);
        util.exportExcel(response, list, "采购计划数据");
    }

    /**
     * 获取采购计划详细信息
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:query')")
    @GetMapping(value = "/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(pmsPurchasePlanService.selectPmsPurchasePlanById(planId));
    }

    /**
     * 新增采购计划
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:add')")
    @Log(title = "采购计划", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PmsPurchasePlan pmsPurchasePlan)
    {
        return toAjax(pmsPurchasePlanService.insertPmsPurchasePlan(pmsPurchasePlan));
    }

    /**
     * 修改采购计划
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:edit')")
    @Log(title = "采购计划", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PmsPurchasePlan pmsPurchasePlan)
    {
        return toAjax(pmsPurchasePlanService.updatePmsPurchasePlan(pmsPurchasePlan));
    }

    /**
     * 删除采购计划
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:remove')")
    @Log(title = "采购计划", businessType = BusinessType.DELETE)
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(pmsPurchasePlanService.deletePmsPurchasePlanByIds(planIds));
    }

    /**
     * 审核采购计划
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:audit')")
    @Log(title = "采购计划", businessType = BusinessType.UPDATE)
    @PutMapping("/audit/{planId}")
    public AjaxResult audit(@PathVariable Long planId, @RequestParam String status,
            @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsPurchasePlanService.auditPmsPurchasePlan(planId, status, auditOpinion));
    }

    /**
     * 关闭采购计划
     */
    @PreAuthorize("@ss.hasPermi('pms:plan:edit')")
    @Log(title = "采购计划", businessType = BusinessType.UPDATE)
    @PutMapping("/close/{planId}")
    public AjaxResult close(@PathVariable Long planId)
    {
        return toAjax(pmsPurchasePlanService.closePmsPurchasePlan(planId));
    }
}
