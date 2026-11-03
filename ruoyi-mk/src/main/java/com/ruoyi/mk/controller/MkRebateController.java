package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.mk.domain.MkRebateOffset;
import com.ruoyi.mk.domain.MkRebatePolicy;
import com.ruoyi.mk.service.IMkRebateService;

/**
 * 返利政策/结算 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/rebate")
public class MkRebateController extends BaseController
{
    @Autowired
    private IMkRebateService mkRebateService;

    /** 政策列表（阶梯组聚合） */
    @PreAuthorize("@ss.hasPermi('marketing:rebate:list')")
    @GetMapping("/policyList")
    public TableDataInfo policyList(MkRebatePolicy policy)
    {
        startPage();
        List<java.util.Map<String, Object>> list = mkRebateService.selectPolicyGroupList(policy);
        return getDataTable(list);
    }

    /** 新增政策（阶梯组） */
    @Log(title = "返利政策", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:add')")
    @PostMapping("/policy")
    public AjaxResult addPolicy(@RequestBody List<MkRebatePolicy> tiers)
    {
        return toAjax(mkRebateService.insertPolicyGroup(tiers));
    }

    /** 修改政策（整组替换） */
    @Log(title = "返利政策", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:edit')")
    @PutMapping("/policy")
    public AjaxResult editPolicy(@RequestBody List<MkRebatePolicy> tiers)
    {
        return toAjax(mkRebateService.updatePolicyGroup(tiers));
    }

    /** 删除政策（整组） */
    @Log(title = "返利政策", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:remove')")
    @DeleteMapping("/policy/{policyNo}")
    public AjaxResult removePolicy(@PathVariable String policyNo)
    {
        return toAjax(mkRebateService.deletePolicyGroup(policyNo));
    }

    /** 结算单列表 */
    @PreAuthorize("@ss.hasPermi('marketing:rebate:list')")
    @GetMapping("/settleList")
    public TableDataInfo settleList(com.ruoyi.mk.domain.MkRebateSettle settle)
    {
        startPage();
        List<com.ruoyi.mk.domain.MkRebateSettle> list = mkRebateService.selectSettleList(settle);
        return getDataTable(list);
    }

    /** 返利核算 */
    @Log(title = "返利核算", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:settle')")
    @PostMapping("/settle")
    public AjaxResult settle(@RequestBody java.util.Map<String, String> body)
    {
        Long customerId = body.get("customerId") != null ? Long.parseLong(body.get("customerId")) : null;
        List<com.ruoyi.mk.domain.MkRebateSettle> created = mkRebateService.settle(customerId, body.get("period"));
        return AjaxResult.success("核算完成，生成结算单 " + created.size() + " 张", created);
    }

    // ====== 返利冲抵台账（财务性质按销售折扣处理，红票由财务据待办开具） ======

    /** 冲抵台账列表 */
    @PreAuthorize("@ss.hasPermi('marketing:rebate:offset:list')")
    @GetMapping("/offsetList")
    public TableDataInfo offsetList(MkRebateOffset offset)
    {
        startPage();
        List<MkRebateOffset> list = mkRebateService.selectOffsetList(offset);
        return getDataTable(list);
    }

    /** 按结算单查冲抵明细 */
    @PreAuthorize("@ss.hasPermi('marketing:rebate:list')")
    @GetMapping("/offset/{settleId}")
    public AjaxResult offsetBySettle(@PathVariable Long settleId)
    {
        return AjaxResult.success(mkRebateService.selectOffsetListBySettleId(settleId));
    }

    /** 冲抵登记（支持拆分，推送财务待办） */
    @Log(title = "返利冲抵登记", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:offset:add')")
    @PostMapping("/offset")
    public AjaxResult addOffset(@RequestBody MkRebateOffset offset)
    {
        return toAjax(mkRebateService.insertOffset(offset));
    }

    /** 冲抵审核（财务一级审批） */
    @Log(title = "返利冲抵审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:rebate:offset:audit')")
    @PutMapping("/offset/audit/{offsetId}")
    public AjaxResult auditOffset(@PathVariable Long offsetId, @RequestBody MkRebateOffset audit)
    {
        audit.setOffsetId(offsetId);
        return toAjax(mkRebateService.auditOffset(audit));
    }
}
