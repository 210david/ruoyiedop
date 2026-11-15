package com.ruoyi.flowable.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.flowable.domain.FlowDraft;

/**
 * 流程草稿 Mapper 接口
 *
 * @author ruoyi
 */
public interface FlowDraftMapper
{
    /** 条件查询草稿列表（按发起人隔离） */
    public List<FlowDraft> selectFlowDraftList(FlowDraft query);

    /** 按主键 + 发起人查询（越权防护） */
    public FlowDraft selectFlowDraftById(@Param("draftId") Long draftId, @Param("startUserId") Long startUserId);

    /** 新增草稿 */
    public int insertFlowDraft(FlowDraft draft);

    /** 更新草稿内容（仅本人可改） */
    public int updateFlowDraft(FlowDraft draft);

    /** 按主键 + 发起人删除 */
    public int deleteFlowDraftByIds(@Param("draftIds") Long[] draftIds, @Param("startUserId") Long startUserId);

    /** 按流程 key + 发起人清理（提交成功后删除该流程下的草稿） */
    public int deleteByUserAndKey(@Param("processKey") String processKey, @Param("startUserId") Long startUserId);
}
