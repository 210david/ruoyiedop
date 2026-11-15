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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowAiGenTask;
import com.ruoyi.flowable.service.IFlowAiGenService;

/**
 * AI流程生成 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/aigen")
public class FlowAiGenController extends BaseController
{
    @Autowired
    private IFlowAiGenService flowAiGenService;

    /**
     * 查询AI流程生成任务列表
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:list')")
    @GetMapping("/list")
    public TableDataInfo list(FlowAiGenTask query)
    {
        startPage();
        List<FlowAiGenTask> list = flowAiGenService.selectFlowAiGenTaskList(query);
        return getDataTable(list);
    }

    /**
     * 查询任务详情（含结构化解析结果）
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:query')")
    @GetMapping("/{taskId}")
    public AjaxResult getInfo(@PathVariable Long taskId)
    {
        return AjaxResult.success(withParsedResult(flowAiGenService.selectFlowAiGenTaskById(taskId)));
    }

    /**
     * 上传流程方案并调用AI解析
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:analyze')")
    @Log(title = "AI流程生成", businessType = BusinessType.INSERT)
    @PostMapping("/analyze")
    public AjaxResult analyze(@RequestParam("file") MultipartFile file,
            @RequestParam(value = "taskName", required = false) String taskName,
            @RequestParam(value = "remark", required = false) String remark)
    {
        Long taskId = flowAiGenService.uploadAndAnalyze(file, taskName, remark);
        FlowAiGenTask task = flowAiGenService.selectFlowAiGenTaskById(taskId);
        return AjaxResult.success(withParsedResult(task));
    }

    /**
     * 基于已保存的方案文本重新解析
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:analyze')")
    @Log(title = "AI流程生成", businessType = BusinessType.UPDATE)
    @PostMapping("/reanalyze/{taskId}")
    public AjaxResult reanalyze(@PathVariable Long taskId,
            @RequestParam(value = "remark", required = false) String remark)
    {
        return AjaxResult.success(withParsedResult(flowAiGenService.reanalyze(taskId, remark)));
    }

    /**
     * 按确认后的结构化结果生成表单与流程模型草稿（不部署）
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:generate')")
    @Log(title = "AI流程生成", businessType = BusinessType.INSERT)
    @PostMapping("/generate")
    public AjaxResult generate(@RequestBody JSONObject body)
    {
        if (body == null || body.getLong("taskId") == null)
        {
            return AjaxResult.error("参数不完整：taskId不能为空");
        }
        String resultJson = body.getString("resultJson");
        Map<String, Object> data = flowAiGenService.generate(body.getLong("taskId"), resultJson);
        return AjaxResult.success("流程草稿生成成功，请在流程模型与表单管理中确认后部署", data);
    }

    /**
     * 批量删除任务
     */
    @PreAuthorize("@ss.hasPermi('flowable:aigen:remove')")
    @Log(title = "AI流程生成", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds)
    {
        return toAjax(flowAiGenService.deleteFlowAiGenTaskByIds(taskIds));
    }

    /** 任务 + 解析结果 */
    private Map<String, Object> withParsedResult(FlowAiGenTask task)
    {
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("task", task);
        if (task != null && StringUtils.isNotEmpty(task.getResultJson()))
        {
            try
            {
                data.put("result", JSON.parseObject(task.getResultJson()));
            }
            catch (Exception e)
            {
                data.put("result", null);
            }
        }
        return data;
    }
}
