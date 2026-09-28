package com.ruoyi.mk.controller;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkContent;
import com.ruoyi.mk.service.IMkContentService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 内容发布 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/content")
public class MkContentController extends BaseController
{
    @Autowired
    private IMkContentService mkContentService;

    @PreAuthorize("@ss.hasPermi('marketing:content:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkContent content)
    {
        startPage();
        List<MkContent> list = mkContentService.selectContentList(content);
        return getDataTable(list);
    }

    @Log(title = "内容发布", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:content:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkContent content)
    {
        List<MkContent> list = mkContentService.selectContentList(content);
        ExcelUtil<MkContent> util = new ExcelUtil<>(MkContent.class);
        util.exportExcel(response, list, "内容数据");
    }

    /** 主档+内容包集合（回显） */
    @PreAuthorize("@ss.hasPermi('marketing:content:query')")
    @GetMapping(value = "/{contentId}")
    public AjaxResult getInfo(@PathVariable("contentId") Long contentId)
    {
        return AjaxResult.success(mkContentService.selectContentById(contentId));
    }

    @Log(title = "内容发布", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:content:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkContent content)
    {
        mkContentService.insertContent(content);
        return AjaxResult.success(content.getContentId());
    }

    @Log(title = "内容发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:content:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkContent content)
    {
        return toAjax(mkContentService.updateContent(content));
    }

    @Log(title = "内容发布", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:content:remove')")
    @DeleteMapping("/{contentIds}")
    public AjaxResult remove(@PathVariable Long[] contentIds)
    {
        return toAjax(mkContentService.deleteContentByIds(contentIds));
    }

    /** 提交审核：草稿/已驳回 → 待审核 */
    @Log(title = "内容提交审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:content:edit')")
    @PutMapping("/submit/{contentId}")
    public AjaxResult submit(@PathVariable Long contentId)
    {
        return toAjax(mkContentService.submitContent(contentId));
    }

    /** 审核：{pass, auditRemark} */
    @Log(title = "内容审核", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:content:audit')")
    @PutMapping("/audit/{contentId}")
    public AjaxResult audit(@PathVariable Long contentId, @RequestBody Map<String, Object> body)
    {
        boolean pass = Boolean.TRUE.equals(body.get("pass")) || "true".equals(String.valueOf(body.get("pass")));
        String auditRemark = body.get("auditRemark") != null ? String.valueOf(body.get("auditRemark")) : null;
        return toAjax(mkContentService.auditContent(contentId, pass, auditRemark));
    }

    /** 发布内容包：{publishUrl} */
    @Log(title = "内容包发布", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:content:publish')")
    @PutMapping("/publish/{packageId}")
    public AjaxResult publish(@PathVariable Long packageId, @RequestBody Map<String, String> body)
    {
        return toAjax(mkContentService.publishPackage(packageId, body.get("publishUrl")));
    }

    /** AI生成各渠道文案草稿：{contentId, channelTypes[]}，结果仅返回前端填充 */
    @PreAuthorize("@ss.hasPermi('marketing:content:query')")
    @PostMapping("/aiGenerate")
    public AjaxResult aiGenerate(@RequestBody Map<String, Object> body)
    {
        Long contentId = Long.valueOf(String.valueOf(body.get("contentId")));
        @SuppressWarnings("unchecked")
        List<String> channelTypes = (List<String>) body.get("channelTypes");
        return AjaxResult.success(mkContentService.aiGenerate(contentId, channelTypes));
    }
}
