package com.ruoyi.pjm.mapper;

import java.util.List;
import com.ruoyi.pjm.domain.PjmProjectMember;

public interface PjmProjectMemberMapper
{
    PjmProjectMember selectMemberById(Long memberId);
    List<PjmProjectMember> selectMemberList(PjmProjectMember member);
    /** 按项目查询（物理列出，不过滤 del_flag 由 SQL 处理） */
    List<PjmProjectMember> selectByProjectId(Long projectId);
    int insertMember(PjmProjectMember member);
    int updateMember(PjmProjectMember member);
    int deleteMemberByIds(Long[] memberIds);
    /** 按项目物理删除（用于项目级联重建） */
    int deleteByProjectId(Long projectId);
    /** 刷新成员负载（实际来自工时汇总） */
    int refreshMemberWorkload(Long memberId);
}
