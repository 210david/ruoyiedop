package com.ruoyi.bid.service.impl;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.bid.domain.BidNotice;
import com.ruoyi.bid.domain.BidProject;
import com.ruoyi.bid.mapper.BidNoticeMapper;
import com.ruoyi.bid.mapper.BidProjectMapper;
import com.ruoyi.bid.service.IBidNoticeService;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 招标公告 Service 实现
 */
@Service
public class BidNoticeServiceImpl implements IBidNoticeService
{
    private static final Logger log = LoggerFactory.getLogger(BidNoticeServiceImpl.class);

    @Autowired
    private BidNoticeMapper bidNoticeMapper;

    @Autowired
    private BidProjectMapper bidProjectMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private AiClient aiClient;

    @Override
    public List<BidNotice> selectNoticeList(BidNotice notice)
    {
        return bidNoticeMapper.selectNoticeList(notice);
    }

    @Override
    public BidNotice selectNoticeById(Long noticeId)
    {
        return bidNoticeMapper.selectNoticeById(noticeId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertNotice(BidNotice notice)
    {
        validate(notice);
        notice.setInputType("0");
        return bidNoticeMapper.insertNotice(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateNotice(BidNotice notice)
    {
        validate(notice);
        if (notice.getNoticeId() == null)
        {
            throw new ServiceException("公告ID不能为空");
        }
        return bidNoticeMapper.updateNotice(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteNoticeByIds(Long[] noticeIds)
    {
        return bidNoticeMapper.deleteNoticeByIds(noticeIds);
    }

    @Override
    public List<BidNotice> selectArchiveByProjectKey(String projectKey)
    {
        if (StringUtils.isEmpty(projectKey))
        {
            throw new ServiceException("项目关联键不能为空");
        }
        return bidNoticeMapper.selectArchiveByProjectKey(projectKey);
    }

    @Override
    public Map<String, Object> aiExtract(Long noticeId)
    {
        BidNotice notice = bidNoticeMapper.selectNoticeById(noticeId);
        if (notice == null)
        {
            throw new ServiceException("公告不存在");
        }
        if (StringUtils.isEmpty(notice.getContent()))
        {
            throw new ServiceException("公告正文为空，无法进行AI抽取");
        }
        String prompt = "你是招投标公告信息抽取助手。请从下面的招标公告正文中抽取结构化字段，"
                + "以JSON对象输出，字段名固定为：deadlineTime（报名/递交截止时间，格式yyyy-MM-dd HH:mm:ss，无法识别填null）、"
                + "openTime（开标时间，同格式）、budgetAmount（预算金额，数字，单位元，无法识别填null）、"
                + "tenderee（招标人名称）、agent（代理机构名称）、contactPerson（联系人）、contactPhone（联系电话）、"
                + "region（项目所在省市，如：云南省曲靖市）。只输出JSON，不要输出任何其他文字。\n\n公告标题："
                + notice.getTitle() + "\n\n公告正文：\n" + truncate(notice.getContent(), 4000);

        AiResult result = aiClient.chat("bid", "notice_extract", prompt, 1024);
        if (result == null || !result.isSuccess())
        {
            String error = result == null ? "AI服务无响应" : result.getError();
            log.warn("公告AI抽取失败：noticeId={}, error={}", noticeId, error);
            throw new ServiceException("AI抽取未成功：" + error + "，请人工核对录入");
        }
        try
        {
            String content = result.getContent().trim();
            // 剥离可能的 markdown 代码块包裹
            content = content.replaceAll("^```(json)?", "").replaceAll("```$", "").trim();
            JSONObject json = JSON.parseObject(content);
            // 组装预览字段（字符串形式，供前端确认；不落库）
            Map<String, Object> preview = new LinkedHashMap<>();
            preview.put("deadlineTime", normalizeNull(json.getString("deadlineTime")));
            preview.put("openTime", normalizeNull(json.getString("openTime")));
            preview.put("budgetAmount", normalizeNull(json.getString("budgetAmount")));
            preview.put("tenderee", normalizeNull(json.getString("tenderee")));
            preview.put("agent", normalizeNull(json.getString("agent")));
            preview.put("contactPerson", normalizeNull(json.getString("contactPerson")));
            preview.put("contactPhone", normalizeNull(json.getString("contactPhone")));
            preview.put("region", normalizeNull(json.getString("region")));
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("noticeId", noticeId);
            resp.put("fields", preview);
            return resp;
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            log.error("公告AI抽取结果解析失败：noticeId={}", noticeId, e);
            throw new ServiceException("AI抽取结果解析失败，请人工核对录入");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int applyAiExtract(Long noticeId, Map<String, String> fields)
    {
        BidNotice notice = bidNoticeMapper.selectNoticeById(noticeId);
        if (notice == null)
        {
            throw new ServiceException("公告不存在");
        }
        if (fields == null || fields.isEmpty())
        {
            throw new ServiceException("未选择任何要回填的字段");
        }
        BidNotice update = new BidNotice();
        update.setNoticeId(noticeId);
        // 已有值不覆盖：仅当字段为空时才采纳 AI 抽取值
        if (fields.containsKey("deadlineTime") && notice.getDeadlineTime() == null)
        {
            update.setDeadlineTime(parseDateField(fields.get("deadlineTime")));
        }
        if (fields.containsKey("openTime") && notice.getOpenTime() == null)
        {
            update.setOpenTime(parseDateField(fields.get("openTime")));
        }
        if (fields.containsKey("budgetAmount") && notice.getBudgetAmount() == null)
        {
            String budget = fields.get("budgetAmount");
            if (StringUtils.isNotEmpty(budget))
            {
                update.setBudgetAmount(new java.math.BigDecimal(budget.replaceAll("[^0-9.]", "")));
            }
        }
        if (fields.containsKey("tenderee") && StringUtils.isEmpty(notice.getTenderee()))
        {
            update.setTenderee(fields.get("tenderee"));
        }
        if (fields.containsKey("agent") && StringUtils.isEmpty(notice.getAgent()))
        {
            update.setAgent(fields.get("agent"));
        }
        if (fields.containsKey("contactPerson") && StringUtils.isEmpty(notice.getContactPerson()))
        {
            update.setContactPerson(fields.get("contactPerson"));
        }
        if (fields.containsKey("contactPhone") && StringUtils.isEmpty(notice.getContactPhone()))
        {
            update.setContactPhone(fields.get("contactPhone"));
        }
        if (fields.containsKey("region") && StringUtils.isEmpty(notice.getRegionCode()))
        {
            update.setRegionCode(fields.get("region"));
        }
        update.setAiExtracted("1");
        return bidNoticeMapper.updateNotice(update);
    }

    /** 解析 AI 抽取的时间字段，空/null 返回 null */
    private Date parseDateField(String value)
    {
        if (StringUtils.isEmpty(value) || "null".equals(value))
        {
            return null;
        }
        return DateUtils.parseDate(value.replace('T', ' '));
    }

    /** 归一化 AI 返回值：null 字符串转为 null */
    private String normalizeNull(String value)
    {
        return (value == null || "null".equals(value)) ? null : value;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String toProject(Long noticeId)
    {
        BidNotice notice = bidNoticeMapper.selectNoticeById(noticeId);
        if (notice == null)
        {
            throw new ServiceException("公告不存在");
        }
        // 精确防重：仅同公告已存在未删除的投标项目时拒绝
        BidProject existingProject = bidProjectMapper.selectActiveProjectByNoticeId(noticeId);
        if (existingProject != null)
        {
            throw new ServiceException("该公告已登记投标项目（" + existingProject.getBidNo() + "），请勿重复登记");
        }
        BidProject project = new BidProject();
        project.setNoticeId(noticeId);
        project.setProjectName(truncate(notice.getTitle(), 290));
        project.setTenderee(notice.getTenderee());
        project.setAgent(notice.getAgent());
        project.setRegionCode(notice.getRegionCode());
        project.setIndustry(notice.getIndustry());
        project.setBudgetAmount(notice.getBudgetAmount());
        project.setStatus("1");
        project.setApplyDeadline(notice.getDeadlineTime());
        project.setOpenTime(notice.getOpenTime());
        String bidNo = mkNumberRuleService.generateNumber("bid_project");
        project.setBidNo(bidNo);
        bidProjectMapper.insertProject(project);
        // 默认进度节点
        String[] types = {"1", "2", "3", "4", "5", "6", "7", "8"};
        int sort = 1;
        for (String type : types)
        {
            com.ruoyi.bid.domain.BidProjectNode node = new com.ruoyi.bid.domain.BidProjectNode();
            node.setProjectId(project.getProjectId());
            node.setNodeType(type);
            node.setSortOrder(sort++);
            node.setFinishFlag("0");
            bidProjectMapper.insertNode(node);
        }
        return bidNo;
    }

    private void validate(BidNotice notice)
    {
        if (StringUtils.isEmpty(notice.getTitle()))
        {
            throw new ServiceException("公告标题不能为空");
        }
        // 日期先后校验：截止时间须晚于发布时间
        if (notice.getPublishTime() != null && notice.getDeadlineTime() != null
                && !notice.getDeadlineTime().after(notice.getPublishTime()))
        {
            throw new ServiceException("截止时间必须晚于发布时间");
        }
        if (notice.getOpenTime() != null && notice.getDeadlineTime() != null
                && !notice.getOpenTime().after(notice.getDeadlineTime()))
        {
            throw new ServiceException("开标时间必须晚于截止时间");
        }
    }

    private String truncate(String s, int max)
    {
        if (s == null)
        {
            return "";
        }
        return s.length() <= max ? s : s.substring(0, max);
    }
}
