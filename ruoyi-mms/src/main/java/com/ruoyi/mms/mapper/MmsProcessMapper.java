package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsProcess;

public interface MmsProcessMapper {
    public List<MmsProcess> selectProcessList(MmsProcess process);
    public MmsProcess selectProcessById(Long processId);
    public int insertProcess(MmsProcess process);
    public int updateProcess(MmsProcess process);
    public int deleteProcessByIds(Long[] processIds);
}