package com.ruoyi.pms.controller;

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
import com.ruoyi.pms.domain.PmsReceive;
import com.ruoyi.pms.service.IPmsReceiveService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 收货验收 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/receive")
public class PmsReceiveController extends BaseController
{
    @Autowired
    private IPmsReceiveService pmsReceiveService;

    @PreAuthorize("@ss.hasPermi('pms:receive:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsReceive receive)
    {
        startPage();
        List<PmsReceive> list = pmsReceiveService.selectReceiveList(receive);
        return getDataTable(list);
    }

    @Log(title = "收货验收", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:receive:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsReceive receive)
    {
        List<PmsReceive> list = pmsReceiveService.selectReceiveList(receive);
        ExcelUtil<PmsReceive> util = new ExcelUtil<>(PmsReceive.class);
        util.exportExcel(response, list, "收货验收数据");
    }

    /**
     * 获取已有有效收货记录的采购订单ID列表
     * 前端用于过滤已收货的订单，避免重复发起收货
     */
    @PreAuthorize("@ss.hasPermi('pms:receive:list')")
    @GetMapping("/receivedOrderIds")
    public AjaxResult getReceivedOrderIds()
    {
        return AjaxResult.success(pmsReceiveService.selectReceivedOrderIds());
    }

    /**
     * 获取存在进行中收货单的采购订单ID列表
     * 进行中 = 状态为待验收(0)、待审核(5)、已驳回(6)
     * 前端用于过滤，避免同一订单同时存在多个进行中的收货单
     */
    @PreAuthorize("@ss.hasPermi('pms:receive:list')")
    @GetMapping("/inProgressOrderIds")
    public AjaxResult getInProgressOrderIds()
    {
        return AjaxResult.success(pmsReceiveService.selectInProgressReceiveOrderIds());
    }

    @PreAuthorize("@ss.hasPermi('pms:receive:query')")
    @GetMapping("/{receiveId}")
    public AjaxResult getInfo(@PathVariable("receiveId") Long receiveId)
    {
        return AjaxResult.success(pmsReceiveService.selectReceiveById(receiveId));
    }

    @Log(title = "收货验收", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:receive:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsReceive receive)
    {
        return toAjax(pmsReceiveService.insertReceive(receive));
    }

    @Log(title = "收货验收", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:receive:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsReceive receive)
    {
        return toAjax(pmsReceiveService.updateReceive(receive));
    }

    @Log(title = "收货验收", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:receive:remove')")
    @DeleteMapping("/{receiveIds}")
    public AjaxResult remove(@PathVariable Long[] receiveIds)
    {
        return toAjax(pmsReceiveService.deleteReceiveByIds(receiveIds));
    }

    @Log(title = "收货验收确认", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:receive:edit')")
    @PutMapping("/inspect")
    public AjaxResult inspect(@RequestBody PmsReceive receive)
    {
        return toAjax(pmsReceiveService.doInspect(receive));
    }

    @Log(title = "收货验收提交审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:receive:edit')")
    @PutMapping("/submit/{receiveId}")
    public AjaxResult submit(@PathVariable Long receiveId)
    {
        return toAjax(pmsReceiveService.submitReceive(receiveId));
    }

    @Log(title = "收货验收审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:receive:audit')")
    @PutMapping("/audit/{receiveId}")
    public AjaxResult audit(@PathVariable Long receiveId, @RequestParam String status,
                           @RequestParam(required = false) String auditOpinion)
    {
        java.util.Map<String, Object> result = pmsReceiveService.auditReceiveWithInboundOrder(receiveId, status, auditOpinion);
        AjaxResult ajax = AjaxResult.success("审核成功");
        ajax.putAll(result);
        return ajax;
    }
}
