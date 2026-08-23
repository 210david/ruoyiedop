package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomImport;
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

    @Log(title = "BOM", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('mms:bom:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<MmsBomImport> util = new ExcelUtil<>(MmsBomImport.class);
        List<MmsBomImport> list = util.importExcel(file.getInputStream());
        String operName = getUsername();
        return mmsBomService.importBom(list, updateSupport, operName);
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<MmsBomImport> util = new ExcelUtil<>(MmsBomImport.class);
        util.importTemplateExcel(response, "BOM导入模板");
    }

    @PreAuthorize("@ss.hasPermi('mms:bom:query')")
    @GetMapping(value = "/{bomId}")
    public AjaxResult getInfo(@PathVariable("bomId") Long bomId)
    {
        return AjaxResult.success(mmsBomService.selectBomById(bomId));
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
    @DeleteMapping("/{bomIds}")
    public AjaxResult remove(@PathVariable Long[] bomIds)
    {
        return toAjax(mmsBomService.deleteBomByIds(bomIds));
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
     * BOM停用（已发布→已停用）
     */
    @Log(title = "BOM", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:bom:edit')")
    @PutMapping("/disable/{bomId}")
    public AjaxResult disable(@PathVariable("bomId") Long bomId)
    {
        return toAjax(mmsBomService.disableBom(bomId));
    }

    /**
     * BOM启用（已停用→已发布）
     */
    @Log(title = "BOM", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:bom:edit')")
    @PutMapping("/enable/{bomId}")
    public AjaxResult enable(@PathVariable("bomId") Long bomId)
    {
        return toAjax(mmsBomService.enableBom(bomId));
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
