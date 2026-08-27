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
import com.ruoyi.hr.domain.HrPosition;
import com.ruoyi.hr.service.IHrPositionService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/hr/position")
public class HrPositionController extends BaseController
{
    @Autowired
    private IHrPositionService hrPositionService;

    @PreAuthorize("@ss.hasPermi('hr:position:list')")
    @GetMapping("/list")
    public TableDataInfo list(HrPosition hrPosition)
    {
        startPage();
        List<HrPosition> list = hrPositionService.selectHrPositionList(hrPosition);
        return getDataTable(list);
    }

    @Log(title = "岗位", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('hr:position:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, HrPosition hrPosition)
    {
        List<HrPosition> list = hrPositionService.selectHrPositionList(hrPosition);
        ExcelUtil<HrPosition> util = new ExcelUtil<>(HrPosition.class);
        util.exportExcel(response, list, "岗位");
    }

    @PreAuthorize("@ss.hasPermi('hr:position:query')")
    @GetMapping(value = "/{positionId}")
    public AjaxResult getInfo(@PathVariable("positionId") Long positionId)
    {
        return AjaxResult.success(hrPositionService.selectHrPositionById(positionId));
    }

    @PreAuthorize("@ss.hasPermi('hr:position:add')")
    @Log(title = "岗位", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody HrPosition hrPosition)
    {
        return toAjax(hrPositionService.insertHrPosition(hrPosition));
    }

    @PreAuthorize("@ss.hasPermi('hr:position:edit')")
    @Log(title = "岗位", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody HrPosition hrPosition)
    {
        return toAjax(hrPositionService.updateHrPosition(hrPosition));
    }

    @PreAuthorize("@ss.hasPermi('hr:position:remove')")
    @Log(title = "岗位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{positionIds}")
    public AjaxResult remove(@PathVariable Long[] positionIds)
    {
        return toAjax(hrPositionService.deleteHrPositionByIds(positionIds));
    }
}
