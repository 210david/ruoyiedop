package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrLaborWorker;
import com.ruoyi.hr.service.IHrLaborWorkerService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/laborWorker")
public class HrLaborWorkerController extends BaseController
{
    @Autowired
    private IHrLaborWorkerService hrLaborWorkerService;

    @PreAuthorize("@ss.hasPermi('hr:laborWorker:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrLaborWorker hrLaborWorker)
    {
        startPage();
        List<HrLaborWorker> list = hrLaborWorkerService.selectHrLaborWorkerList(hrLaborWorker);
        return getDataTable(list);
    }

    @Log(title = "劳务用工", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:laborWorker:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrLaborWorker hrLaborWorker)
    {
        List<HrLaborWorker> list = hrLaborWorkerService.selectHrLaborWorkerList(hrLaborWorker);
        ExcelUtil<HrLaborWorker> util = new ExcelUtil<>(HrLaborWorker.class);
        util.exportExcel(response, list, "劳务用工");
    }

    @PreAuthorize("@ss.hasPermi('hr:laborWorker:query')")
    @GetMapping(value = "/{laborId}")
    public AjaxResult getInfo(@PathVariable("laborId") Long laborId)
    {
        return AjaxResult.success(hrLaborWorkerService.selectHrLaborWorkerById(laborId));
    }

    @PreAuthorize("@ss.hasPermi('hr:laborWorker:add')")
    @Log(title = "劳务用工", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrLaborWorker hrLaborWorker)
    {
        return toAjax(hrLaborWorkerService.insertHrLaborWorker(hrLaborWorker));
    }

    @PreAuthorize("@ss.hasPermi('hr:laborWorker:edit')")
    @Log(title = "劳务用工", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrLaborWorker hrLaborWorker)
    {
        return toAjax(hrLaborWorkerService.updateHrLaborWorker(hrLaborWorker));
    }

    @PreAuthorize("@ss.hasPermi('hr:laborWorker:remove')")
    @Log(title = "劳务用工", businessType = BusinessType.DELETE)
    @DeleteMapping("/{laborIds}")
    public AjaxResult remove(@PathVariable Long[] laborIds)
    {
        return toAjax(hrLaborWorkerService.deleteHrLaborWorkerByIds(laborIds));
    }
}
