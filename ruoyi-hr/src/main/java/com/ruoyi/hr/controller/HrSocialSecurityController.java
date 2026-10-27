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
import com.ruoyi.hr.domain.HrSocialSecurity;
import com.ruoyi.hr.service.IHrSocialSecurityService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/socialSecurity")
public class HrSocialSecurityController extends BaseController
{
    @Autowired
    private IHrSocialSecurityService hrSocialSecurityService;

    @Autowired
    private com.ruoyi.hr.service.IHrSsPlanService hrSsPlanService;

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrSocialSecurity hrSocialSecurity)
    {
        startPage();
        List<HrSocialSecurity> list = hrSocialSecurityService.selectHrSocialSecurityList(hrSocialSecurity);
        return getDataTable(list);
    }

    @Log(title = "社保公积金", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrSocialSecurity hrSocialSecurity)
    {
        List<HrSocialSecurity> list = hrSocialSecurityService.selectHrSocialSecurityList(hrSocialSecurity);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, hrSocialSecurity);
        ExcelUtil<HrSocialSecurity> util = new ExcelUtil<>(HrSocialSecurity.class);
        util.exportExcel(response, list, "社保公积金");
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:query')")
    @GetMapping(value = "/{ssId}")
    public AjaxResult getInfo(@PathVariable("ssId") Long ssId)
    {
        return AjaxResult.success(hrSocialSecurityService.selectHrSocialSecurityById(ssId));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:add')")
    @Log(title = "社保公积金", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrSocialSecurity hrSocialSecurity)
    {
        return toAjax(hrSocialSecurityService.insertHrSocialSecurity(hrSocialSecurity));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:edit')")
    @Log(title = "社保公积金", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrSocialSecurity hrSocialSecurity)
    {
        return toAjax(hrSocialSecurityService.updateHrSocialSecurity(hrSocialSecurity));
    }

    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:remove')")
    @Log(title = "社保公积金", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ssIds}")
    public AjaxResult remove(@PathVariable Long[] ssIds)
    {
        return toAjax(hrSocialSecurityService.deleteHrSocialSecurityByIds(ssIds));
    }

    /**
     * 批量初始化：按指定年度默认方案，为在职且未配置该年度社保的员工生成记录
     * 基数取方案下限（可后续逐个调整），比例继承方案
     */
    @Log(title = "社保公积金-批量初始化", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('hr:socialSecurity:batchInit')")
    @PostMapping("/batchInit/{year}")
    public AjaxResult batchInit(@PathVariable("year") Integer year)
    {
        com.ruoyi.hr.domain.HrSsPlan plan = hrSsPlanService.selectDefaultPlanByYear(year);
        if (plan == null)
        {
            return error("该年度未配置默认社保方案，请先在[社保方案]中维护");
        }
        int rows = hrSocialSecurityService.batchInitByPlan(plan);
        return AjaxResult.success("初始化完成，共生成 " + rows + " 条员工社保记录", rows);
    }
}
