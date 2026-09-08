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
import com.ruoyi.hr.domain.HrScheduleTemplate;
import com.ruoyi.hr.service.IHrScheduleTemplateService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/scheduleTemplate")
public class HrScheduleTemplateController extends BaseController
{
    @Autowired
    private IHrScheduleTemplateService hrScheduleTemplateService;

    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrScheduleTemplate template)
    {
        startPage();
        List<HrScheduleTemplate> list = hrScheduleTemplateService.selectHrScheduleTemplateList(template);
        return getDataTable(list);
    }

    @Log(title = "排班模板", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrScheduleTemplate template)
    {
        List<HrScheduleTemplate> list = hrScheduleTemplateService.selectHrScheduleTemplateList(template);
        ExcelUtil<HrScheduleTemplate> util = new ExcelUtil<>(HrScheduleTemplate.class);
        util.exportExcel(response, list, "排班模板");
    }

    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:query')")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId)
    {
        return AjaxResult.success(hrScheduleTemplateService.selectHrScheduleTemplateById(templateId));
    }

    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:add')")
    @Log(title = "排班模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrScheduleTemplate template)
    {
        return toAjax(hrScheduleTemplateService.insertHrScheduleTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:edit')")
    @Log(title = "排班模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrScheduleTemplate template)
    {
        return toAjax(hrScheduleTemplateService.updateHrScheduleTemplate(template));
    }

    @PreAuthorize("@ss.hasPermi('hr:scheduleTemplate:remove')")
    @Log(title = "排班模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds)
    {
        return toAjax(hrScheduleTemplateService.deleteHrScheduleTemplateByIds(templateIds));
    }
}
