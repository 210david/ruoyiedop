package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsMaterialAttr;
import com.ruoyi.qms.service.IQmsMaterialAttrService;

/**
 * 物料质量属性 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/materialattr")
public class QmsMaterialAttrController extends BaseController
{
    @Autowired
    private IQmsMaterialAttrService qmsMaterialAttrService;

    @PreAuthorize("@ss.hasPermi('qms:materialattr:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsMaterialAttr materialAttr)
    {
        startPage();
        List<QmsMaterialAttr> list = qmsMaterialAttrService.selectMaterialAttrList(materialAttr);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('qms:materialattr:query')")
    @GetMapping(value = "/{attrId}")
    public AjaxResult getInfo(@PathVariable("attrId") Long attrId)
    {
        return AjaxResult.success(qmsMaterialAttrService.selectMaterialAttrById(attrId));
    }

    @Log(title = "物料质量属性", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:materialattr:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsMaterialAttr materialAttr)
    {
        return toAjax(qmsMaterialAttrService.insertMaterialAttr(materialAttr));
    }

    @Log(title = "物料质量属性", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:materialattr:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsMaterialAttr materialAttr)
    {
        return toAjax(qmsMaterialAttrService.updateMaterialAttr(materialAttr));
    }

    @Log(title = "物料质量属性", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:materialattr:remove')")
    @DeleteMapping("/{attrIds}")
    public AjaxResult remove(@PathVariable Long[] attrIds)
    {
        return toAjax(qmsMaterialAttrService.deleteMaterialAttrByIds(attrIds));
    }
}
