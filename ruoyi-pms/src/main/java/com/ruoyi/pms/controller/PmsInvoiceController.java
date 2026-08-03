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
import com.ruoyi.pms.domain.PmsInvoice;
import com.ruoyi.pms.service.IPmsInvoiceService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 发票结算 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/invoice")
public class PmsInvoiceController extends BaseController
{
    @Autowired
    private IPmsInvoiceService pmsInvoiceService;

    @PreAuthorize("@ss.hasPermi('pms:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsInvoice invoice)
    {
        startPage();
        List<PmsInvoice> list = pmsInvoiceService.selectInvoiceList(invoice);
        return getDataTable(list);
    }

    @Log(title = "发票结算", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:invoice:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsInvoice invoice)
    {
        List<PmsInvoice> list = pmsInvoiceService.selectInvoiceList(invoice);
        ExcelUtil<PmsInvoice> util = new ExcelUtil<>(PmsInvoice.class);
        util.exportExcel(response, list, "发票结算数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:invoice:query')")
    @GetMapping(value = "/{invoiceId}")
    public AjaxResult getInfo(@PathVariable("invoiceId") Long invoiceId)
    {
        return AjaxResult.success(pmsInvoiceService.selectInvoiceById(invoiceId));
    }

    @Log(title = "发票结算", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:invoice:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsInvoice invoice)
    {
        return toAjax(pmsInvoiceService.insertInvoice(invoice));
    }

    @Log(title = "发票结算", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:invoice:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsInvoice invoice)
    {
        return toAjax(pmsInvoiceService.updateInvoice(invoice));
    }

    @Log(title = "发票结算", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:invoice:remove')")
    @DeleteMapping("/{invoiceIds}")
    public AjaxResult remove(@PathVariable Long[] invoiceIds)
    {
        return toAjax(pmsInvoiceService.deleteInvoiceByIds(invoiceIds));
    }

    @Log(title = "发票提交审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:invoice:edit')")
    @PutMapping("/submit/{invoiceId}")
    public AjaxResult submit(@PathVariable Long invoiceId)
    {
        return toAjax(pmsInvoiceService.submitInvoice(invoiceId));
    }

    @Log(title = "发票审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:invoice:audit')")
    @PutMapping("/audit/{invoiceId}")
    public AjaxResult audit(@PathVariable Long invoiceId, @RequestParam String status,
                           @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsInvoiceService.auditInvoice(invoiceId, status, auditOpinion));
    }

    @Log(title = "发票付款", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:invoice:pay')")
    @PutMapping("/pay")
    public AjaxResult pay(@RequestBody com.ruoyi.pms.domain.dto.InvoicePaymentDTO paymentDTO)
    {
        return toAjax(pmsInvoiceService.payInvoice(paymentDTO));
    }

    /**
     * 三方匹配：采购订单、收货验收、发票金额比对
     */
    @PreAuthorize("@ss.hasPermi('pms:invoice:query')")
    @GetMapping("/match/{invoiceId}")
    public AjaxResult threeWayMatch(@PathVariable Long invoiceId)
    {
        return AjaxResult.success(pmsInvoiceService.threeWayMatch(invoiceId));
    }

    /**
     * 发票识别：上传PDF发票文件，自动解析发票信息
     */
    @PreAuthorize("@ss.hasPermi('pms:invoice:add')")
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestParam("file") org.springframework.web.multipart.MultipartFile file)
    {
        try
        {
            return AjaxResult.success(pmsInvoiceService.recognizeInvoice(file));
        }
        catch (Exception e)
        {
            return AjaxResult.error("发票识别失败：" + e.getMessage());
        }
    }
}
