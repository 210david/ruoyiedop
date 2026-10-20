package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pdm.domain.PdmMbomRelease;
import com.ruoyi.pdm.service.IPdmMbomReleaseService;

/**
 * BOM下发记录 Controller（EBOM→MBOM 版本对应关系台账）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/mbomRelease")
public class PdmMbomReleaseController extends BaseController
{
    @Autowired
    private IPdmMbomReleaseService releaseService;

    /**
     * 查询下发记录列表（按 bomId/productId 过滤）
     */
    @PreAuthorize("@ss.hasPermi('pdm:designBom:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmMbomRelease release)
    {
        startPage();
        List<PdmMbomRelease> list = releaseService.selectReleaseList(release);
        return getDataTable(list);
    }

    /**
     * 生效设计BOM一键下发转换为制造BOM（mms 接收）
     */
    @Log(title = "BOM下发MBOM", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:release')")
    @PostMapping("/release/{bomId}")
    public AjaxResult release(@PathVariable Long bomId)
    {
        return success(releaseService.releaseMbom(bomId));
    }
}
