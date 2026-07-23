package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkInvoice;
import com.ruoyi.mk.service.IMkInvoiceService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 发票 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/invoice")
public class MkInvoiceController extends BaseController
{
    @Autowired
    private IMkInvoiceService mkInvoiceService;

    @PreAuthorize("@ss.hasPermi('marketing:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkInvoice invoice)
    {
        startPage();
        List<MkInvoice> list = mkInvoiceService.selectInvoiceList(invoice);
        return getDataTable(list);
    }

    @Log(title = "发票管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkInvoice invoice)
    {
        List<MkInvoice> list = mkInvoiceService.selectInvoiceList(invoice);
        ExcelUtil<MkInvoice> util = new ExcelUtil<>(MkInvoice.class);
        util.exportExcel(response, list, "发票数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:invoice:query')")
    @GetMapping(value = "/{invoiceId}")
    public AjaxResult getInfo(@PathVariable("invoiceId") Long invoiceId)
    {
        return AjaxResult.success(mkInvoiceService.selectInvoiceById(invoiceId));
    }

    @Log(title = "发票管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkInvoice invoice)
    {
        return toAjax(mkInvoiceService.insertInvoice(invoice));
    }

    @Log(title = "发票管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkInvoice invoice)
    {
        return toAjax(mkInvoiceService.updateInvoice(invoice));
    }

    @Log(title = "发票管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:remove')")
    @DeleteMapping("/{invoiceIds}")
    public AjaxResult remove(@PathVariable Long[] invoiceIds)
    {
        return toAjax(mkInvoiceService.deleteInvoiceByIds(invoiceIds));
    }

    /**
     * 开票：待开票(0) -> 已开票(1)
     */
    @Log(title = "发票管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:issue')")
    @PutMapping("/issue/{invoiceId}")
    public AjaxResult issue(@PathVariable("invoiceId") Long invoiceId)
    {
        return toAjax(mkInvoiceService.issueInvoice(invoiceId));
    }

    /**
     * 作废发票：已开票(1) -> 已作废(2)
     */
    @Log(title = "发票管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:invoice:void')")
    @PutMapping("/void/{invoiceId}")
    public AjaxResult voidInvoice(@PathVariable("invoiceId") Long invoiceId)
    {
        return toAjax(mkInvoiceService.voidInvoice(invoiceId));
    }

    /**
     * 发票识别：上传PDF发票文件，自动解析发票信息
     */
    @PreAuthorize("@ss.hasPermi('marketing:invoice:add')")
    @PostMapping("/recognize")
    public AjaxResult recognize(@RequestParam("file") MultipartFile file)
    {
        try
        {
            return AjaxResult.success(mkInvoiceService.recognizeInvoice(file));
        }
        catch (Exception e)
        {
            return AjaxResult.error("发票识别失败：" + e.getMessage());
        }
    }
}
