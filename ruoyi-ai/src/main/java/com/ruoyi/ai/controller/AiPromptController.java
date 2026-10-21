package com.ruoyi.ai.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.ai.core.AiPromptDefaults;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.system.domain.SysConfig;
import com.ruoyi.system.service.ISysConfigService;
import com.ruoyi.common.utils.StringUtils;

/**
 * 提示词模板管理 Controller
 * 提示词存 sys_config（键 ai.prompt.<module>.<feature>），修改后热生效
 * 支持占位符 {xxx}，由 AiClient.chatWithTemplate 填充
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/prompt")
public class AiPromptController extends AiBaseConfigController
{
    /** 功能标识中文说明（供管理页展示） */
    private static final Map<String, String> FEATURE_NAMES = new HashMap<>();
    static
    {
        FEATURE_NAMES.put("repair_assistant", "智能报修（DMS）");
        FEATURE_NAMES.put("dispatch_recommend", "智能派工推荐（DMS）");
        FEATURE_NAMES.put("knowledge_qa", "维修知识问答（DMS）/ 知识库问答（RAG）");
        FEATURE_NAMES.put("faq_generate", "FAQ自动沉淀（DMS）");
        FEATURE_NAMES.put("complete_report", "完工报告生成（DMS）");
        FEATURE_NAMES.put("ncr_advice", "NCR处置建议（QMS）");
        FEATURE_NAMES.put("report_draft", "质量报告初稿（QMS）");
        FEATURE_NAMES.put("abnormal_advice", "异常诊断建议（MMS）");
        FEATURE_NAMES.put("daily_report", "生产日报生成（MMS）");
        FEATURE_NAMES.put("request_advice", "采购申请建议（PMS）");
        FEATURE_NAMES.put("price_summary", "比价分析摘要（PMS）");
        FEATURE_NAMES.put("content_copy", "内容文案生成（MK）");
    }

    /**
     * 查询提示词模板列表（ai.prompt.* 配置项）
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:list')")
    @GetMapping("/list")
    public AjaxResult list()
    {
        SysConfig query = new SysConfig();
        query.setConfigKey("ai.prompt.");
        List<SysConfig> configs = configService.selectConfigList(query);
        List<Map<String, Object>> list = new ArrayList<>();
        if (configs != null)
        {
            for (SysConfig c : configs)
            {
                Map<String, Object> item = new HashMap<>();
                item.put("configId", c.getConfigId());
                item.put("configKey", c.getConfigKey());
                // ai.prompt.<module>.<feature> 解析
                String[] parts = c.getConfigKey().split("\\.");
                String module = parts.length > 2 ? parts[2] : "";
                String feature = parts.length > 3 ? parts[3] : "";
                item.put("module", module);
                item.put("feature", feature);
                item.put("featureName", FEATURE_NAMES.getOrDefault(feature, feature));
                item.put("template", c.getConfigValue());
                item.put("updateTime", c.getUpdateTime());
                list.add(item);
            }
        }
        return AjaxResult.success(list);
    }

    /**
     * 查询各功能内置默认提示词（供管理页回显，避免"默认"状态编辑框为空）
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:list')")
    @GetMapping("/defaults")
    public AjaxResult defaults()
    {
        return AjaxResult.success(AiPromptDefaults.DEFAULTS);
    }

    /**
     * 修改提示词模板（热生效）
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody Map<String, String> body)
    {
        String configKey = body.get("configKey");
        String template = body.get("template");
        if (StringUtils.isEmpty(configKey) || !configKey.startsWith("ai.prompt."))
        {
            return AjaxResult.error("提示词键必须以 ai.prompt. 开头");
        }
        if (StringUtils.isEmpty(template))
        {
            return AjaxResult.error("提示词内容不能为空");
        }
        updateConfigByKey(configKey, template);
        return AjaxResult.success("保存成功，即时生效");
    }

    /**
     * 恢复默认模板（删除 ai.prompt.* 自定义配置，回退系统内置模板，热生效）
     */
    @PreAuthorize("@ss.hasPermi('ai:prompt:edit')")
    @DeleteMapping("/{configKey}")
    public AjaxResult reset(@PathVariable String configKey)
    {
        if (StringUtils.isEmpty(configKey) || !configKey.startsWith("ai.prompt."))
        {
            return AjaxResult.error("提示词键必须以 ai.prompt. 开头");
        }
        SysConfig query = new SysConfig();
        query.setConfigKey(configKey);
        List<SysConfig> list = configService.selectConfigList(query);
        SysConfig target = null;
        if (list != null)
        {
            for (SysConfig c : list)
            {
                if (configKey.equals(c.getConfigKey()))
                {
                    target = c;
                    break;
                }
            }
        }
        if (target == null)
        {
            return AjaxResult.success("该功能未配置自定义模板，无需恢复");
        }
        // 历史保存可能写为 Y（内置参数），deleteConfigByIds 会拦截；先改为 N 再删除
        if ("Y".equals(target.getConfigType()))
        {
            target.setConfigType("N");
            configService.updateConfig(target);
        }
        configService.deleteConfigByIds(new Long[] { target.getConfigId() });
        return AjaxResult.success("已恢复默认模板，即时生效");
    }
}
