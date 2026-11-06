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
import com.ruoyi.pjm.domain.PjmWbsTask;
import com.ruoyi.pjm.service.IPjmWbsTaskService;

/**
 * WBS 任务 Controller
 */
@RestController
@RequestMapping("/pjm/task")
public class PjmWbsTaskController extends BaseController
{
    @Autowired
    private IPjmWbsTaskService wbsTaskService;

    /** 平铺列表（分页） */
    @PreAuthorize("@ss.hasPermi('pjm:task:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmWbsTask task)
    {
        startPage();
        List<PjmWbsTask> list = wbsTaskService.selectTaskList(task);
        return getDataTable(list);
    }

    /** 树结构（按项目） */
    @PreAuthorize("@ss.hasPermi('pjm:task:list')")
    @GetMapping("/tree/{projectId}")
    public AjaxResult tree(@PathVariable Long projectId)
    {
        return AjaxResult.success(wbsTaskService.selectTaskTree(projectId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:query')")
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId)
    {
        return AjaxResult.success(wbsTaskService.selectTaskById(taskId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:add')")
    @Log(title = "WBS任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmWbsTask task)
    {
        return toAjax(wbsTaskService.insertTask(task));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:edit')")
    @Log(title = "WBS任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmWbsTask task)
    {
        return toAjax(wbsTaskService.updateTask(task));
    }

    @PreAuthorize("@ss.hasPermi('pjm:task:remove')")
    @Log(title = "WBS任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(wbsTaskService.deleteTaskByIds(taskIds));
    }
}
