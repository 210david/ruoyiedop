package com.ruoyi.mms.controller;

import java.math.BigDecimal;
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
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.service.IMmsWorkOrderService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 生产工单 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/workorder")
public class MmsWorkOrderController extends BaseController
{
    @Autowired
    private IMmsWorkOrderService mmsWorkOrderService;

    // ========== 标准 CRUD ==========

    /**
     * 查询工单列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('mms:workorder:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsWorkOrder workorder)
    {
        startPage();
        List<MmsWorkOrder> list = mmsWorkOrderService.selectWorkOrderList(workorder);
        return getDataTable(list);
    }

    /**
     * 导出 Excel
     */
    @Log(title = "生产工单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:workorder:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsWorkOrder workorder)
    {
        List<MmsWorkOrder> list = mmsWorkOrderService.selectWorkOrderList(workorder);
        ExcelUtil<MmsWorkOrder> util = new ExcelUtil<>(MmsWorkOrder.class);
        util.exportExcel(response, list, "生产工单数据");
    }

    /**
     * 根据ID获取工单详情
     */
    @PreAuthorize("@ss.hasPermi('mms:workorder:query')")
    @GetMapping(value = "/{workOrderId}")
    public AjaxResult getInfo(@PathVariable("workOrderId") Long workOrderId)
    {
        return AjaxResult.success(mmsWorkOrderService.selectWorkOrderById(workOrderId));
    }

    /**
     * 新增工单
     */
    @Log(title = "生产工单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:workorder:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MmsWorkOrder workorder)
    {
        return toAjax(mmsWorkOrderService.insertWorkOrder(workorder));
    }

    /**
     * 修改工单
     */
    @Log(title = "生产工单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MmsWorkOrder workorder)
    {
        return toAjax(mmsWorkOrderService.updateWorkOrder(workorder));
    }

    /**
     * 删除工单（软删除）
     */
    @Log(title = "生产工单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:remove')")
    @DeleteMapping("/{workOrderIds}")
    public AjaxResult remove(@PathVariable Long[] workOrderIds)
    {
        return toAjax(mmsWorkOrderService.deleteWorkOrderByIds(workOrderIds));
    }

    // ========== 业务流程操作 ==========

    /**
     * 工单下达预览
     * 返回BOM明细+工艺工序+校验结果，供前端弹窗展示
     */
    @PreAuthorize("@ss.hasPermi('mms:workorder:release')")
    @GetMapping("/release/preview/{workOrderId}")
    public AjaxResult releasePreview(@PathVariable("workOrderId") Long workOrderId)
    {
        return mmsWorkOrderService.getReleasePreview(workOrderId);
    }

    /**
     * 工单下达
     * 状态：0(新建) → 1(已下达)
     * 事务内完成：校验状态、冻结BOM/工艺快照、生成首工序派工单、写入下达时间
     */
    @Log(title = "生产工单-下达", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:release')")
    @PutMapping("/release/{workOrderId}")
    public AjaxResult release(@PathVariable("workOrderId") Long workOrderId)
    {
        return toAjax(mmsWorkOrderService.releaseWorkOrder(workOrderId));
    }

    /**
     * 工单暂停
     * 状态：1(已下达)/2(执行中) → 7(已暂停)
     */
    @Log(title = "生产工单-暂停", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:pause')")
    @PutMapping("/pause/{workOrderId}")
    public AjaxResult pause(@PathVariable("workOrderId") Long workOrderId,
            @RequestParam(required = false) String pauseReason)
    {
        return toAjax(mmsWorkOrderService.pauseWorkOrder(workOrderId, pauseReason));
    }

    /**
     * 工单恢复
     * 状态：7(已暂停) → 1(已下达)
     */
    @Log(title = "生产工单-恢复", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:resume')")
    @PutMapping("/resume/{workOrderId}")
    public AjaxResult resume(@PathVariable("workOrderId") Long workOrderId)
    {
        return toAjax(mmsWorkOrderService.resumeWorkOrder(workOrderId));
    }

    /**
     * 工单完工
     * 状态：2(执行中)/3(报工中) → 4(待完工质检)
     */
    @Log(title = "生产工单-完工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:finish')")
    @PutMapping("/finish/{workOrderId}")
    public AjaxResult finish(@PathVariable("workOrderId") Long workOrderId)
    {
        return toAjax(mmsWorkOrderService.finishWorkOrder(workOrderId));
    }

    /**
     * 工单关闭
     * 状态：4(待完工质检)/5(完工入库) → 6(已关闭)
     */
    @Log(title = "生产工单-关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:close')")
    @PutMapping("/close/{workOrderId}")
    public AjaxResult close(@PathVariable("workOrderId") Long workOrderId,
            @RequestParam(required = false) String closeRemark)
    {
        return toAjax(mmsWorkOrderService.closeWorkOrder(workOrderId, closeRemark));
    }

    /**
     * 工单作废
     * 状态：任意非已关闭/已作废状态 → 8(已作废)，需无在制报工
     */
    @Log(title = "生产工单-作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:workorder:cancel')")
    @PutMapping("/cancel/{workOrderId}")
    public AjaxResult cancel(@PathVariable("workOrderId") Long workOrderId,
            @RequestParam(required = false) String cancelReason)
    {
        return toAjax(mmsWorkOrderService.cancelWorkOrder(workOrderId, cancelReason));
    }

    /**
     * 工单拆分
     * 将一张工单拆分为两张，继承源单信息
     */
    @Log(title = "生产工单-拆分", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:workorder:split')")
    @PostMapping("/split/{workOrderId}")
    public AjaxResult split(@PathVariable("workOrderId") Long workOrderId,
            @RequestParam BigDecimal splitQty)
    {
        return AjaxResult.success(mmsWorkOrderService.splitWorkOrder(workOrderId, splitQty));
    }

    /**
     * 查询工单审核日志
     */
    @PreAuthorize("@ss.hasPermi('mms:workorder:query')")
    @GetMapping("/auditLog/{workOrderId}")
    public AjaxResult auditLog(@PathVariable("workOrderId") Long workOrderId)
    {
        return AjaxResult.success(mmsWorkOrderService.selectAuditLogByWorkOrderId(workOrderId));
    }
}
