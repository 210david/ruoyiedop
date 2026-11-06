package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmTaskWorklog;

public interface PjmTaskWorklogMapper
{
    PjmTaskWorklog selectWorklogById(Long worklogId);
    List<PjmTaskWorklog> selectWorklogList(PjmTaskWorklog worklog);
    int insertWorklog(PjmTaskWorklog worklog);
    int updateWorklog(PjmTaskWorklog worklog);
    int deleteWorklogByIds(Long[] worklogIds);

    /** 刷新 WBS 任务实际工时 */
    int refreshTaskActualHours(Long taskId);
}
