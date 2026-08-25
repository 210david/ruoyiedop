package com.ruoyi.safety.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;
import com.ruoyi.safety.domain.vo.SafetyTrainingHoursExportVO;
import com.ruoyi.safety.service.ISafetyTrainingAttendeeService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/safety/training/attendee")
public class SafetyTrainingAttendeeController extends BaseController
{
    @Autowired
    private ISafetyTrainingAttendeeService safetyTrainingAttendeeService;

    @PreAuthorize("@ss.hasPermi('safety:training:attendee:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyTrainingAttendee attendee)
    {
        startPage();
        List<SafetyTrainingAttendee> list = safetyTrainingAttendeeService.selectSafetyTrainingAttendeeList(attendee);
        return getDataTable(list);
    }

    /** 学时统计列表 */
    @PreAuthorize("@ss.hasPermi('safety:training:hours:list')")
    @GetMapping("/hours")
    public TableDataInfo hoursList(SafetyTrainingAttendee attendee)
    {
        startPage();
        List<SafetyTrainingAttendee> list = safetyTrainingAttendeeService.selectHoursStatistics(attendee);
        return getDataTable(list);
    }

    /** 学时统计导出 */
    @Log(title = "学时统计", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:hours:export')")
    @PostMapping("/hours/export")
    public void hoursExport(HttpServletResponse response, SafetyTrainingAttendee attendee)
    {
        List<SafetyTrainingAttendee> list = safetyTrainingAttendeeService.selectHoursStatistics(attendee);
        // 转换为导出VO，与前端列表列保持一致
        List<SafetyTrainingHoursExportVO> exportList = new java.util.ArrayList<>();
        for (SafetyTrainingAttendee item : list)
        {
            SafetyTrainingHoursExportVO vo = new SafetyTrainingHoursExportVO();
            vo.setUserName(item.getUserName());
            vo.setDeptName(item.getDeptName());
            vo.setTotalHours(item.getTotalHours() != null ? item.getTotalHours() : BigDecimal.ZERO);
            vo.setAttendCount(item.getAttendCount() != null ? item.getAttendCount() : 0);
            vo.setPassCount(item.getPassCount() != null ? item.getPassCount() : 0);
            // 合格率
            int attend = item.getAttendCount() != null ? item.getAttendCount() : 0;
            int pass = item.getPassCount() != null ? item.getPassCount() : 0;
            vo.setPassRate(attend > 0 ? Math.round((float) pass / attend * 100) : 0);
            // 达标状态（年度标准24学时）
            BigDecimal hours = item.getTotalHours() != null ? item.getTotalHours() : BigDecimal.ZERO;
            vo.setComplianceStatus(hours.compareTo(BigDecimal.valueOf(24)) >= 0 ? "1" : "0");
            exportList.add(vo);
        }
        ExcelUtil<SafetyTrainingHoursExportVO> util = new ExcelUtil<>(SafetyTrainingHoursExportVO.class);
        util.exportExcel(response, exportList, "学时统计");
    }

    @Log(title = "参训人员", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:attendee:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyTrainingAttendee attendee)
    {
        List<SafetyTrainingAttendee> list = safetyTrainingAttendeeService.selectSafetyTrainingAttendeeList(attendee);
        ExcelUtil<SafetyTrainingAttendee> util = new ExcelUtil<>(SafetyTrainingAttendee.class);
        util.exportExcel(response, list, "参训人员");
    }

    @PreAuthorize("@ss.hasPermi('safety:training:attendee:query')")
    @GetMapping(value = "/{attendeeId}")
    public AjaxResult getInfo(@PathVariable("attendeeId") Long attendeeId)
    {
        return AjaxResult.success(safetyTrainingAttendeeService.selectSafetyTrainingAttendeeById(attendeeId));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:attendee:add')")
    @Log(title = "参训人员", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SafetyTrainingAttendee attendee)
    {
        return toAjax(safetyTrainingAttendeeService.insertSafetyTrainingAttendee(attendee));
    }

    /** 批量添加参训人员 */
    @PreAuthorize("@ss.hasPermi('safety:training:attendee:add')")
    @Log(title = "参训人员", businessType = BusinessType.INSERT)
    @PostMapping("/batch")
    public AjaxResult batchAdd(@RequestBody List<SafetyTrainingAttendee> attendeeList)
    {
        return toAjax(safetyTrainingAttendeeService.batchInsertAttendee(attendeeList));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:attendee:edit')")
    @Log(title = "参训人员", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SafetyTrainingAttendee attendee)
    {
        return toAjax(safetyTrainingAttendeeService.updateSafetyTrainingAttendee(attendee));
    }

    /** 批量维护考试成绩 */
    @PreAuthorize("@ss.hasPermi('safety:training:attendee:edit')")
    @Log(title = "参训人员", businessType = BusinessType.UPDATE)
    @PostMapping("/batchExamScore")
    public AjaxResult batchExamScore(@RequestBody java.util.Map<String, Object> payload)
    {
        return toAjax(safetyTrainingAttendeeService.batchUpdateExamScore(payload));
    }

    @PreAuthorize("@ss.hasPermi('safety:training:attendee:remove')")
    @Log(title = "参训人员", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attendeeIds}")
    public AjaxResult remove(@PathVariable Long[] attendeeIds)
    {
        return toAjax(safetyTrainingAttendeeService.deleteSafetyTrainingAttendeeByIds(attendeeIds));
    }

    /** 导入参训人员 */
    @Log(title = "参训人员", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('safety:training:attendee:add')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<SafetyTrainingAttendee> util = new ExcelUtil<>(SafetyTrainingAttendee.class);
        List<SafetyTrainingAttendee> list = util.importExcel(file.getInputStream());
        String operName = getUsername();
        String message = safetyTrainingAttendeeService.importAttendee(list, updateSupport, operName);
        return success(message);
    }

    /** 导入模板 */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SafetyTrainingAttendee> util = new ExcelUtil<>(SafetyTrainingAttendee.class);
        util.importTemplateExcel(response, "参训人员");
    }
}
