package com.ruoyi.mk.controller;

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
import com.ruoyi.mk.domain.MkContract;
import com.ruoyi.mk.domain.MkContractChangeLog;
import com.ruoyi.mk.service.IMkContractService;
import com.ruoyi.mk.service.IMkContractChangeService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 合同 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/contract")
public class MkContractController extends BaseController
{
    @Autowired
    private IMkContractService mkContractService;

    @Autowired
    private IMkContractChangeService mkContractChangeService;

    @PreAuthorize("@ss.hasPermi('marketing:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkContract contract)
    {
        startPage();
        List<MkContract> list = mkContractService.selectContractList(contract);
        return getDataTable(list);
    }

    @Log(title = "合同管理", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:contract:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkContract contract)
    {
        List<MkContract> list = mkContractService.selectContractList(contract);
        ExcelUtil<MkContract> util = new ExcelUtil<>(MkContract.class);
        util.exportExcel(response, list, "合同数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:contract:query')")
    @GetMapping(value = "/{contractId}")
    public AjaxResult getInfo(@PathVariable("contractId") Long contractId)
    {
        return AjaxResult.success(mkContractService.selectContractById(contractId));
    }

    @Log(title = "合同管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:contract:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkContract contract)
    {
        return toAjax(mkContractService.insertContract(contract));
    }

    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkContract contract)
    {
        return toAjax(mkContractService.updateContract(contract));
    }

    @Log(title = "合同管理", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:remove')")
    @DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(mkContractService.deleteContractByIds(contractIds));
    }

    /**
     * 提交审批：草稿(0) -> 审批中(1)
     */
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:edit')")
    @PutMapping("/submit/{contractId}")
    public AjaxResult submit(@PathVariable("contractId") Long contractId)
    {
        return toAjax(mkContractService.submitApproval(contractId));
    }

    /**
     * 审批通过：审批中(1) -> 已生效(2)
     */
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:approve')")
    @PutMapping("/approve/{contractId}")
    public AjaxResult approve(@PathVariable("contractId") Long contractId, @RequestParam(required = false) String approveOpinion)
    {
        return toAjax(mkContractService.approveContract(contractId, approveOpinion));
    }

    /**
     * 审批驳回：审批中(1) -> 草稿(0)
     */
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:approve')")
    @PutMapping("/reject/{contractId}")
    public AjaxResult reject(@PathVariable("contractId") Long contractId, @RequestParam(required = false) String approveOpinion)
    {
        return toAjax(mkContractService.rejectContract(contractId, approveOpinion));
    }

    /**
     * 终止合同：已生效(2) -> 已终止(4)
     */
    @Log(title = "合同管理", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:edit')")
    @PutMapping("/terminate/{contractId}")
    public AjaxResult terminate(@PathVariable("contractId") Long contractId, @RequestParam(required = false) String terminateReason)
    {
        return toAjax(mkContractService.terminateContract(contractId, terminateReason));
    }

    /**
     * 合同续签
     */
    @Log(title = "合同管理", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:contract:renew')")
    @PostMapping("/renew/{contractId}")
    public AjaxResult renew(@PathVariable("contractId") Long contractId,
                           @RequestBody com.ruoyi.mk.domain.dto.ContractRenewDTO renewDTO)
    {
        return toAjax(mkContractService.renewContract(contractId, renewDTO));
    }

    /**
     * 查询即将到期的合同
     */
    @PreAuthorize("@ss.hasPermi('marketing:contract:list')")
    @GetMapping("/expiring/{days}")
    public AjaxResult expiring(@PathVariable("days") int days)
    {
        return AjaxResult.success(mkContractService.selectExpiringContracts(days));
    }

    /**
     * 提交合同变更申请
     */
    @Log(title = "合同变更", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:contract:change')")
    @PostMapping("/change")
    public AjaxResult submitChange(@RequestBody MkContractChangeLog changeLog)
    {
        return toAjax(mkContractService.submitContractChange(changeLog));
    }

    /**
     * 查询合同变更记录
     */
    @PreAuthorize("@ss.hasPermi('marketing:contract:list')")
    @GetMapping("/change/list/{contractId}")
    public AjaxResult changeLogs(@PathVariable("contractId") Long contractId)
    {
        return AjaxResult.success(mkContractService.selectContractChangeLogs(contractId));
    }

    /**
     * 审批合同变更
     */
    @Log(title = "合同变更", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:contract:change')")
    @PutMapping("/change/approve/{logId}/{approved}")
    public AjaxResult approveChange(@PathVariable("logId") Long logId,
                                   @PathVariable("approved") boolean approved,
                                   @RequestParam(required = false) String opinion)
    {
        return toAjax(mkContractChangeService.approveChange(logId, approved, opinion));
    }
}
