package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.ruoyi.flowable.domain.FlowAttach;
import com.ruoyi.flowable.service.IFlowAttachService;

/**
 * 流程附件 Controller
 *
 * 文件本体通过 /common/upload 上传落盘，此处只登记附件与流程实例/任务的归属关系，
 * 供办理页「相关资源」页签与流程详情页汇总展示。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/attach")
public class FlowAttachController extends BaseController
{
    @Autowired
    private IFlowAttachService flowAttachService;

    /** 某流程实例下的全部附件 */
    @GetMapping("/list/{instanceId}")
    public AjaxResult list(@PathVariable("instanceId") String instanceId)
    {
        List<FlowAttach> list = flowAttachService.selectByInstance(instanceId);
        return success(list);
    }

    /** 登记附件（上传成功后调用） */
    @Log(title = "流程附件", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @PostMapping
    public AjaxResult add(@RequestBody FlowAttach attach)
    {
        flowAttachService.addAttach(attach);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("attachId", attach.getAttachId());
        return ajax;
    }

    /** 删除附件 */
    @Log(title = "流程附件", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:task:complete')")
    @DeleteMapping("/{attachId}")
    public AjaxResult remove(@PathVariable("attachId") Long attachId)
    {
        return toAjax(flowAttachService.deleteAttach(attachId));
    }
}
