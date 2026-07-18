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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.dms.domain.DmsWorkOrder;
import com.ruoyi.dms.service.IDmsWorkOrderLogService;
import com.ruoyi.dms.service.IDmsWorkOrderService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/dms/workorder")
public class DmsWorkOrderController extends BaseController
{
    @Autowired
    private IDmsWorkOrderService dmsWorkOrderService;

    @Autowired
    private IDmsWorkOrderLogService dmsWorkOrderLogService;

    @PreAuthorize("@ss.hasPermi('dms:workorder:list')")
    @GetMapping("/list")
    public TableDataInfo list(DmsWorkOrder workOrder)
    {
        startPage();
        List<DmsWorkOrder> list = dmsWorkOrderService.selectWorkOrderList(workOrder);
        return getDataTable(list);
    }

    @Log(title = "工单管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('dms:workorder:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DmsWorkOrder workOrder)
    {
        List<DmsWorkOrder> list = dmsWorkOrderService.selectWorkOrderList(workOrder);
        ExcelUtil<DmsWorkOrder> util = new ExcelUtil<>(DmsWorkOrder.class);
        util.exportExcel(response, list, "工单数据");
    }

    @PreAuthorize("@ss.hasPermi('dms:workorder:query')")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(dmsWorkOrderService.selectWorkOrderById(orderId));
    }

    @Log(title = "工单管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('dms:workorder:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DmsWorkOrder workOrder)
    {
        workOrder.setCreateBy(getUsername());
        return toAjax(dmsWorkOrderService.insertWorkOrder(workOrder));
    }

    @Log(title = "工单管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DmsWorkOrder workOrder)
    {
        workOrder.setUpdateBy(getUsername());
        return toAjax(dmsWorkOrderService.updateWorkOrder(workOrder));
    }

    @Log(title = "工单管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:remove')")
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds)
    {
        return toAjax(dmsWorkOrderService.deleteWorkOrderByIds(orderIds));
    }

    /** 派工 */
    @Log(title = "工单派工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/dispatch")
    public AjaxResult dispatch(@RequestBody DmsWorkOrder workOrder)
    {
        return toAjax(dmsWorkOrderService.dispatch(workOrder));
    }

    /** 改派 */
    @Log(title = "工单改派", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/reassign")
    public AjaxResult reassign(@RequestBody DmsWorkOrder workOrder)
    {
        return toAjax(dmsWorkOrderService.reassign(workOrder));
    }

    /** 接单 */
    @Log(title = "工单接单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/accept/{orderId}")
    public AjaxResult accept(@PathVariable("orderId") Long orderId)
    {
        return toAjax(dmsWorkOrderService.accept(orderId));
    }

    /** 开始处理 */
    @Log(title = "工单开始处理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/process/{orderId}")
    public AjaxResult startProcess(@PathVariable("orderId") Long orderId)
    {
        return toAjax(dmsWorkOrderService.startProcess(orderId));
    }

    /** 完工 */
    @Log(title = "工单完工", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/complete")
    public AjaxResult complete(@RequestBody DmsWorkOrder workOrder)
    {
        return toAjax(dmsWorkOrderService.complete(workOrder));
    }

    /** 验收通过 */
    @Log(title = "工单验收", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/verify")
    public AjaxResult verify(@RequestBody DmsWorkOrder workOrder)
    {
        return toAjax(dmsWorkOrderService.verify(workOrder));
    }

    /** 驳回重做 */
    @Log(title = "工单驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/reject")
    public AjaxResult reject(@RequestBody DmsWorkOrder workOrder)
    {
        return toAjax(dmsWorkOrderService.reject(workOrder));
    }

    /** 撤销 */
    @Log(title = "工单撤销", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('dms:workorder:edit')")
    @PutMapping("/cancel/{orderId}")
    public AjaxResult cancel(@PathVariable("orderId") Long orderId)
    {
        return toAjax(dmsWorkOrderService.cancel(orderId));
    }

    /** 查询工单操作历史 */
    @PreAuthorize("@ss.hasPermi('dms:workorder:query')")
    @GetMapping("/log/{orderId}")
    public AjaxResult orderLog(@PathVariable("orderId") Long orderId)
    {
        return AjaxResult.success(dmsWorkOrderLogService.selectLogByOrderId(orderId));
    }
}
