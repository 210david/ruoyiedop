package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsOutsource;

public interface MmsOutsourceMapper
{
    public List<MmsOutsource> selectOutsourceList(MmsOutsource outsource);
    public MmsOutsource selectOutsourceById(Long outsourceId);
    public int insertOutsource(MmsOutsource outsource);
    public int updateOutsource(MmsOutsource outsource);
    public int deleteOutsourceByIds(Long[] outsourceIds);
}
