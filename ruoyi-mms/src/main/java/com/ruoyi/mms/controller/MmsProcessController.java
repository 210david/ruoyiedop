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
import com.ruoyi.mms.domain.MmsProcess;
import com.ruoyi.mms.service.IMmsProcessService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Process Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/process")
public class MmsProcessController extends BaseController
{
    @Autowired
    private IMmsProcessService mmsProcessService;

    @PreAuthorize("@ss.hasPermi('mms:process:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsProcess process)
    {
        startPage();
        List<MmsProcess> list = mmsProcessService.selectProcessList(process);
        return getDataTable(list);
    }

    @Log(title = "Process", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:process:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsProcess process)
    {
        List<MmsProcess> list = mmsProcessService.selectProcessList(process);
        ExcelUtil<MmsProcess> util = new ExcelUtil<>(MmsProcess.class);
        util.exportExcel(response, list, "Process");
    }

    @PreAuthorize("@ss.hasPermi('mms:process:query')")
    @GetMapping(value = "/{ProcessId}")
    public AjaxResult getInfo(@PathVariable("ProcessId") Long ProcessId)
    {
        return AjaxResult.success(mmsProcessService.selectProcessById(ProcessId));
    }

    @Log(title = "Process", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:process:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsProcess process)
    {
        return toAjax(mmsProcessService.insertProcess(process));
    }

    @Log(title = "Process", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:process:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsProcess process)
    {
        return toAjax(mmsProcessService.updateProcess(process));
    }

    @Log(title = "Process", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:process:remove')")
    @DeleteMapping("/{ProcessIds}")
    public AjaxResult remove(@PathVariable Long[] ProcessIds)
    {
        return toAjax(mmsProcessService.deleteProcessByIds(ProcessIds));
    }
}
