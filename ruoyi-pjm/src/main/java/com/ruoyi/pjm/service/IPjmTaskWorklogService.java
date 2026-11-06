package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmTaskWorklog;

public interface IPjmTaskWorklogService
{
    PjmTaskWorklog selectWorklogById(Long worklogId);
    List<PjmTaskWorklog> selectWorklogList(PjmTaskWorklog worklog);
    int insertWorklog(PjmTaskWorklog worklog);
    int updateWorklog(PjmTaskWorklog worklog);
    int deleteWorklogByIds(Long[] worklogIds);
}
