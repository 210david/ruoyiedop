package com.ruoyi.mms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.service.IMmsIssueService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Issue Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/issue")
public class MmsIssueController extends BaseController
{
    @Autowired
    private IMmsIssueService mmsIssueService;

    @PreAuthorize("@ss.hasPermi('mms:issue:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsIssue issue)
    {
        startPage();
        List<MmsIssue> list = mmsIssueService.selectIssueList(issue);
        return getDataTable(list);
    }

    @Log(title = "Issue", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:issue:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsIssue issue)
    {
        List<MmsIssue> list = mmsIssueService.selectIssueList(issue);
        ExcelUtil<MmsIssue> util = new ExcelUtil<>(MmsIssue.class);
        util.exportExcel(response, list, "Issue");
    }

    @PreAuthorize("@ss.hasPermi('mms:issue:query')")
    @GetMapping(value = "/{IssueId}")
    public AjaxResult getInfo(@PathVariable("IssueId") Long IssueId)
    {
        return AjaxResult.success(mmsIssueService.selectIssueById(IssueId));
    }

    @Log(title = "Issue", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:issue:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsIssue issue)
    {
        return toAjax(mmsIssueService.insertIssue(issue));
    }

    @Log(title = "Issue", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:issue:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsIssue issue)
    {
        return toAjax(mmsIssueService.updateIssue(issue));
    }

    @Log(title = "Issue", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:issue:remove')")
    @DeleteMapping("/{IssueIds}")
    public AjaxResult remove(@PathVariable Long[] IssueIds)
    {
        return toAjax(mmsIssueService.deleteIssueByIds(IssueIds));
    }
}
