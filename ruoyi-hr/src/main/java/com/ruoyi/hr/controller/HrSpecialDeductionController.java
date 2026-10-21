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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.hr.domain.HrSpecialDeduction;
import com.ruoyi.hr.service.IHrSpecialDeductionService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/specialDeduction")
public class HrSpecialDeductionController extends BaseController
{
    @Autowired
    private IHrSpecialDeductionService hrSpecialDeductionService;

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSpecialDeduction hrSpecialDeduction)
    {
        startPage();
        List<HrSpecialDeduction> list = hrSpecialDeductionService.selectHrSpecialDeductionList(hrSpecialDeduction);
        return getDataTable(list);
    }

    @Log(title = "专项附加扣除", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSpecialDeduction hrSpecialDeduction)
    {
        List<HrSpecialDeduction> list = hrSpecialDeductionService.selectHrSpecialDeductionList(hrSpecialDeduction);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, hrSpecialDeduction);
        ExcelUtil<HrSpecialDeduction> util = new ExcelUtil<>(HrSpecialDeduction.class);
        util.exportExcel(response, list, "专项附加扣除");
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:query')")
    @GetMapping(value = "/{sdId}")
    public AjaxResult getInfo(@PathVariable("sdId") Long sdId)
    {
        return AjaxResult.success(hrSpecialDeductionService.selectHrSpecialDeductionById(sdId));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:add')")
    @Log(title = "专项附加扣除", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSpecialDeduction hrSpecialDeduction)
    {
        return toAjax(hrSpecialDeductionService.insertHrSpecialDeduction(hrSpecialDeduction));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:edit')")
    @Log(title = "专项附加扣除", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSpecialDeduction hrSpecialDeduction)
    {
        return toAjax(hrSpecialDeductionService.updateHrSpecialDeduction(hrSpecialDeduction));
    }

    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:remove')")
    @Log(title = "专项附加扣除", businessType = BusinessType.DELETE)
    @DeleteMapping("/{sdIds}")
    public AjaxResult remove(@PathVariable Long[] sdIds)
    {
        return toAjax(hrSpecialDeductionService.deleteHrSpecialDeductionByIds(sdIds));
    }

    /**
     * 批量导入专项附加扣除（员工从个税App导出后，HR整理成模板一次性导入）
     * 按工号匹配员工；同员工+同扣除项目+同生效日期存在时更新，否则新增
     */
    @Log(title = "专项附加扣除", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('hr:specialDeduction:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception
    {
        ExcelUtil<HrSpecialDeduction> util = new ExcelUtil<>(HrSpecialDeduction.class);
        List<HrSpecialDeduction> list = util.importExcel(file.getInputStream());
        if (list == null || list.isEmpty() || list.stream().allMatch(java.util.Objects::isNull))
        {
            return AjaxResult.error("导入失败！请使用系统提供的标准模板填写数据。");
        }
        list.removeIf(java.util.Objects::isNull);
        String operName = getUsername();
        return hrSpecialDeductionService.importSpecialDeduction(list, updateSupport, operName);
    }

    /** 下载导入模板 */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<HrSpecialDeduction> util = new ExcelUtil<>(HrSpecialDeduction.class);
        util.excludeFields = new String[]{"sdId", "employeeId", "delFlag"};
        util.importTemplateExcel(response, "专项附加扣除数据");
    }
}
