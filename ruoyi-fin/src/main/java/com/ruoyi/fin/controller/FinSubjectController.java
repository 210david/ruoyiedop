package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.service.IFinSubjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 会计科目 Controller
 *
 * <p>【职责分离】会计可新增/修改科目，但删除受 Service 层规则约束
 * （有子科目或已有凭证分录时拒绝）。</p>
 *
 * <p>【规范】菜单 perms 对应 fin_02_menu.sql 中定义的：</p>
 * <ul>
 *   fin:subject:list / query / add / edit / remove / import / export</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/subject")
public class FinSubjectController extends BaseController
{
    @Autowired
    private IFinSubjectService subjectService;

    /**
     * 查询科目列表
     *
     * <p>不分页：科目总量在 300 行以内，由前端 handleTree 组树。</p>
     */
    @PreAuthorize("@ss.hasPermi('fin:subject:list')")
    @GetMapping("/list")
    public AjaxResult list(FinSubject subject)
    {
        return AjaxResult.success(subjectService.selectSubjectList(subject));
    }

    /**
     * 查询全部启用科目（供凭证录入科目下拉）
     */
    @PreAuthorize("@ss.hasPermi('fin:subject:query')")
    @GetMapping("/allEnabled")
    public AjaxResult allEnabled()
    {
        return AjaxResult.success(subjectService.selectAllEnabled());
    }

    /**
     * 查询科目详情
     */
    @PreAuthorize("@ss.hasPermi('fin:subject:query')")
    @GetMapping("/{subjectId}")
    public AjaxResult getInfo(@PathVariable("subjectId") Long subjectId)
    {
        return AjaxResult.success(subjectService.selectSubjectById(subjectId));
    }

    /**
     * 新增科目
     */
    @Log(title = "会计科目", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:subject:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FinSubject subject)
    {
        return toAjax(subjectService.insertSubject(subject));
    }

    /**
     * 修改科目
     */
    @Log(title = "会计科目", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:subject:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FinSubject subject)
    {
        return toAjax(subjectService.updateSubject(subject));
    }

    /**
     * 删除科目
     *
     * <p>有子科目或已有凭证分录时由 Service 层拒绝并返回明确原因。</p>
     */
    @Log(title = "会计科目", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:subject:remove')")
    @DeleteMapping("/{subjectIds}")
    public AjaxResult remove(@PathVariable Long[] subjectIds)
    {
        return toAjax(subjectService.deleteSubjectByIds(subjectIds));
    }

    /**
     * 校验科目编码唯一性
     */
    @PreAuthorize("@ss.hasPermi('fin:subject:query')")
    @GetMapping("/checkCodeUnique")
    public AjaxResult checkCodeUnique(@RequestParam String subjectCode,
                                      @RequestParam(required = false) Long subjectId)
    {
        return AjaxResult.success(subjectService.checkCodeUnique(subjectCode, subjectId));
    }

    /**
     * 导出科目
     */
    @Log(title = "会计科目", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:subject:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinSubject subject)
    {
        List<FinSubject> list = subjectService.selectSubjectList(subject);
        ExcelUtil<FinSubject> util = new ExcelUtil<>(FinSubject.class);
        util.exportExcel(response, list, "会计科目数据");
    }

    /**
     * 科目导入（Excel，全部成功或全部失败）
     */
    @Log(title = "会计科目导入", businessType = BusinessType.IMPORT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:subject:import')")
    @PostMapping("/importData")
    public AjaxResult importData(@RequestParam("file") MultipartFile file) throws Exception
    {
        ExcelUtil<FinSubject> util = new ExcelUtil<>(FinSubject.class);
        List<FinSubject> subjects = util.importExcel(file.getInputStream());
        int n = subjectService.importSubjects(subjects, getUsername());
        return AjaxResult.success("导入成功 " + n + " 条科目");
    }

    /**
     * 下载导入模板
     */
    @PostMapping("/importTemplate")
    public void importTemplate(HttpServletResponse response)
    {
        ExcelUtil<FinSubject> util = new ExcelUtil<>(FinSubject.class);
        util.importTemplateExcel(response, "会计科目导入模板");
    }
}
