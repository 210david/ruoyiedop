package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.domain.MmsIssueDetail;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.service.IMmsIssueService;
import com.ruoyi.mms.service.IMmsReturnMaterialService;
import java.math.BigDecimal;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ReturnMaterial Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/return")
public class MmsReturnMaterialController extends BaseController
{
    @Autowired
    private IMmsReturnMaterialService mmsReturnMaterialService;

    @Autowired
    private IMmsIssueService mmsIssueService;

    @PreAuthorize("@ss.hasPermi('mms:return:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsReturnMaterial returnMaterial)
    {
        startPage();
        List<MmsReturnMaterial> list = mmsReturnMaterialService.selectReturnMaterialList(returnMaterial);
        return getDataTable(list);
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:return:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsReturnMaterial returnMaterial)
    {
        List<MmsReturnMaterial> list = mmsReturnMaterialService.selectReturnMaterialList(returnMaterial);
        ExcelUtil<MmsReturnMaterial> util = new ExcelUtil<>(MmsReturnMaterial.class);
        util.exportExcel(response, list, "ReturnMaterial");
    }

    @PreAuthorize("@ss.hasPermi('mms:return:query')")
    @GetMapping(value = "/{ReturnId}")
    public AjaxResult getInfo(@PathVariable("ReturnId") Long ReturnId)
    {
        return AjaxResult.success(mmsReturnMaterialService.selectReturnMaterialById(ReturnId));
    }

    /**
     * 查询已领料的领料单列表（供退料选择）
     */
    @PreAuthorize("@ss.hasPermi('mms:return:add')")
    @GetMapping("/issueList")
    public TableDataInfo issueList(MmsIssue issue)
    {
        // 只查已领料状态(1)的领料单
        issue.setStatus("1");
        startPage();
        List<MmsIssue> list = mmsIssueService.selectIssueList(issue);
        return getDataTable(list);
    }

    /**
     * 根据领料单ID查询领料单详情（带明细），供退料页面选择领料单后带出信息
     * 明细中包含已退数量和可退数量（支持多次退料）
     */
    @PreAuthorize("@ss.hasPermi('mms:return:add')")
    @GetMapping("/issueInfo/{issueId}")
    public AjaxResult getIssueInfo(@PathVariable("issueId") Long issueId)
    {
        MmsIssue issue = mmsIssueService.selectIssueById(issueId);
        if (issue == null)
        {
            return AjaxResult.error("领料单不存在");
        }
        // 查询该领料单的所有已退料明细，按物料编码汇总已退数量
        List<com.ruoyi.mms.domain.MmsReturnDetail> returnedDetails = mmsReturnMaterialService.selectReturnedQtyByIssueId(issueId);
        java.util.Map<String, BigDecimal> returnedMap = new java.util.HashMap<>();
        if (returnedDetails != null)
        {
            for (com.ruoyi.mms.domain.MmsReturnDetail rd : returnedDetails)
            {
                if (rd == null || rd.getMaterialCode() == null) continue;
                String key = rd.getMaterialCode();
                BigDecimal qty = rd.getReturnQty() != null ? rd.getReturnQty() : BigDecimal.ZERO;
                BigDecimal existing = returnedMap.get(key);
                returnedMap.put(key, existing == null ? qty : existing.add(qty));
            }
        }
        // 为每条领料明细设置已退数量和可退数量
        if (issue.getDetailList() != null)
        {
            for (MmsIssueDetail d : issue.getDetailList())
            {
                BigDecimal returned = returnedMap.get(d.getMaterialCode());
                if (returned == null) returned = BigDecimal.ZERO;
                d.setReturnedQty(returned);
                BigDecimal issueQty = d.getIssueQty() != null ? d.getIssueQty() : BigDecimal.ZERO;
                BigDecimal available = issueQty.subtract(returned);
                d.setAvailableQty(available.compareTo(BigDecimal.ZERO) < 0 ? BigDecimal.ZERO : available);
            }
        }
        return AjaxResult.success(issue);
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:return:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsReturnMaterial returnMaterial)
    {
        return toAjax(mmsReturnMaterialService.insertReturnMaterial(returnMaterial));
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:return:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsReturnMaterial returnMaterial)
    {
        return toAjax(mmsReturnMaterialService.updateReturnMaterial(returnMaterial));
    }

    @Log(title = "ReturnMaterial", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:return:remove')")
    @DeleteMapping("/{ReturnIds}")
    public AjaxResult remove(@PathVariable Long[] ReturnIds)
    {
        return toAjax(mmsReturnMaterialService.deleteReturnMaterialByIds(ReturnIds));
    }

    /**
     * 确认退料：状态 0(待退料) → 1(已退料)，记录退料时间
     */
    @Log(title = "退料-确认退料", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:return:confirm')")
    @PutMapping("/confirm")
    public AjaxResult confirm(@RequestBody MmsReturnMaterial returnMaterial)
    {
        return toAjax(mmsReturnMaterialService.confirmReturnMaterial(returnMaterial));
    }
}
