package com.ruoyi.bid.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.bid.crawler.ParserRegistry;
import com.ruoyi.bid.domain.BidCrawlKeyword;
import com.ruoyi.bid.domain.BidCrawlLog;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.bid.domain.BidKeywordScope;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.bid.mapper.BidCrawlKeywordMapper;
import com.ruoyi.bid.mapper.BidCrawlLogMapper;
import com.ruoyi.bid.mapper.BidCrawlSourceMapper;
import com.ruoyi.bid.mapper.BidNoticeMapper;
import com.ruoyi.bid.service.IBidCrawlService;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 采集引擎：抓取 → 去重入库 → 关键词匹配 → 订阅推送
 */
@Service
public class BidCrawlServiceImpl implements IBidCrawlService
{
    private static final Logger log = LoggerFactory.getLogger(BidCrawlServiceImpl.class);

    /** 启用关键词缓存（TTL 5 分钟），避免每条公告逐词查库 */
    private volatile List<BidCrawlKeyword> keywordCache = new ArrayList<>();
    private volatile long keywordCacheTime = 0L;
    private static final long KEYWORD_CACHE_TTL = 5 * 60 * 1000L;

    @Autowired
    private BidCrawlSourceMapper sourceMapper;

    @Autowired
    private BidCrawlKeywordMapper keywordMapper;

    @Autowired
    private BidCrawlLogMapper logMapper;

    @Autowired
    private BidNoticeMapper noticeMapper;

    @Autowired
    private ParserRegistry parserRegistry;

    @Autowired
    private ISysMessageService messageService;

    @Override
    public Long crawlSource(Long sourceId, String operName)
    {
        BidCrawlSource source = sourceMapper.selectSourceById(sourceId);
        if (source == null)
        {
            throw new com.ruoyi.common.exception.ServiceException("采集源不存在：" + sourceId);
        }
        return doCrawl(source, operName);
    }

    @Override
    public int crawlAllSources(String operName)
    {
        List<BidCrawlSource> sources = sourceMapper.selectEnabledSources();
        int count = 0;
        for (BidCrawlSource source : sources)
        {
            try
            {
                doCrawl(source, operName);
                count++;
            }
            catch (Exception e)
            {
                log.error("采集源抓取失败：{}", source.getSourceName(), e);
            }
        }
        return count;
    }

    private Long doCrawl(BidCrawlSource source, String operName)
    {
        Date start = new Date();
        BidCrawlLog crawlLog = new BidCrawlLog();
        crawlLog.setSourceId(source.getSourceId());
        crawlLog.setSourceName(source.getSourceName());
        crawlLog.setStartTime(start);
        int fetched = 0;
        int inserted = 0;
        int matched = 0;
        String status = "0";
        String errorMsg = null;
        try
        {
            List<BidNotice> notices = parserRegistry.getParser(source.getParserCode()).parse(source);
            fetched = notices == null ? 0 : notices.size();
            if (notices != null)
            {
                for (BidNotice notice : notices)
                {
                    // source_url 去重
                    if (StringUtils.isNotEmpty(notice.getSourceUrl())
                            && noticeMapper.selectNoticeByUrl(notice.getSourceUrl()) != null)
                    {
                        continue;
                    }
                    int hit = matchAndPush(notice);
                    noticeMapper.insertNotice(notice);
                    inserted++;
                    if (hit > 0)
                    {
                        matched++;
                    }
                }
            }
            if (fetched > 0 && inserted == 0)
            {
                status = "0"; // 全部重复，仍算成功
            }
        }
        catch (Exception e)
        {
            log.error("采集源执行失败：{}", source.getSourceName(), e);
            status = "2";
            errorMsg = StringUtils.substring(e.getMessage(), 0, 990);
        }
        Date end = new Date();
        crawlLog.setRunStatus(status);
        crawlLog.setFetchedCount(fetched);
        crawlLog.setInsertedCount(inserted);
        crawlLog.setMatchedCount(matched);
        crawlLog.setErrorMsg(errorMsg);
        crawlLog.setEndTime(end);
        logMapper.insertLog(crawlLog);
        // 更新采集源最近执行状态
        BidCrawlSource update = new BidCrawlSource();
        update.setSourceId(source.getSourceId());
        update.setLastRunTime(end);
        update.setLastRunStatus(status);
        update.setLastRunMsg("抓取" + fetched + "条，新增" + inserted + "条，命中" + matched + "条"
                + (errorMsg == null ? "" : "；错误：" + errorMsg));
        sourceMapper.updateSource(update);
        return crawlLog.getLogId();
    }

    @Override
    public int matchAndPush(BidNotice notice)
    {
        List<BidCrawlKeyword> keywords = loadKeywords();
        Set<Long> userSubscribers = new HashSet<>();
        boolean deptBroadcast = false;
        List<String> hitWords = new ArrayList<>();
        for (BidCrawlKeyword k : keywords)
        {
            if (!hit(notice, k))
            {
                continue;
            }
            hitWords.add(k.getKeyword());
            if ("1".equals(k.getNotifyEnabled()))
            {
                deptBroadcast = collectSubscribers(k, userSubscribers) || deptBroadcast;
            }
        }
        if (!hitWords.isEmpty())
        {
            notice.setMatchedKeywords(String.join(",", hitWords));
        }
        if (!userSubscribers.isEmpty() || deptBroadcast)
        {
            String title = "【招标订阅】" + StringUtils.substring(notice.getTitle(), 0, 60);
            StringBuilder content = new StringBuilder();
            content.append("命中关键词：").append(String.join("、", hitWords));
            if (notice.getDeadlineTime() != null)
            {
                content.append("；截止时间：").append(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(notice.getDeadlineTime()));
            }
            content.append("。请及时查看招标公告库。");
            // 1. 用户订阅：定向推送
            for (Long userId : userSubscribers)
            {
                try
                {
                    SysMessage msg = new SysMessage();
                    msg.setMessageTitle(title);
                    msg.setMessageContent(content.toString());
                    msg.setMessageType("2"); // 业务预警
                    msg.setMessageLevel("2");
                    msg.setBizSource("bid");
                    msg.setBizId(notice.getNoticeId());
                    msg.setRecipientId(userId);
                    msg.setStatus("1");
                    messageService.insertMessage(msg);
                }
                catch (Exception e)
                {
                    log.warn("订阅推送失败：user={}, err={}", userId, e.getMessage());
                }
            }
            // 2. 部门订阅：按模块权限串广播（持有 bid:notice:list 权限的用户可见）
            if (deptBroadcast)
            {
                try
                {
                    SysMessage msg = new SysMessage();
                    msg.setMessageTitle(title);
                    msg.setMessageContent(content.toString());
                    msg.setMessageType("2");
                    msg.setMessageLevel("2");
                    msg.setBizSource("bid");
                    msg.setBizId(notice.getNoticeId());
                    msg.setRecipientRoleKey("bid:notice:list");
                    msg.setStatus("1");
                    messageService.insertMessage(msg);
                }
                catch (Exception e)
                {
                    log.warn("部门订阅广播失败：err={}", e.getMessage());
                }
            }
        }
        return hitWords.size();
    }

    private boolean hit(BidNotice notice, BidCrawlKeyword k)
    {
        // 排除词命中即剔除
        for (String ex : splitWords(k.getExcludeWords()))
        {
            if (notice.getTitle() != null && notice.getTitle().contains(ex))
            {
                return false;
            }
        }
        // 主词/同义词命中
        boolean titleHit = containsAny(notice.getTitle(), splitWords(k.getSynonymWords()));
        boolean hit = titleHit;
        if (!hit && "2".equals(k.getMatchType()) && notice.getContent() != null)
        {
            hit = containsAny(notice.getContent(), splitWords(k.getSynonymWords()));
        }
        if (!hit)
        {
            return false;
        }
        // 过滤条件：地区/行业/金额/信息类型
        if (StringUtils.isNotEmpty(k.getRegionCode()) && notice.getRegionCode() != null
                && !notice.getRegionCode().startsWith(k.getRegionCode()))
        {
            return false;
        }
        if (StringUtils.isNotEmpty(k.getIndustry()) && !k.getIndustry().equals(notice.getIndustry()))
        {
            return false;
        }
        if (k.getMinAmount() != null && notice.getBudgetAmount() != null
                && notice.getBudgetAmount().compareTo(k.getMinAmount()) < 0)
        {
            return false;
        }
        if (StringUtils.isNotEmpty(k.getNoticeTypes()) && notice.getNoticeType() != null
                && !java.util.Arrays.asList(k.getNoticeTypes().split(",")).contains(notice.getNoticeType()))
        {
            return false;
        }
        return true;
    }

    private boolean containsAny(String text, List<String> words)
    {
        if (text == null)
        {
            return false;
        }
        for (String w : words)
        {
            if (text.contains(w))
            {
                return true;
            }
        }
        return false;
    }

    private List<String> splitWords(String joined)
    {
        List<String> words = new ArrayList<>();
        if (StringUtils.isNotEmpty(joined))
        {
            for (String w : joined.split("[,，]"))
            {
                if (StringUtils.isNotEmpty(w.trim()))
                {
                    words.add(w.trim());
                }
            }
        }
        return words;
    }

    private boolean collectSubscribers(BidCrawlKeyword k, Set<Long> userSubscribers)
    {
        boolean deptBroadcast = false;
        List<BidKeywordScope> scopes = keywordMapper.selectScopeByKeywordId(k.getKeywordId());
        if (scopes == null)
        {
            return false;
        }
        for (BidKeywordScope scope : scopes)
        {
            if ("1".equals(scope.getScopeType()) && scope.getDeptId() != null)
            {
                // 部门订阅：由消息中心按权限串广播给持有 bid:notice:list 权限的用户
                deptBroadcast = true;
            }
            else if ("2".equals(scope.getScopeType()) && scope.getUserId() != null)
            {
                userSubscribers.add(scope.getUserId());
            }
        }
        return deptBroadcast;
    }

    private List<BidCrawlKeyword> loadKeywords()
    {
        long now = System.currentTimeMillis();
        if (now - keywordCacheTime > KEYWORD_CACHE_TTL)
        {
            synchronized (this)
            {
                if (now - keywordCacheTime > KEYWORD_CACHE_TTL)
                {
                    keywordCache = keywordMapper.selectEnabledKeywords();
                    keywordCacheTime = now;
                }
            }
        }
        return keywordCache;
    }

    @Override
    public Map<String, Object> noticeStats()
    {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", noticeMapper.countAll());
        return stats;
    }

    @Override
    public List<Map<String, Object>> noticeTrend()
    {
        return new ArrayList<>();
    }
}
