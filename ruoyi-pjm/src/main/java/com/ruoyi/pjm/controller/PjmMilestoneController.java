package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmMilestone;
import com.ruoyi.pjm.service.IPjmMilestoneService;

/**
 * 项目里程碑 Controller
 */
@RestController
@RequestMapping("/pjm/milestone")
public class PjmMilestoneController extends BaseController
{
    @Autowired
    private IPjmMilestoneService milestoneService;

    @PreAuthorize("@ss.hasPermi('pjm:milestone:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmMilestone milestone)
    {
        startPage();
        List<PjmMilestone> list = milestoneService.selectMilestoneList(milestone);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('pjm:milestone:query')")
    @GetMapping(value = "/{milestoneId}")
    public AjaxResult getInfo(@PathVariable("milestoneId") Long milestoneId)
    {
        return AjaxResult.success(milestoneService.selectMilestoneById(milestoneId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:milestone:add')")
    @Log(title = "项目里程碑", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmMilestone milestone)
    {
        return toAjax(milestoneService.insertMilestone(milestone));
    }

    @PreAuthorize("@ss.hasPermi('pjm:milestone:edit')")
    @Log(title = "项目里程碑", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmMilestone milestone)
    {
        return toAjax(milestoneService.updateMilestone(milestone));
    }

    @PreAuthorize("@ss.hasPermi('pjm:milestone:remove')")
    @Log(title = "项目里程碑", businessType = BusinessType.DELETE)
    @DeleteMapping("/{milestoneIds}")
    public AjaxResult remove(@PathVariable Long[] milestoneIds)
    {
        return toAjax(milestoneService.deleteMilestoneByIds(milestoneIds));
    }
}
