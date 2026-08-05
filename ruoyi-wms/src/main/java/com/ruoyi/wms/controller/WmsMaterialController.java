package com.ruoyi.wms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.wms.domain.WmsMaterial;
import com.ruoyi.wms.service.IWmsMaterialService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/material")
public class WmsMaterialController extends BaseController
{
    @Autowired
    private IWmsMaterialService wmsMaterialService;

    @PreAuthorize("@ss.hasPermi('wms:material:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsMaterial material)
    {
        startPage();
        List<WmsMaterial> list = wmsMaterialService.selectMaterialList(material);
        return getDataTable(list);
    }

    @Log(title = "物料管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:material:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsMaterial material)
    {
        List<WmsMaterial> list = wmsMaterialService.selectMaterialList(material);
        ExcelUtil<WmsMaterial> util = new ExcelUtil<>(WmsMaterial.class);
        util.exportExcel(response, list, "物料数据");
    }

    @Log(title = "物料管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('wms:material:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport,
            @RequestParam(value = "updateKey", required = false, defaultValue = "materialName") String updateKey) throws Exception
    {
        ExcelUtil<WmsMaterial> util = new ExcelUtil<>(WmsMaterial.class);
        List<WmsMaterial> list = util.importExcel(file.getInputStream());
        String operName = getUsername();
        return wmsMaterialService.importMaterial(list, updateSupport, updateKey, operName);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<WmsMaterial> util = new ExcelUtil<>(WmsMaterial.class);
        // 排除自动生成的物料编码字段，使导入模板与新建表单一致
        util.excludeFields = new String[]{"materialCode"};
        util.importTemplateExcel(response, "物料数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:material:query')")
    @GetMapping(value = "/{materialId}")
    public AjaxResult getInfo(@PathVariable("materialId") Long materialId)
    {
        return AjaxResult.success(wmsMaterialService.selectMaterialById(materialId));
    }

    @Log(title = "物料管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:material:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsMaterial material)
    {
        return toAjax(wmsMaterialService.insertMaterial(material));
    }

    @Log(title = "物料管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:material:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsMaterial material)
    {
        return toAjax(wmsMaterialService.updateMaterial(material));
    }

    @Log(title = "物料管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:material:remove')")
    @DeleteMapping("/{materialIds}")
    public AjaxResult remove(@PathVariable Long[] materialIds)
    {
        return toAjax(wmsMaterialService.deleteMaterialByIds(materialIds));
    }

}
