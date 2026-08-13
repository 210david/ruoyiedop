package com.ruoyi.qms.controller;

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
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.service.IQmsNcrService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 不合格品报告 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/ncr")
public class QmsNcrController extends BaseController
{
    @Autowired
    private IQmsNcrService qmsNcrService;

    @Autowired
    private com.ruoyi.qms.service.IQmsCapaService qmsCapaService;

    @PreAuthorize("@ss.hasPermi('qms:ncr:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsNcr ncr)
    {
        startPage();
        List<QmsNcr> list = qmsNcrService.selectNcrList(ncr);
        return getDataTable(list);
    }

    @Log(title = "不合格品报告", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:ncr:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsNcr ncr)
    {
        List<QmsNcr> list = qmsNcrService.selectNcrList(ncr);
        ExcelUtil<QmsNcr> util = new ExcelUtil<>(QmsNcr.class);
        util.exportExcel(response, list, "不合格品报告数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:ncr:query')")
    @GetMapping(value = "/{ncrId}")
    public AjaxResult getInfo(@PathVariable("ncrId") Long ncrId)
    {
        return AjaxResult.success(qmsNcrService.selectNcrById(ncrId));
    }

    @Log(title = "不合格品报告", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:ncr:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsNcr ncr)
    {
        return toAjax(qmsNcrService.insertNcr(ncr));
    }

    @Log(title = "不合格品报告", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsNcr ncr)
    {
        return toAjax(qmsNcrService.updateNcr(ncr));
    }

    @Log(title = "不合格品报告", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:remove')")
    @DeleteMapping("/{ncrIds}")
    public AjaxResult remove(@PathVariable Long[] ncrIds)
    {
        return toAjax(qmsNcrService.deleteNcrByIds(ncrIds));
    }

    /**
     * 提交MRB评审：已登记(0) → 评审中(1)
     */
    @Log(title = "NCR提交评审", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:edit')")
    @PutMapping("/submitReview/{ncrId}")
    public AjaxResult submitReview(@PathVariable Long ncrId)
    {
        return toAjax(qmsNcrService.submitReview(ncrId));
    }

    /**
     * MRB审批通过：推进到下一节点或进入处置中
     */
    @Log(title = "NCR审批通过", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:approve')")
    @PutMapping("/approve/{ncrId}")
    public AjaxResult approve(@PathVariable Long ncrId, @RequestParam(required = false) String approveOpinion)
    {
        return toAjax(qmsNcrService.approveNode(ncrId, approveOpinion));
    }

    /**
     * MRB审批驳回：退回到已登记
     */
    @Log(title = "NCR审批驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:approve')")
    @PutMapping("/reject/{ncrId}")
    public AjaxResult reject(@PathVariable Long ncrId, @RequestParam(required = false) String approveOpinion)
    {
        return toAjax(qmsNcrService.rejectNode(ncrId, approveOpinion));
    }

    /**
     * 提交处置结论：处置中(2) → 待验证(3)
     */
    @Log(title = "NCR提交处置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:edit')")
    @PutMapping("/submitDisposition")
    public AjaxResult submitDisposition(@RequestBody QmsNcr ncr)
    {
        return toAjax(qmsNcrService.submitDisposition(ncr));
    }

    /**
     * NCR验证：待验证(3) → 已关闭(4) 或 处置中(2)
     */
    @Log(title = "NCR验证", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:approve')")
    @PutMapping("/verify")
    public AjaxResult verifyNcr(@RequestBody QmsNcr ncr)
    {
        return toAjax(qmsNcrService.verifyNcr(ncr));
    }

    /**
     * NCR关闭
     */
    @Log(title = "NCR关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:ncr:approve')")
    @PutMapping("/close/{ncrId}")
    public AjaxResult closeNcr(@PathVariable Long ncrId)
    {
        return toAjax(qmsNcrService.closeNcr(ncrId));
    }

    /**
     * 一键从NCR发起CAPA
     */
    @Log(title = "NCR发起CAPA", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:ncr:edit')")
    @PostMapping("/createCapa/{ncrId}")
    public AjaxResult createCapaFromNcr(@PathVariable Long ncrId)
    {
        Long capaId = qmsCapaService.createCapaFromNcr(ncrId);
        return AjaxResult.success(capaId);
    }
}
