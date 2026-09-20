package com.ruoyi.bid.service;

import java.util.List;
import com.ruoyi.bid.domain.BidNotice;

/**
 * 招标公告 Service 接口
 */
public interface IBidNoticeService
{
    public List<BidNotice> selectNoticeList(BidNotice notice);

    public BidNotice selectNoticeById(Long noticeId);

    public int insertNotice(BidNotice notice);

    public int updateNotice(BidNotice notice);

    public int deleteNoticeByIds(Long[] noticeIds);

    /** 项目档案：按 projectKey 聚合同项目全部公告 */
    public List<BidNotice> selectArchiveByProjectKey(String projectKey);

    /** AI 抽取公告结构化字段 */
    public BidNotice aiExtract(Long noticeId);

    /** 公告一键转投标项目，返回投标编号 */
    public String toProject(Long noticeId);
}
