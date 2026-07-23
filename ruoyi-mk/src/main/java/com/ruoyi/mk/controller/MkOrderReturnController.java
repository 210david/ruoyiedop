package com.ruoyi.mk.controller;

import java.math.BigDecimal;
import java.util.Date;
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
import com.ruoyi.mk.domain.MkOrderReturn;
import com.ruoyi.mk.service.IMkOrderReturnService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 订单退货 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/return")
public class MkOrderReturnController extends BaseController
{
    @Autowired
    private IMkOrderReturnService mkOrderReturnService;

    @PreAuthorize("@ss.hasPermi('marketing:return:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkOrderReturn orderReturn)
    {
        startPage();
        List<MkOrderReturn> list = mkOrderReturnService.selectReturnList(orderReturn);
        return getDataTable(list);
    }

    @Log(title = "订单退货", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:return:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkOrderReturn orderReturn)
    {
        List<MkOrderReturn> list = mkOrderReturnService.selectReturnList(orderReturn);
        ExcelUtil<MkOrderReturn> util = new ExcelUtil<>(MkOrderReturn.class);
        util.exportExcel(response, list, "退货数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:return:query')")
    @GetMapping(value = "/{returnId}")
    public AjaxResult getInfo(@PathVariable("returnId") Long returnId)
    {
        return AjaxResult.success(mkOrderReturnService.selectReturnById(returnId));
    }

    @Log(title = "订单退货", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:return:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkOrderReturn orderReturn)
    {
        return toAjax(mkOrderReturnService.insertReturn(orderReturn));
    }

    @Log(title = "订单退货", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:return:remove')")
    @DeleteMapping("/{returnIds}")
    public AjaxResult remove(@PathVariable Long[] returnIds)
    {
        return toAjax(mkOrderReturnService.deleteReturnByIds(returnIds));
    }

    /**
     * 审批退货
     */
    @Log(title = "订单退货", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:return:approve')")
    @PutMapping("/approve/{returnId}/{approved}")
    public AjaxResult approve(@PathVariable("returnId") Long returnId,
                              @PathVariable("approved") boolean approved,
                              @RequestParam(required = false) String opinion)
    {
        return toAjax(mkOrderReturnService.approveReturn(returnId, approved, opinion));
    }

    /**
     * 退款
     */
    @Log(title = "订单退货", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:return:refund')")
    @PutMapping("/refund/{returnId}")
    public AjaxResult refund(@PathVariable("returnId") Long returnId,
                            @RequestParam BigDecimal refundAmount,
                            @RequestParam String refundDate)
    {
        return toAjax(mkOrderReturnService.refundReturn(returnId, refundAmount, java.sql.Date.valueOf(refundDate)));
    }
}
