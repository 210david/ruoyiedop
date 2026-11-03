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
import com.ruoyi.safety.domain.SafetyRuleLearn;
import com.ruoyi.safety.domain.SafetyRuleLearnTask;
import com.ruoyi.safety.service.ISafetyRuleLearnService;

/**
 * 安全守则学习任务/签阅 Controller（需求一）
 */
@RestController
@RequestMapping("/safety/ruleLearn")
public class SafetyRuleLearnTaskController extends BaseController {
    @Autowired
    private ISafetyRuleLearnService learnService;

    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:list')")
    @GetMapping("/task/list")
    public TableDataInfo list(SafetyRuleLearnTask task) { return getDataTable(learnService.selectTaskList(task)); }

    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:query')")
    @GetMapping("/task/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId) { return success(learnService.selectTaskById(taskId)); }

    @Log(title = "守则学习任务", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:add')")
    @PostMapping("/task")
    public AjaxResult add(@RequestBody SafetyRuleLearnTask task) { return toAjax(learnService.insertTask(task)); }

    /** SR-03 名单追加 */
    @Log(title = "守则学习任务名单追加", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:add')")
    @PostMapping("/task/{taskId}/learns")
    public AjaxResult appendLearns(@PathVariable Long taskId, @RequestBody List<SafetyRuleLearn> learns) {
        SafetyRuleLearnTask task = new SafetyRuleLearnTask();
        task.setTaskId(taskId);
        return toAjax(learnService.appendLearnBatch(task, learns));
    }

    /** 签阅确认 */
    @Log(title = "守则签阅", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:sign')")
    @PutMapping("/sign")
    public AjaxResult sign(@RequestBody SafetyRuleLearn learn) { return toAjax(learnService.signLearn(learn)); }

    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:query')")
    @GetMapping("/task/{taskId}/learns")
    public AjaxResult learns(@PathVariable Long taskId) {
        SafetyRuleLearn query = new SafetyRuleLearn();
        query.setTaskId(taskId);
        return success(learnService.selectLearnList(query));
    }

    @PreAuthorize("@ss.hasPermi('safety:ruleLearn:query')")
    @GetMapping("/task/stats")
    public AjaxResult stats() { return success(learnService.selectTaskStatList()); }
}
