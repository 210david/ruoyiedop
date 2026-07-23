package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkOpportunityStage;
import com.ruoyi.mk.service.IMkOpportunityStageService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 销售阶段配置 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/stage")
public class MkOpportunityStageController extends BaseController
{
    @Autowired
    private IMkOpportunityStageService mkOpportunityStageService;

    @PreAuthorize("@ss.hasPermi('marketing:stage:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkOpportunityStage stage)
    {
        startPage();
        List<MkOpportunityStage> list = mkOpportunityStageService.selectStageList(stage);
        return getDataTable(list);
    }

    @Log(title = "销售阶段配置", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:stage:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkOpportunityStage stage)
    {
        List<MkOpportunityStage> list = mkOpportunityStageService.selectStageList(stage);
        ExcelUtil<MkOpportunityStage> util = new ExcelUtil<>(MkOpportunityStage.class);
        util.exportExcel(response, list, "销售阶段配置数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:stage:query')")
    @GetMapping(value = "/{stageId}")
    public AjaxResult getInfo(@PathVariable("stageId") Long stageId)
    {
        return AjaxResult.success(mkOpportunityStageService.selectStageById(stageId));
    }

    @Log(title = "销售阶段配置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:stage:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkOpportunityStage stage)
    {
        return toAjax(mkOpportunityStageService.insertStage(stage));
    }

    @Log(title = "销售阶段配置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:stage:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkOpportunityStage stage)
    {
        return toAjax(mkOpportunityStageService.updateStage(stage));
    }

    @Log(title = "销售阶段配置", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:stage:remove')")
    @DeleteMapping("/{stageIds}")
    public AjaxResult remove(@PathVariable Long[] stageIds)
    {
        return toAjax(mkOpportunityStageService.deleteStageByIds(stageIds));
    }
}
