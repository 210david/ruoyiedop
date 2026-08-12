package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsDefectCode;
import com.ruoyi.qms.mapper.QmsDefectCodeMapper;
import com.ruoyi.qms.service.IQmsDefectCodeService;

/**
 * 缺陷代码 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsDefectCodeServiceImpl implements IQmsDefectCodeService
{
    @Autowired
    private QmsDefectCodeMapper qmsDefectCodeMapper;

    @Override
    public List<QmsDefectCode> selectDefectCodeList(QmsDefectCode defectCode)
    {
        return qmsDefectCodeMapper.selectDefectCodeList(defectCode);
    }

    @Override
    public QmsDefectCode selectDefectCodeById(Long defectId)
    {
        return qmsDefectCodeMapper.selectDefectCodeById(defectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDefectCode(QmsDefectCode defectCode)
    {
        QmsDefectCode existing = qmsDefectCodeMapper.selectDefectCodeByCode(defectCode.getDefectCode());
        if (existing != null)
        {
            throw new ServiceException("缺陷代码已存在：" + defectCode.getDefectCode());
        }
        defectCode.setDelFlag("0");
        if (defectCode.getStatus() == null)
        {
            defectCode.setStatus("0");
        }
        if (defectCode.getDefectLevel() == null)
        {
            defectCode.setDefectLevel("3");
        }
        return qmsDefectCodeMapper.insertDefectCode(defectCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDefectCode(QmsDefectCode defectCode)
    {
        return qmsDefectCodeMapper.updateDefectCode(defectCode);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDefectCodeByIds(Long[] defectIds)
    {
        return qmsDefectCodeMapper.deleteDefectCodeByIds(defectIds);
    }
}
