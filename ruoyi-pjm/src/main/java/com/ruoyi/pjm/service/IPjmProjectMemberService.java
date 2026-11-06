package com.ruoyi.pjm.service;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectMember;

public interface IPjmProjectMemberService
{
    PjmProjectMember selectMemberById(Long memberId);
    List<PjmProjectMember> selectMemberList(PjmProjectMember member);
    int insertMember(PjmProjectMember member);
    int updateMember(PjmProjectMember member);
    int deleteMemberByIds(Long[] memberIds);
}
