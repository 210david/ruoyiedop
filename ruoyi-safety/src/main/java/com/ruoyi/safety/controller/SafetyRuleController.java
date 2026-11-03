package com.ruoyi.safety.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.safety.domain.SafetyRule;
import com.ruoyi.safety.service.ISafetyRuleService;

/**
 * 安全守则 Controller（需求一）
 */
@RestController
@RequestMapping("/safety/rule")
public class SafetyRuleController extends BaseController {
    @Autowired
    private ISafetyRuleService ruleService;

    @PreAuthorize("@ss.hasPermi('safety:rule:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyRule rule) { return getDataTable(ruleService.selectRuleList(rule)); }

    @PreAuthorize("@ss.hasPermi('safety:rule:query')")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId) { return success(ruleService.selectRuleById(ruleId)); }

    @Log(title = "安全守则", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:rule:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyRule rule) { return toAjax(ruleService.insertRule(rule)); }

    @Log(title = "安全守则", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:rule:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyRule rule) { return toAjax(ruleService.updateRule(rule)); }

    @Log(title = "安全守则发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:rule:publish')")
    @PutMapping("/publish/{ruleId}")
    public AjaxResult publish(@PathVariable Long ruleId) { return toAjax(ruleService.publishRule(ruleId)); }

    @Log(title = "安全守则废止", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:rule:abolish')")
    @PutMapping("/abolish/{ruleId}")
    public AjaxResult abolish(@PathVariable Long ruleId) { return toAjax(ruleService.abolishRule(ruleId)); }

    @Log(title = "安全守则", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('safety:rule:remove')")
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds) { return toAjax(ruleService.deleteRuleByIds(ruleIds)); }

    @PreAuthorize("@ss.hasPermi('safety:rule:query')")
    @GetMapping("/{ruleId}/versions")
    public AjaxResult versions(@PathVariable Long ruleId) { return success(ruleService.selectRuleVersionList(ruleId)); }

    @PreAuthorize("@ss.hasPermi('safety:rule:query')")
    @GetMapping("/{ruleId}/learnStats")
    public AjaxResult learnStats(@PathVariable Long ruleId) {
        return success(new Object() {
            public int getTotal() { return ruleService.countLearnTotalByRule(Long.valueOf(ruleId)); }
            public int getSigned() { return ruleService.countLearnSignedByRule(Long.valueOf(ruleId)); }
        });
    }
}
