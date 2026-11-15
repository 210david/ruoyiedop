package com.ruoyi.flowable.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.flowable.domain.FlowDraft;
import com.ruoyi.flowable.mapper.FlowDraftMapper;
import com.ruoyi.flowable.service.IFlowDraftService;

/**
 * 流程草稿 Service 实现
 *
 * 数据归属一律取当前登录人，前端传的 startUserId 不被信任（防越权读写他人草稿）。
 *
 * @author ruoyi
 */
@Service
public class FlowDraftServiceImpl implements IFlowDraftService
{
    @Autowired
    private FlowDraftMapper flowDraftMapper;

    @Override
    public List<FlowDraft> selectMyDraftList(FlowDraft query)
    {
        if (query == null)
        {
            query = new FlowDraft();
        }
        query.setStartUserId(currentUserId());
        return flowDraftMapper.selectFlowDraftList(query);
    }

    @Override
    public FlowDraft selectMyDraftById(Long draftId)
    {
        if (draftId == null)
        {
            throw new ServiceException("草稿ID不能为空");
        }
        FlowDraft draft = flowDraftMapper.selectFlowDraftById(draftId, currentUserId());
        if (draft == null)
        {
            throw new ServiceException("草稿不存在或无权访问");
        }
        return draft;
    }

    @Override
    public Long saveMyDraft(FlowDraft draft)
    {
        if (draft == null || StringUtils.isEmpty(draft.getProcessKey()))
        {
            throw new ServiceException("流程标识不能为空");
        }
        Long userId = currentUserId();
        draft.setStartUserId(userId);
        if (draft.getDraftId() != null)
        {
            // 更新：先校验归属，避免改到别人的草稿
            FlowDraft exist = flowDraftMapper.selectFlowDraftById(draft.getDraftId(), userId);
            if (exist == null)
            {
                throw new ServiceException("草稿不存在或无权修改");
            }
            flowDraftMapper.updateFlowDraft(draft);
            return draft.getDraftId();
        }
        LoginUser loginUser = SecurityUtils.getLoginUser();
        draft.setStartUserName(loginUser.getUsername());
        SysUser user = loginUser.getUser();
        draft.setStartNickName(user != null && StringUtils.isNotEmpty(user.getNickName()) ? user.getNickName() : loginUser.getUsername());
        draft.setDeptId(loginUser.getDeptId());
        flowDraftMapper.insertFlowDraft(draft);
        return draft.getDraftId();
    }

    @Override
    public int deleteMyDrafts(Long[] draftIds)
    {
        if (draftIds == null || draftIds.length == 0)
        {
            return 0;
        }
        return flowDraftMapper.deleteFlowDraftByIds(draftIds, currentUserId());
    }

    private Long currentUserId()
    {
        Long userId = SecurityUtils.getUserId();
        if (userId == null)
        {
            throw new ServiceException("未获取到当前登录用户");
        }
        return userId;
    }
}
