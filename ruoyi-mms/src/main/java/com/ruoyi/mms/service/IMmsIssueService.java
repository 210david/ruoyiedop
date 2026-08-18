package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsIssue;

/**
 * 领料 Service接口
 *
 * @author ruoyi
 */
public interface IMmsIssueService
{
    public List<MmsIssue> selectIssueList(MmsIssue issue);
    public MmsIssue selectIssueById(Long IssueId);
    public int insertIssue(MmsIssue issue);
    public int updateIssue(MmsIssue issue);
    public int deleteIssueByIds(Long[] IssueIds);
}
