package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsQc;

public interface MmsQcMapper {
    public List<MmsQc> selectQcList(MmsQc qc);
    public MmsQc selectQcById(Long qcId);
    public int insertQc(MmsQc qc);
    public int updateQc(MmsQc qc);
    public int deleteQcByIds(Long[] qcIds);
}