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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.qms.domain.QmsGauge;
import com.ruoyi.qms.service.IQmsGaugeService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 量检具台账 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/gauge")
public class QmsGaugeController extends BaseController
{
    @Autowired
    private IQmsGaugeService qmsGaugeService;

    @PreAuthorize("@ss.hasPermi('qms:gauge:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsGauge gauge)
    {
        startPage();
        List<QmsGauge> list = qmsGaugeService.selectGaugeList(gauge);
        return getDataTable(list);
    }

    @Log(title = "量检具台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:gauge:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsGauge gauge)
    {
        List<QmsGauge> list = qmsGaugeService.selectGaugeList(gauge);
        ExcelUtil<QmsGauge> util = new ExcelUtil<>(QmsGauge.class);
        util.exportExcel(response, list, "量检具台账数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:gauge:query')")
    @GetMapping(value = "/{gaugeId}")
    public AjaxResult getInfo(@PathVariable("gaugeId") Long gaugeId)
    {
        return AjaxResult.success(qmsGaugeService.selectGaugeById(gaugeId));
    }

    @Log(title = "量检具台账", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:gauge:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsGauge gauge)
    {
        return toAjax(qmsGaugeService.insertGauge(gauge));
    }

    @Log(title = "量检具台账", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:gauge:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsGauge gauge)
    {
        return toAjax(qmsGaugeService.updateGauge(gauge));
    }

    @Log(title = "量检具台账", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:gauge:remove')")
    @DeleteMapping("/{gaugeIds}")
    public AjaxResult remove(@PathVariable Long[] gaugeIds)
    {
        return toAjax(qmsGaugeService.deleteGaugeByIds(gaugeIds));
    }
}
