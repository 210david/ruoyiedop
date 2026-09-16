package com.ruoyi.safety.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.util.AiJsonParser;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyHazard;
import com.ruoyi.safety.domain.SafetyTrainingRecord;
import com.ruoyi.safety.service.ISafetyHazardService;
import com.ruoyi.safety.service.ISafetyTrainingRecordService;

/**
 * 安全管理 AI辅助 Controller（外挂式，不改动既有业务代码）
 * 功能：隐患描述结构化+整改建议 + 培训考题自动生成
 * AI输出均为"草稿/建议"，仅供决策参考
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/ai")
public class SafetyAiAssistController extends BaseController
{
    @Autowired
    private AiClient aiClient;

    @Autowired
    private ISafetyHazardService hazardService;

    @Autowired
    private ISafetyTrainingRecordService trainingRecordService;

    /**
     * 隐患描述结构化+整改建议：按ID拉取隐患，LLM输出结构化JSON（分类/风险评估/整改措施/时限/预防措施）
     */
    @PreAuthorize("@ss.hasPermi('safety:ai:hazardAdvice')")
    @GetMapping("/hazardAdvice/{hazardId}")
    public AjaxResult hazardAdvice(@PathVariable("hazardId") Long hazardId)
    {
        SafetyHazard hazard = hazardService.selectSafetyHazardById(hazardId);
        if (hazard == null)
        {
            return AjaxResult.error("隐患记录不存在");
        }

        StringBuilder data = new StringBuilder();
        data.append("隐患编号:").append(StringUtils.nvl(hazard.getHazardCode(), "-"))
            .append(" | 描述:").append(StringUtils.nvl(hazard.getHazardDesc(), "-"))
            .append(" | 类型:").append(StringUtils.nvl(hazard.getHazardType(), "-"))
            .append(" | 等级:").append(StringUtils.nvl(hazard.getHazardLevel(), "-"))
            .append(" | 发现人:").append(StringUtils.nvl(hazard.getDiscoverPerson(), "-"))
            .append(" | 责任部门:").append(StringUtils.nvl(hazard.getDeptName(), "-"))
            .append(" | 整改要求:").append(StringUtils.nvl(hazard.getRectifyReq(), "-"))
            .append(" | 当前状态:").append(StringUtils.nvl(hazard.getHazardStatus(), "-"));

        String prompt = "你是安全生产管理助手。根据以下隐患信息，输出结构化分析与整改建议。\n"
                + "必须只返回一个JSON对象（不要多余文字，可用```json```包裹），字段：\n"
                + "hazardCategory(隐患分类), riskAssessment(风险评估), rectifyAdvice(整改措施数组,3-5条), "
                + "deadlineAdvice(整改时限建议), preventiveMeasure(预防措施)。\n"
                + "要求：基于给定信息分析，数据中缺失的信息对应字段写【待补充】，不得编造。\n\n"
                + "【隐患信息】\n" + data;

        AiResult result = aiClient.chatCached("safety", "hazard_advice", prompt);
        JSONObject parsed = AiJsonParser.parse(result.getContent());
        Map<String, Object> resp = new HashMap<>(result.toMap());
        resp.put("parsed", parsed);
        return AjaxResult.success(resp);
    }

    /**
     * 培训考题自动生成：按培训记录ID拉取课程信息，LLM生成5道单选题+2道判断题（JSON）
     */
    @PreAuthorize("@ss.hasPermi('safety:ai:trainingQuiz')")
    @GetMapping("/trainingQuiz/{recordId}")
    public AjaxResult trainingQuiz(@PathVariable("recordId") Long recordId)
    {
        SafetyTrainingRecord record = trainingRecordService.selectSafetyTrainingRecordById(recordId);
        if (record == null)
        {
            return AjaxResult.error("培训记录不存在");
        }

        StringBuilder data = new StringBuilder();
        data.append("课程名称:").append(StringUtils.nvl(record.getCourseName(), "-"))
            .append(" | 课程类别:").append(StringUtils.nvl(record.getCourseType(), "-"))
            .append(" | 培训讲师:").append(StringUtils.nvl(record.getTrainer(), "-"))
            .append(" | 课时:").append(record.getHours() == null ? "-" : record.getHours().toPlainString());

        String prompt = "你是安全生产培训助手。根据以下培训课程信息，自动生成培训考核试题。\n"
                + "必须只返回一个JSON对象（不要多余文字，可用```json```包裹），格式：\n"
                + "{\"questions\":[{\"type\":\"single|judge\",\"question\":\"题干\",\"options\":[\"A...\",\"B...\"],"
                + "\"answer\":\"答案\",\"explanation\":\"解析\"}]}\n"
                + "题目数量：5道单选题（options含4个选项）+2道判断题（options为[\"正确\",\"错误\"]），共7题。\n"
                + "题目内容须紧扣课程名称与类别；课程信息缺失时题干相关背景写【待补充】，不得编造课程细节。\n\n"
                + "【培训课程信息】\n" + data;

        AiResult result = aiClient.chatCached("safety", "training_quiz", prompt, 3000);
        JSONObject parsed = AiJsonParser.parse(result.getContent());
        Map<String, Object> resp = new HashMap<>(result.toMap());
        resp.put("parsed", parsed);
        return AjaxResult.success(resp);
    }
}
