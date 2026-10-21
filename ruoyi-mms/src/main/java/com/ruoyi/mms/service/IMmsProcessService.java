package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsProcess;

/**
 * 工序 Service接口
 *
 * @author ruoyi
 */
public interface IMmsProcessService
{
    public List<MmsProcess> selectProcessList(MmsProcess process);
    public MmsProcess selectProcessById(Long ProcessId);
    public int insertProcess(MmsProcess process);
    public int updateProcess(MmsProcess process);
    public int deleteProcessByIds(Long[] ProcessIds);
}
