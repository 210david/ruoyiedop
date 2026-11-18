package com.ruoyi.flowable.controller;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.ruoyi.flowable.domain.FlowLedger;
import com.ruoyi.flowable.domain.FlowLedgerGrant;
import com.ruoyi.flowable.service.IFlowLedgerService;

/**
 * 流程台账 Controller
 *
 * 后台（流程管理 → 台账配置）：台账定义、列配置、授权配置
 * 前台（流程中心 → 流程台账）：我的台账、台账数据、导出
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/ledger")
public class FlowLedgerController extends BaseController
{
    @Autowired
    private IFlowLedgerService flowLedgerService;

    // ============================================================
    // 后台配置
    // ============================================================

    @PreAuthorize("@ss.hasPermi('flowable:ledger:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowLedger query)
    {
        startPage();
        return getDataTable(flowLedgerService.selectFlowLedgerList(query));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:query')")
    @GetMapping("/{ledgerId}")
    public AjaxResult getInfo(@PathVariable Long ledgerId)
    {
        return success(flowLedgerService.selectFlowLedgerById(ledgerId));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:query')")
    @GetMapping("/fields")
    public AjaxResult fields(@RequestParam Long formId)
    {
        return success(flowLedgerService.selectLedgerFields(formId));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:add')")
    @Log(title = "流程台账", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FlowLedger ledger)
    {
        flowLedgerService.insertFlowLedger(ledger);
        // 返回带 ledgerId 的台账定义，便于前端「保存并进入详细设置」继续配置
        return success(ledger);
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:edit')")
    @Log(title = "流程台账", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FlowLedger ledger)
    {
        return toAjax(flowLedgerService.updateFlowLedger(ledger));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:remove')")
    @Log(title = "流程台账", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ledgerIds}")
    public AjaxResult remove(@PathVariable Long[] ledgerIds)
    {
        return toAjax(flowLedgerService.deleteFlowLedgerByIds(ledgerIds));
    }

    // ============================================================
    // 后台：授权配置
    // ============================================================

    @PreAuthorize("@ss.hasPermi('flowable:ledger:grant')")
    @GetMapping("/grant/list")
    public AjaxResult grantList(@RequestParam Long ledgerId)
    {
        return success(flowLedgerService.selectGrantList(ledgerId));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:grant')")
    @Log(title = "台账授权", businessType = BusinessType.INSERT)
    @PostMapping("/grant")
    public AjaxResult addGrant(@RequestBody FlowLedgerGrant grant)
    {
        return toAjax(flowLedgerService.insertGrant(grant));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:grant')")
    @Log(title = "台账授权", businessType = BusinessType.UPDATE)
    @PutMapping("/grant")
    public AjaxResult editGrant(@RequestBody FlowLedgerGrant grant)
    {
        return toAjax(flowLedgerService.updateGrant(grant));
    }

    @PreAuthorize("@ss.hasPermi('flowable:ledger:grant')")
    @Log(title = "台账授权", businessType = BusinessType.DELETE)
    @DeleteMapping("/grant/{grantIds}")
    public AjaxResult removeGrant(@PathVariable Long[] grantIds)
    {
        return toAjax(flowLedgerService.deleteGrantByIds(grantIds));
    }

    // ============================================================
    // 前台台账（登录即可，具体数据范围由授权决定）
    // ============================================================

    @GetMapping("/mine")
    public AjaxResult mine()
    {
        return success(flowLedgerService.selectMyLedgers());
    }

    @GetMapping("/{ledgerId}/config")
    public AjaxResult portalConfig(@PathVariable Long ledgerId)
    {
        return success(flowLedgerService.selectPortalConfig(ledgerId));
    }

    @GetMapping("/{ledgerId}/data")
    public AjaxResult data(@PathVariable Long ledgerId, @RequestParam Map<String, String> params)
    {
        return success(flowLedgerService.selectLedgerData(ledgerId, params));
    }

    @PostMapping("/{ledgerId}/export")
    public ResponseEntity<byte[]> export(@PathVariable Long ledgerId, @RequestParam Map<String, String> params)
    {
        byte[] bytes = flowLedgerService.exportLedgerData(ledgerId, params);
        String fileName = URLEncoder.encode("流程台账.xlsx", StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }
}
