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
import com.ruoyi.hr.domain.HrPieceworkRecord;
import com.ruoyi.hr.service.IHrPieceworkRecordService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/pieceworkRecord")
public class HrPieceworkRecordController extends BaseController
{
    @Autowired
    private IHrPieceworkRecordService hrPieceworkRecordService;

    @PreAuthorize("@ss.hasPermi('hr:piecework:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPieceworkRecord hrPieceworkRecord)
    {
        startPage();
        List<HrPieceworkRecord> list = hrPieceworkRecordService.selectHrPieceworkRecordList(hrPieceworkRecord);
        return getDataTable(list);
    }

    @Log(title = "计件报工", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:piecework:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrPieceworkRecord hrPieceworkRecord)
    {
        List<HrPieceworkRecord> list = hrPieceworkRecordService.selectHrPieceworkRecordList(hrPieceworkRecord);
        ExcelUtil<HrPieceworkRecord> util = new ExcelUtil<>(HrPieceworkRecord.class);
        util.exportExcel(response, list, "计件报工");
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:query')")
    @GetMapping(value = "/{pwRecordId}")
    public AjaxResult getInfo(@PathVariable("pwRecordId") Long pwRecordId)
    {
        return AjaxResult.success(hrPieceworkRecordService.selectHrPieceworkRecordById(pwRecordId));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:add')")
    @Log(title = "计件报工", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrPieceworkRecord hrPieceworkRecord)
    {
        return toAjax(hrPieceworkRecordService.insertHrPieceworkRecord(hrPieceworkRecord));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:edit')")
    @Log(title = "计件报工", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrPieceworkRecord hrPieceworkRecord)
    {
        return toAjax(hrPieceworkRecordService.updateHrPieceworkRecord(hrPieceworkRecord));
    }

    @PreAuthorize("@ss.hasPermi('hr:piecework:remove')")
    @Log(title = "计件报工", businessType = BusinessType.DELETE)
    @DeleteMapping("/{pwRecordIds}")
    public AjaxResult remove(@PathVariable Long[] pwRecordIds)
    {
        return toAjax(hrPieceworkRecordService.deleteHrPieceworkRecordByIds(pwRecordIds));
    }
}
