package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
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

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

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
        // 自动生成工序编码（通过编号规则 mms_process 生成）
        if (StringUtils.isEmpty(process.getProcessCode()))
        {
            process.setProcessCode(mkNumberRuleService.generateNumber("mms_process"));
        }
        process.setCreateBy(SecurityUtils.getUsername());
        process.setCreateTime(DateUtils.getNowDate());
        return processMapper.insertProcess(process);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateProcess(MmsProcess process)
    {
        process.setUpdateBy(SecurityUtils.getUsername());
        return processMapper.updateProcess(process);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteProcessByIds(Long[] ProcessIds)
    {
        return processMapper.deleteProcessByIds(ProcessIds);
    }
}
