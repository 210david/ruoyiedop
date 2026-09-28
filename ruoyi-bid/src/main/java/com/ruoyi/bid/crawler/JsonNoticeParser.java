package com.ruoyi.bid.crawler;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 通用 JSON 数据接口解析器
 *
 * parse_config JSON 结构示例：
 * {
 *   "dataPath": "value.list",       // 结果数组所在 JSON 路径
 *   "fieldMap": { "title": "name", "url": "detailUrl", "date": "publishDate",
 *                 "tenderee": "owner", "budget": "money", "projectKey": "code",
 *                 "noticeType": "type", "deadlineTime": "endTime" },
 *   "pageParam": "pageNo", "pageSizeParam": "pageSize", "pageSize": 20, "maxPage": 2,
 *   "method": "post",               // get(默认) / post(JSON body) / postForm(表单)
 *   "body": { "cityId": "003", "industryCode": "", "title": "" },   // 附加固定参数
 *   "urlTemplate": "https://xxx/detail?guid={guid}",   // 详情链接模板，{字段名} 取行数据
 *   "detail": {                     // 可选：详情级抓取，逐条调详情接口补全字段
 *     "url": "https://xxx/findZbggByGuid?guid={guid}",  // 详情接口，{字段名} 取列表行数据
 *     "method": "get",              // get / post / postForm
 *     "dataPath": "value",          // 详情对象所在 JSON 路径（默认 value）
 *     "fieldMap": { "title": "bulletinname", "content": "bulletincontent", ... },
 *     "regex": {                    // 可选：对 content 纯文本做正则提取
 *       "deadlineTime": "递交投标文件截止[时间:]*\\s*([0-9]{4}-[0-9]{2}-[0-9]{2}[ 0-9:]*)",
 *       "tenderee": "建设单位[：:]\\s*([^，。\\s]{2,40})",
 *       "budget": "合同估算价[：:]\\s*([0-9.]+)\\s*万元"
 *     }
 *   }
 * }
 */
@Component
public class JsonNoticeParser implements NoticeParser
{
    private static final Logger log = LoggerFactory.getLogger(JsonNoticeParser.class);

    @Override
    public boolean supports(String parserCode)
    {
        return "common-json".equals(parserCode);
    }

    @Override
    public List<BidNotice> parse(BidCrawlSource source) throws Exception
    {
        if (StringUtils.isEmpty(source.getParseConfig()))
        {
            throw new IllegalStateException("采集源[" + source.getSourceName() + "]未配置解析配置 parse_config");
        }
        JSONObject cfg = JSONObject.parseObject(source.getParseConfig());
        JSONObject fieldMap = cfg.getJSONObject("fieldMap");
        if (fieldMap == null)
        {
            throw new IllegalStateException("parse_config 缺少 fieldMap 配置");
        }
        int pageStart = cfg.getIntValue("pageStart", 1);
        int maxPage = Math.min(cfg.getIntValue("maxPage", 1), 5);
        int pageSize = cfg.getIntValue("pageSize", 20);
        String pageParam = cfg.getString("pageParam");
        String pageSizeParam = cfg.getString("pageSizeParam");
        String method = StringUtils.isNotEmpty(cfg.getString("method")) ? cfg.getString("method").toLowerCase() : "get";
        JSONObject body = cfg.getJSONObject("body");
        String urlTemplate = cfg.getString("urlTemplate");

        List<BidNotice> result = new ArrayList<>();
        for (int page = pageStart; page < pageStart + maxPage; page++)
        {
            JSONObject params = body == null ? new JSONObject() : new JSONObject(body);
            if (StringUtils.isNotEmpty(pageParam))
            {
                params.put(pageParam, page);
            }
            if (StringUtils.isNotEmpty(pageSizeParam))
            {
                params.put(pageSizeParam, pageSize);
            }
            String text;
            if ("post".equals(method))
            {
                text = httpPostJson(source.getBaseUrl(), params.toJSONString());
            }
            else if ("postform".equals(method))
            {
                Map<String, String> form = new HashMap<>();
                for (String k : params.keySet())
                {
                    form.put(k, params.getString(k));
                }
                text = httpPostForm(source.getBaseUrl(), form);
            }
            else
            {
                StringBuilder url = new StringBuilder(source.getBaseUrl());
                url.append(source.getBaseUrl().contains("?") ? "&" : "?");
                for (String k : params.keySet())
                {
                    url.append(k).append("=").append(params.getString(k)).append("&");
                }
                text = httpGet(url.toString());
            }
            JSONObject root = JSON.parseObject(text);
            JSONArray rows = resolveArray(root, cfg.getString("dataPath"));
            if (rows == null || rows.isEmpty())
            {
                break;
            }
            for (int i = 0; i < rows.size(); i++)
            {
                JSONObject row = rows.getJSONObject(i);
                BidNotice notice = mapNotice(row, fieldMap);
                if (notice != null && StringUtils.isNotEmpty(notice.getTitle()))
                {
                    // 详情级抓取：调详情接口补全正文/截止时间/招标人等字段
                    JSONObject detailCfg = cfg.getJSONObject("detail");
                    if (detailCfg != null)
                    {
                        try
                        {
                            JSONObject detail = fetchDetail(detailCfg, row);
                            if (detail != null)
                            {
                                mergeNotice(notice, detail, detailCfg);
                            }
                        }
                        catch (Exception e)
                        {
                            log.warn("详情抓取失败：title={}, err={}", notice.getTitle(), e.getMessage());
                        }
                    }
                    if (StringUtils.isEmpty(notice.getSourceUrl()) && StringUtils.isNotEmpty(urlTemplate))
                    {
                        notice.setSourceUrl(fillUrlTemplate(urlTemplate, row));
                    }
                    notice.setSourceId(source.getSourceId());
                    notice.setSourceName(source.getSourceName());
                    notice.setInputType("1");
                    result.add(notice);
                }
            }
        }
        return result;
    }

    private BidNotice mapNotice(JSONObject row, JSONObject fieldMap)
    {
        BidNotice notice = new BidNotice();
        notice.setTitle(str(row, fieldMap.getString("title")));
        notice.setSourceUrl(str(row, fieldMap.getString("url")));
        notice.setTenderee(str(row, fieldMap.getString("tenderee")));
        notice.setAgent(str(row, fieldMap.getString("agent")));
        notice.setProjectKey(str(row, fieldMap.getString("projectKey")));
        notice.setContactPerson(str(row, fieldMap.getString("contactPerson")));
        notice.setContactPhone(str(row, fieldMap.getString("contactPhone")));
        notice.setRegionCode(mapRegion(str(row, fieldMap.getString("regionCode"))));
        notice.setIndustry(mapIndustry(str(row, fieldMap.getString("industry"))));
        notice.setNoticeType(mapNoticeType(str(row, fieldMap.getString("noticeType"))));
        // 接口无类型字段时，从标题推断信息类型
        if (StringUtils.isEmpty(notice.getNoticeType()))
        {
            notice.setNoticeType(mapNoticeType(notice.getTitle()));
        }
        String date = str(row, fieldMap.getString("date"));
        notice.setPublishTime(parseDate(date));
        notice.setDeadlineTime(parseDate(str(row, fieldMap.getString("deadlineTime"))));
        notice.setOpenTime(parseDate(str(row, fieldMap.getString("openTime"))));
        String budget = str(row, fieldMap.getString("budget"));
        if (StringUtils.isNotEmpty(budget))
        {
            String num = budget.replaceAll("[^0-9.]", "");
            if (StringUtils.isNotEmpty(num))
            {
                notice.setBudgetAmount(new java.math.BigDecimal(num));
            }
        }
        return notice;
    }

    private String str(JSONObject row, String key)
    {
        if (StringUtils.isEmpty(key))
        {
            return null;
        }
        return row.getString(key);
    }

    /** 调详情接口，返回详情 JSON 对象 */
    private JSONObject fetchDetail(JSONObject detailCfg, JSONObject row) throws Exception
    {
        String url = fillUrlTemplate(detailCfg.getString("url"), row);
        String method = StringUtils.isNotEmpty(detailCfg.getString("method")) ? detailCfg.getString("method").toLowerCase() : "get";
        String text;
        if ("post".equals(method))
        {
            JSONObject body = detailCfg.getJSONObject("body");
            text = httpPostJson(url, body == null ? "{}" : body.toJSONString());
        }
        else if ("postform".equals(method))
        {
            Map<String, String> form = new HashMap<>();
            JSONObject body = detailCfg.getJSONObject("body");
            if (body != null)
            {
                for (String k : body.keySet())
                {
                    form.put(k, body.getString(k));
                }
            }
            text = httpPostForm(url, form);
        }
        else
        {
            text = httpGet(url);
        }
        if (StringUtils.isEmpty(text))
        {
            return null;
        }
        // 详情可能是 JSON 或 HTML 片段
        if (text.trim().startsWith("{"))
        {
            JSONObject root = JSON.parseObject(text);
            String dataPath = detailCfg.getString("dataPath");
            JSONObject detail;
            if (StringUtils.isEmpty(dataPath))
            {
                Object v = root.get("value");
                detail = v instanceof JSONObject ? (JSONObject) v : root;
            }
            else
            {
                Object cur = root;
                for (String seg : dataPath.split("\\."))
                {
                    if (cur instanceof JSONObject)
                    {
                        cur = ((JSONObject) cur).get(seg);
                    }
                }
                detail = cur instanceof JSONObject ? (JSONObject) cur : null;
            }
            return detail;
        }
        // HTML：包装为伪 detail 对象供 regex 提取
        JSONObject wrap = new JSONObject();
        wrap.put("__html__", text);
        return wrap;
    }

    /** 用详情对象补全公告字段（仅覆盖空字段），并支持正则从正文提取 */
    private void mergeNotice(BidNotice notice, JSONObject detail, JSONObject detailCfg)
    {
        JSONObject fieldMap = detailCfg.getJSONObject("fieldMap");
        String html = detail.getString("__html__");
        if (fieldMap != null && html == null)
        {
            // 先用详情字段补空
            if (StringUtils.isEmpty(notice.getTitle())) { notice.setTitle(detail.getString(fieldMap.getString("title"))); }
            if (StringUtils.isEmpty(notice.getSourceUrl())) { notice.setSourceUrl(detail.getString(fieldMap.getString("url"))); }
            if (notice.getPublishTime() == null) { notice.setPublishTime(parseAnyDate(detail.getString(fieldMap.getString("date")))); }
            if (notice.getDeadlineTime() == null) { notice.setDeadlineTime(parseAnyDate(detail.getString(fieldMap.getString("deadlineTime")))); }
            if (notice.getOpenTime() == null) { notice.setOpenTime(parseAnyDate(detail.getString(fieldMap.getString("openTime")))); }
            if (StringUtils.isEmpty(notice.getTenderee())) { notice.setTenderee(detail.getString(fieldMap.getString("tenderee"))); }
            if (StringUtils.isEmpty(notice.getAgent())) { notice.setAgent(detail.getString(fieldMap.getString("agent"))); }
            if (StringUtils.isEmpty(notice.getProjectKey())) { notice.setProjectKey(detail.getString(fieldMap.getString("projectKey"))); }
            if (StringUtils.isEmpty(notice.getContactPerson())) { notice.setContactPerson(detail.getString(fieldMap.getString("contactPerson"))); }
            if (StringUtils.isEmpty(notice.getContactPhone())) { notice.setContactPhone(detail.getString(fieldMap.getString("contactPhone"))); }
            if (StringUtils.isEmpty(notice.getRegionCode())) { notice.setRegionCode(mapRegion(detail.getString(fieldMap.getString("regionCode")))); }
            if (StringUtils.isEmpty(notice.getIndustry())) { notice.setIndustry(mapIndustry(detail.getString(fieldMap.getString("industry")))); }
            if (StringUtils.isEmpty(notice.getNoticeType())) { notice.setNoticeType(mapNoticeType(detail.getString(fieldMap.getString("noticeType")))); }
            if (StringUtils.isEmpty(notice.getAttachmentUrl())) { notice.setAttachmentUrl(extractAttachments(detail.get("fileList"))); }
            if (StringUtils.isEmpty(notice.getContent()))
            {
                String c = detail.getString(fieldMap.getString("content"));
                if (StringUtils.isNotEmpty(c))
                {
                    // 接口返回的正文多为 HTML，统一转纯文本入库
                    notice.setContent(c.trim().startsWith("<") ? cleanContent(htmlToText(c)) : c);
                }
            }
            if (notice.getBudgetAmount() == null)
            {
                String budget = detail.getString(fieldMap.getString("budget"));
                applyBudget(notice, budget);
            }
        }
        // 详情接口若带正式 url 字段（如 url），优先用于详情链接
        if (StringUtils.isNotEmpty(detail.getString("url")))
        {
            notice.setSourceUrl(detail.getString("url"));
        }
        // 正则提取：对正文纯文本逐字段匹配（只在目标字段为空时填充）
        JSONObject regexCfg = detailCfg.getJSONObject("regex");
        if (regexCfg != null)
        {
            String text = html != null ? html : notice.getContent();
            if (StringUtils.isNotEmpty(text))
            {
                String plain = htmlToText(text);
                // HTML 详情页：把规整后的纯文本同时存入正文（去噪声、键值对分行、节标题分隔）
                if (html != null && StringUtils.isEmpty(notice.getContent()))
                {
                    notice.setContent(StringUtils.substring(cleanContent(plain), 0, 20000));
                }
                for (String field : regexCfg.keySet())
                {
                    String pattern = regexCfg.getString(field);
                    if (StringUtils.isEmpty(pattern))
                    {
                        continue;
                    }
                    applyRegexField(notice, field, plain, pattern);
                }
            }
        }
    }

    private void applyRegexField(BidNotice notice, String field, String plain, String pattern)
    {
        try
        {
            java.util.regex.Matcher m = java.util.regex.Pattern.compile(pattern).matcher(plain);
            if (!m.find())
            {
                return;
            }
            String v = m.groupCount() >= 1 ? m.group(1).trim() : m.group().trim();
            if (StringUtils.isEmpty(v))
            {
                return;
            }
            switch (field)
            {
                case "tenderee": if (StringUtils.isEmpty(notice.getTenderee())) notice.setTenderee(v); break;
                case "agent": if (StringUtils.isEmpty(notice.getAgent())) notice.setAgent(v); break;
                case "budget": if (notice.getBudgetAmount() == null) applyBudget(notice, v); break;
                case "projectKey": if (StringUtils.isEmpty(notice.getProjectKey())) notice.setProjectKey(v); break;
                case "contactPerson": if (StringUtils.isEmpty(notice.getContactPerson())) notice.setContactPerson(v); break;
                case "contactPhone": if (StringUtils.isEmpty(notice.getContactPhone())) notice.setContactPhone(v); break;
                case "title": if (StringUtils.isEmpty(notice.getTitle())) notice.setTitle(v); break;
                case "regionCode": if (StringUtils.isEmpty(notice.getRegionCode())) notice.setRegionCode(mapRegion(v)); break;
                case "industry": if (StringUtils.isEmpty(notice.getIndustry())) notice.setIndustry(mapIndustry(v)); break;
                case "noticeType": if (StringUtils.isEmpty(notice.getNoticeType())) notice.setNoticeType(mapNoticeType(v)); break;
                case "attachmentUrl": if (StringUtils.isEmpty(notice.getAttachmentUrl())) notice.setAttachmentUrl(v); break;
                default:
                    // 时间类字段
                    Date d = parseAnyDate(v);
                    if (d != null)
                    {
                        if ("deadlineTime".equals(field) && notice.getDeadlineTime() == null) { notice.setDeadlineTime(d); }
                        else if ("openTime".equals(field) && notice.getOpenTime() == null) { notice.setOpenTime(d); }
                        else if ("publishTime".equals(field) && notice.getPublishTime() == null) { notice.setPublishTime(d); }
                    }
            }
        }
        catch (Exception ignore)
        {
        }
    }

    private void applyBudget(BidNotice notice, String raw)
    {
        if (StringUtils.isEmpty(raw))
        {
            return;
        }
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("([0-9]+(?:\\.[0-9]+)?)\\s*(万元|万|元|亿)?").matcher(raw);
        if (m.find())
        {
            try
            {
                java.math.BigDecimal v = new java.math.BigDecimal(m.group(1));
                String unit = m.group(2);
                if ("万元".equals(unit) || "万".equals(unit))
                {
                    v = v.multiply(new java.math.BigDecimal("10000"));
                }
                else if ("亿".equals(unit))
                {
                    v = v.multiply(new java.math.BigDecimal("100000000"));
                }
                notice.setBudgetAmount(v);
            }
            catch (Exception ignore)
            {
            }
        }
    }

    /** HTML 转纯文本（保留空格分隔） */
    private String htmlToText(String html)
    {        String t = html.replaceAll("(?i)<(script|style)[^>]*>.*?</\\1>", " ");
        t = t.replaceAll("<br\\s*/?>", "\n").replaceAll("</(tr|p|div|h[1-6])>", "\n");
        t = t.replaceAll("<[^>]+>", " ");
        t = t.replaceAll("&nbsp;?", " ").replaceAll("&amp;", "&").replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"");
        return t.replaceAll("[ \\t]+", " ").replaceAll("\\n\\s*", "\n");
    }

    /** 正文规整：去平台内部噪声（UUID 等）、键值对独立成行、节标题前后空行 */
    private String cleanContent(String text)
    {
        if (StringUtils.isEmpty(text))
        {
            return text;
        }
        // 去独立 UUID / 32 位十六进制噪声（行中或行尾）
        text = text.replaceAll("(?i)\\b[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}\\b", "");
        text = text.replaceAll("(?i)\\b[0-9a-f]{32}\\b", "");
        // 已知字段键：在同行出现第二个"键："前换行，让每个键值对独占一行
        String[] keys = {"招标项目名称", "项目名称", "资金来源", "建设规模", "公共资源交易行业分类", "工程类型",
                "招标方式", "资格审查方式", "招标文件/资格预审文件获取方式", "获取方式", "交易地点",
                "公告性质", "是否对外发布", "公告发布开始时间", "监督部门及联系方式", "备注", "建设单位", "经办人",
                "办公电话", "移动电话", "招标代理机构", "项目负责人", "标段编号", "标段名称", "标段合同估算价",
                "招标文件获取截止时间", "递交投标文件截止时间", "开标地点", "开标方式", "本次招标内容",
                "公告时间", "获取采购文件时间", "响应文件递交地点", "响应文件开启时间", "响应文件开启地点",
                "预算金额", "最高限价", "项目联系人", "项目联系电话", "采购单位", "采购单位地址", "采购单位联系方式",
                "代理机构名称", "代理机构地址", "代理机构联系方式", "行政区域", "合同履约期限", "采购需求",
                "简要规格描述", "监督部门", "开启时间", "截止时间", "开标时间", "投标截止时间", "联系人", "联系电话"};
        StringBuilder sb = new StringBuilder();
        for (String line : text.split("\n"))
        {
            line = line.replaceAll("\\s{2,}", " ").trim();
            if (line.isEmpty())
            {
                continue;
            }
            line = splitKnownKeys(line, keys);
            if (isSectionTitle(line))
            {
                if (sb.length() > 0)
                {
                    sb.append("\n");
                }
                sb.append("【").append(line).append("】\n");
                continue;
            }
            sb.append(line).append("\n");
        }
        return sb.toString().replaceAll("\n{3,}", "\n\n").trim();
    }

    /** 同一行内出现多个已知"键："时拆分为多行 */
    private String splitKnownKeys(String line, String[] keys)
    {
        for (String k : keys)
        {
            String kw = k + "：";
            String kw2 = k + ":";
            int count = 0;
            int idx = -1;
            int from = 0;
            while ((idx = line.indexOf(kw, from)) >= 0 || (idx = line.indexOf(kw2, from)) >= 0)
            {
                count++;
                from = idx + 1;
                if (count >= 2)
                {
                    line = line.substring(0, idx) + "\n" + line.substring(idx);
                    count = 1;
                    from = idx + 2;
                }
            }
        }
        return line.replaceAll("(?m)^\\s+", "");
    }

    /** 节标题判断：无冒号、短文本、已知节名集合 */
    private boolean isSectionTitle(String line)
    {
        if (line == null || line.contains("：") || line.contains(":") || line.length() < 2 || line.length() > 14)
        {
            return false;
        }
        String[] titles = {"招标概况", "招标人与招标代理", "详细公告内容", "公告概要", "项目概况", "相关公告",
                "标段信息", "一、项目基本情况", "二、申请人的资格要求", "三、获取采购文件", "四、响应文件提交",
                "五、响应文件开启", "六、公告期限", "七、其他补充事宜", "详细公告内容", "联系方式", "监督部门"};
        for (String t : titles)
        {
            if (line.equals(t) || line.startsWith(t))
            {
                return true;
            }
        }
        return false;
    }

    /** 附件列表转逗号分隔下载地址串 */
    private String extractAttachments(Object fileList)
    {
        if (!(fileList instanceof com.alibaba.fastjson2.JSONArray))
        {
            return null;
        }
        com.alibaba.fastjson2.JSONArray arr = (com.alibaba.fastjson2.JSONArray) fileList;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.size(); i++)
        {
            com.alibaba.fastjson2.JSONObject f = arr.getJSONObject(i);
            String id = f.getString("id");
            if (StringUtils.isEmpty(id))
            {
                continue;
            }
            if (sb.length() > 0)
            {
                sb.append(",");
            }
            sb.append("https://ggzy.yn.gov.cn/ynggfwpt-home-api/u/file/downloadFile?fileId=").append(id);
        }
        return sb.length() == 0 ? null : sb.toString();
    }

    /** 行业名称映射为字典值 bid_industry */
    private String mapIndustry(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        String n = name.trim();
        // ggzy 平台行业编码（A01房屋建筑 A02市政 A03公路 A07水利 等）
        if ("A01".equalsIgnoreCase(n)) { return "1"; }
        if ("A02".equalsIgnoreCase(n)) { return "2"; }
        if ("A07".equalsIgnoreCase(n)) { return "3"; }
        if (n.contains("房屋建筑") || n.contains("建筑")) { return "1"; }
        if (n.contains("市政")) { return "2"; }
        if (n.contains("水利") || n.contains("水电")) { return "3"; }
        if (n.contains("装修") || n.contains("装饰")) { return "4"; }
        if (n.contains("机电") || n.contains("安装") || n.contains("设备")) { return "5"; }
        if (n.contains("园林") || n.contains("绿化")) { return "6"; }
        return "7";
    }

    /** 公告类型名称映射为字典值 bid_notice_type（1招标 2中标 3更正 4其他 5资格预审） */
    private String mapNoticeType(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        String n = name.trim();
        if (n.contains("资格预审")) { return "5"; }
        if (n.contains("中标") || n.contains("成交") || n.contains("结果")) { return "2"; }
        if (n.contains("变更") || n.contains("更正") || n.contains("澄清") || n.contains("补遗")) { return "3"; }
        if (n.contains("招标") || n.contains("磋商") || n.contains("谈判") || n.contains("询价")) { return "1"; }
        return "4";
    }

    /** 地区名称规范化为"云南+州市(+区县)"层级前缀，便于关键词按前缀/包含匹配 */
    private String mapRegion(String name)
    {
        if (StringUtils.isEmpty(name))
        {
            return null;
        }
        String n = name.trim();
        // ggzy 平台 areaCode（001~017）映射为州市
        java.util.Map<String, String> areaMap = new HashMap<>();
        areaMap.put("001", "云南");
        areaMap.put("002", "云南昆明");
        areaMap.put("003", "云南曲靖");
        areaMap.put("004", "云南玉溪");
        areaMap.put("005", "云南保山");
        areaMap.put("006", "云南昭通");
        areaMap.put("007", "云南丽江");
        areaMap.put("008", "云南普洱");
        areaMap.put("009", "云南临沧");
        areaMap.put("010", "云南德宏");
        areaMap.put("011", "云南怒江");
        areaMap.put("012", "云南迪庆");
        areaMap.put("013", "云南大理");
        areaMap.put("014", "云南楚雄");
        areaMap.put("015", "云南红河");
        areaMap.put("016", "云南文山");
        areaMap.put("017", "云南西双版纳");
        if (areaMap.containsKey(n))
        {
            return areaMap.get(n);
        }
        String[] cities = {"昆明", "曲靖", "玉溪", "保山", "昭通", "丽江", "普洱", "临沧", "楚雄", "红河",
                "文山", "西双版纳", "大理", "德宏", "怒江", "迪庆"};
        for (String c : cities)
        {
            if (n.contains(c))
            {
                return n.startsWith("云南") ? n : "云南" + n;
            }
        }
        if (n.contains("省") || n.contains("云南"))
        {
            return "云南";
        }
        // 区县名（如"师宗县"）：无法确定州市，按"云南+区县"存放，保证"云南"关键词可命中
        return n.length() > 10 ? null : "云南" + n;
    }

    /** 支持 yyyyMMddHHmmss 等无分隔格式 */
    private Date parseDateTime(String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        String v = s.trim().replaceAll("[^0-9]", "");
        if (v.length() == 14)
        {
            try
            {
                return new java.text.SimpleDateFormat("yyyyMMddHHmmss").parse(v);
            }
            catch (Exception ignore) { }
        }
        return null;
    }

    /** 综合日期解析：先试无分隔格式，再试常规格式 */
    private Date parseAnyDate(String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        Date d = parseDateTime(s);
        if (d != null)
        {
            return d;
        }
        return parseDate(s);
    }

    private java.util.Date parseDate(String s)
    {
        if (StringUtils.isEmpty(s) || "null".equals(s))
        {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "yyyy年MM月dd日 HH:mm", "yyyy年MM月dd日"};
        for (String p : patterns)
        {
            try
            {
                return new java.text.SimpleDateFormat(p).parse(s.trim().replace('T', ' '));
            }
            catch (Exception ignore)
            {
            }
        }
        return null;
    }

    private JSONArray resolveArray(JSONObject root, String path)
    {
        if (StringUtils.isEmpty(path))
        {
            return root.getJSONArray("data");
        }
        Object cur = root;
        for (String seg : path.split("\\."))
        {
            if (cur instanceof JSONObject)
            {
                cur = ((JSONObject) cur).get(seg);
            }
            else
            {
                return null;
            }
        }
        return cur instanceof JSONArray ? (JSONArray) cur : null;
    }

    private String httpGet(String url) throws Exception
    {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; EDOP-BidCrawler/1.0)");
        return readResponse(conn);
    }

    private String httpPostJson(String url, String json) throws Exception
    {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; EDOP-BidCrawler/1.0)");
        conn.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        try (java.io.OutputStream os = conn.getOutputStream())
        {
            os.write(json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String httpPostForm(String url, Map<String, String> form) throws Exception
    {
        java.net.HttpURLConnection conn = (java.net.HttpURLConnection) new java.net.URL(url).openConnection();
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; EDOP-BidCrawler/1.0)");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
        StringBuilder qs = new StringBuilder();
        for (Map.Entry<String, String> e : form.entrySet())
        {
            if (qs.length() > 0)
            {
                qs.append("&");
            }
            qs.append(java.net.URLEncoder.encode(e.getKey(), "UTF-8")).append("=")
                    .append(java.net.URLEncoder.encode(e.getValue() == null ? "" : e.getValue(), "UTF-8"));
        }
        try (java.io.OutputStream os = conn.getOutputStream())
        {
            os.write(qs.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
        return readResponse(conn);
    }

    private String readResponse(java.net.HttpURLConnection conn) throws Exception
    {
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(conn.getInputStream(), java.nio.charset.StandardCharsets.UTF_8)))
        {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null)
            {
                sb.append(line);
            }
            return sb.toString();
        }
        finally
        {
            conn.disconnect();
        }
    }

    private String fillUrlTemplate(String template, JSONObject row)
    {
        String url = template;
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\\{(\\w+)\\}").matcher(template);
        while (m.find())
        {
            String v = row.getString(m.group(1));
            if (v != null)
            {
                url = url.replace(m.group(0), v);
            }
        }
        return url;
    }
}
