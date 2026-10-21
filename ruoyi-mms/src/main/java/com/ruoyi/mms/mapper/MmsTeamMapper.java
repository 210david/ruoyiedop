package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsTeam;

public interface MmsTeamMapper {
    public List<MmsTeam> selectTeamList(MmsTeam team);
    public MmsTeam selectTeamById(Long teamId);
    public int insertTeam(MmsTeam team);
    public int updateTeam(MmsTeam team);
    public int deleteTeamByIds(Long[] teamIds);
}
