package com.ruoyi.flowable.service;

import java.util.List;
import com.ruoyi.flowable.domain.FlowDraft;

/**
 * 流程草稿 Service 接口
 *
 * @author ruoyi
 */
public interface IFlowDraftService
{
    /** 当前登录人的草稿列表 */
    public List<FlowDraft> selectMyDraftList(FlowDraft query);

    /** 当前登录人的草稿详情（含表单数据） */
    public FlowDraft selectMyDraftById(Long draftId);

    /** 保存草稿（有 draftId 则更新，否则新增），返回草稿ID */
    public Long saveMyDraft(FlowDraft draft);

    /** 删除草稿（只能删自己的） */
    public int deleteMyDrafts(Long[] draftIds);
}
