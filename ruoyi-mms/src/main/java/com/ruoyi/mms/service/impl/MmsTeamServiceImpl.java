package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsTeam;
import com.ruoyi.mms.mapper.MmsTeamMapper;
import com.ruoyi.mms.service.IMmsTeamService;

/**
 * 班组 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsTeamServiceImpl implements IMmsTeamService
{
    @Autowired
    private MmsTeamMapper teamMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsTeam> selectTeamList(MmsTeam team)
    {
        return teamMapper.selectTeamList(team);
    }

    @Override
    public MmsTeam selectTeamById(Long TeamId)
    {
        return teamMapper.selectTeamById(TeamId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTeam(MmsTeam team)
    {
        // 班组名称必填校验
        if (StringUtils.isEmpty(team.getTeamName()))
        {
            throw new ServiceException("班组名称不能为空");
        }
        // 产能单元必填校验
        if (team.getResourceId() == null)
        {
            throw new ServiceException("产能单元不能为空");
        }
        // 班组长必填校验
        if (StringUtils.isEmpty(team.getLeader()))
        {
            throw new ServiceException("班组长不能为空");
        }
        // 联系电话必填校验
        if (StringUtils.isEmpty(team.getPhone()))
        {
            throw new ServiceException("联系电话不能为空");
        }
        // 班组人数必填校验
        if (team.getMemberCount() == null)
        {
            throw new ServiceException("班组人数不能为空");
        }
        // 班组名称唯一性校验
        MmsTeam query = new MmsTeam();
        query.setTeamName(team.getTeamName());
        List<MmsTeam> existing = teamMapper.selectTeamList(query);
        if (existing != null && !existing.isEmpty())
        {
            throw new ServiceException("班组名称'" + team.getTeamName() + "'已存在");
        }
        // 状态默认已启用
        if (StringUtils.isEmpty(team.getStatus()))
        {
            team.setStatus("0");
        }
        // 自动生成班组编号（通过编号规则 mms_team 生成）
        if (StringUtils.isEmpty(team.getTeamCode()))
        {
            team.setTeamCode(mkNumberRuleService.generateNumber("mms_team"));
        }
        team.setDelFlag("0");
        team.setCreateBy(SecurityUtils.getUsername());
        team.setCreateTime(DateUtils.getNowDate());
        team.setUpdateBy(SecurityUtils.getUsername());
        team.setUpdateTime(DateUtils.getNowDate());
        return teamMapper.insertTeam(team);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateTeam(MmsTeam team)
    {
        // 班组名称必填校验
        if (StringUtils.isEmpty(team.getTeamName()))
        {
            throw new ServiceException("班组名称不能为空");
        }
        // 产能单元必填校验
        if (team.getResourceId() == null)
        {
            throw new ServiceException("产能单元不能为空");
        }
        // 班组长必填校验
        if (StringUtils.isEmpty(team.getLeader()))
        {
            throw new ServiceException("班组长不能为空");
        }
        // 联系电话必填校验
        if (StringUtils.isEmpty(team.getPhone()))
        {
            throw new ServiceException("联系电话不能为空");
        }
        // 班组人数必填校验
        if (team.getMemberCount() == null)
        {
            throw new ServiceException("班组人数不能为空");
        }
        // 班组名称唯一性校验（排除自身）
        if (StringUtils.isNotEmpty(team.getTeamName()))
        {
            MmsTeam query = new MmsTeam();
            query.setTeamName(team.getTeamName());
            List<MmsTeam> existing = teamMapper.selectTeamList(query);
            if (existing != null)
            {
                for (MmsTeam item : existing)
                {
                    if (!item.getTeamId().equals(team.getTeamId()))
                    {
                        throw new ServiceException("班组名称'" + team.getTeamName() + "'已存在");
                    }
                }
            }
        }
        team.setUpdateBy(SecurityUtils.getUsername());
        team.setUpdateTime(DateUtils.getNowDate());
        return teamMapper.updateTeam(team);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteTeamByIds(Long[] TeamIds)
    {
        return teamMapper.deleteTeamByIds(TeamIds);
    }
}
