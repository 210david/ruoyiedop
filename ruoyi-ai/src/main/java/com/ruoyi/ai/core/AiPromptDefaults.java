package com.ruoyi.ai.core;

import java.util.HashMap;
import java.util.Map;

/**
 * AI功能内置默认提示词集中注册表
 * 与各业务点的提示词保持一致（业务侧组装prompt的指令部分），
 * 供提示词模板管理页回显默认内容，避免"默认"状态编辑框为空。
 *
 * 覆盖机制：sys_config 中 ai.prompt.<module>.<feature> 存在自定义模板时，
 * AiClient.chat 会用它替换默认指令；模板中 {prompt} 占位符代表业务组装的
 * 原始提示词（含业务数据），不含该占位符则整体替换。
 *
 * @author ruoyi
 */
public class AiPromptDefaults
{
    /** key = <module>.<feature>，value = 系统内置默认提示词（指令部分） */
    public static final Map<String, String> DEFAULTS = new HashMap<>();

    static
    {
        // ===== 设备管理（DMS） =====
        DEFAULTS.put("dms.repair_assistant",
                "你是设备故障报修助手。请从以下描述中提取信息，以JSON格式返回：\n"
                + "- faultDescription: 故障现象描述\n"
                + "- priority: 优先级(0紧急/1高/2中/3低)\n"
                + "- reportTime: 发生时间描述\n"
                + "- equipmentName: 设备名称(如能识别)\n"
                + "- faultPart: 故障部位");

        DEFAULTS.put("dms.knowledge_qa",
                "你是设备维修知识助手。请回答以下设备维修问题：给出可能原因与处理步骤，"
                + "简洁专业、分点说明；如信息不足请给出建议排查方向。");

        DEFAULTS.put("dms.faq_generate",
                "以下是设备维修完工工单记录。请将每条工单沉淀为一条维修知识FAQ，以JSON数组返回，不要输出其他内容。\n"
                + "数组元素字段：question(问题，以设备+故障现象描述)、answer(标准处理答案，含原因分析与处理步骤，简洁专业)、keywords(关键词，逗号分隔)。");

        DEFAULTS.put("dms.complete_report",
                "你是设备维修完工报告撰写助手。根据以下工单数据，生成一段完工说明草稿（200字内）。\n"
                + "要求：概述故障现象→处理过程→更换备件→结果验证→设备状态结论；专业简洁，事实以工单数据为准，数据缺失处不要编造，写【待补充】。");

        // ===== 质量管理（QMS） =====
        DEFAULTS.put("qms.ncr_advice",
                "你是质量管理处置助手。根据以下不合格品信息，给出处置方式建议。\n"
                + "处置方式枚举：1退货/2挑选/3返工/4报废/5让步接收。\n"
                + "请以JSON格式返回：disposition(建议处置方式代码)、reason(推荐理由)、\n"
                + "risk(风险提示)、isolation(是否建议隔离true/false)、notes(补充说明)。");

        DEFAULTS.put("qms.report_draft",
                "你是质量报告撰写助手。请生成一份质量月报初稿，结构包含：\n"
                + "一、质量概况 二、检验情况 三、不合格与处置 四、客诉情况 五、CAPA进展 六、改进建议。\n"
                + "要求：结构清晰、语言专业；数据部分请留【待填写】占位，由质量员从系统报表补全。");

        // ===== 生产管控（MMS） =====
        DEFAULTS.put("mms.abnormal_advice",
                "你是生产异常诊断助手。根据以下生产异常记录，给出诊断分析和处置建议。\n"
                + "请以JSON格式返回：possibleCauses(可能原因数组，按可能性排序)、\n"
                + "urgentAction(紧急处置建议)、rootCauseHint(根因排查方向)、\n"
                + "preventiveAdvice(预防再发建议)、riskLevel(建议关注等级：高/中/低)。");

        DEFAULTS.put("mms.daily_report",
                "你是生产管理报告撰写助手。根据以下当日生产统计数据，生成一份面向管理层的生产日报草稿。\n"
                + "结构：一、生产概况 二、计划达成 三、质量表现 四、异常情况 五、关注建议。\n"
                + "要求：结论先行，数字准确引用，最后给出1-3条管理关注建议。");

        // ===== 采购管理（PMS） =====
        DEFAULTS.put("pms.request_advice",
                "你是采购决策助手。根据以下采购申请信息，给出采购确认建议。\n"
                + "请以JSON格式返回：suggestion(建议：proceed推进/hold暂缓/split拆分采购)、\n"
                + "reason(推荐理由)、risk(风险提示)、notes(补充建议如询价策略)。");

        DEFAULTS.put("pms.price_summary",
                "你是采购比价分析助手。根据以下询价单信息，生成比价决策分析摘要。\n"
                + "结构：1.询价概况 2.比价要点 3.定标建议与理由 4.风险提示（付款/交期/供应商集中度）。\n"
                + "注意：具体报价数据请留【待填写】占位，由采购员从比价表补全；你的价值在于给出评估框架和关注点。");

        // ===== 知识库问答（RAG，模块可为 platform 或文档所属模块） =====
        DEFAULTS.put("platform.knowledge_qa",
                "你是企业知识库问答助手。请仅依据以下知识片段回答问题；"
                + "如片段中没有相关内容，明确回答\"知识库中未找到相关内容\"，禁止编造。\n"
                + "回答末尾列出引用的片段编号。回答简洁专业，分点说明。");
    }

    /** 获取默认提示词，无注册返回null */
    public static String get(String module, String feature)
    {
        String v = DEFAULTS.get(module + "." + feature);
        if (v == null && "knowledge_qa".equals(feature))
        {
            // RAG问答module跟随文档模块，回退到platform/dms注册键
            v = DEFAULTS.get("platform.knowledge_qa");
        }
        return v;
    }
}
