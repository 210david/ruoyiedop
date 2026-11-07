package com.ruoyi.safety.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.safety.service.ISafetyExamTakeService;

/**
 * 考生端考试 Controller（我的考试 / 在线答题）
 *
 * 全部接口强制以当前登录人身份操作本人考试记录，防代考。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/exam/take")
public class SafetyExamTakeController extends BaseController
{
    @Autowired
    private ISafetyExamTakeService safetyExamTakeService;

    /**
     * 我的考试列表
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:myList')")
    @GetMapping("/myList")
    public AjaxResult myList()
    {
        return AjaxResult.success(safetyExamTakeService.selectMyExamList(getUserId()));
    }

    /**
     * 进入考试（开始计时 / 续答）
     */
    @Log(title = "进入考试", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:take')")
    @PostMapping("/start/{recordId}")
    public AjaxResult start(@PathVariable Long recordId)
    {
        return AjaxResult.success(safetyExamTakeService.startExam(recordId, getUserId()));
    }

    /**
     * 保存答题草稿
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:take')")
    @PostMapping("/saveDraft")
    public AjaxResult saveDraft(@RequestBody Map<String, Object> body)
    {
        Long recordId = Long.valueOf(String.valueOf(body.get("recordId")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        return toAjax(safetyExamTakeService.saveDraft(recordId, answers, getUserId()));
    }

    /**
     * 交卷并自动判分
     */
    @Log(title = "交卷判分", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:take')")
    @PostMapping("/submit")
    public AjaxResult submit(@RequestBody Map<String, Object> body)
    {
        Long recordId = Long.valueOf(String.valueOf(body.get("recordId")));
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> answers = (List<Map<String, Object>>) body.get("answers");
        Integer switchCount = body.get("switchCount") == null ? null : Integer.valueOf(String.valueOf(body.get("switchCount")));
        String submitType = body.get("submitType") == null ? "1" : String.valueOf(body.get("submitType"));
        return AjaxResult.success(safetyExamTakeService.submitExam(recordId, answers, switchCount, submitType, getUserId()));
    }

    /**
     * 查看成绩与解析
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:take')")
    @GetMapping("/result/{recordId}")
    public AjaxResult result(@PathVariable Long recordId)
    {
        return AjaxResult.success(safetyExamTakeService.getResult(recordId, getUserId()));
    }
}
