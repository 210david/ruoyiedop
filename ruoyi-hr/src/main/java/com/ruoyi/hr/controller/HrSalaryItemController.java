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
import com.ruoyi.hr.domain.HrSalaryItem;
import com.ruoyi.hr.service.IHrSalaryItemService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/salaryItem")
public class HrSalaryItemController extends BaseController
{
    @Autowired
    private IHrSalaryItemService hrSalaryItemService;

    @PreAuthorize("@ss.hasPermi('hr:salaryItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSalaryItem hrSalaryItem)
    {
        startPage();
        List<HrSalaryItem> list = hrSalaryItemService.selectHrSalaryItemList(hrSalaryItem);
        return getDataTable(list);
    }

    @Log(title = "薪资项字典", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:salaryItem:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSalaryItem hrSalaryItem)
    {
        List<HrSalaryItem> list = hrSalaryItemService.selectHrSalaryItemList(hrSalaryItem);
        ExcelUtil<HrSalaryItem> util = new ExcelUtil<>(HrSalaryItem.class);
        util.exportExcel(response, list, "薪资项字典");
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryItem:query')")
    @GetMapping(value = "/{itemId}")
    public AjaxResult getInfo(@PathVariable("itemId") Long itemId)
    {
        return AjaxResult.success(hrSalaryItemService.selectHrSalaryItemById(itemId));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryItem:add')")
    @Log(title = "薪资项字典", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSalaryItem hrSalaryItem)
    {
        return toAjax(hrSalaryItemService.insertHrSalaryItem(hrSalaryItem));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryItem:edit')")
    @Log(title = "薪资项字典", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSalaryItem hrSalaryItem)
    {
        return toAjax(hrSalaryItemService.updateHrSalaryItem(hrSalaryItem));
    }

    @PreAuthorize("@ss.hasPermi('hr:salaryItem:remove')")
    @Log(title = "薪资项字典", businessType = BusinessType.DELETE)
    @DeleteMapping("/{itemIds}")
    public AjaxResult remove(@PathVariable Long[] itemIds)
    {
        return toAjax(hrSalaryItemService.deleteHrSalaryItemByIds(itemIds));
    }
}
