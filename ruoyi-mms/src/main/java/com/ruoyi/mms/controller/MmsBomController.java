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
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.service.IMmsBomService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * BOM Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/bom")
public class MmsBomController extends BaseController
{
    @Autowired
    private IMmsBomService mmsBomService;

    @PreAuthorize("@ss.hasPermi('mms:bom:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsBom bom)
    {
        startPage();
        List<MmsBom> list = mmsBomService.selectBomList(bom);
        return getDataTable(list);
    }

    @Log(title = "BOM", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:bom:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsBom bom)
    {
        List<MmsBom> list = mmsBomService.selectBomList(bom);
        ExcelUtil<MmsBom> util = new ExcelUtil<>(MmsBom.class);
        util.exportExcel(response, list, "BOM");
    }

    @PreAuthorize("@ss.hasPermi('mms:bom:query')")
    @GetMapping(value = "/{BomId:[0-9]+}")
    public AjaxResult getInfo(@PathVariable("BomId") Long BomId)
    {
        return AjaxResult.success(mmsBomService.selectBomById(BomId));
    }

    @Log(title = "BOM", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:bom:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsBom bom)
    {
        return toAjax(mmsBomService.insertBom(bom));
    }

    @Log(title = "BOM", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:bom:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsBom bom)
    {
        return toAjax(mmsBomService.updateBom(bom));
    }

    @Log(title = "BOM", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:bom:remove')")
    @DeleteMapping("/{BomIds:[0-9]+}")
    public AjaxResult remove(@PathVariable Long[] BomIds)
    {
        return toAjax(mmsBomService.deleteBomByIds(BomIds));
    }

    /**
     * BOM发布（草稿→已发布）
     */
    @Log(title = "BOM", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:bom:edit')")
    @PutMapping("/publish/{bomId}")
    public AjaxResult publish(@PathVariable("bomId") Long bomId)
    {
        return toAjax(mmsBomService.publishBom(bomId));
    }

    /**
     * BOM复制（创建新版本草稿）
     */
    @Log(title = "BOM", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:bom:add')")
    @PostMapping("/copy/{bomId}")
    public AjaxResult copy(@PathVariable("bomId") Long bomId)
    {
        return AjaxResult.success(mmsBomService.copyBom(bomId));
    }

    /**
     * BOM多层级展开树查询
     */
    @PreAuthorize("@ss.hasPermi('mms:bom:query')")
    @GetMapping("/tree/{bomId}")
    public AjaxResult tree(@PathVariable("bomId") Long bomId)
    {
        return AjaxResult.success(mmsBomService.selectBomTreeByBomId(bomId));
    }
}
