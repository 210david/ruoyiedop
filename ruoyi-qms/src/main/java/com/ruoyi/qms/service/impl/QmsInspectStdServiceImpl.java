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
        checkInspectStd(inspectStd);
        return qmsInspectStdMapper.insertInspectStd(inspectStd);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateInspectStd(QmsInspectStd inspectStd)
    {
        checkInspectStd(inspectStd);
        return qmsInspectStdMapper.updateInspectStd(inspectStd);
    }

    /**
     * 检验标准关键字段与数值关系校验：
     * 必填：检验项目/检验方法/计量单位/判定规则；
     * 区间判定：上下限必填且上限不能小于下限；单值判定：目标值必填
     */
    private void checkInspectStd(QmsInspectStd std)
    {
        if (StringUtils.isEmpty(std.getStdName()))
        {
            throw new ServiceException("检验项目不能为空");
        }
        if (StringUtils.isEmpty(std.getInspectMethod()))
        {
            throw new ServiceException("检验方法不能为空");
        }
        if (StringUtils.isEmpty(std.getUnit()))
        {
            throw new ServiceException("计量单位不能为空");
        }
        String judgeRule = StringUtils.isEmpty(std.getJudgeRule()) ? "1" : std.getJudgeRule();
        if ("1".equals(judgeRule))
        {
            if (std.getValMin() == null || std.getValMax() == null)
            {
                throw new ServiceException("区间判定必须填写标准下限和标准上限");
            }
            if (std.getValMax().compareTo(std.getValMin()) < 0)
            {
                throw new ServiceException("标准上限不能小于标准下限");
            }
        }
        else if ("2".equals(judgeRule))
        {
            if (std.getTargetVal() == null)
            {
                throw new ServiceException("单值判定必须填写目标值");
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteInspectStdByIds(Long[] stdIds)
    {
        return qmsInspectStdMapper.deleteInspectStdByIds(stdIds);
    }
}
