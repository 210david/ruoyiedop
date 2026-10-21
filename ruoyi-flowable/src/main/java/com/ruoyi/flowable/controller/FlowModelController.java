package com.ruoyi.flowable.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
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
import com.ruoyi.flowable.domain.FlowModel;
import com.ruoyi.flowable.service.IFlowModelService;

/**
 * 流程模型管理 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/model")
public class FlowModelController extends BaseController
{
    @Autowired
    private IFlowModelService flowModelService;

    /**
     * 查询流程模型列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:model:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowModel model)
    {
        startPage();
        List<FlowModel> list = flowModelService.selectModelList(model);
        return getDataTable(list);
    }

    /**
     * 获取模型详情（含BPMN XML）
     */
    @PreAuthorize("@ss.hasPermi('flowable:model:query')")
    @GetMapping("/{modelId}")
    public AjaxResult getInfo(@PathVariable("modelId") String modelId)
    {
        return success(flowModelService.selectModelById(modelId));
    }

    /**
     * 新增模型
     */
    @Log(title = "流程模型", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:model:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody FlowModel model)
    {
        return success(flowModelService.insertModel(model));
    }

    /**
     * 修改模型（保存设计器内容）
     */
    @Log(title = "流程模型", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:model:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody FlowModel model)
    {
        return toAjax(flowModelService.updateModel(model));
    }

    /**
     * 删除模型
     */
    @Log(title = "流程模型", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:model:remove')")
    @DeleteMapping("/{modelIds}")
    public AjaxResult remove(@PathVariable("modelIds") String[] modelIds)
    {
        return toAjax(flowModelService.deleteModelByIds(modelIds));
    }

    /**
     * 部署模型（mode=new 新版本并存，默认；mode=replace 覆盖当前版本，部署后停用旧版本并设为活动版本；
     * remark 版本备注；setActive 是否设为活动版本）
     */
    @Log(title = "流程模型", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:model:deploy')")
    @PostMapping("/deploy/{modelId}")
    public AjaxResult deploy(@PathVariable("modelId") String modelId,
            @RequestParam(value = "mode", required = false, defaultValue = "new") String mode,
            @RequestParam(value = "remark", required = false) String remark,
            @RequestParam(value = "setActive", required = false, defaultValue = "true") Boolean setActive)
    {
        return toAjax(flowModelService.deployModel(modelId, mode, remark, setActive));
    }
}
