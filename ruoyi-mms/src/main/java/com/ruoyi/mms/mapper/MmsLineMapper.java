package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsLine;

/**
 * 产线 Mapper
 */
public interface MmsLineMapper
{
    List<MmsLine> selectLineList(MmsLine line);
    MmsLine selectLineById(Long lineId);
    int insertLine(MmsLine line);
    int updateLine(MmsLine line);
    int deleteLineByIds(Long[] lineIds);
}
