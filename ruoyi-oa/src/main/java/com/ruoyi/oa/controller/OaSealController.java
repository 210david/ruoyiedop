package com.ruoyi.oa.controller;

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
import com.ruoyi.oa.domain.OaSeal;
import com.ruoyi.oa.service.IOaSealService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 印章管理
 */
@RestController
@RequestMapping("/oa/seal")
public class OaSealController extends BaseController
{
    @Autowired
    private IOaSealService oaSealService;

    @PreAuthorize("@ss.hasPermi('oa:seal:list')")
    @GetMapping("/list")
    public TableDataInfo list(OaSeal oaSeal)
    {
        startPage();
        List<OaSeal> list = oaSealService.selectOaSealList(oaSeal);
        return getDataTable(list);
    }

    /**
     * 流程表单浏览框数据源：正常状态印章列表（仅需登录，不要求 OA 菜单权限，供用印申请等流程弹框选择）
     */
    @GetMapping("/pickerList")
    public TableDataInfo pickerList(OaSeal oaSeal)
    {
        // 正常 + 使用中：占用中的印章仍可被预约到其他时段
        oaSeal.getParams().put("statusIn", java.util.Arrays.asList("0", "3"));
        startPage();
        List<OaSeal> list = oaSealService.selectOaSealList(oaSeal);
        return getDataTable(list);
    }

    @Log(title = "印章管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('oa:seal:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, OaSeal oaSeal)
    {
        List<OaSeal> list = oaSealService.selectOaSealList(oaSeal);
        ExcelUtil<OaSeal> util = new ExcelUtil<>(OaSeal.class);
        util.exportExcel(response, list, "印章台账");
    }

    @Log(title = "印章管理", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('oa:seal:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<OaSeal> util = new ExcelUtil<>(OaSeal.class);
        List<OaSeal> sealList = util.importExcel(file.getInputStream());
        return oaSealService.importSeal(sealList, updateSupport, getUsername());
    }

    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<OaSeal> util = new ExcelUtil<>(OaSeal.class);
        util.importTemplateExcel(response, "印章台账");
    }

    @PreAuthorize("@ss.hasPermi('oa:seal:query')")
    @GetMapping(value = "/{sealId}")
    public AjaxResult getInfo(@PathVariable("sealId") Long sealId)
    {
        return AjaxResult.success(oaSealService.selectOaSealById(sealId));
    }

    @PreAuthorize("@ss.hasPermi('oa:seal:add')")
    @Log(title = "印章管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody OaSeal oaSeal)
    {
        oaSeal.setCreateBy(getUsername());
        return toAjax(oaSealService.insertOaSeal(oaSeal));
    }

    @PreAuthorize("@ss.hasPermi('oa:seal:edit')")
    @Log(title = "印章管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody OaSeal oaSeal)
    {
        oaSeal.setUpdateBy(getUsername());
        return toAjax(oaSealService.updateOaSeal(oaSeal));
    }

    @PreAuthorize("@ss.hasPermi('oa:seal:remove')")
    @Log(title = "印章管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sealIds}")
    public AjaxResult remove(@PathVariable Long[] sealIds)
    {
        return toAjax(oaSealService.deleteOaSealByIds(sealIds));
    }
}
