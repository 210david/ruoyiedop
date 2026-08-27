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
import com.ruoyi.hr.domain.HrRegular;
import com.ruoyi.hr.service.IHrRegularService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/regular")
public class HrRegularController extends BaseController
{
    @Autowired
    private IHrRegularService hrRegularService;

    @PreAuthorize("@ss.hasPermi('hr:regular:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrRegular hrRegular)
    {
        startPage();
        List<HrRegular> list = hrRegularService.selectHrRegularList(hrRegular);
        return getDataTable(list);
    }

    @Log(title = "转正", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:regular:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrRegular hrRegular)
    {
        List<HrRegular> list = hrRegularService.selectHrRegularList(hrRegular);
        ExcelUtil<HrRegular> util = new ExcelUtil<>(HrRegular.class);
        util.exportExcel(response, list, "转正");
    }

    @PreAuthorize("@ss.hasPermi('hr:regular:query')")
    @GetMapping(value = "/{regularId}")
    public AjaxResult getInfo(@PathVariable("regularId") Long regularId)
    {
        return AjaxResult.success(hrRegularService.selectHrRegularById(regularId));
    }

    @PreAuthorize("@ss.hasPermi('hr:regular:add')")
    @Log(title = "转正", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrRegular hrRegular)
    {
        return toAjax(hrRegularService.insertHrRegular(hrRegular));
    }

    @PreAuthorize("@ss.hasPermi('hr:regular:edit')")
    @Log(title = "转正", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrRegular hrRegular)
    {
        return toAjax(hrRegularService.updateHrRegular(hrRegular));
    }

    @PreAuthorize("@ss.hasPermi('hr:regular:remove')")
    @Log(title = "转正", businessType = BusinessType.DELETE)
    @DeleteMapping("/{regularIds}")
    public AjaxResult remove(@PathVariable Long[] regularIds)
    {
        return toAjax(hrRegularService.deleteHrRegularByIds(regularIds));
    }
}
