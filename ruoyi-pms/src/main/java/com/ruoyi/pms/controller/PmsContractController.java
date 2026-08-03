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
import com.ruoyi.pms.domain.PmsContract;
import com.ruoyi.pms.domain.PmsContractChange;
import com.ruoyi.pms.service.IPmsContractService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 采购合同 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/contract")
public class PmsContractController extends BaseController
{
    @Autowired
    private IPmsContractService pmsContractService;

    @PreAuthorize("@ss.hasPermi('pms:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(PmsContract contract)
    {
        startPage();
        List<PmsContract> list = pmsContractService.selectContractList(contract);
        return getDataTable(list);
    }

    @Log(title = "采购合同", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pms:contract:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, PmsContract contract)
    {
        List<PmsContract> list = pmsContractService.selectContractList(contract);
        ExcelUtil<PmsContract> util = new ExcelUtil<>(PmsContract.class);
        util.exportExcel(response, list, "采购合同数据");
    }

    @PreAuthorize("@ss.hasPermi('pms:contract:query')")
    @GetMapping(value = "/{contractId}")
    public AjaxResult getInfo(@PathVariable("contractId") Long contractId)
    {
        return AjaxResult.success(pmsContractService.selectContractById(contractId));
    }

    @Log(title = "采购合同", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:contract:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody PmsContract contract)
    {
        return toAjax(pmsContractService.insertContract(contract));
    }

    @Log(title = "采购合同", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody PmsContract contract)
    {
        return toAjax(pmsContractService.updateContract(contract));
    }

    @Log(title = "采购合同", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pms:contract:remove')")
    @DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(pmsContractService.deleteContractByIds(contractIds));
    }

    @Log(title = "合同变更", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pms:contract:change')")
    @PostMapping("/change")
    public AjaxResult addChange(@Validated @RequestBody PmsContractChange change)
    {
        return toAjax(pmsContractService.addContractChange(change));
    }

    @Log(title = "合同变更审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:audit')")
    @PutMapping("/change/audit/{changeId}")
    public AjaxResult auditChange(@PathVariable Long changeId, @RequestParam String auditStatus,
                                  @RequestParam(required = false) String auditRemark)
    {
        return toAjax(pmsContractService.auditContractChange(changeId, auditStatus, auditRemark));
    }

    @Log(title = "合同变更批量审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:audit')")
    @PutMapping("/change/audit/contract/{contractId}/{auditStatus}")
    public AjaxResult auditChangeByContractId(@PathVariable Long contractId, @PathVariable String auditStatus,
                                               @RequestParam(required = false) String auditRemark)
    {
        return toAjax(pmsContractService.auditContractChangeByContractId(contractId, auditStatus, auditRemark));
    }

    @Log(title = "采购合同提交审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:edit')")
    @PutMapping("/submit/{contractId}")
    public AjaxResult submit(@PathVariable Long contractId)
    {
        return toAjax(pmsContractService.submitContract(contractId));
    }

    @Log(title = "采购合同审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:audit')")
    @PutMapping("/audit/{contractId}")
    public AjaxResult audit(@PathVariable Long contractId, @RequestParam String status,
                           @RequestParam(required = false) String auditOpinion)
    {
        return toAjax(pmsContractService.auditContract(contractId, status, auditOpinion));
    }

    @Log(title = "采购合同终止", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pms:contract:edit')")
    @PutMapping("/terminate/{contractId}")
    public AjaxResult terminate(@PathVariable Long contractId, @RequestParam String terminateReason)
    {
        return toAjax(pmsContractService.terminateContract(contractId, terminateReason));
    }
}
