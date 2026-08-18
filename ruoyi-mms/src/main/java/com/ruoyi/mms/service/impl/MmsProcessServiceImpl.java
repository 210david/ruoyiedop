package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mms.domain.MmsProcess;
import com.ruoyi.mms.mapper.MmsProcessMapper;
import com.ruoyi.mms.service.IMmsProcessService;

/**
 * 工序 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsProcessServiceImpl implements IMmsProcessService
{
    @Autowired
    private MmsProcessMapper processMapper;

    @Override
    public List<MmsProcess> selectProcessList(MmsProcess process)
    {
        return processMapper.selectProcessList(process);
    }

    @Override
    public MmsProcess selectProcessById(Long ProcessId)
    {
        return processMapper.selectProcessById(ProcessId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertProcess(MmsProcess process)
    {
        process.setDelFlag("0");
        return processMapper.insertProcess(process);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProcess(MmsProcess process)
    {
        return processMapper.updateProcess(process);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProcessByIds(Long[] ProcessIds)
    {
        return processMapper.deleteProcessByIds(ProcessIds);
    }
}
