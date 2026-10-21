package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pdm.domain.PdmScheme;
import com.ruoyi.pdm.service.IPdmSchemeService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 设计方案 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/scheme")
public class PdmSchemeController extends BaseController
{
    @Autowired
    private IPdmSchemeService schemeService;

    /**
     * 查询设计方案列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('pdm:scheme:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmScheme scheme)
    {
        startPage();
        List<PdmScheme> list = schemeService.selectSchemeList(scheme);
        return getDataTable(list);
    }

    /**
     * 导出设计方案
     */
    @Log(title = "设计方案", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmScheme scheme)
    {
        List<PdmScheme> list = schemeService.selectSchemeList(scheme);
        ExcelUtil<PdmScheme> util = new ExcelUtil<>(PdmScheme.class);
        util.exportExcel(response, list, "设计方案数据");
    }

    /**
     * 获取方案详情（配方明细涉密：无 pdm:scheme:formula 权限不下发明细）
     */
    @PreAuthorize("@ss.hasPermi('pdm:scheme:query')")
    @GetMapping(value = "/{schemeId}")
    public AjaxResult getInfo(@PathVariable("schemeId") Long schemeId)
    {
        PdmScheme scheme = schemeService.selectSchemeById(schemeId);
        if (scheme != null && !SecurityUtils.hasPermi("pdm:scheme:formula"))
        {
            scheme.setFormulaList(null);
        }
        return success(scheme);
    }

    /**
     * 新增设计方案
     */
    @Log(title = "设计方案", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmScheme scheme)
    {
        return toAjax(schemeService.insertScheme(scheme));
    }

    /**
     * 修改设计方案（已定稿方案修订自动升版）
     */
    @Log(title = "设计方案", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmScheme scheme)
    {
        return toAjax(schemeService.updateScheme(scheme));
    }

    /**
     * 方案定稿（版本生效 + 产品档案定型联动）
     */
    @Log(title = "方案定稿", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:effective')")
    @PutMapping("/effective")
    public AjaxResult effective(@RequestBody PdmScheme params)
    {
        return toAjax(schemeService.effectiveScheme(params.getSchemeId(), params.getVersionId(), params.getRemark()));
    }

    /**
     * 方案升版（生成新版本记录）
     */
    @Log(title = "方案升版", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:edit')")
    @PutMapping("/upgrade")
    public AjaxResult upgrade(@RequestBody PdmScheme params)
    {
        return toAjax(schemeService.upgradeScheme(params.getSchemeId(), params.getRemark(), Boolean.TRUE.equals(params.getMajorUpgrade())));
    }

    /**
     * 停用方案（已定稿 → 已停用；生命周期终止动作，不走变更单）
     */
    @Log(title = "方案停用", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:disable')")
    @PutMapping("/disable/{schemeId}")
    public AjaxResult disable(@PathVariable Long schemeId, @RequestBody(required = false) PdmScheme params)
    {
        return toAjax(schemeService.disableScheme(schemeId, params != null ? params.getRemark() : null));
    }

    /**
     * 恢复方案（已停用 → 已定稿）
     */
    @Log(title = "方案恢复", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:enable')")
    @PutMapping("/enable/{schemeId}")
    public AjaxResult enable(@PathVariable Long schemeId)
    {
        return toAjax(schemeService.enableScheme(schemeId));
    }

    /**
     * 删除设计方案（仅限编制中）
     */
    @Log(title = "设计方案", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:scheme:remove')")
    @DeleteMapping("/{schemeIds}")
    public AjaxResult remove(@PathVariable Long[] schemeIds)
    {
        return toAjax(schemeService.deleteSchemeByIds(schemeIds));
    }
}
