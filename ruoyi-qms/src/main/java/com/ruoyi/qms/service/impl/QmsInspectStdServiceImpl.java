package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsInspectStd;
import com.ruoyi.qms.mapper.QmsInspectStdMapper;
import com.ruoyi.qms.service.IQmsInspectStdService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 检验标准库 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsInspectStdServiceImpl implements IQmsInspectStdService
{
    @Autowired
    private QmsInspectStdMapper qmsInspectStdMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsInspectStd> selectInspectStdList(QmsInspectStd inspectStd)
    {
        return qmsInspectStdMapper.selectInspectStdList(inspectStd);
    }

    @Override
    public QmsInspectStd selectInspectStdById(Long stdId)
    {
        return qmsInspectStdMapper.selectInspectStdById(stdId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertInspectStd(QmsInspectStd inspectStd)
    {
        // 标准编码自动生成
        if (StringUtils.isEmpty(inspectStd.getStdCode()))
        {
            inspectStd.setStdCode(mkNumberRuleService.generateNumber("qms_std"));
        }
        else
        {
            QmsInspectStd existing = qmsInspectStdMapper.selectInspectStdByCode(inspectStd.getStdCode());
            if (existing != null)
            {
                throw new ServiceException("标准编码已存在：" + inspectStd.getStdCode());
            }
        }
        inspectStd.setDelFlag("0");
        if (inspectStd.getStatus() == null)
        {
            inspectStd.setStatus("0");
        }
        if (inspectStd.getJudgeRule() == null)
        {
            inspectStd.setJudgeRule("1");
        }
        return qmsInspectStdMapper.insertInspectStd(inspectStd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectStd(QmsInspectStd inspectStd)
    {
        return qmsInspectStdMapper.updateInspectStd(inspectStd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectStdByIds(Long[] stdIds)
    {
        return qmsInspectStdMapper.deleteInspectStdByIds(stdIds);
    }
}
