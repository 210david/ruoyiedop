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
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.system.service.ISysUserService;

@Service
public class PjmProjectMemberServiceImpl implements IPjmProjectMemberService
{
    @Autowired
    private PjmProjectMemberMapper memberMapper;

    /** 系统用户服务：手动输入姓名时按姓名匹配自动回填 userId/deptId/deptName */
    @Autowired
    private ISysUserService sysUserService;

    @Override
    public PjmProjectMember selectMemberById(Long memberId) { return memberMapper.selectMemberById(memberId); }

    @Override
    public List<PjmProjectMember> selectMemberList(PjmProjectMember member) { return memberMapper.selectMemberList(member); }

    @Override
    @Transactional
    public int insertMember(PjmProjectMember member)
    {
        // ★ 手动输入姓名（未从组织架构选择）时，按姓名匹配系统用户自动回填 userId/deptId/deptName，匹配失败保持仅文本模式
        enrichMemberByUserName(member);
        // 同一项目同一用户不重复（userId 为空时按姓名查重，避免手动输入的姓名重复添加）
        PjmProjectMember dup = new PjmProjectMember();
        dup.setProjectId(member.getProjectId());
        if (member.getUserId() != null)
        {
            dup.setUserId(member.getUserId());
        }
        else
        {
            dup.setUserName(member.getUserName());
        }
        List<PjmProjectMember> exist = memberMapper.selectMemberList(dup);
        if (!exist.isEmpty())
        {
            throw new ServiceException(member.getUserId() != null ? "该用户已是本项目成员" : "姓名「" + member.getUserName() + "」已是本项目成员");
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

    /**
     * 根据成员姓名自动匹配系统用户，回填 userId/deptId/deptName（与项目立项 PjmProjectServiceImpl 保持一致）。
     * 匹配失败保持原值（允许只存姓名文本）。
     */
    private void enrichMemberByUserName(PjmProjectMember m)
    {
        if (m.getUserId() != null || m.getUserName() == null || m.getUserName().isEmpty())
        {
            return;
        }
        try
        {
            SysUser q = new SysUser();
            q.setNickName(m.getUserName().trim());
            List<SysUser> users = sysUserService.selectUserList(q);
            if (users != null && !users.isEmpty())
            {
                SysUser hit = users.stream()
                        .filter(u -> m.getUserName().trim().equals(u.getNickName()))
                        .findFirst().orElse(users.get(0));
                m.setUserId(hit.getUserId());
                if (hit.getDeptId() != null)
                {
                    m.setDeptId(hit.getDeptId());
                    if (hit.getDept() != null && hit.getDept().getDeptName() != null)
                    {
                        m.setDeptName(hit.getDept().getDeptName());
                    }
                }
            }
        }
        catch (Exception ignored)
        {
        }
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
