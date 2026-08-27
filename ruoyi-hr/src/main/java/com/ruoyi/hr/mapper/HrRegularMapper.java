package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrRegular;

public interface HrRegularMapper {
    public HrRegular selectHrRegularById(Long regularId);
    public List<HrRegular> selectHrRegularList(HrRegular hrRegular);
    public int insertHrRegular(HrRegular hrRegular);
    public int updateHrRegular(HrRegular hrRegular);
    public int deleteHrRegularByIds(Long[] regularIds);
    public int deleteHrRegularById(Long regularId);
}
