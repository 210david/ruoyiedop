package com.ruoyi.pjm.controller;

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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.pjm.domain.PjmProjectAttachment;
import com.ruoyi.pjm.service.IPjmProjectAttachmentService;

/**
 * 项目立项附件 Controller
 */
@RestController
@RequestMapping("/pjm/project/attachment")
public class PjmProjectAttachmentController extends BaseController
{
    @Autowired
    private IPjmProjectAttachmentService attachmentService;

    @PreAuthorize("@ss.hasPermi('pjm:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmProjectAttachment attachment)
    {
        startPage();
        List<PjmProjectAttachment> list = attachmentService.selectPjmProjectAttachmentList(attachment);
        return getDataTable(list);
    }

    /** 按项目ID查询 */
    @GetMapping("/project/{projectId}")
    public AjaxResult listByProject(@PathVariable Long projectId)
    {
        return success(attachmentService.selectByProjectId(projectId));
    }

    @GetMapping("/{attachmentId}")
    public AjaxResult getInfo(@PathVariable Long attachmentId)
    {
        return success(attachmentService.selectPjmProjectAttachmentById(attachmentId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:add')")
    @Log(title = "项目附件", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmProjectAttachment attachment)
    {
        attachment.setCreateBy(getUsername());
        return toAjax(attachmentService.insertPjmProjectAttachment(attachment));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:edit')")
    @Log(title = "项目附件", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmProjectAttachment attachment)
    {
        attachment.setUpdateBy(getUsername());
        return toAjax(attachmentService.updatePjmProjectAttachment(attachment));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:remove')")
    @Log(title = "项目附件", businessType = BusinessType.DELETE)
    @DeleteMapping("/{attachmentIds}")
    public AjaxResult remove(@PathVariable Long[] attachmentIds)
    {
        return toAjax(attachmentService.deletePjmProjectAttachmentByIds(attachmentIds));
    }
}
