package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmChange;

public interface PjmChangeMapper
{
    PjmChange selectChangeById(Long changeId);
    List<PjmChange> selectChangeList(PjmChange change);
    int insertChange(PjmChange change);
    int updateChange(PjmChange change);
    int deleteChangeByIds(Long[] changeIds);
}
