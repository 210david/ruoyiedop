package com.ruoyi.flowable.controller;

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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.flowable.domain.FlowForm;
import com.ruoyi.flowable.domain.FlowNodeForm;
import com.ruoyi.flowable.service.IFlowFormService;
import com.ruoyi.flowable.service.IFlowNodeFormService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 流程表单 Controller（建模引擎：表单设计/数据表生成/表单发起）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/form")
public class FlowFormController extends BaseController
{
    @Autowired
    private IFlowFormService flowFormService;

    @Autowired
    private IFlowNodeFormService flowNodeFormService;

    /**
     * 查询表单列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowForm form)
    {
        startPage();
        List<FlowForm> list = flowFormService.selectFlowFormList(form);
        return getDataTable(list);
    }

    /**
     * 表单列表（不分页，供发起页下拉）
     */
    @GetMapping("/all")
    public AjaxResult all()
    {
        return success(flowFormService.selectFlowFormList(new FlowForm()));
    }

    /**
     * 根据流程Key获取绑定表单（发起页渲染表单）
     */
    @GetMapping("/byProcess/{processKey}")
    public AjaxResult byProcess(@PathVariable("processKey") String processKey)
    {
        // 发起节点模板：只覆盖「显示布局」与模板名称；字段定义始终取表单管理里的那一份，
        // 这样在表单管理增删字段后，所有节点（含发起节点）立即同步，无需重新部署
        FlowNodeForm nodeForm = flowNodeFormService.selectStartByModelKey(processKey);
        if (nodeForm != null)
        {
            FlowForm bound = flowFormService.selectFlowFormByProcessKey(processKey);
            com.alibaba.fastjson2.JSONObject data = new com.alibaba.fastjson2.JSONObject();
            data.put("formId", bound == null ? null : bound.getFormId());
            data.put("formKey", nodeForm.getFormKey() != null ? nodeForm.getFormKey() : (bound == null ? null : bound.getFormKey()));
            data.put("formName", StringUtils.isNotEmpty(nodeForm.getFormName()) ? nodeForm.getFormName() : (bound == null ? null : bound.getFormName()));
            data.put("tableName", bound == null ? null : bound.getTableName());
            // 字段：表单为准（表单缺失时才退回节点模板里的历史快照，避免老数据整页空白）
            data.put("fieldsJson", resolveNodeFieldsJson(nodeForm, bound));
            // 布局：节点布局优先（节点可裁剪显示哪些字段），没有则用表单布局
            data.put("layoutJson", StringUtils.isNotEmpty(nodeForm.getLayoutJson())
                    ? nodeForm.getLayoutJson() : (bound == null ? null : bound.getLayoutJson()));
            data.put("nodeForm", true);
            return success(data);
        }
        return success(flowFormService.selectFlowFormByProcessKey(processKey));
    }

    /**
     * 节点模板场景下的字段定义：以表单管理里的表单为准（字段只有一份），
     * 仅当表单不存在或字段为空时，退回节点模板里的历史快照，避免老数据整页空白。
     */
    private String resolveNodeFieldsJson(FlowNodeForm nodeForm, FlowForm bound)
    {
        if (bound != null && StringUtils.isNotEmpty(bound.getFieldsJson()))
        {
            return bound.getFieldsJson();
        }
        return nodeForm == null || nodeForm.getFieldsJson() == null ? "" : nodeForm.getFieldsJson();
    }

    /**
     * 获取表单详情
     */
    @PreAuthorize("@ss.hasPermi('flowable:form:query')")
    @GetMapping("/{formId}")
    public AjaxResult getInfo(@PathVariable("formId") Long formId)
    {
        return success(flowFormService.selectFlowFormByFormId(formId));
    }

    /**
     * 新增表单
     */
    @Log(title = "流程表单", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:form:add')")
    @PostMapping
    public AjaxResult add(@RequestBody FlowForm form)
    {
        flowFormService.insertFlowForm(form);
        return success(form.getFormId());
    }

    /**
     * 修改表单
     */
    @Log(title = "流程表单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:form:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody FlowForm form)
    {
        return toAjax(flowFormService.updateFlowForm(form));
    }

    /**
     * 删除表单
     */
    @Log(title = "流程表单", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('flowable:form:remove')")
    @DeleteMapping("/{formIds}")
    public AjaxResult remove(@PathVariable Long[] formIds)
    {
        return toAjax(flowFormService.deleteFlowFormByFormIds(formIds));
    }

    /**
     * 生成数据表
     */
    @Log(title = "流程表单", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('flowable:form:table')")
    @PostMapping("/createTable/{formId}")
    public AjaxResult createTable(@PathVariable("formId") Long formId)
    {
        // 显式返回 (msg, data)：success(String) 重载会把表名放进 msg，前端取 data 时拿到 null
        return AjaxResult.success("操作成功", flowFormService.createTable(formId));
    }

    /**
     * 通用流程发起（支持表单数据落表）
     */
    @Log(title = "流程发起", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('flowable:process:start')")
    @PostMapping("/launch")
    public AjaxResult launch(@RequestBody Map<String, Object> body)
    {
        String definitionKey = (String) body.get("definitionKey");
        Object formIdObj = body.get("formId");
        Long formId = formIdObj == null ? null : Long.valueOf(String.valueOf(formIdObj));
        Object approverObj = body.get("approverUserId");
        Long approverUserId = approverObj == null || "".equals(approverObj) ? null : Long.valueOf(String.valueOf(approverObj));
        @SuppressWarnings("unchecked")
        Map<String, Object> variables = (Map<String, Object>) body.get("variables");
        String title = (String) body.get("title");
        String instanceId = flowFormService.launch(definitionKey, formId, variables, approverUserId, title);
        // 注意：不要写成 success(flowFormService.launch(...))。
        // launch() 返回 String，会命中 BaseController.success(String message) 重载，
        // 实例 ID 被当成提示语塞进 msg、data 为 null，前端拿不到单号（提交后跳详情/提示单号都会失效）。
        Map<String, Object> data = new HashMap<>();
        data.put("instanceId", instanceId);
        return AjaxResult.success("流程发起成功", data);
    }
}
