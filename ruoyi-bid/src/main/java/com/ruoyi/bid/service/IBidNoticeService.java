package com.ruoyi.bid.service;

import java.util.List;
import java.util.Map;

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

    /** AI 抽取公告结构化字段（仅预览，不落库），返回抽取字段与当前值对比 */
    public Map<String, Object> aiExtract(Long noticeId);

    /** AI 抽取结果确认回填：仅更新用户采纳的字段，已有值不覆盖 */
    public int applyAiExtract(Long noticeId, Map<String, String> fields);

    /** 公告一键转投标项目，返回投标编号 */
    public String toProject(Long noticeId);
}
