package com.ruoyi.bid.controller;

import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.bid.service.IBidNoticeService;

/**
 * 招标公告库 Controller
 */
@RestController
@RequestMapping("/bid/notice")
public class BidNoticeController extends BaseController
{
    @Autowired
    private IBidNoticeService noticeService;

    /** 查询列表（分页） */
    @PreAuthorize("@ss.hasPermi('bid:notice:list')")
    @GetMapping("/list")
    public TableDataInfo list(BidNotice notice)
    {
        startPage();
        List<BidNotice> list = noticeService.selectNoticeList(notice);
        return getDataTable(list);
    }

    /** 导出 Excel */
    @Log(title = "招标公告", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('bid:notice:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, BidNotice notice)
    {
        List<BidNotice> list = noticeService.selectNoticeList(notice);
        ExcelUtil<BidNotice> util = new ExcelUtil<>(BidNotice.class);
        util.exportExcel(response, list, "招标公告数据");
    }

    /** 项目档案：按 projectKey 聚合 */
    @PreAuthorize("@ss.hasPermi('bid:notice:query')")
    @GetMapping("/archive/{projectKey}")
    public AjaxResult archive(@PathVariable("projectKey") String projectKey)
    {
        return AjaxResult.success(noticeService.selectArchiveByProjectKey(projectKey));
    }

    /** 详情 */
    @PreAuthorize("@ss.hasPermi('bid:notice:query')")
    @GetMapping("/{noticeId}")
    public AjaxResult getInfo(@PathVariable("noticeId") Long noticeId)
    {
        return AjaxResult.success(noticeService.selectNoticeById(noticeId));
    }

    /** 新增（手动登记） */
    @Log(title = "招标公告", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:notice:add')")
    @PostMapping
    public AjaxResult add(@org.springframework.validation.annotation.Validated @RequestBody BidNotice notice)
    {
        return toAjax(noticeService.insertNotice(notice));
    }

    /** 修改 */
    @Log(title = "招标公告", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:notice:edit')")
    @PutMapping
    public AjaxResult edit(@org.springframework.validation.annotation.Validated @RequestBody BidNotice notice)
    {
        return toAjax(noticeService.updateNotice(notice));
    }

    /** 删除 */
    @Log(title = "招标公告", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('bid:notice:remove')")
    @DeleteMapping("/{noticeIds}")
    public AjaxResult remove(@PathVariable Long[] noticeIds)
    {
        return toAjax(noticeService.deleteNoticeByIds(noticeIds));
    }

    /** AI 智能抽取（仅预览，不落库） */
    @Log(title = "招标公告AI抽取", businessType = BusinessType.OTHER)
    @PreAuthorize("@ss.hasPermi('bid:notice:edit')")
    @PostMapping("/aiExtract/{noticeId}")
    public AjaxResult aiExtract(@PathVariable("noticeId") Long noticeId)
    {
        return AjaxResult.success(noticeService.aiExtract(noticeId));
    }

    /** AI 抽取结果确认回填：仅采纳字段，已有值不覆盖 */
    @Log(title = "招标公告AI抽取回填", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('bid:notice:edit')")
    @PostMapping("/aiExtract/{noticeId}/apply")
    public AjaxResult applyAiExtract(@PathVariable("noticeId") Long noticeId, @RequestBody Map<String, String> fields)
    {
        return toAjax(noticeService.applyAiExtract(noticeId, fields));
    }

    /** 公告一键转投标项目 */
    @Log(title = "公告转投标项目", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('bid:notice:edit')")
    @PostMapping("/toProject/{noticeId}")
    public AjaxResult toProject(@PathVariable("noticeId") Long noticeId)
    {
        String bidNo = noticeService.toProject(noticeId);
        return AjaxResult.success("已登记投标项目，编号：" + bidNo, bidNo);
    }
}
