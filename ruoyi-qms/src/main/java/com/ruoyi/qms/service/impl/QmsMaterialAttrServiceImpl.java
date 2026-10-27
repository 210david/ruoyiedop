package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsMaterialAttr;
import com.ruoyi.qms.mapper.QmsMaterialAttrMapper;
import com.ruoyi.qms.service.IQmsMaterialAttrService;

/**
 * 物料质量属性 Service实现
 *
 * @author ruoyi
 */
@Service
public class QmsMaterialAttrServiceImpl implements IQmsMaterialAttrService
{
    @Autowired
    private QmsMaterialAttrMapper qmsMaterialAttrMapper;

    @Override
    public List<QmsMaterialAttr> selectMaterialAttrList(QmsMaterialAttr materialAttr)
    {
        return qmsMaterialAttrMapper.selectMaterialAttrList(materialAttr);
    }

    @Override
    public QmsMaterialAttr selectMaterialAttrById(Long attrId)
    {
        return qmsMaterialAttrMapper.selectMaterialAttrById(attrId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMaterialAttr(QmsMaterialAttr materialAttr)
    {
        QmsMaterialAttr existing = qmsMaterialAttrMapper.selectMaterialAttrByMaterialId(materialAttr.getMaterialId());
        if (existing != null)
        {
            throw new ServiceException("该物料已存在质量属性配置");
        }
        materialAttr.setDelFlag("0");
        if (materialAttr.getStatus() == null)
        {
            materialAttr.setStatus("0");
        }
        if (materialAttr.getIsExempt() == null)
        {
            materialAttr.setIsExempt("0");
        }
        return qmsMaterialAttrMapper.insertMaterialAttr(materialAttr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMaterialAttr(QmsMaterialAttr materialAttr)
    {
        return qmsMaterialAttrMapper.updateMaterialAttr(materialAttr);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMaterialAttrByIds(Long[] attrIds)
    {
        return qmsMaterialAttrMapper.deleteMaterialAttrByIds(attrIds);
    }
}
