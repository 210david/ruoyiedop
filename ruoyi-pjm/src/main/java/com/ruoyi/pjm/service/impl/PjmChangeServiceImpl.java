package com.ruoyi.pjm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmChange;
import com.ruoyi.pjm.mapper.PjmChangeMapper;
import com.ruoyi.pjm.service.IPjmChangeService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class PjmChangeServiceImpl implements IPjmChangeService
{
    @Autowired
    private PjmChangeMapper changeMapper;

    @Autowired
    private IMkNumberRuleService numberRuleService;

    @Override
    public PjmChange selectChangeById(Long changeId) { return changeMapper.selectChangeById(changeId); }

    @Override
    public List<PjmChange> selectChangeList(PjmChange change) { return changeMapper.selectChangeList(change); }

    @Override
    @Transactional
    public int insertChange(PjmChange change)
    {
        if (change.getChangeNo() == null || change.getChangeNo().isEmpty())
        {
            change.setChangeNo(numberRuleService.generateNumber("PJM_CHANGE"));
        }
        if (change.getStatus() == null) change.setStatus("0");
        if (change.getRequesterId() == null) change.setRequesterId(SecurityUtils.getUserId());
        if (change.getRequesterName() == null) change.setRequesterName(SecurityUtils.getUsername());
        change.setDelFlag("0");
        change.setCreateTime(new Date());
        change.setCreateBy(SecurityUtils.getUsername());
        return changeMapper.insertChange(change);
    }

    @Override
    @Transactional
    public int updateChange(PjmChange change)
    {
        PjmChange existing = changeMapper.selectChangeById(change.getChangeId());
        if (existing == null) throw new ServiceException("变更申请不存在");
        if ("1".equals(existing.getStatus()) || "2".equals(existing.getStatus()) || "3".equals(existing.getStatus()))
        {
            throw new ServiceException("待审/已审状态不可修改");
        }
        change.setUpdateTime(new Date());
        change.setUpdateBy(SecurityUtils.getUsername());
        return changeMapper.updateChange(change);
    }

    @Override
    @Transactional
    public int deleteChangeByIds(Long[] changeIds) { return changeMapper.deleteChangeByIds(changeIds); }

    @Override
    @Transactional
    public int submit(Long changeId)
    {
        PjmChange existing = changeMapper.selectChangeById(changeId);
        if (existing == null) throw new ServiceException("变更申请不存在");
        if (!"0".equals(existing.getStatus())) throw new ServiceException("非草稿状态不可提交");
        PjmChange upd = new PjmChange();
        upd.setChangeId(changeId);
        upd.setStatus("1");
        upd.setUpdateTime(new Date());
        upd.setUpdateBy(SecurityUtils.getUsername());
        return changeMapper.updateChange(upd);
    }

    @Override
    @Transactional
    public int audit(Long changeId, String status, String auditOpinion)
    {
        PjmChange existing = changeMapper.selectChangeById(changeId);
        if (existing == null) throw new ServiceException("变更申请不存在");
        if (!"1".equals(existing.getStatus())) throw new ServiceException("非待审状态不可审核");
        if (!"2".equals(status) && !"3".equals(status)) throw new ServiceException("审核状态非法");
        PjmChange upd = new PjmChange();
        upd.setChangeId(changeId);
        upd.setStatus(status);
        upd.setAuditOpinion(auditOpinion);
        upd.setAuditBy(SecurityUtils.getUsername());
        upd.setAuditTime(new Date());
        upd.setUpdateTime(new Date());
        return changeMapper.updateChange(upd);
    }
}
