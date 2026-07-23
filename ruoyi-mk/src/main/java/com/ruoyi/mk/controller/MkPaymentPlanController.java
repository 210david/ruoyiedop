package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkPaymentPlan;
import com.ruoyi.mk.service.IMkPaymentPlanService;
import com.ruoyi.mk.service.IMkPaymentRecordService;
import com.ruoyi.mk.domain.MkPaymentRecord;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 回款计划 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/payment")
public class MkPaymentPlanController extends BaseController
{
    @Autowired
    private IMkPaymentPlanService mkPaymentPlanService;

    @Autowired
    private IMkPaymentRecordService mkPaymentRecordService;

    @PreAuthorize("@ss.hasPermi('marketing:payment:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkPaymentPlan paymentPlan)
    {
        startPage();
        List<MkPaymentPlan> list = mkPaymentPlanService.selectPaymentPlanList(paymentPlan);
        return getDataTable(list);
    }

    @Log(title = "回款管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:payment:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkPaymentPlan paymentPlan)
    {
        List<MkPaymentPlan> list = mkPaymentPlanService.selectPaymentPlanList(paymentPlan);
        ExcelUtil<MkPaymentPlan> util = new ExcelUtil<>(MkPaymentPlan.class);
        util.exportExcel(response, list, "回款计划数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:payment:query')")
    @GetMapping(value = "/detail/{planId}")
    public AjaxResult getInfo(@PathVariable("planId") Long planId)
    {
        return AjaxResult.success(mkPaymentPlanService.selectPaymentPlanById(planId));
    }

    @Log(title = "回款管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:payment:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkPaymentPlan paymentPlan)
    {
        return toAjax(mkPaymentPlanService.insertPaymentPlan(paymentPlan));
    }

    @Log(title = "回款管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:payment:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkPaymentPlan paymentPlan)
    {
        return toAjax(mkPaymentPlanService.updatePaymentPlan(paymentPlan));
    }

    @Log(title = "回款管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:payment:remove')")
    @DeleteMapping("/{planIds}")
    public AjaxResult remove(@PathVariable Long[] planIds)
    {
        return toAjax(mkPaymentPlanService.deletePaymentPlanByIds(planIds));
    }

    /**
     * 回款统计
     */
    @PreAuthorize("@ss.hasPermi('marketing:payment:list')")
    @GetMapping("/statistics")
    public AjaxResult statistics()
    {
        return AjaxResult.success(mkPaymentPlanService.selectPaymentStatistics());
    }

    // ==================== 回款记录接口 ====================

    /**
     * 查询回款记录列表
     */
    @PreAuthorize("@ss.hasPermi('marketing:payment:list')")
    @GetMapping("/record/list")
    public TableDataInfo recordList(MkPaymentRecord record)
    {
        startPage();
        List<MkPaymentRecord> list = mkPaymentRecordService.selectPaymentRecordList(record);
        return getDataTable(list);
    }

    /**
     * 查询回款记录详细
     */
    @PreAuthorize("@ss.hasPermi('marketing:payment:query')")
    @GetMapping("/record/{recordId}")
    public AjaxResult recordGetInfo(@PathVariable("recordId") Long recordId)
    {
        return AjaxResult.success(mkPaymentRecordService.selectPaymentRecordById(recordId));
    }

    /**
     * 新增回款记录（多次回款登记）
     */
    @Log(title = "回款记录", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:payment:edit')")
    @PostMapping("/record")
    public AjaxResult recordAdd(@Validated @RequestBody MkPaymentRecord record)
    {
        return toAjax(mkPaymentRecordService.insertPaymentRecord(record));
    }

    /**
     * 修改回款记录
     */
    @Log(title = "回款记录", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:payment:edit')")
    @PutMapping("/record")
    public AjaxResult recordEdit(@Validated @RequestBody MkPaymentRecord record)
    {
        return toAjax(mkPaymentRecordService.updatePaymentRecord(record));
    }

    /**
     * 删除回款记录
     */
    @Log(title = "回款记录", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:payment:edit')")
    @DeleteMapping("/record/{recordIds}")
    public AjaxResult recordRemove(@PathVariable Long[] recordIds)
    {
        return toAjax(mkPaymentRecordService.deletePaymentRecordByIds(recordIds));
    }

    /**
     * 确认回款记录
     */
    @Log(title = "回款确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:payment:confirm')")
    @PutMapping("/record/confirm/{recordId}/{confirmStatus}")
    public AjaxResult recordConfirm(@PathVariable("recordId") Long recordId,
                                    @PathVariable("confirmStatus") String confirmStatus,
                                    @RequestParam(required = false) String remark)
    {
        return toAjax(mkPaymentRecordService.confirmPaymentRecord(recordId, confirmStatus, remark));
    }
}
