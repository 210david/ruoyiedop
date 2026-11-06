package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmRisk;
import com.ruoyi.pjm.service.IPjmRiskService;

/**
 * 项目风险/问题 Controller
 */
@RestController
@RequestMapping("/pjm/risk")
public class PjmRiskController extends BaseController
{
    @Autowired
    private IPjmRiskService riskService;

    @PreAuthorize("@ss.hasPermi('pjm:risk:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmRisk risk)
    {
        startPage();
        List<PjmRisk> list = riskService.selectRiskList(risk);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pjm:risk:query')")
    @GetMapping(value = "/{riskId}")
    public AjaxResult getInfo(@PathVariable("riskId") Long riskId)
    {
        return AjaxResult.success(riskService.selectRiskById(riskId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:risk:add')")
    @Log(title = "项目风险", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmRisk risk)
    {
        return toAjax(riskService.insertRisk(risk));
    }

    @PreAuthorize("@ss.hasPermi('pjm:risk:edit')")
    @Log(title = "项目风险", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmRisk risk)
    {
        return toAjax(riskService.updateRisk(risk));
    }

    @PreAuthorize("@ss.hasPermi('pjm:risk:remove')")
    @Log(title = "项目风险", businessType = BusinessType.DELETE)
    @DeleteMapping("/{riskIds}")
    public AjaxResult remove(@PathVariable Long[] riskIds)
    {
        return toAjax(riskService.deleteRiskByIds(riskIds));
    }

    /** ★ 按工程类别从国家标准风险点库推荐风险点 */
    @PreAuthorize("@ss.hasPermi('pjm:risk:query')")
    @GetMapping("/standard-match")
    public AjaxResult standardMatch(@RequestParam("engineeringCategory") String engineeringCategory,
                                    @RequestParam(value = "keyword", required = false) String keyword)
    {
        return AjaxResult.success(riskService.matchStandardRisks(engineeringCategory, keyword));
    }
}
