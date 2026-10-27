package com.ruoyi.mk.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.domain.MkActivity;
import com.ruoyi.mk.domain.MkContent;
import com.ruoyi.mk.domain.MkContentPackage;
import com.ruoyi.mk.mapper.MkActivityMapper;
import com.ruoyi.mk.mapper.MkContentAuditLogMapper;
import com.ruoyi.mk.mapper.MkContentMapper;
import com.ruoyi.mk.mapper.MkContentPackageMapper;
import com.ruoyi.mk.domain.MkContentAuditLog;
import com.ruoyi.mk.service.IMkContentService;
import com.ruoyi.mk.service.IMkMaterialService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 内容发布 Service业务层处理
 *
 * @author ruoyi
 */
@Service
public class MkContentServiceImpl implements IMkContentService
{
    /** AI 模块/功能标识 */
    private static final String AI_MODULE = "mk";
    private static final String AI_FEATURE = "content_copy";

    @Autowired
    private MkContentMapper mkContentMapper;

    @Autowired
    private MkContentPackageMapper mkContentPackageMapper;

    @Autowired
    private MkContentAuditLogMapper mkContentAuditLogMapper;

    @Autowired
    private MkActivityMapper mkActivityMapper;

    @Autowired
    private IMkMaterialService mkMaterialService;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private AiClient aiClient;

    @Override
    public List<MkContent> selectContentList(MkContent content)
    {
        return mkContentMapper.selectContentList(content);
    }

    @Override
    public MkContent selectContentById(Long contentId)
    {
        MkContent content = mkContentMapper.selectContentById(contentId);
        if (content != null)
        {
            content.setPackageList(mkContentPackageMapper.selectByContentId(contentId));
            content.setAuditLogList(mkContentAuditLogMapper.selectAuditLogByContentId(contentId));
        }
        return content;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertContent(MkContent content)
    {
        if (StringUtils.isEmpty(content.getContentNo()))
        {
            content.setContentNo(mkNumberRuleService.generateNumber("content"));
        }
        if (content.getPlanPublishDate() == null)
        {
            throw new ServiceException("计划发布日期不能为空");
        }
        content.setAuditStatus("0");
        content.setUserId(SecurityUtils.getUserId());
        content.setCreateBy(SecurityUtils.getUsername());
        int rows = mkContentMapper.insertContent(content);
        savePackages(content, null);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateContent(MkContent content)
    {
        MkContent old = mkContentMapper.selectContentById(content.getContentId());
        if (old == null)
        {
            throw new ServiceException("内容不存在");
        }
        if (!"0".equals(old.getAuditStatus()) && !"3".equals(old.getAuditStatus()))
        {
            throw new ServiceException("仅草稿或已驳回的内容可编辑");
        }
        if (content.getPlanPublishDate() == null)
        {
            throw new ServiceException("计划发布日期不能为空");
        }
        content.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkContentMapper.updateContent(content);
        // 子表先删后插，维护素材引用计数
        List<MkContentPackage> oldPackages = mkContentPackageMapper.selectByContentId(content.getContentId());
        Set<Long> oldMaterialIds = extractMaterialIds(oldPackages);
        mkContentPackageMapper.deleteByContentId(content.getContentId());
        savePackages(content, oldMaterialIds);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteContentByIds(Long[] contentIds)
    {
        for (Long contentId : contentIds)
        {
            mkContentPackageMapper.deleteByContentId(contentId);
        }
        return mkContentMapper.deleteContentByIds(contentIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitContent(Long contentId)
    {
        MkContent content = mkContentMapper.selectContentById(contentId);
        if (content == null)
        {
            throw new ServiceException("内容不存在");
        }
        if (!"0".equals(content.getAuditStatus()) && !"3".equals(content.getAuditStatus()))
        {
            throw new ServiceException("仅草稿或已驳回的内容可提交审核");
        }
        MkContent update = new MkContent();
        update.setContentId(contentId);
        update.setAuditStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkContentMapper.updateContent(update);
        // 写入提交流水（提交为中性动作，动作码0，时间线展示"提交审核"）
        MkContentAuditLog log = new MkContentAuditLog();
        log.setContentId(contentId);
        log.setAuditAction("0");
        log.setAuditBy(SecurityUtils.getUsername());
        log.setAuditRemark("提交审核");
        mkContentAuditLogMapper.insertAuditLog(log);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditContent(Long contentId, boolean pass, String auditRemark)
    {
        MkContent content = mkContentMapper.selectContentById(contentId);
        if (content == null)
        {
            throw new ServiceException("内容不存在");
        }
        if (!"1".equals(content.getAuditStatus()))
        {
            throw new ServiceException("仅待审核的内容可审核");
        }
        if (!pass && StringUtils.isEmpty(auditRemark))
        {
            throw new ServiceException("驳回时必须填写审核意见");
        }
        MkContent update = new MkContent();
        update.setContentId(contentId);
        update.setAuditStatus(pass ? "2" : "3");
        update.setAuditUserId(SecurityUtils.getUserId());
        update.setAuditTime(new java.util.Date());
        update.setAuditRemark(auditRemark);
        update.setUpdateBy(SecurityUtils.getUsername());
        int rows = mkContentMapper.updateContent(update);
        // 写入审核日志（通过1/驳回2）
        MkContentAuditLog log = new MkContentAuditLog();
        log.setContentId(contentId);
        log.setAuditAction(pass ? "1" : "2");
        log.setAuditBy(SecurityUtils.getUsername());
        log.setAuditRemark(auditRemark);
        mkContentAuditLogMapper.insertAuditLog(log);
        return rows;
    }

    @Override
    public int publishPackage(Long packageId, String publishUrl)
    {
        MkContentPackage pkg = mkContentPackageMapper.selectContentPackageById(packageId);
        if (pkg == null)
        {
            throw new ServiceException("内容包不存在");
        }
        MkContent content = mkContentMapper.selectContentById(pkg.getContentId());
        if (content == null || !"2".equals(content.getAuditStatus()))
        {
            throw new ServiceException("仅审核通过的内容可发布");
        }
        MkContentPackage update = new MkContentPackage();
        update.setPackageId(packageId);
        update.setPackageStatus("1");
        update.setPublishUrl(publishUrl);
        update.setPublishTime(new java.util.Date());
        update.setUpdateBy(SecurityUtils.getUsername());
        return mkContentPackageMapper.updateContentPackage(update);
    }

    @Override
    public List<Map<String, Object>> aiGenerate(Long contentId, List<String> channelTypes)
    {
        MkContent content = mkContentMapper.selectContentById(contentId);
        if (content == null)
        {
            throw new ServiceException("内容不存在");
        }
        if (channelTypes == null || channelTypes.isEmpty())
        {
            throw new ServiceException("请选择生成渠道");
        }
        if (!aiClient.isEnabled(AI_MODULE, AI_FEATURE))
        {
            throw new ServiceException("AI 文案生成未启用，请在 AI 配置中开启");
        }

        StringBuilder prompt = new StringBuilder();
        prompt.append("你是新媒体运营专家。请为以下营销内容生成适配各平台的文案草稿。\n");
        prompt.append("内容标题：").append(content.getTitle()).append("\n");
        prompt.append("内容类型：").append(convertContentType(content.getContentType())).append("\n");
        if (StringUtils.isNotEmpty(content.getTheme()))
        {
            prompt.append("创作主题/要点：").append(content.getTheme()).append("\n");
        }
        if (content.getActivityId() != null)
        {
            MkActivity activity = mkActivityMapper.selectActivityById(content.getActivityId());
            if (activity != null)
            {
                prompt.append("关联活动：").append(activity.getActivityName());
                if (StringUtils.isNotEmpty(activity.getSummary()))
                {
                    prompt.append("（").append(activity.getSummary()).append("）");
                }
                prompt.append("\n");
            }
        }
        prompt.append("需生成渠道及要求：\n");
        for (String chType : channelTypes)
        {
            prompt.append(platformRequirement(chType));
        }
        prompt.append("输出要求：严格的 JSON 数组，不要任何解释文字和代码围栏，数组每个元素格式：");
        prompt.append("{\"channelType\":\"渠道类型编码\",\"title\":\"标题\",\"body\":\"正文\",\"tags\":\"标签1,标签2\"}。");
        prompt.append("渠道类型编码：0抖音 1视频号 2小红书 3公众号 4官网 5其他。");

        AiResult result = aiClient.chat(AI_MODULE, AI_FEATURE, prompt.toString(), 2000);
        if (result == null || !result.isSuccess())
        {
            throw new ServiceException("AI 生成失败：" + (result != null ? result.getError() : "服务不可用"));
        }

        List<Map<String, Object>> list = parseAiResult(result.getContent());
        if (list.isEmpty())
        {
            throw new ServiceException("AI 未返回有效文案，请稍后重试");
        }
        return list;
    }

    /** 保存内容包集合，维护素材引用计数（新增引用+1，移除引用-1） */
    private void savePackages(MkContent content, Set<Long> oldMaterialIds)
    {
        List<MkContentPackage> packages = content.getPackageList();
        Set<Long> newMaterialIds = extractMaterialIds(packages);
        if (packages == null || packages.isEmpty())
        {
            if (oldMaterialIds != null && !oldMaterialIds.isEmpty())
            {
                mkMaterialService.decreaseUseCount(oldMaterialIds.toArray(new Long[0]));
            }
            return;
        }
        for (MkContentPackage pkg : packages)
        {
            pkg.setContentId(content.getContentId());
            pkg.setCreateBy(SecurityUtils.getUsername());
            mkContentPackageMapper.insertContentPackage(pkg);
        }
        // 引用计数：新增的 +1
        List<Long> added = new ArrayList<>();
        if (oldMaterialIds != null)
        {
            for (Long id : newMaterialIds)
            {
                if (!oldMaterialIds.contains(id))
                {
                    added.add(id);
                }
            }
        }
        else
        {
            added.addAll(newMaterialIds);
        }
        if (!added.isEmpty())
        {
            mkMaterialService.increaseUseCount(added.toArray(new Long[0]));
        }
        // 移除的 -1
        if (oldMaterialIds != null)
        {
            List<Long> removed = oldMaterialIds.stream().filter(id -> !newMaterialIds.contains(id)).collect(Collectors.toList());
            if (!removed.isEmpty())
            {
                mkMaterialService.decreaseUseCount(removed.toArray(new Long[0]));
            }
        }
    }

    /** 提取内容包集合中引用的全部素材ID */
    private Set<Long> extractMaterialIds(List<MkContentPackage> packages)
    {
        Set<Long> ids = new HashSet<>();
        if (packages == null)
        {
            return ids;
        }
        for (MkContentPackage pkg : packages)
        {
            if (StringUtils.isNotEmpty(pkg.getMaterialIds()))
            {
                Arrays.stream(pkg.getMaterialIds().split(","))
                        .map(String::trim).filter(StringUtils::isNotEmpty)
                        .forEach(s -> {
                            try { ids.add(Long.valueOf(s)); } catch (NumberFormatException ignored) {}
                        });
            }
        }
        return ids;
    }

    /** 解析 AI 返回的 JSON 数组（剥离围栏，单包失败跳过，整体失败返回原文报错） */
    private List<Map<String, Object>> parseAiResult(String raw)
    {
        List<Map<String, Object>> list = new ArrayList<>();
        if (StringUtils.isEmpty(raw))
        {
            return list;
        }
        String text = raw.trim();
        // 剥离 markdown 代码围栏
        if (text.startsWith("```"))
        {
            int start = text.indexOf('\n');
            int end = text.lastIndexOf("```");
            if (start > 0 && end > start)
            {
                text = text.substring(start + 1, end).trim();
            }
        }
        // 提取第一个 [ 到最后一个 ] 之间的内容
        int lb = text.indexOf('[');
        int rb = text.lastIndexOf(']');
        if (lb >= 0 && rb > lb)
        {
            text = text.substring(lb, rb + 1);
        }
        try
        {
            JSONArray array = JSON.parseArray(text);
            for (int i = 0; i < array.size(); i++)
            {
                JSONObject obj = array.getJSONObject(i);
                if (obj == null || StringUtils.isEmpty(obj.getString("channelType")))
                {
                    continue;
                }
                Map<String, Object> item = new HashMap<>();
                item.put("channelType", obj.getString("channelType"));
                item.put("title", obj.getString("title"));
                item.put("body", obj.getString("body"));
                item.put("tags", obj.getString("tags"));
                list.add(item);
            }
        }
        catch (Exception ignored)
        {
            // JSON 漂移：整体解析失败，返回空由上层降级提示
        }
        return list;
    }

    private String convertContentType(String contentType)
    {
        switch (contentType == null ? "" : contentType)
        {
            case "0": return "活动宣传";
            case "1": return "产品介绍";
            case "2": return "案例故事";
            case "3": return "干货知识";
            case "4": return "品牌宣传";
            default: return "其他";
        }
    }

    /** 各平台文案要求模板 */
    private String platformRequirement(String channelType)
    {
        switch (channelType == null ? "" : channelType)
        {
            case "0": return "- 抖音(0)：强钩子开场，口语化短句，节奏快，结尾引导关注，附3个话题标签\n";
            case "1": return "- 视频号(1)：简洁务实，突出价值点，引导转发给需要的朋友，附2-3个话题标签\n";
            case "2": return "- 小红书(2)：第一人称分享口吻，适当使用emoji，分段清晰，附5-8个话题标签\n";
            case "3": return "- 公众号(3)：结构化长文框架，有小标题层次，专业理性，开头有导语结尾有行动号召\n";
            case "4": return "- 官网(4)：正式专业，突出产品与服务价值，适合企业官网新闻或活动页文案\n";
            default: return "- 其他(5)：通用风格，简洁清晰\n";
        }
    }
}
