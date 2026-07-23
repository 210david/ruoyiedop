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
import com.ruoyi.mk.domain.MkCustomerTag;
import com.ruoyi.mk.service.IMkCustomerTagService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 客户标签 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/tag")
public class MkCustomerTagController extends BaseController
{
    @Autowired
    private IMkCustomerTagService mkCustomerTagService;

    @PreAuthorize("@ss.hasPermi('marketing:tag:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkCustomerTag tag)
    {
        startPage();
        List<MkCustomerTag> list = mkCustomerTagService.selectTagList(tag);
        return getDataTable(list);
    }

    @Log(title = "客户标签", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:tag:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkCustomerTag tag)
    {
        List<MkCustomerTag> list = mkCustomerTagService.selectTagList(tag);
        ExcelUtil<MkCustomerTag> util = new ExcelUtil<>(MkCustomerTag.class);
        util.exportExcel(response, list, "客户标签数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:tag:query')")
    @GetMapping(value = "/{tagId}")
    public AjaxResult getInfo(@PathVariable("tagId") Long tagId)
    {
        return AjaxResult.success(mkCustomerTagService.selectTagById(tagId));
    }

    @Log(title = "客户标签", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:tag:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkCustomerTag tag)
    {
        return toAjax(mkCustomerTagService.insertTag(tag));
    }

    @Log(title = "客户标签", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:tag:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkCustomerTag tag)
    {
        return toAjax(mkCustomerTagService.updateTag(tag));
    }

    @Log(title = "客户标签", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:tag:remove')")
    @DeleteMapping("/{tagIds}")
    public AjaxResult remove(@PathVariable Long[] tagIds)
    {
        return toAjax(mkCustomerTagService.deleteTagByIds(tagIds));
    }
}
