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
import com.ruoyi.mk.domain.MkInteraction;
import com.ruoyi.mk.service.IMkInteractionService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 互动记录 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/interaction")
public class MkInteractionController extends BaseController
{
    @Autowired
    private IMkInteractionService mkInteractionService;

    @PreAuthorize("@ss.hasPermi('marketing:interaction:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkInteraction interaction)
    {
        startPage();
        List<MkInteraction> list = mkInteractionService.selectInteractionList(interaction);
        return getDataTable(list);
    }

    @Log(title = "互动记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:interaction:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkInteraction interaction)
    {
        List<MkInteraction> list = mkInteractionService.selectInteractionList(interaction);
        ExcelUtil<MkInteraction> util = new ExcelUtil<>(MkInteraction.class);
        util.exportExcel(response, list, "互动记录数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:interaction:query')")
    @GetMapping(value = "/{recordId}")
    public AjaxResult getInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(mkInteractionService.selectInteractionById(recordId));
    }

    @Log(title = "互动记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:interaction:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkInteraction interaction)
    {
        return toAjax(mkInteractionService.insertInteraction(interaction));
    }

    @Log(title = "互动记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:interaction:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkInteraction interaction)
    {
        return toAjax(mkInteractionService.updateInteraction(interaction));
    }

    @Log(title = "互动记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:interaction:remove')")
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds)
    {
        return toAjax(mkInteractionService.deleteInteractionByIds(recordIds));
    }
}
