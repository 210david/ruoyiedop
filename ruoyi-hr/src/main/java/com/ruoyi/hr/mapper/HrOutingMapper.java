package com.ruoyi.hr.mapper;

import java.util.List;
import com.ruoyi.hr.domain.HrOuting;

public interface HrOutingMapper {
    public HrOuting selectHrOutingById(Long outingId);
    public List<HrOuting> selectHrOutingList(HrOuting hrOuting);
    public int insertHrOuting(HrOuting hrOuting);
    public int updateHrOuting(HrOuting hrOuting);
    public int deleteHrOutingByIds(Long[] outingIds);
    public int deleteHrOutingById(Long outingId);
}
