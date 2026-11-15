package com.ruoyi.flowable.controller;

import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.flowable.service.FlowProcessNoService;

/**
 * 流程编号规则：预览（按规则算下一单的真实编号，不消耗流水号）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/flowable/processNo")
public class FlowProcessNoController extends BaseController
{
    @Autowired
    private FlowProcessNoService processNoService;

    /**
     * 编号预览
     *
     * 请求体：{ processKey: 'leave_process', rule: {...正在编辑的规则...}, sample: { bm: '研发部' } }
     * 返回：{ enabled, preview, nextSeq, noKey, started }
     */
    @PostMapping("/preview")
    public AjaxResult preview(@RequestBody Map<String, Object> body)
    {
        String processKey = body.get("processKey") == null ? null : String.valueOf(body.get("processKey"));
        JSONObject rule = null;
        Object ruleObj = body.get("rule");
        if (ruleObj instanceof Map)
        {
            rule = new JSONObject((Map<String, Object>) ruleObj);
        }
        else if (ruleObj != null && String.valueOf(ruleObj).trim().startsWith("{"))
        {
            rule = JSONObject.parseObject(String.valueOf(ruleObj));
        }
        Map<String, Object> sample = new HashMap<>();
        Object sampleObj = body.get("sample");
        if (sampleObj instanceof Map)
        {
            for (Map.Entry<?, ?> e : ((Map<?, ?>) sampleObj).entrySet())
            {
                if (e.getKey() != null && e.getValue() != null)
                {
                    sample.put(String.valueOf(e.getKey()), e.getValue());
                }
            }
        }
        return success(processNoService.preview(processKey, rule, sample));
    }
}
