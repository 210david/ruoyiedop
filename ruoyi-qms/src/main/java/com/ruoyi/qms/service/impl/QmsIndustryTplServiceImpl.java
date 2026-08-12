package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsIndustryTpl;
import com.ruoyi.qms.mapper.QmsIndustryTplMapper;
import com.ruoyi.qms.service.IQmsIndustryTplService;

/**
 * 行业模板配置 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsIndustryTplServiceImpl implements IQmsIndustryTplService
{
    @Autowired
    private QmsIndustryTplMapper qmsIndustryTplMapper;

    @Override
    public List<QmsIndustryTpl> selectIndustryTplList(QmsIndustryTpl industryTpl)
    {
        return qmsIndustryTplMapper.selectIndustryTplList(industryTpl);
    }

    @Override
    public QmsIndustryTpl selectIndustryTplById(Long tplId)
    {
        return qmsIndustryTplMapper.selectIndustryTplById(tplId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertIndustryTpl(QmsIndustryTpl industryTpl)
    {
        QmsIndustryTpl existing = qmsIndustryTplMapper.selectIndustryTplByCode(industryTpl.getTplCode());
        if (existing != null)
        {
            throw new ServiceException("模板编码已存在：" + industryTpl.getTplCode());
        }
        industryTpl.setDelFlag("0");
        if (industryTpl.getStatus() == null)
        {
            industryTpl.setStatus("0");
        }
        if (industryTpl.getAuditForce() == null)
        {
            industryTpl.setAuditForce("0");
        }
        return qmsIndustryTplMapper.insertIndustryTpl(industryTpl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateIndustryTpl(QmsIndustryTpl industryTpl)
    {
        return qmsIndustryTplMapper.updateIndustryTpl(industryTpl);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIndustryTplByIds(Long[] tplIds)
    {
        return qmsIndustryTplMapper.deleteIndustryTplByIds(tplIds);
    }
}
