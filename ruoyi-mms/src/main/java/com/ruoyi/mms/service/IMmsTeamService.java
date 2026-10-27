package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsTeam;

/**
 * 班组 Service接口
 *
 * @author ruoyi
 */
public interface IMmsTeamService
{
    public List<MmsTeam> selectTeamList(MmsTeam team);
    public MmsTeam selectTeamById(Long TeamId);
    public int insertTeam(MmsTeam team);
    public int updateTeam(MmsTeam team);
    public int deleteTeamByIds(Long[] TeamIds);
}
