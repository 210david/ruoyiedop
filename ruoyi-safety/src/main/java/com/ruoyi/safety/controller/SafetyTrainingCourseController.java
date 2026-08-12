package com.ruoyi.safety.controller;

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
import com.ruoyi.safety.domain.SafetyTrainingCourse;
import com.ruoyi.safety.service.ISafetyTrainingCourseService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/training/course")
public class SafetyTrainingCourseController extends BaseController
{
    @Autowired
    private ISafetyTrainingCourseService safetyTrainingCourseService;

    @PreAuthorize("@ss.hasPermi('safety:training:course:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyTrainingCourse course)
    {
        startPage();
        List<SafetyTrainingCourse> list = safetyTrainingCourseService.selectSafetyTrainingCourseList(course);
        return getDataTable(list);
    }

    @Log(title = "培训课程", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:course:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyTrainingCourse course)
    {
        List<SafetyTrainingCourse> list = safetyTrainingCourseService.selectSafetyTrainingCourseList(course);
        ExcelUtil<SafetyTrainingCourse> util = new ExcelUtil<>(SafetyTrainingCourse.class);
        util.exportExcel(response, list, "培训课程");
    }

    @PreAuthorize("@ss.hasPermi('safety:training:course:query')")
    @GetMapping(value = "/{courseId}")
    public AjaxResult getInfo(@PathVariable("courseId") Long courseId)
    {
        return AjaxResult.success(safetyTrainingCourseService.selectSafetyTrainingCourseById(courseId));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:course:add')")
    @Log(title = "培训课程", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyTrainingCourse course)
    {
        return toAjax(safetyTrainingCourseService.insertSafetyTrainingCourse(course));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:course:edit')")
    @Log(title = "培训课程", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyTrainingCourse course)
    {
        return toAjax(safetyTrainingCourseService.updateSafetyTrainingCourse(course));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:course:remove')")
    @Log(title = "培训课程", businessType = BusinessType.DELETE)
    @DeleteMapping("/{courseIds}")
    public AjaxResult remove(@PathVariable Long[] courseIds)
    {
        return toAjax(safetyTrainingCourseService.deleteSafetyTrainingCourseByIds(courseIds));
    }
}
