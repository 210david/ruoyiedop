package com.ruoyi.bid.controller;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.service.IBidProjectService;

/**
 * 投标项目台账 Controller
 */
@RestController
@RequestMapping("/bid/project")
public class BidProjectController extends BaseController
{
    @Autowired
    private IBidProjectService projectService;

    @PreAuthorize("@ss.hasPermi('bid:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidProject project)
    {
        startPage();
        List<BidProject> list = projectService.selectProjectList(project);
        return getDataTable(list);
    }

    @Log(title = "投标项目", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bid:project:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BidProject project)
    {
        List<BidProject> list = projectService.selectProjectList(project);
        ExcelUtil<BidProject> util = new ExcelUtil<>(BidProject.class);
        util.exportExcel(response, list, "投标项目数据");
    }

    @PreAuthorize("@ss.hasPermi('bid:project:query')")
    @GetMapping("/{projectId}")
    public AjaxResult getInfo(@PathVariable("projectId") Long projectId)
    {
        return AjaxResult.success(projectService.selectProjectById(projectId));
    }

    @Log(title = "投标项目", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:project:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidProject project)
    {
        return toAjax(projectService.insertProject(project));
    }

    @Log(title = "投标项目", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:project:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidProject project)
    {
        return toAjax(projectService.updateProject(project));
    }

    @Log(title = "投标项目", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:project:remove')")
    @DeleteMapping("/{projectIds}")
    public AjaxResult remove(@PathVariable Long[] projectIds)
    {
        return toAjax(projectService.deleteProjectByIds(projectIds));
    }

    /** 状态机流转 */
    @Log(title = "投标项目状态流转", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:project:edit')")
    @PutMapping("/changeStatus/{projectId}")
    public AjaxResult changeStatus(@PathVariable("projectId") Long projectId,
            @RequestParam String status, @RequestParam(required = false) String reason)
    {
        return toAjax(projectService.changeStatus(projectId, status, reason));
    }

    /** 完成进度节点 */
    @Log(title = "投标节点完成", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:project:edit')")
    @PutMapping("/finishNode/{nodeId}")
    public AjaxResult finishNode(@PathVariable("nodeId") Long nodeId,
            @RequestParam(required = false) String blockDesc)
    {
        return toAjax(projectService.finishNode(nodeId, blockDesc));
    }

    /** 中标转 PMS 立项（返回预填数据与跳转编号） */
    @Log(title = "中标转立项", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('bid:project:edit')")
    @PostMapping("/toPms/{projectId}")
    public AjaxResult toPms(@PathVariable("projectId") Long projectId)
    {
        String bidNo = projectService.toPms(projectId);
        return AjaxResult.success("中标项目「" + bidNo + "」数据已就绪，请在项目立项页完成登记", bidNo);
    }
}
