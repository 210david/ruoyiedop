package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.domain.MmsIssueDetail;

public interface MmsIssueMapper {
    public List<MmsIssue> selectIssueList(MmsIssue issue);
    public MmsIssue selectIssueById(Long issueId);
    public int insertIssue(MmsIssue issue);
    public int updateIssue(MmsIssue issue);
    public int deleteIssueByIds(Long[] issueIds);

    /** 查询领料明细列表 */
    public List<MmsIssueDetail> selectIssueDetailByIssueId(Long issueId);
    /** 新增领料明细 */
    public int insertIssueDetail(MmsIssueDetail detail);
    /** 删除领料明细（按领料单ID） */
    public int deleteIssueDetailByIssueId(Long issueId);
}
