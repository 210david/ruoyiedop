package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsMaterialAttr;

/**
 * 物料质量属性 Service接口
 *
 * @author ruoyi
 */
public interface IQmsMaterialAttrService
{
    public List<QmsMaterialAttr> selectMaterialAttrList(QmsMaterialAttr materialAttr);
    public QmsMaterialAttr selectMaterialAttrById(Long attrId);
    public int insertMaterialAttr(QmsMaterialAttr materialAttr);
    public int updateMaterialAttr(QmsMaterialAttr materialAttr);
    public int deleteMaterialAttrByIds(Long[] attrIds);
}
