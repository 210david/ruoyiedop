package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.mapper.MmsIssueMapper;
import com.ruoyi.mms.service.IMmsIssueService;

/**
 * 领料 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsIssueServiceImpl implements IMmsIssueService
{
    @Autowired
    private MmsIssueMapper issueMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsIssue> selectIssueList(MmsIssue issue)
    {
        return issueMapper.selectIssueList(issue);
    }

    @Override
    public MmsIssue selectIssueById(Long IssueId)
    {
        return issueMapper.selectIssueById(IssueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertIssue(MmsIssue issue)
    {
        // 自动生成发料单号
        if (StringUtils.isEmpty(issue.getIssueNo()))
        {
            issue.setIssueNo(mkNumberRuleService.generateNumber("mms_issue"));
        }
        issue.setDelFlag("0");
        issue.setCreateBy(SecurityUtils.getUsername());
        issue.setCreateTime(DateUtils.getNowDate());
        return issueMapper.insertIssue(issue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateIssue(MmsIssue issue)
    {
        return issueMapper.updateIssue(issue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIssueByIds(Long[] IssueIds)
    {
        return issueMapper.deleteIssueByIds(IssueIds);
    }
}
