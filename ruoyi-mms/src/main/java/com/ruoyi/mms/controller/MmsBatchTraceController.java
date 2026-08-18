package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsBatchTrace;
import com.ruoyi.mms.service.IMmsBatchTraceService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/mms/trace")
public class MmsBatchTraceController extends BaseController
{
    @Autowired
    private IMmsBatchTraceService mmsBatchTraceService;

    @PreAuthorize("@ss.hasPermi('mms:trace:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsBatchTrace trace)
    {
        startPage();
        List<MmsBatchTrace> list = mmsBatchTraceService.selectBatchTraceList(trace);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('mms:trace:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsBatchTrace trace)
    {
        List<MmsBatchTrace> list = mmsBatchTraceService.selectBatchTraceList(trace);
        ExcelUtil<MmsBatchTrace> util = new ExcelUtil<>(MmsBatchTrace.class);
        util.exportExcel(response, list, "批次追溯数据");
    }

    @PreAuthorize("@ss.hasPermi('mms:trace:query')")
    @GetMapping(value = "/{traceId}")
    public AjaxResult getInfo(@PathVariable("traceId") Long traceId)
    {
        return AjaxResult.success(mmsBatchTraceService.selectBatchTraceById(traceId));
    }
}
