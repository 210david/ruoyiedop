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
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.safety.domain.SafetyExam;
import com.ruoyi.safety.domain.SafetyExamRecord;
import com.ruoyi.safety.mapper.SafetyExamUserMapper;
import com.ruoyi.safety.service.ISafetyExamService;

/**
 * 考试场次管理 Controller（管理端）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/safety/exam")
public class SafetyExamController extends BaseController
{
    @Autowired
    private ISafetyExamService safetyExamService;

    @Autowired
    private SafetyExamUserMapper safetyExamUserMapper;

    /**
     * 查询考试场次列表（分页）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:list')")
    @GetMapping("/list")
    public TableDataInfo list(SafetyExam query)
    {
        startPage();
        List<SafetyExam> list = safetyExamService.selectSafetyExamList(query);
        return getDataTable(list);
    }

    /**
     * 场次详情
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:query')")
    @GetMapping(value = "/{examId}")
    public AjaxResult getInfo(@PathVariable("examId") Long examId)
    {
        return AjaxResult.success(safetyExamService.selectSafetyExamById(examId));
    }

    /**
     * 考试情况（考生名单与成绩列表，含成绩信息，需成绩统计权限）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:stats')")
    @GetMapping(value = "/{examId}/records")
    public TableDataInfo records(@PathVariable("examId") Long examId, SafetyExamRecord query)
    {
        startPage();
        List<SafetyExamRecord> list = safetyExamService.selectExamRecords(examId, query);
        return getDataTable(list);
    }

    /**
     * 成绩统计
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:stats')")
    @GetMapping(value = "/{examId}/stats")
    public AjaxResult stats(@PathVariable("examId") Long examId)
    {
        return AjaxResult.success(safetyExamService.selectExamStats(examId));
    }

    /**
     * 成绩导出
     */
    @Log(title = "考试成绩导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:exam:export')")
    @PostMapping(value = "/{examId}/records/export")
    public void exportRecords(jakarta.servlet.http.HttpServletResponse response, @PathVariable("examId") Long examId)
    {
        List<SafetyExamRecord> list = safetyExamService.selectExamRecordsForExport(examId);
        com.ruoyi.common.utils.poi.ExcelUtil<SafetyExamRecord> util = new com.ruoyi.common.utils.poi.ExcelUtil<>(SafetyExamRecord.class);
        util.exportExcel(response, list, "考试成绩");
    }

    /**
     * FR-7：错题率分析（按题目聚合，错误率降序）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:stats')")
    @GetMapping(value = "/{examId}/questionStats")
    public AjaxResult questionStats(@PathVariable("examId") Long examId)
    {
        return AjaxResult.success(safetyExamService.selectQuestionStats(examId));
    }

    /**
     * FR-7：按高错误率题目一键生成复训/补考试卷草稿
     */
    @Log(title = "错题复训卷生成", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:stats')")
    @PostMapping(value = "/{examId}/remedialPaper")
    public AjaxResult remedialPaper(@PathVariable("examId") Long examId,
            @RequestBody(required = false) java.util.Map<String, Object> body)
    {
        java.math.BigDecimal threshold = null;
        if (body != null && body.get("errorRate") != null)
        {
            threshold = new java.math.BigDecimal(String.valueOf(body.get("errorRate")));
        }
        Long paperId = safetyExamService.createRemedialPaper(examId, threshold, getUsername());
        return AjaxResult.success("复训试卷草稿已生成", paperId);
    }

    /**
     * FR-8：一键创建补考场次（名单=不合格+缺考且有账号者）
     */
    @Log(title = "考试补考场次", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:publish')")
    @PostMapping(value = "/{examId}/retake")
    public AjaxResult retake(@PathVariable("examId") Long examId, @RequestBody SafetyExam retakeForm)
    {
        Long retakeId = safetyExamService.createRetakeExam(examId, retakeForm, getUsername());
        return AjaxResult.success("补考场次已创建并发布", retakeId);
    }

    /**
     * FR-12：完整答卷存档导出（每题考生原始作答）
     */
    @Log(title = "答卷存档导出", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('safety:exam:export')")
    @PostMapping(value = "/{examId}/archiveExport")
    public void archiveExport(jakarta.servlet.http.HttpServletResponse response, @PathVariable("examId") Long examId)
    {
        List<com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive> list = safetyExamService.selectAnswerArchive(examId);
        com.ruoyi.common.utils.poi.ExcelUtil<com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive> util =
                new com.ruoyi.common.utils.poi.ExcelUtil<>(com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive.class);
        util.exportExcel(response, list, "答卷存档");
    }

    /**
     * 新增场次
     */
    @Log(title = "考试场次", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('safety:exam:add')")
    @PostMapping
    public AjaxResult add(@RequestBody SafetyExam exam)
    {
        return toAjax(safetyExamService.insertSafetyExam(exam));
    }

    /**
     * 可选考试人员（考生来源=手动添加时的选人下拉）
     */
    @PreAuthorize("@ss.hasPermi('safety:exam:query') or @ss.hasPermi('safety:exam:add') or @ss.hasPermi('safety:exam:edit')")
    @GetMapping("/userOptions")
    public AjaxResult userOptions(@RequestParam(required = false) String keyword)
    {
        return AjaxResult.success(safetyExamUserMapper.selectUserOptions(keyword));
    }

    /**
     * 修改场次（仅草稿）
     */
    @Log(title = "考试场次", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody SafetyExam exam)
    {
        return toAjax(safetyExamService.updateSafetyExam(exam));
    }

    /**
     * 发布场次（生成考生名单）
     */
    @Log(title = "考试场次发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:publish')")
    @PutMapping("/publish/{examId}")
    public AjaxResult publish(@PathVariable Long examId)
    {
        return toAjax(safetyExamService.publishExam(examId, getUsername()));
    }

    /**
     * 发布后增补考生（只增不减：手动来源按 userIds 追加，培训来源按参训人同步追加）
     */
    @Log(title = "考试场次增补考生", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:publish')")
    @PostMapping("/{examId}/users")
    public AjaxResult addUsers(@PathVariable Long examId, @RequestBody SafetyExam body)
    {
        int added = safetyExamService.addExamUsers(examId, body.getUserIds(), getUsername());
        return AjaxResult.success("已增补 " + added + " 名考生", added);
    }

    /**
     * 撤回场次（仅未开始）
     */
    @Log(title = "考试场次撤回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:publish')")
    @PutMapping("/cancel/{examId}")
    public AjaxResult cancel(@PathVariable Long examId)
    {
        return toAjax(safetyExamService.cancelExam(examId));
    }

    /**
     * 强制结束（未交卷者标记缺考）
     */
    @Log(title = "考试场次强制结束", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('safety:exam:forceEnd')")
    @PutMapping("/forceEnd/{examId}")
    public AjaxResult forceEnd(@PathVariable Long examId)
    {
        return toAjax(safetyExamService.forceEndExam(examId));
    }

    /**
     * 删除场次（仅草稿/已撤回）
     */
    @Log(title = "考试场次", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('safety:exam:remove')")
    @DeleteMapping("/{examIds}")
    public AjaxResult remove(@PathVariable Long[] examIds)
    {
        return toAjax(safetyExamService.deleteSafetyExamByIds(examIds));
    }
}
