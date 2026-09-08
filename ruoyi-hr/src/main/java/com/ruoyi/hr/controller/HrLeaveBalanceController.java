package com.ruoyi.hr.controller;

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
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrLeaveBalance;
import com.ruoyi.hr.domain.dto.HrLeaveBalanceBatchDTO;
import com.ruoyi.hr.service.IHrLeaveBalanceService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/leaveBalance")
public class HrLeaveBalanceController extends BaseController
{
    @Autowired
    private IHrLeaveBalanceService hrLeaveBalanceService;

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrLeaveBalance hrLeaveBalance)
    {
        startPage();
        List<HrLeaveBalance> list = hrLeaveBalanceService.selectHrLeaveBalanceList(hrLeaveBalance);
        return getDataTable(list);
    }

    @Log(title = "假期余额", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrLeaveBalance hrLeaveBalance)
    {
        List<HrLeaveBalance> list = hrLeaveBalanceService.selectHrLeaveBalanceList(hrLeaveBalance);
        ExcelUtil<HrLeaveBalance> util = new ExcelUtil<>(HrLeaveBalance.class);
        util.exportExcel(response, list, "假期余额");
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:query')")
    @GetMapping(value = "/{balanceId}")
    public AjaxResult getInfo(@PathVariable("balanceId") Long balanceId)
    {
        return AjaxResult.success(hrLeaveBalanceService.selectHrLeaveBalanceById(balanceId));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:add')")
    @Log(title = "假期余额", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrLeaveBalance hrLeaveBalance)
    {
        return toAjax(hrLeaveBalanceService.insertHrLeaveBalance(hrLeaveBalance));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:edit')")
    @Log(title = "假期余额", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrLeaveBalance hrLeaveBalance)
    {
        return toAjax(hrLeaveBalanceService.updateHrLeaveBalance(hrLeaveBalance));
    }

    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:remove')")
    @Log(title = "假期余额", businessType = BusinessType.DELETE)
    @DeleteMapping("/{balanceIds}")
    public AjaxResult remove(@PathVariable Long[] balanceIds)
    {
        return toAjax(hrLeaveBalanceService.deleteHrLeaveBalanceByIds(balanceIds));
    }

    /**
     * 批量生成假期余额
     */
    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:add')")
    @Log(title = "批量生成假期余额", businessType = BusinessType.INSERT)
    @PostMapping("/batchGenerate")
    public AjaxResult batchGenerate(@RequestBody HrLeaveBalanceBatchDTO dto)
    {
        try
        {
            String msg = hrLeaveBalanceService.batchGenerateBalance(dto, SecurityUtils.getUsername());
            return AjaxResult.success(msg);
        }
        catch (Exception e)
        {
            return AjaxResult.error(e.getMessage());
        }
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<HrLeaveBalance> util = new ExcelUtil<>(HrLeaveBalance.class);
        util.importTemplateExcel(response, "假期余额数据");
    }

    /**
     * 批量导入假期余额
     */
    @PreAuthorize("@ss.hasPermi('hr:leaveBalance:add')")
    @Log(title = "批量导入假期余额", businessType = BusinessType.IMPORT)
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<HrLeaveBalance> util = new ExcelUtil<>(HrLeaveBalance.class);
        List<HrLeaveBalance> list = util.importExcel(file.getInputStream());
        if (list == null || list.isEmpty() || list.stream().allMatch(java.util.Objects::isNull))
        {
            return AjaxResult.error("导入失败！请使用系统提供的标准模板填写数据，不要使用非模板格式的文件。");
        }
        list.removeIf(java.util.Objects::isNull);
        return hrLeaveBalanceService.importBalance(list, updateSupport, SecurityUtils.getUsername());
    }
}
