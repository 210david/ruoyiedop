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
import com.ruoyi.mms.domain.MmsQc;
import com.ruoyi.mms.service.IMmsQcService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Qc Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/qc")
public class MmsQcController extends BaseController
{
    @Autowired
    private IMmsQcService mmsQcService;

    @PreAuthorize("@ss.hasPermi('mms:qc:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsQc qc)
    {
        startPage();
        List<MmsQc> list = mmsQcService.selectQcList(qc);
        return getDataTable(list);
    }

    @Log(title = "Qc", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:qc:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsQc qc)
    {
        List<MmsQc> list = mmsQcService.selectQcList(qc);
        ExcelUtil<MmsQc> util = new ExcelUtil<>(MmsQc.class);
        util.exportExcel(response, list, "Qc");
    }

    @PreAuthorize("@ss.hasPermi('mms:qc:query')")
    @GetMapping(value = "/{QcId}")
    public AjaxResult getInfo(@PathVariable("QcId") Long QcId)
    {
        return AjaxResult.success(mmsQcService.selectQcById(QcId));
    }

    @Log(title = "Qc", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:qc:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsQc qc)
    {
        return toAjax(mmsQcService.insertQc(qc));
    }

    @Log(title = "Qc", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:qc:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsQc qc)
    {
        return toAjax(mmsQcService.updateQc(qc));
    }

    @Log(title = "Qc", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:qc:remove')")
    @DeleteMapping("/{QcIds}")
    public AjaxResult remove(@PathVariable Long[] QcIds)
    {
        return toAjax(mmsQcService.deleteQcByIds(QcIds));
    }
}
