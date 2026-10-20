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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.service.IPdmDesignBomService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 设计BOM Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/designBom")
public class PdmDesignBomController extends BaseController
{
    @Autowired
    private IPdmDesignBomService bomService;

    /**
     * 查询设计BOM列表（分页，含明细数量）
     */
    @PreAuthorize("@ss.hasPermi('pdm:designBom:list')")
    @GetMapping("/list")
    public TableDataInfo list(PdmDesignBom bom)
    {
        startPage();
        List<PdmDesignBom> list = bomService.selectBomList(bom);
        return getDataTable(list);
    }

    /**
     * 导出设计BOM
     */
    @Log(title = "设计BOM", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PdmDesignBom bom)
    {
        List<PdmDesignBom> list = bomService.selectBomList(bom);
        ExcelUtil<PdmDesignBom> util = new ExcelUtil<>(PdmDesignBom.class);
        util.exportExcel(response, list, "设计BOM数据");
    }

    /**
     * 获取BOM详情（含明细平铺列表）
     */
    @PreAuthorize("@ss.hasPermi('pdm:designBom:query')")
    @GetMapping(value = "/{bomId}")
    public AjaxResult getInfo(@PathVariable("bomId") Long bomId)
    {
        return success(bomService.selectBomById(bomId));
    }

    /**
     * 查询BOM明细
     */
    @PreAuthorize("@ss.hasPermi('pdm:designBom:query')")
    @GetMapping(value = "/items/{bomId}")
    public AjaxResult items(@PathVariable("bomId") Long bomId)
    {
        return success(bomService.selectItemsByBomId(bomId));
    }

    /**
     * 新增设计BOM
     */
    @Log(title = "设计BOM", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:add')")
    @PostMapping
    public AjaxResult add(@RequestBody PdmDesignBom bom)
    {
        return toAjax(bomService.insertBom(bom));
    }

    /**
     * 修改设计BOM（含明细全量替换；已生效BOM修订自动升版）
     */
    @Log(title = "设计BOM", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody PdmDesignBom bom)
    {
        return toAjax(bomService.updateBom(bom));
    }

    /**
     * BOM生效（版本生效 + 置为已生效状态）
     */
    @Log(title = "BOM生效", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:effective')")
    @PutMapping("/effective")
    public AjaxResult effective(@RequestBody PdmDesignBom params)
    {
        return toAjax(bomService.effectiveBom(params.getBomId(), params.getVersionId(), params.getRemark()));
    }

    /**
     * 手工升版（生成新版本记录）
     */
    @Log(title = "BOM升版", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:edit')")
    @PutMapping("/upgrade")
    public AjaxResult upgrade(@RequestBody PdmDesignBom params)
    {
        return toAjax(bomService.upgradeBom(params.getBomId(), params.getRemark(), Boolean.TRUE.equals(params.getMajorUpgrade())));
    }

    /**
     * 删除设计BOM（仅限编制中）
     */
    @Log(title = "设计BOM", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:designBom:remove')")
    @DeleteMapping("/{bomIds}")
    public AjaxResult remove(@PathVariable Long[] bomIds)
    {
        return toAjax(bomService.deleteBomByIds(bomIds));
    }
}
