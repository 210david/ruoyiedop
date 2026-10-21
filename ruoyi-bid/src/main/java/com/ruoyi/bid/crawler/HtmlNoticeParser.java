package com.ruoyi.bid.crawler;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.common.utils.StringUtils;

/**
 * 通用 HTML 页面解析器（Jsoup）
 *
 * parse_config JSON 结构示例：
 * {
 *   "listSelector": "ul.news-list li",
 *   "fields": { "title": "a.title", "url": "a.title@href", "date": "span.date", "budget": "span.money" },
 *   "pageParam": "page", "pageStart": 1, "maxPage": 2, "dateFormat": "yyyy-MM-dd",
 *   "absoluteUrl": true
 * }
 */
@Component
public class HtmlNoticeParser implements NoticeParser
{
    private static final Logger log = LoggerFactory.getLogger(HtmlNoticeParser.class);

    private static final String UA = "Mozilla/5.0 (compatible; EDOP-BidCrawler/1.0; +respect robots.txt)";

    @Override
    public boolean supports(String parserCode)
    {
        return "common-html".equals(parserCode);
    }

    @Override
    public List<BidNotice> parse(BidCrawlSource source) throws Exception
    {
        if (StringUtils.isEmpty(source.getParseConfig()))
        {
            throw new IllegalStateException("采集源[" + source.getSourceName() + "]未配置解析配置 parse_config");
        }
        JSONObject cfg = JSONObject.parseObject(source.getParseConfig());
        String listSelector = cfg.getString("listSelector");
        JSONObject fields = cfg.getJSONObject("fields");
        if (StringUtils.isEmpty(listSelector) || fields == null)
        {
            throw new IllegalStateException("parse_config 缺少 listSelector 或 fields 配置");
        }
        int pageStart = cfg.getIntValue("pageStart", 1);
        int maxPage = Math.min(cfg.getIntValue("maxPage", 1), 5);
        String pageParam = cfg.getString("pageParam");
        boolean absoluteUrl = cfg.getBooleanValue("absoluteUrl", true);

        List<BidNotice> result = new ArrayList<>();
        for (int page = pageStart; page < pageStart + maxPage; page++)
        {
            String url = source.getBaseUrl();
            if (StringUtils.isNotEmpty(pageParam))
            {
                url = url + (url.contains("?") ? "&" : "?") + pageParam + "=" + page;
            }
            Document doc = Jsoup.connect(url)
                    .userAgent(UA)
                    .timeout(15000)
                    .referrer(url)
                    .get();
            Elements items = doc.select(listSelector);
            for (Element item : items)
            {
                try
                {
                    BidNotice notice = buildNotice(item, fields, absoluteUrl ? url : null);
                    if (notice != null && StringUtils.isNotEmpty(notice.getTitle()))
                    {
                        notice.setSourceId(source.getSourceId());
                        notice.setSourceName(source.getSourceName());
                        notice.setInputType("1");
                        result.add(notice);
                    }
                }
                catch (Exception e)
                {
                    log.warn("HTML解析单条公告失败：source={}, err={}", source.getSourceName(), e.getMessage());
                }
            }
            // 单条列表页解析不到数据即停止翻页
            if (items.isEmpty())
            {
                break;
            }
        }
        return result;
    }

    private BidNotice buildNotice(Element item, JSONObject fields, String baseUrl)
    {
        BidNotice notice = new BidNotice();
        notice.setTitle(text(item, fields.getString("title")));
        String url = text(item, fields.getString("url"));
        if (StringUtils.isNotEmpty(url))
        {
            if (baseUrl != null && url.startsWith("/"))
            {
                try
                {
                    url = new java.net.URL(baseUrl).toURI().resolve(url).toString();
                }
                catch (Exception ignore)
                {
                }
            }
            notice.setSourceUrl(url);
        }
        String date = text(item, fields.getString("date"));
        if (StringUtils.isNotEmpty(date))
        {
            java.util.Date d = parseDate(date);
            if (d != null)
            {
                notice.setPublishTime(d);
            }
        }
        String budget = text(item, fields.getString("budget"));
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

    private String text(Element item, String selector)
    {
        if (StringUtils.isEmpty(selector))
        {
            return null;
        }
        if (selector.endsWith("@href"))
        {
            Element el = item.selectFirst(selector.substring(0, selector.length() - 5));
            return el == null ? null : el.attr("href");
        }
        Element el = item.selectFirst(selector);
        return el == null ? null : el.text().trim();
    }

    private java.util.Date parseDate(String s)
    {
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd", "yyyy年MM月dd日"};
        for (String p : patterns)
        {
            try
            {
                return new java.text.SimpleDateFormat(p).parse(s.trim());
            }
            catch (Exception ignore)
            {
            }
        }
        return null;
    }
}
