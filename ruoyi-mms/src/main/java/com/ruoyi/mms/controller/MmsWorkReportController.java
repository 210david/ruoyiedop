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
import com.ruoyi.mms.domain.MmsWorkReport;
import com.ruoyi.mms.service.IMmsWorkReportService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 报工 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/report")
public class MmsWorkReportController extends BaseController
{
    @Autowired
    private IMmsWorkReportService mmsWorkReportService;

    // ========== 标准 CRUD ==========

    /**
     * 查询报工列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:report:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsWorkReport report)
    {
        startPage();
        List<MmsWorkReport> list = mmsWorkReportService.selectWorkReportList(report);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "报工记录", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:report:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsWorkReport report)
    {
        List<MmsWorkReport> list = mmsWorkReportService.selectWorkReportList(report);
        ExcelUtil<MmsWorkReport> util = new ExcelUtil<>(MmsWorkReport.class);
        util.exportExcel(response, list, "报工记录数据");
    }

    /**
     * 根据ID获取报工详情
     */
    @PreAuthorize("@ss.hasPermi('mms:report:query')")
    @GetMapping(value = "/{reportId}")
    public AjaxResult getInfo(@PathVariable("reportId") Long reportId)
    {
        return AjaxResult.success(mmsWorkReportService.selectWorkReportById(reportId));
    }

    /**
     * 新增报工
     */
    @Log(title = "报工记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:report:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsWorkReport report)
    {
        return toAjax(mmsWorkReportService.insertWorkReport(report));
    }

    /**
     * 修改报工
     */
    @Log(title = "报工记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:report:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsWorkReport report)
    {
        return toAjax(mmsWorkReportService.updateWorkReport(report));
    }

    /**
     * 删除报工（软删除）
     */
    @Log(title = "报工记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:report:remove')")
    @DeleteMapping("/{reportIds}")
    public AjaxResult remove(@PathVariable Long[] reportIds)
    {
        return toAjax(mmsWorkReportService.deleteWorkReportByIds(reportIds));
    }

    // ========== 业务操作 ==========

    /**
     * 报工审核
     * 状态：0(待审核) → 1(已审核) 或 2(已驳回)
     * 审核通过时联动更新工单进度
     */
    @Log(title = "报工记录-审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:report:audit')")
    @PutMapping("/audit/{reportId}")
    public AjaxResult audit(@PathVariable("reportId") Long reportId,
            @RequestParam String status,
            @RequestParam(required = false) String auditRemark)
    {
        return toAjax(mmsWorkReportService.auditWorkReport(reportId, status, auditRemark));
    }
}
