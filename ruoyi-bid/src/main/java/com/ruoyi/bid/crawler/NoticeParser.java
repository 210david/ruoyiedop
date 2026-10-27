package com.ruoyi.bid.crawler;

import java.util.List;
import com.ruoyi.bid.domain.BidCrawlSource;

/**
 * 公告解析器接口（策略模式）
 * 一个站点一种解析实现；新站点优先用通用解析器 + parse_config 配置，不改框架代码。
 */
public interface NoticeParser
{
    /**
     * 解析采集源列表页，返回待入库公告数据
     *
     * @param source 采集源配置（含 parse_config）
     * @return 解析出的公告列表（字段映射到 BidNotice）
     */
    List<com.ruoyi.bid.domain.BidNotice> parse(BidCrawlSource source) throws Exception;

    /** 是否支持该解析器标识 */
    boolean supports(String parserCode);
}
