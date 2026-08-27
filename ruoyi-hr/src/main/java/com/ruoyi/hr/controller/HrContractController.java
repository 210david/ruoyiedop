package com.ruoyi.hr.controller;

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
import com.ruoyi.hr.domain.HrContract;
import com.ruoyi.hr.service.IHrContractService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/contract")
public class HrContractController extends BaseController
{
    @Autowired
    private IHrContractService hrContractService;

    @PreAuthorize("@ss.hasPermi('hr:contract:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrContract hrContract)
    {
        startPage();
        List<HrContract> list = hrContractService.selectHrContractList(hrContract);
        return getDataTable(list);
    }

    @Log(title = "劳动合同", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:contract:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrContract hrContract)
    {
        List<HrContract> list = hrContractService.selectHrContractList(hrContract);
        ExcelUtil<HrContract> util = new ExcelUtil<>(HrContract.class);
        util.exportExcel(response, list, "劳动合同");
    }

    @PreAuthorize("@ss.hasPermi('hr:contract:query')")
    @GetMapping(value = "/{contractId}")
    public AjaxResult getInfo(@PathVariable("contractId") Long contractId)
    {
        return AjaxResult.success(hrContractService.selectHrContractById(contractId));
    }

    @PreAuthorize("@ss.hasPermi('hr:contract:add')")
    @Log(title = "劳动合同", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrContract hrContract)
    {
        return toAjax(hrContractService.insertHrContract(hrContract));
    }

    @PreAuthorize("@ss.hasPermi('hr:contract:edit')")
    @Log(title = "劳动合同", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrContract hrContract)
    {
        return toAjax(hrContractService.updateHrContract(hrContract));
    }

    @PreAuthorize("@ss.hasPermi('hr:contract:remove')")
    @Log(title = "劳动合同", businessType = BusinessType.DELETE)
    @DeleteMapping("/{contractIds}")
    public AjaxResult remove(@PathVariable Long[] contractIds)
    {
        return toAjax(hrContractService.deleteHrContractByIds(contractIds));
    }
}
