package com.ruoyi.oa.mapper;

import java.util.List;
import com.ruoyi.oa.domain.OaSeal;

public interface OaSealMapper
{
    public OaSeal selectOaSealById(Long sealId);

    public OaSeal selectOaSealByCode(String sealCode);

    public List<OaSeal> selectOaSealList(OaSeal oaSeal);

    public int insertOaSeal(OaSeal oaSeal);

    public int updateOaSeal(OaSeal oaSeal);

    public int deleteOaSealByIds(Long[] sealIds);

    public int deleteOaSealById(Long sealId);
}
