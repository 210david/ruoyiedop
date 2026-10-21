package com.ruoyi.mk.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.mk.domain.MkContent;

/**
 * 内容发布 Service接口
 *
 * @author ruoyi
 */
public interface IMkContentService
{
    public List<MkContent> selectContentList(MkContent content);

    /** 查询主档+内容包集合（回显） */
    public MkContent selectContentById(Long contentId);

    /** 新增主档+内容包（整体保存） */
    public int insertContent(MkContent content);

    /** 修改主档+内容包（子表先删后插），维护素材引用计数 */
    public int updateContent(MkContent content);

    public int deleteContentByIds(Long[] contentIds);

    /** 提交审核：草稿/已驳回 → 待审核 */
    public int submitContent(Long contentId);

    /** 审核：待审核 → 已通过/已驳回 */
    public int auditContent(Long contentId, boolean pass, String auditRemark);

    /** 发布内容包：仅审核通过的主档，回填发布链接+时间 */
    public int publishPackage(Long packageId, String publishUrl);

    /** AI生成各渠道文案草稿：结果仅返回前端填充，不落库 */
    public List<Map<String, Object>> aiGenerate(Long contentId, List<String> channelTypes);
}
