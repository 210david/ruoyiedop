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
import com.ruoyi.qms.domain.QmsDefectCode;
import com.ruoyi.qms.service.IQmsDefectCodeService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 缺陷代码 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/defect")
public class QmsDefectCodeController extends BaseController
{
    @Autowired
    private IQmsDefectCodeService qmsDefectCodeService;

    @PreAuthorize("@ss.hasPermi('qms:defect:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsDefectCode defectCode)
    {
        startPage();
        List<QmsDefectCode> list = qmsDefectCodeService.selectDefectCodeList(defectCode);
        return getDataTable(list);
    }

    @Log(title = "缺陷代码", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:defect:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsDefectCode defectCode)
    {
        List<QmsDefectCode> list = qmsDefectCodeService.selectDefectCodeList(defectCode);
        ExcelUtil<QmsDefectCode> util = new ExcelUtil<>(QmsDefectCode.class);
        util.exportExcel(response, list, "缺陷代码数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:defect:query')")
    @GetMapping(value = "/{defectId}")
    public AjaxResult getInfo(@PathVariable("defectId") Long defectId)
    {
        return AjaxResult.success(qmsDefectCodeService.selectDefectCodeById(defectId));
    }

    @Log(title = "缺陷代码", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:defect:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsDefectCode defectCode)
    {
        return toAjax(qmsDefectCodeService.insertDefectCode(defectCode));
    }

    @Log(title = "缺陷代码", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:defect:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsDefectCode defectCode)
    {
        return toAjax(qmsDefectCodeService.updateDefectCode(defectCode));
    }

    @Log(title = "缺陷代码", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:defect:remove')")
    @DeleteMapping("/{defectIds}")
    public AjaxResult remove(@PathVariable Long[] defectIds)
    {
        return toAjax(qmsDefectCodeService.deleteDefectCodeByIds(defectIds));
    }
}
