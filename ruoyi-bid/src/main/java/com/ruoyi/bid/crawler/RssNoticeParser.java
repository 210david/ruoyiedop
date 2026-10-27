package com.ruoyi.bid.crawler;

import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.common.utils.StringUtils;

/**
 * RSS/Atom 订阅解析器（兜底支持，部分站点提供 RSS 输出）
 */
@Component
public class RssNoticeParser implements NoticeParser
{
    @Override
    public boolean supports(String parserCode)
    {
        return "rss".equals(parserCode);
    }

    @Override
    public List<BidNotice> parse(BidCrawlSource source) throws Exception
    {
        List<BidNotice> result = new ArrayList<>();
        Document doc = Jsoup.connect(source.getBaseUrl())
                .userAgent("Mozilla/5.0 (compatible; EDOP-BidCrawler/1.0)")
                .timeout(15000)
                .get();
        Elements items = doc.select("item");
        if (items.isEmpty())
        {
            items = doc.select("entry"); // Atom
        }
        for (org.jsoup.nodes.Element item : items)
        {
            String title = text(item, "title");
            if (StringUtils.isEmpty(title))
            {
                continue;
            }
            BidNotice notice = new BidNotice();
            notice.setTitle(title);
            notice.setSourceUrl(text(item, "link"));
            String date = text(item, "pubDate");
            if (StringUtils.isEmpty(date))
            {
                date = text(item, "updated");
            }
            notice.setPublishTime(parseDate(date));
            notice.setSourceId(source.getSourceId());
            notice.setSourceName(source.getSourceName());
            notice.setInputType("1");
            result.add(notice);
        }
        return result;
    }

    private String text(org.jsoup.nodes.Element item, String selector)
    {
        org.jsoup.nodes.Element el = item.selectFirst(selector);
        return el == null ? null : el.text().trim();
    }

    private java.util.Date parseDate(String s)
    {
        if (StringUtils.isEmpty(s))
        {
            return null;
        }
        try
        {
            return new java.text.SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", java.util.Locale.ENGLISH).parse(s.trim());
        }
        catch (Exception e)
        {
            try
            {
                return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(s.trim());
            }
            catch (Exception ignore)
            {
                return null;
            }
        }
    }
}
