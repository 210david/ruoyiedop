package com.ruoyi.mms.controller;

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
import com.ruoyi.mms.domain.MmsSchedule;
import com.ruoyi.mms.service.IMmsScheduleService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 排产管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/schedule")
public class MmsScheduleController extends BaseController
{
    @Autowired
    private IMmsScheduleService mmsScheduleService;

    // ========== 标准 CRUD ==========

    /**
     * 查询排产列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:schedule:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsSchedule schedule)
    {
        startPage();
        List<MmsSchedule> list = mmsScheduleService.selectScheduleList(schedule);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "排产管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:schedule:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsSchedule schedule)
    {
        List<MmsSchedule> list = mmsScheduleService.selectScheduleList(schedule);
        ExcelUtil<MmsSchedule> util = new ExcelUtil<>(MmsSchedule.class);
        util.exportExcel(response, list, "排产管理数据");
    }

    /**
     * 根据ID获取排产详情
     */
    @PreAuthorize("@ss.hasPermi('mms:schedule:query')")
    @GetMapping(value = "/{scheduleId}")
    public AjaxResult getInfo(@PathVariable("scheduleId") Long scheduleId)
    {
        return AjaxResult.success(mmsScheduleService.selectScheduleById(scheduleId));
    }

    /**
     * 新增排产
     */
    @Log(title = "排产管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:schedule:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsSchedule schedule)
    {
        return toAjax(mmsScheduleService.insertSchedule(schedule));
    }

    /**
     * 修改排产
     */
    @Log(title = "排产管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsSchedule schedule)
    {
        return toAjax(mmsScheduleService.updateSchedule(schedule));
    }

    /**
     * 删除排产（软删除）
     */
    @Log(title = "排产管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:remove')")
    @DeleteMapping("/{scheduleIds}")
    public AjaxResult remove(@PathVariable Long[] scheduleIds)
    {
        return toAjax(mmsScheduleService.deleteScheduleByIds(scheduleIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 排产确认（已废弃：流程简化后直接下达）
     */
    @Log(title = "排产管理-确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:confirm')")
    @PutMapping("/confirm/{scheduleId}")
    public AjaxResult confirm(@PathVariable("scheduleId") Long scheduleId)
    {
        return toAjax(mmsScheduleService.confirmSchedule(scheduleId));
    }

    /**
     * 排产下达（已废弃：流程简化后直接下达）
     */
    @Log(title = "排产管理-下达", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:release')")
    @PutMapping("/release/{scheduleId}")
    public AjaxResult release(@PathVariable("scheduleId") Long scheduleId)
    {
        return toAjax(mmsScheduleService.releaseSchedule(scheduleId));
    }

    /**
     * 排产取消
     * 状态：0(已下达) → 1(已取消)
     */
    @Log(title = "排产管理-取消", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:cancel')")
    @PutMapping("/cancel/{scheduleId}")
    public AjaxResult cancel(@PathVariable("scheduleId") Long scheduleId,
            @RequestParam(required = false) String cancelReason)
    {
        return toAjax(mmsScheduleService.cancelSchedule(scheduleId, cancelReason));
    }

    // ========== 甘特图排产工作台 ==========

    /**
     * 按日期范围查询排产任务（不分页，供甘特图渲染）
     */
    @PreAuthorize("@ss.hasPermi('mms:schedule:list')")
    @GetMapping("/gantt")
    public AjaxResult gantt(@RequestParam("startDate") String startDate,
                            @RequestParam("endDate") String endDate)
    {
        return AjaxResult.success(mmsScheduleService.selectScheduleByDateRange(startDate, endDate));
    }

    /**
     * 拖拽保存：更新排产任务的时间和资源
     */
    @Log(title = "排产管理-拖拽", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:schedule:edit')")
    @PutMapping("/drag")
    public AjaxResult drag(@RequestBody MmsSchedule schedule)
    {
        return toAjax(mmsScheduleService.dragUpdateSchedule(schedule));
    }

    /**
     * 批量排产：将多个工单排到指定产能单元
     */
    @Log(title = "排产管理-批量排产", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:schedule:add')")
    @PostMapping("/batch")
    public AjaxResult batch(@RequestBody List<MmsSchedule> scheduleList)
    {
        java.util.Map<String, Object> result = mmsScheduleService.batchSchedule(scheduleList);
        boolean hasConflict = (boolean) result.get("hasConflict");
        AjaxResult ajax = AjaxResult.success(result);
        if (hasConflict)
        {
            // 有冲突但已保存成功，msg 中带提示
            ajax.put("msg", "排产保存成功，但存在冲突");
        }
        return ajax;
    }

    /**
     * 冲突检测：检查同一资源同一时段是否有重叠
     */
    @PreAuthorize("@ss.hasPermi('mms:schedule:list')")
    @GetMapping("/checkConflict")
    public AjaxResult checkConflict(@RequestParam("resourceId") Long resourceId,
                                   @RequestParam("planStart") String planStart,
                                   @RequestParam("planEnd") String planEnd,
                                   @RequestParam(value = "excludeScheduleId", required = false) Long excludeScheduleId)
    {
        try
        {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            java.util.Date start = sdf.parse(planStart);
            java.util.Date end = sdf.parse(planEnd);
            boolean conflict = mmsScheduleService.checkConflict(resourceId, start, end, excludeScheduleId);
            java.util.Map<String, Object> result = new java.util.HashMap<>();
            result.put("conflict", conflict);
            if (conflict)
            {
                result.put("msg", "该产能单元在此时段已有排产任务，存在冲突");
            }
            return AjaxResult.success(result);
        }
        catch (Exception e)
        {
            return AjaxResult.error("参数解析失败: " + e.getMessage());
        }
    }

    /**
     * 查询所有有已下达排产记录的工单ID（不限日期范围，供待排产列表排除）
     */
    @PreAuthorize("@ss.hasPermi('mms:schedule:list')")
    @GetMapping("/wo/scheduled-ids")
    public AjaxResult scheduledWoIds()
    {
        return AjaxResult.success(mmsScheduleService.selectScheduledWorkOrderIds());
    }
}
