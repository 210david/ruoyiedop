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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mk.service.IMkInvoiceService;
import com.ruoyi.pjm.domain.PjmInvoice;
import com.ruoyi.pjm.service.IPjmInvoiceService;
import org.springframework.web.multipart.MultipartFile;

/**
 * 项目开票记录Controller
 */
@RestController
@RequestMapping("/pjm/invoice")
public class PjmInvoiceController extends BaseController
{
    @Autowired
    private IPjmInvoiceService pjmInvoiceService;

    /** 发票智能识别（复用营销模块发票识别服务：PDFBox解析电子发票PDF） */
    @Autowired
    private IMkInvoiceService mkInvoiceService;

    /**
     * 发票识别：上传PDF发票文件，自动解析发票信息
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:add')")
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

    /**
     * 查询开票记录列表
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmInvoice pjmInvoice)
    {
        startPage();
        List<PjmInvoice> list = pjmInvoiceService.selectInvoiceList(pjmInvoice);
        return getDataTable(list);
    }

    /**
     * 获取开票记录详情
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:query')")
    @GetMapping("/{invoiceId}")
    public AjaxResult getInfo(@PathVariable Long invoiceId)
    {
        return success(pjmInvoiceService.selectInvoiceById(invoiceId));
    }

    /**
     * 新增开票记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:add')")
    @Log(title = "项目开票", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmInvoice pjmInvoice)
    {
        return toAjax(pjmInvoiceService.insertInvoice(pjmInvoice));
    }

    /**
     * 修改开票记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:edit')")
    @Log(title = "项目开票", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmInvoice pjmInvoice)
    {
        return toAjax(pjmInvoiceService.updateInvoice(pjmInvoice));
    }

    /**
     * 删除开票记录
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:remove')")
    @Log(title = "项目开票", businessType = BusinessType.DELETE)
    @DeleteMapping("/{invoiceIds}")
    public AjaxResult remove(@PathVariable Long[] invoiceIds)
    {
        return toAjax(pjmInvoiceService.deleteInvoiceByIds(invoiceIds));
    }

    /**
     * 开票确认（待开票 -> 已开票）
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:issue')")
    @Log(title = "项目开票", businessType = BusinessType.UPDATE)
    @PutMapping("/issue/{invoiceId}")
    public AjaxResult issue(@PathVariable("invoiceId") Long invoiceId)
    {
        return toAjax(pjmInvoiceService.issueInvoice(invoiceId));
    }

    /**
     * 开票作废（已开票 -> 已作废）
     */
    @PreAuthorize("@ss.hasPermi('pjm:invoice:void')")
    @Log(title = "项目开票", businessType = BusinessType.UPDATE)
    @PutMapping("/void/{invoiceId}")
    public AjaxResult voidInvoice(@PathVariable("invoiceId") Long invoiceId)
    {
        return toAjax(pjmInvoiceService.voidInvoice(invoiceId));
    }
}
