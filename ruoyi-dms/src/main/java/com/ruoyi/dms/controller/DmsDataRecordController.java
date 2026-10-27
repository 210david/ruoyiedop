package com.ruoyi.dms.controller;

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
import com.ruoyi.dms.domain.DmsDataRecord;
import com.ruoyi.dms.service.IDmsDataRecordService;

@RestController
@RequestMapping("/dms/data/record")
public class DmsDataRecordController extends BaseController
{
    @Autowired
    private IDmsDataRecordService service;

    @PreAuthorize("@ss.hasPermi('dms:data:record:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsDataRecord record) { startPage(); return getDataTable(service.selectRecordList(record)); }

    @PreAuthorize("@ss.hasPermi('dms:data:record:query')")
    @GetMapping("/{recordId}")
    public AjaxResult getInfo(@PathVariable Long recordId) { return AjaxResult.success(service.selectRecordById(recordId)); }

    @Log(title = "数据记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:data:record:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsDataRecord record) { return toAjax(service.insertRecord(record)); }

    @Log(title = "数据记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:data:record:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsDataRecord record)
    {
        record.setUpdateBy(getUsername());
        return toAjax(service.updateRecord(record));
    }

    @Log(title = "数据记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:data:record:remove')")
    @DeleteMapping("/{recordIds}")
    public AjaxResult remove(@PathVariable Long[] recordIds) { return toAjax(service.deleteRecordByIds(recordIds)); }
}
