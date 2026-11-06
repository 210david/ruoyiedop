package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmChange;

public interface IPjmChangeService
{
    PjmChange selectChangeById(Long changeId);
    List<PjmChange> selectChangeList(PjmChange change);
    int insertChange(PjmChange change);
    int updateChange(PjmChange change);
    int deleteChangeByIds(Long[] changeIds);

    /** 提交审核（草稿→待审） */
    int submit(Long changeId);
    /** 审核通过/驳回 */
    int audit(Long changeId, String status, String auditOpinion);
}
