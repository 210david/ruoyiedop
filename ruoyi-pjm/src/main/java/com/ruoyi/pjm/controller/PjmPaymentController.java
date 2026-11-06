package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmPayment;
import com.ruoyi.pjm.service.IPjmPaymentService;

/**
 * 项目收款记录Controller
 */
@RestController
@RequestMapping("/pjm/payment")
public class PjmPaymentController extends BaseController
{
    @Autowired
    private IPjmPaymentService pjmPaymentService;

    /**
     * 查询收款记录列表
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmPayment pjmPayment)
    {
        startPage();
        List<PjmPayment> list = pjmPaymentService.selectPaymentList(pjmPayment);
        return getDataTable(list);
    }

    /**
     * 按项目汇总回款进度
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:list')")
    @GetMapping("/summary")
    public TableDataInfo summary(PjmPayment pjmPayment)
    {
        startPage();
        List<PjmPayment> list = pjmPaymentService.selectPaymentSummary(pjmPayment);
        return getDataTable(list);
    }

    /**
     * 获取收款记录详情
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:query')")
    @GetMapping("/{paymentId}")
    public AjaxResult getInfo(@PathVariable Long paymentId)
    {
        return success(pjmPaymentService.selectPaymentById(paymentId));
    }

    /**
     * 新增收款记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:add')")
    @Log(title = "项目收款", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmPayment pjmPayment)
    {
        return toAjax(pjmPaymentService.insertPayment(pjmPayment));
    }

    /**
     * 修改收款记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:edit')")
    @Log(title = "项目收款", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmPayment pjmPayment)
    {
        return toAjax(pjmPaymentService.updatePayment(pjmPayment));
    }

    /**
     * 删除收款记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:payment:remove')")
    @Log(title = "项目收款", businessType = BusinessType.DELETE)
    @DeleteMapping("/{paymentIds}")
    public AjaxResult remove(@PathVariable Long[] paymentIds)
    {
        return toAjax(pjmPaymentService.deletePaymentByIds(paymentIds));
    }
}
