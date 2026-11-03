package com.ruoyi.safety.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.safety.domain.SafetyRuleCheck;
import com.ruoyi.safety.service.ISafetyRuleCheckService;

/**
 * 安全守则执行检查 Controller（需求一）
 */
@RestController
@RequestMapping("/safety/ruleCheck")
public class SafetyRuleCheckController extends BaseController {
    @Autowired
    private ISafetyRuleCheckService checkService;

    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyRuleCheck check) { return getDataTable(checkService.selectCheckList(check)); }

    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:query')")
    @GetMapping("/{checkId}")
    public AjaxResult getInfo(@PathVariable Long checkId) { return success(checkService.selectCheckById(checkId)); }

    @Log(title = "守则执行检查", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyRuleCheck check) { return toAjax(checkService.insertCheck(check)); }

    @Log(title = "守则执行检查", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyRuleCheck check) { return toAjax(checkService.updateCheck(check)); }

    /** SR-04 整改闭环复查 */
    @Log(title = "守则整改闭环", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:review')")
    @PutMapping("/review")
    public AjaxResult review(@RequestBody SafetyRuleCheck check) { return toAjax(checkService.reviewCheck(check)); }

    /** SR-04 归档 */
    @Log(title = "守则检查归档", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:archive')")
    @PutMapping("/archive/{checkId}")
    public AjaxResult archive(@PathVariable Long checkId) { return toAjax(checkService.archiveCheck(checkId)); }

    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:remove')")
    @DeleteMapping("/{checkIds}")
    public AjaxResult remove(@PathVariable Long[] checkIds) { return toAjax(checkService.deleteCheckByIds(checkIds)); }

    @PreAuthorize("@ss.hasPermi('safety:ruleCheck:query')")
    @GetMapping("/stats")
    public AjaxResult stats() {
        return success(new Object() {
            public int getTotal() { return checkService.countChecks(); }
            public int getQualified() { return checkService.countQualified(); }
        });
    }
}
