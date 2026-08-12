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
import com.ruoyi.qms.domain.QmsInspectStd;
import com.ruoyi.qms.service.IQmsInspectStdService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 检验标准库 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/std")
public class QmsInspectStdController extends BaseController
{
    @Autowired
    private IQmsInspectStdService qmsInspectStdService;

    @PreAuthorize("@ss.hasPermi('qms:std:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsInspectStd inspectStd)
    {
        startPage();
        List<QmsInspectStd> list = qmsInspectStdService.selectInspectStdList(inspectStd);
        return getDataTable(list);
    }

    @Log(title = "检验标准库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:std:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsInspectStd inspectStd)
    {
        List<QmsInspectStd> list = qmsInspectStdService.selectInspectStdList(inspectStd);
        ExcelUtil<QmsInspectStd> util = new ExcelUtil<>(QmsInspectStd.class);
        util.exportExcel(response, list, "检验标准库数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:std:query')")
    @GetMapping(value = "/{stdId}")
    public AjaxResult getInfo(@PathVariable("stdId") Long stdId)
    {
        return AjaxResult.success(qmsInspectStdService.selectInspectStdById(stdId));
    }

    @Log(title = "检验标准库", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:std:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsInspectStd inspectStd)
    {
        return toAjax(qmsInspectStdService.insertInspectStd(inspectStd));
    }

    @Log(title = "检验标准库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:std:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsInspectStd inspectStd)
    {
        return toAjax(qmsInspectStdService.updateInspectStd(inspectStd));
    }

    @Log(title = "检验标准库", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:std:remove')")
    @DeleteMapping("/{stdIds}")
    public AjaxResult remove(@PathVariable Long[] stdIds)
    {
        return toAjax(qmsInspectStdService.deleteInspectStdByIds(stdIds));
    }
}
