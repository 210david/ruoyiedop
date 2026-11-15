package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowDraft;
import com.ruoyi.flowable.service.IFlowDraftService;

/**
 * 流程草稿 Controller
 *
 * 对标泛微「保存草稿」：只登记表单内容，不产生流程实例与待办任务；
 * 草稿在「待办流程-草稿」页签可见，继续填写后提交才真正发起流程。
 *
 * 说明：所有接口都以当前登录人为数据边界，不校验菜单权限（普通发起人也需要保存草稿）。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/draft")
public class FlowDraftController extends BaseController
{
    @Autowired
    private IFlowDraftService flowDraftService;

    /**
     * 我的草稿列表
     */
    @GetMapping("/list")
    public TableDataInfo list(FlowDraft query)
    {
        startPage();
        List<FlowDraft> list = flowDraftService.selectMyDraftList(query);
        return getDataTable(list);
    }

    /**
     * 草稿详情（含表单数据，供发起页回填）
     */
    @GetMapping("/{draftId}")
    public AjaxResult getInfo(@PathVariable Long draftId)
    {
        return success(flowDraftService.selectMyDraftById(draftId));
    }

    /**
     * 保存草稿（有 draftId 则更新，否则新增）
     */
    @Log(title = "流程草稿", businessType = BusinessType.INSERT)
    @PostMapping("/save")
    public AjaxResult save(@RequestBody FlowDraft draft)
    {
        Long draftId = flowDraftService.saveMyDraft(draft);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("draftId", draftId);
        return ajax;
    }

    /**
     * 删除草稿
     */
    @Log(title = "流程草稿", businessType = BusinessType.DELETE)
    @DeleteMapping("/{draftIds}")
    public AjaxResult remove(@PathVariable Long[] draftIds)
    {
        return toAjax(flowDraftService.deleteMyDrafts(draftIds));
    }
}
