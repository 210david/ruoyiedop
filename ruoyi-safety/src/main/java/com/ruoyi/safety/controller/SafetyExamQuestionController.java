package com.ruoyi.safety.controller;

import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.safety.domain.SafetyExamQuestion;
import com.ruoyi.safety.service.ISafetyExamQuestionService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 考试题库 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/exam/question")
public class SafetyExamQuestionController extends BaseController
{
    @Autowired
    private ISafetyExamQuestionService safetyExamQuestionService;

    /**
     * 查询题目列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:question:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyExamQuestion query)
    {
        startPage();
        List<SafetyExamQuestion> list = safetyExamQuestionService.selectSafetyExamQuestionList(query);
        return getDataTable(list);
    }

    /**
     * 题目详情
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:question:query')")
    @GetMapping(value = "/{questionId}")
    public AjaxResult getInfo(@PathVariable("questionId") Long questionId)
    {
        return AjaxResult.success(safetyExamQuestionService.selectSafetyExamQuestionById(questionId));
    }

    /**
     * 新增题目
     */
    @Log(title = "考试题库", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyExamQuestion question)
    {
        return toAjax(safetyExamQuestionService.insertSafetyExamQuestion(question));
    }

    /**
     * 修改题目
     */
    @Log(title = "考试题库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyExamQuestion question)
    {
        return toAjax(safetyExamQuestionService.updateSafetyExamQuestion(question));
    }

    /**
     * 删除题目（逻辑删除）
     */
    @Log(title = "考试题库", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:remove')")
    @DeleteMapping("/{questionIds}")
    public AjaxResult remove(@PathVariable Long[] questionIds)
    {
        return toAjax(safetyExamQuestionService.deleteSafetyExamQuestionByIds(questionIds));
    }

    /**
     * 启用/停用
     */
    @Log(title = "考试题库", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:edit')")
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestParam Long questionId, @RequestParam String status)
    {
        return toAjax(safetyExamQuestionService.changeQuestionStatus(questionId, status));
    }

    /**
     * 导出题目
     */
    @Log(title = "考试题库", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SafetyExamQuestion query)
    {
        List<SafetyExamQuestion> list = safetyExamQuestionService.selectSafetyExamQuestionList(query);
        ExcelUtil<SafetyExamQuestion> util = new ExcelUtil<>(SafetyExamQuestion.class);
        util.exportExcel(response, list, "题库数据");
    }

    /**
     * 题目导入模板下载
     */
    @PostMapping("/importTemplate")
    @PreAuthorize("@ss.hasPermi('safety:exam:question:import')")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<SafetyExamQuestion> util = new ExcelUtil<>(SafetyExamQuestion.class);
        util.importTemplateExcel(response, "题库导入模板");
    }

    /**
     * Excel批量导入（行级校验，任一行非法则全量回滚）
     */
    @Log(title = "考试题库", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:import')")
    @PostMapping("/importData")
    public AjaxResult importData(MultipartFile file) throws Exception
    {
        ExcelUtil<SafetyExamQuestion> util = new ExcelUtil<>(SafetyExamQuestion.class);
        List<SafetyExamQuestion> questions = util.importExcel(file.getInputStream());
        String message = safetyExamQuestionService.importQuestion(questions, getUsername());
        return AjaxResult.success(message);
    }

    /**
     * FR-10：AI 出题结果批量收编入库（题干同分类查重跳过）
     */
    @Log(title = "考试题库", businessType = BusinessType.IMPORT)
    @PreAuthorize("@ss.hasPermi('safety:exam:question:import')")
    @PostMapping("/aiImport")
    public AjaxResult aiImport(@RequestBody java.util.Map<String, Object> body)
    {
        Object catObj = body.get("categoryId");
        if (catObj == null)
        {
            return error("请先选择收编入库的分类");
        }
        Long categoryId = Long.valueOf(String.valueOf(catObj));
        List<SafetyExamQuestion> questions = com.alibaba.fastjson2.JSON.parseArray(
                com.alibaba.fastjson2.JSON.toJSONString(body.get("questions")), SafetyExamQuestion.class);
        java.util.Map<String, Integer> result = safetyExamQuestionService.importFromAi(categoryId, questions, getUsername());
        return AjaxResult.success("收编完成", result);
    }
}
