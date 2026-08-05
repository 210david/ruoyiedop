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
import com.ruoyi.pms.domain.PmsReturn;
import com.ruoyi.pms.service.IPmsReturnService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 退货管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/return")
public class PmsReturnController extends BaseController
{
    @Autowired
    private IPmsReturnService pmsReturnService;

    @PreAuthorize("@ss.hasPermi('pms:return:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsReturn pmsReturn)
    {
        startPage();
        List<PmsReturn> list = pmsReturnService.selectReturnList(pmsReturn);
        return getDataTable(list);
    }

    /**
     * 获取存在进行中退货单的采购订单ID列表
     * 进行中 = 状态为草稿(0)、待审批(1)、已审批(2)、已驳回(5)
     * 前端用于过滤，避免同一订单同时存在多个进行中的退货单
     */
    @PreAuthorize("@ss.hasPermi('pms:return:list')")
    @GetMapping("/query/inProgressOrderIds")
    public AjaxResult getInProgressOrderIds()
    {
        return AjaxResult.success(pmsReturnService.selectInProgressReturnOrderIds());
    }

    @Log(title = "退货管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:return:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsReturn pmsReturn)
    {
        List<PmsReturn> list = pmsReturnService.selectReturnList(pmsReturn);
        ExcelUtil<PmsReturn> util = new ExcelUtil<>(PmsReturn.class);
        util.exportExcel(response, list, "退货数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:return:query')")
    @GetMapping(value = "/info/{returnId:\\d+}")
    public AjaxResult getInfo(@PathVariable("returnId") Long returnId)
    {
        return AjaxResult.success(pmsReturnService.selectReturnById(returnId));
    }

    @Log(title = "退货管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:return:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsReturn pmsReturn)
    {
        return toAjax(pmsReturnService.insertReturn(pmsReturn));
    }

    @Log(title = "退货管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:return:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsReturn pmsReturn)
    {
        return toAjax(pmsReturnService.updateReturn(pmsReturn));
    }

    @Log(title = "退货管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:return:remove')")
    @DeleteMapping("/{returnIds}")
    public AjaxResult remove(@PathVariable Long[] returnIds)
    {
        return toAjax(pmsReturnService.deleteReturnByIds(returnIds));
    }

    @Log(title = "退货提交审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:return:edit')")
    @PutMapping("/submit/{returnId}")
    public AjaxResult submit(@PathVariable Long returnId)
    {
        return toAjax(pmsReturnService.submitReturn(returnId));
    }

    @Log(title = "退货审批", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:return:audit')")
    @PutMapping("/audit/{returnId}")
    public AjaxResult audit(@PathVariable Long returnId, @RequestParam String status,
                          @RequestParam(required = false) String auditOpinion)
    {
        java.util.Map<String, Object> result = pmsReturnService.auditReturn(returnId, status, auditOpinion);
        AjaxResult ajax = AjaxResult.success("操作成功");
        if (result.get("outboundOrderNo") != null)
        {
            ajax.put("outboundOrderNo", result.get("outboundOrderNo"));
        }
        if (result.get("outboundOrderError") != null)
        {
            ajax.put("outboundOrderError", result.get("outboundOrderError"));
        }
        return ajax;
    }
}
