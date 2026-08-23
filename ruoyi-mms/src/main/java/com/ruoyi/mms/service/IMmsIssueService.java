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

    /**
     * 确认领料：状态 0(待领料) → 1(已领料)，记录领料时间，可修改领料人和明细数量
     */
    public int confirmIssue(MmsIssue issue);
}
