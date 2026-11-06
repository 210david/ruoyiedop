package com.ruoyi.pjm.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.pjm.domain.PjmProjectMember;
import com.ruoyi.pjm.mapper.PjmProjectMemberMapper;
import com.ruoyi.pjm.service.IPjmProjectMemberService;

@Service
public class PjmProjectMemberServiceImpl implements IPjmProjectMemberService
{
    @Autowired
    private PjmProjectMemberMapper memberMapper;

    @Override
    public PjmProjectMember selectMemberById(Long memberId) { return memberMapper.selectMemberById(memberId); }

    @Override
    public List<PjmProjectMember> selectMemberList(PjmProjectMember member) { return memberMapper.selectMemberList(member); }

    @Override
    @Transactional
    public int insertMember(PjmProjectMember member)
    {
        // 同一项目同一用户不重复
        PjmProjectMember dup = new PjmProjectMember();
        dup.setProjectId(member.getProjectId());
        dup.setUserId(member.getUserId());
        List<PjmProjectMember> exist = memberMapper.selectMemberList(dup);
        if (!exist.isEmpty())
        {
            throw new ServiceException("该用户已是本项目成员");
        }
        if (member.getJoinDate() == null) member.setJoinDate(new Date());
        if (member.getRoleType() == null) member.setRoleType("3");
        member.setWorkload(BigDecimal.ZERO);
        member.setTaskCount(0);
        member.setDelFlag("0");
        member.setCreateTime(new Date());
        member.setCreateBy(SecurityUtils.getUsername());
        return memberMapper.insertMember(member);
    }

    @Override
    @Transactional
    public int updateMember(PjmProjectMember member)
    {
        member.setUpdateTime(new Date());
        member.setUpdateBy(SecurityUtils.getUsername());
        return memberMapper.updateMember(member);
    }

    @Override
    @Transactional
    public int deleteMemberByIds(Long[] memberIds) { return memberMapper.deleteMemberByIds(memberIds); }
}
