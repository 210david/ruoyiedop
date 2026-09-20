package com.ruoyi.bid.crawler;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.common.utils.StringUtils;

/**
 * 通用 JSON 数据接口解析器
 *
 * parse_config JSON 结构示例：
 * {
 *   "dataPath": "data.rows",          // 结果数组所在 JSON 路径
 *   "fieldMap": { "title": "name", "url": "detailUrl", "date": "publishDate",
 *                 "tenderee": "owner", "budget": "money", "projectKey": "code",
 *                 "noticeType": "type", "deadlineTime": "endTime" },
 *   "pageParam": "pageNo", "pageSizeParam": "pageSize", "pageSize": 20, "maxPage": 2
 * }
 */
@Component
public class JsonNoticeParser implements NoticeParser
{
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

        List<BidNotice> result = new ArrayList<>();
        for (int page = pageStart; page < pageStart + maxPage; page++)
        {
            StringBuilder url = new StringBuilder(source.getBaseUrl());
            url.append(source.getBaseUrl().contains("?") ? "&" : "?");
            if (StringUtils.isNotEmpty(pageParam))
            {
                url.append(pageParam).append("=").append(page).append("&");
            }
            if (StringUtils.isNotEmpty(pageSizeParam))
            {
                url.append(pageSizeParam).append("=").append(pageSize).append("&");
            }
            String body = httpGet(url.toString());
            JSONObject root = JSON.parseObject(body);
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

    private java.util.Date parseDate(String s)
    {
        if (StringUtils.isEmpty(s) || "null".equals(s))
        {
            return null;
        }
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
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
}
