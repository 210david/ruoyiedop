package com.ruoyi.flowable.controller;

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
        // 优先：发起节点模板（节点表单设计落库，字段/必填/只读修改无需重新部署流程）
        FlowNodeForm nodeForm = flowNodeFormService.selectStartByModelKey(processKey);
        if (nodeForm != null && StringUtils.isNotEmpty(nodeForm.getFieldsJson()))
        {
            FlowForm bound = flowFormService.selectFlowFormByProcessKey(processKey);
            com.alibaba.fastjson2.JSONObject data = new com.alibaba.fastjson2.JSONObject();
            data.put("formId", bound == null ? null : bound.getFormId());
            data.put("formKey", nodeForm.getFormKey() != null ? nodeForm.getFormKey() : (bound == null ? null : bound.getFormKey()));
            data.put("formName", StringUtils.isNotEmpty(nodeForm.getFormName()) ? nodeForm.getFormName() : (bound == null ? null : bound.getFormName()));
            data.put("tableName", bound == null ? null : bound.getTableName());
            data.put("fieldsJson", nodeForm.getFieldsJson());
            data.put("layoutJson", nodeForm.getLayoutJson());
            data.put("nodeForm", true);
            return success(data);
        }
        return success(flowFormService.selectFlowFormByProcessKey(processKey));
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
        return success(flowFormService.createTable(formId));
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
        return success(flowFormService.launch(definitionKey, formId, variables, approverUserId, title));
    }
}
