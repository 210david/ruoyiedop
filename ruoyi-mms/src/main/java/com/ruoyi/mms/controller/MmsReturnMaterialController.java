package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.service.IMmsReturnMaterialService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ReturnMaterial Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/return")
public class MmsReturnMaterialController extends BaseController
{
    @Autowired
    private IMmsReturnMaterialService mmsReturnMaterialService;

    @PreAuthorize("@ss.hasPermi('mms:return:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsReturnMaterial returnMaterial)
    {
        startPage();
        List<MmsReturnMaterial> list = mmsReturnMaterialService.selectReturnMaterialList(returnMaterial);
        return getDataTable(list);
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:return:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsReturnMaterial returnMaterial)
    {
        List<MmsReturnMaterial> list = mmsReturnMaterialService.selectReturnMaterialList(returnMaterial);
        ExcelUtil<MmsReturnMaterial> util = new ExcelUtil<>(MmsReturnMaterial.class);
        util.exportExcel(response, list, "ReturnMaterial");
    }

    @PreAuthorize("@ss.hasPermi('mms:return:query')")
    @GetMapping(value = "/{ReturnId}")
    public AjaxResult getInfo(@PathVariable("ReturnId") Long ReturnId)
    {
        return AjaxResult.success(mmsReturnMaterialService.selectReturnMaterialById(ReturnId));
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:return:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsReturnMaterial returnMaterial)
    {
        return toAjax(mmsReturnMaterialService.insertReturnMaterial(returnMaterial));
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:return:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsReturnMaterial returnMaterial)
    {
        return toAjax(mmsReturnMaterialService.updateReturnMaterial(returnMaterial));
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:return:remove')")
    @DeleteMapping("/{ReturnIds}")
    public AjaxResult remove(@PathVariable Long[] ReturnIds)
    {
        return toAjax(mmsReturnMaterialService.deleteReturnMaterialByIds(ReturnIds));
    }
}
