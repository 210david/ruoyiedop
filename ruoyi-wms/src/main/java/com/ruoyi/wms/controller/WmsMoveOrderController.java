package com.ruoyi.wms.controller;

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
import com.ruoyi.wms.domain.WmsMoveOrder;
import com.ruoyi.wms.service.IWmsMoveOrderService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/wms/move")
public class WmsMoveOrderController extends BaseController
{
    @Autowired
    private IWmsMoveOrderService wmsMoveOrderService;

    @PreAuthorize("@ss.hasPermi('wms:move:list')")
    @GetMapping("/list")
    public TableDataInfo list(WmsMoveOrder move)
    {
        startPage();
        List<WmsMoveOrder> list = wmsMoveOrderService.selectMoveOrderList(move);
        return getDataTable(list);
    }

    @Log(title = "移库管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('wms:move:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, WmsMoveOrder move)
    {
        List<WmsMoveOrder> list = wmsMoveOrderService.selectMoveOrderList(move);
        ExcelUtil<WmsMoveOrder> util = new ExcelUtil<>(WmsMoveOrder.class);
        util.exportExcel(response, list, "移库单数据");
    }

    @PreAuthorize("@ss.hasPermi('wms:move:query')")
    @GetMapping(value = "/{moveId}")
    public AjaxResult getInfo(@PathVariable("moveId") Long moveId)
    {
        return AjaxResult.success(wmsMoveOrderService.selectMoveOrderById(moveId));
    }

    @Log(title = "移库管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('wms:move:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody WmsMoveOrder move)
    {
        return toAjax(wmsMoveOrderService.insertMoveOrder(move));
    }

    @Log(title = "移库管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:move:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody WmsMoveOrder move)
    {
        return toAjax(wmsMoveOrderService.updateMoveOrder(move));
    }

    @Log(title = "移库管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('wms:move:remove')")
    @DeleteMapping("/{moveIds}")
    public AjaxResult remove(@PathVariable Long[] moveIds)
    {
        return toAjax(wmsMoveOrderService.deleteMoveOrderByIds(moveIds));
    }

    @Log(title = "移库审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:move:edit')")
    @PutMapping("/approve/{moveId}")
    public AjaxResult approve(@PathVariable Long moveId)
    {
        return toAjax(wmsMoveOrderService.approveMove(moveId));
    }

    @Log(title = "执行移库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:move:edit')")
    @PutMapping("/execute/{moveId}")
    public AjaxResult execute(@PathVariable Long moveId)
    {
        return toAjax(wmsMoveOrderService.executeMove(moveId));
    }

    @Log(title = "移库作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('wms:move:edit')")
    @PutMapping("/void/{moveId}")
    public AjaxResult voidOrder(@PathVariable Long moveId)
    {
        return toAjax(wmsMoveOrderService.voidMove(moveId));
    }
}
