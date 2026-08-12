package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsMaterialAttr;

/**
 * 物料质量属性 Mapper
 *
 * @author ruoyi
 */
public interface QmsMaterialAttrMapper
{
    public List<QmsMaterialAttr> selectMaterialAttrList(QmsMaterialAttr materialAttr);
    public QmsMaterialAttr selectMaterialAttrById(Long attrId);
    public QmsMaterialAttr selectMaterialAttrByMaterialId(Long materialId);
    public int insertMaterialAttr(QmsMaterialAttr materialAttr);
    public int updateMaterialAttr(QmsMaterialAttr materialAttr);
    public int deleteMaterialAttrByIds(Long[] attrIds);
}
