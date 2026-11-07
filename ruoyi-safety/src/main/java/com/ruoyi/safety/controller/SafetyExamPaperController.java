package com.ruoyi.safety.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.ruoyi.safety.domain.SafetyExamPaper;
import com.ruoyi.safety.service.ISafetyExamPaperService;

/**
 * 考试试卷 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/exam/paper")
public class SafetyExamPaperController extends BaseController
{
    @Autowired
    private ISafetyExamPaperService safetyExamPaperService;

    /**
     * 查询试卷列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyExamPaper query)
    {
        startPage();
        List<SafetyExamPaper> list = safetyExamPaperService.selectSafetyExamPaperList(query);
        return getDataTable(list);
    }

    /**
     * 试卷详情（含题目快照明细）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:query')")
    @GetMapping(value = "/{paperId}")
    public AjaxResult getInfo(@PathVariable("paperId") Long paperId)
    {
        return AjaxResult.success(safetyExamPaperService.selectSafetyExamPaperById(paperId));
    }

    /**
     * 新增试卷（基础信息，组卷在保存题目接口完成）
     */
    @Log(title = "考试试卷", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyExamPaper paper)
    {
        return toAjax(safetyExamPaperService.insertSafetyExamPaper(paper));
    }

    /**
     * 修改试卷基础信息（仅草稿/停用）
     */
    @Log(title = "考试试卷", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyExamPaper paper)
    {
        return toAjax(safetyExamPaperService.updateSafetyExamPaper(paper));
    }

    /**
     * 保存试卷题目集合（全量替换，仅草稿可操作，保存即快照并回写总分）
     */
    @Log(title = "考试试卷组卷", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:edit')")
    @PutMapping("/questions")
    public AjaxResult saveQuestions(@RequestBody SafetyExamPaper paper)
    {
        return toAjax(safetyExamPaperService.savePaperQuestions(paper));
    }

    /**
     * 随机抽题（管理端预览，不入库）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:list')")
    @GetMapping("/randomPick")
    public AjaxResult randomPick(@RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) String questionType,
                                 @RequestParam(required = false) String difficulty,
                                 @RequestParam(defaultValue = "10") Integer count)
    {
        return AjaxResult.success(safetyExamPaperService.randomPick(categoryId, questionType, difficulty, count));
    }

    /**
     * 发布试卷
     */
    @Log(title = "考试试卷发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:publish')")
    @PutMapping("/publish/{paperId}")
    public AjaxResult publish(@PathVariable Long paperId)
    {
        return toAjax(safetyExamPaperService.publishPaper(paperId, getUsername()));
    }

    /**
     * 撤回发布（无考试场次引用时退回草稿）
     */
    @Log(title = "考试试卷撤回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:publish')")
    @PutMapping("/unpublish/{paperId}")
    public AjaxResult unpublish(@PathVariable Long paperId)
    {
        return toAjax(safetyExamPaperService.unpublishPaper(paperId));
    }

    /**
     * 删除试卷（已发布或被考试引用时拒绝）
     */
    @Log(title = "考试试卷", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('safety:exam:paper:remove')")
    @DeleteMapping("/{paperIds}")
    public AjaxResult remove(@PathVariable Long[] paperIds)
    {
        return toAjax(safetyExamPaperService.deleteSafetyExamPaperByIds(paperIds));
    }
}
