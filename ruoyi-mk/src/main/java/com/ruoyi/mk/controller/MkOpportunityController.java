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
import com.ruoyi.mk.domain.MkOpportunity;
import com.ruoyi.mk.service.IMkOpportunityService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 商机 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/opportunity")
public class MkOpportunityController extends BaseController
{
    @Autowired
    private IMkOpportunityService mkOpportunityService;

    @PreAuthorize("@ss.hasPermi('marketing:opportunity:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkOpportunity opportunity)
    {
        startPage();
        List<MkOpportunity> list = mkOpportunityService.selectOpportunityList(opportunity);
        return getDataTable(list);
    }

    @Log(title = "商机管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkOpportunity opportunity)
    {
        List<MkOpportunity> list = mkOpportunityService.selectOpportunityList(opportunity);
        ExcelUtil<MkOpportunity> util = new ExcelUtil<>(MkOpportunity.class);
        util.exportExcel(response, list, "商机数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:opportunity:query')")
    @GetMapping(value = "/{opportunityId}")
    public AjaxResult getInfo(@PathVariable("opportunityId") Long opportunityId)
    {
        return AjaxResult.success(mkOpportunityService.selectOpportunityById(opportunityId));
    }

    @Log(title = "商机管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkOpportunity opportunity)
    {
        return toAjax(mkOpportunityService.insertOpportunity(opportunity));
    }

    @Log(title = "商机管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkOpportunity opportunity)
    {
        return toAjax(mkOpportunityService.updateOpportunity(opportunity));
    }

    @Log(title = "商机管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:remove')")
    @DeleteMapping("/{opportunityIds}")
    public AjaxResult remove(@PathVariable Long[] opportunityIds)
    {
        return toAjax(mkOpportunityService.deleteOpportunityByIds(opportunityIds));
    }

    /** 看板视图：按阶段分组查询商机 */
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:list')")
    @GetMapping("/kanban")
    public AjaxResult kanban()
    {
        return AjaxResult.success(mkOpportunityService.selectOpportunityByStage(null));
    }

    // ==================== P1: 阶段推进引擎 ====================

    /** 阶段推进 */
    @Log(title = "商机阶段推进", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping("/advance/{opportunityId}")
    public AjaxResult advance(@PathVariable Long opportunityId, @RequestBody MkOpportunity param)
    {
        return toAjax(mkOpportunityService.advanceStage(opportunityId, param.getStageCode(), param.getRemark()));
    }

    /** 阶段退回 */
    @Log(title = "商机阶段退回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping("/retreat/{opportunityId}")
    public AjaxResult retreat(@PathVariable Long opportunityId, @RequestBody MkOpportunity param)
    {
        return toAjax(mkOpportunityService.retreatStage(opportunityId, param.getStageCode(), param.getRemark()));
    }

    /** 赢单关单 */
    @Log(title = "商机赢单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping("/win/{opportunityId}")
    public AjaxResult win(@PathVariable Long opportunityId, @RequestBody MkOpportunity param)
    {
        return toAjax(mkOpportunityService.winOpportunity(opportunityId, param));
    }

    /** 输单关单 */
    @Log(title = "商机输单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping("/lose/{opportunityId}")
    public AjaxResult lose(@PathVariable Long opportunityId, @RequestBody MkOpportunity param)
    {
        return toAjax(mkOpportunityService.loseOpportunity(opportunityId, param));
    }

    /** 重新打开 */
    @Log(title = "商机重新打开", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:edit')")
    @PutMapping("/reopen/{opportunityId}")
    public AjaxResult reopen(@PathVariable Long opportunityId)
    {
        return toAjax(mkOpportunityService.reopenOpportunity(opportunityId));
    }

    /** 查询阶段变更日志 */
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:query')")
    @GetMapping("/stageLog/{opportunityId}")
    public AjaxResult stageLog(@PathVariable Long opportunityId)
    {
        return AjaxResult.success(mkOpportunityService.selectStageLogList(opportunityId));
    }

    /** 查询商机关联数据（互动记录、阶段日志、合同、订单） */
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:query')")
    @GetMapping("/relations/{opportunityId}")
    public AjaxResult relations(@PathVariable Long opportunityId)
    {
        return AjaxResult.success(mkOpportunityService.selectOpportunityRelations(opportunityId));
    }

    /** 查询停滞商机列表 */
    @PreAuthorize("@ss.hasPermi('marketing:opportunity:list')")
    @GetMapping("/stagnant")
    public TableDataInfo stagnant()
    {
        startPage();
        List<MkOpportunity> list = mkOpportunityService.selectStagnantOpportunities();
        return getDataTable(list);
    }
}
