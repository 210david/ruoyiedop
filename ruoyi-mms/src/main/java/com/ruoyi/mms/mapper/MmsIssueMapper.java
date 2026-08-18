package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsIssue;

public interface MmsIssueMapper {
    public List<MmsIssue> selectIssueList(MmsIssue issue);
    public MmsIssue selectIssueById(Long issueId);
    public int insertIssue(MmsIssue issue);
    public int updateIssue(MmsIssue issue);
    public int deleteIssueByIds(Long[] issueIds);
}