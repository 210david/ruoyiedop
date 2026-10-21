package com.ruoyi.bid.mapper;

import java.util.List;
import com.ruoyi.bid.domain.BidNotice;

/**
 * 招标公告 Mapper 接口
 */
public interface BidNoticeMapper
{
    public List<BidNotice> selectNoticeList(BidNotice notice);

    public BidNotice selectNoticeById(Long noticeId);

    public BidNotice selectNoticeByUrl(String sourceUrl);

    public List<BidNotice> selectArchiveByProjectKey(String projectKey);

    public int insertNotice(BidNotice notice);

    public int updateNotice(BidNotice notice);

    public int deleteNoticeByIds(Long[] noticeIds);

    public int countAll();

    public int countByType(String noticeType);

    /** 看板：按状态/类型分组统计 */
    public List<java.util.Map<String, Object>> selectCountGroupBy(String groupColumn);
}
