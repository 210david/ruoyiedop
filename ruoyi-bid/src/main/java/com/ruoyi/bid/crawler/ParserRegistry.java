package com.ruoyi.bid.crawler;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.bid.domain.BidCrawlSource;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;

/**
 * 解析器注册中心（parser_code → 实现）
 */
@Component
public class ParserRegistry
{
    private static final Logger log = LoggerFactory.getLogger(ParserRegistry.class);

    @Autowired
    private HtmlNoticeParser htmlNoticeParser;

    @Autowired
    private JsonNoticeParser jsonNoticeParser;

    @Autowired
    private RssNoticeParser rssNoticeParser;

    public NoticeParser getParser(String parserCode)
    {
        if (StringUtils.isEmpty(parserCode))
        {
            throw new ServiceException("采集源未配置解析器标识 parser_code");
        }
        NoticeParser[] parsers = {htmlNoticeParser, jsonNoticeParser, rssNoticeParser};
        for (NoticeParser parser : parsers)
        {
            if (parser.supports(parserCode))
            {
                return parser;
            }
        }
        throw new ServiceException("未找到解析器实现：" + parserCode);
    }
}
