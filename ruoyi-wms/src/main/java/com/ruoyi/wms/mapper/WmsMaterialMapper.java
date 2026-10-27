package com.ruoyi.wms.mapper;

import java.util.List;
import com.ruoyi.wms.domain.WmsMaterial;

/**
 * 物料管理Mapper接口
 *
 * @author ruoyi
 */
public interface WmsMaterialMapper
{
    public WmsMaterial selectMaterialByCode(String materialCode);

    public List<WmsMaterial> selectMaterialList(WmsMaterial material);

    public WmsMaterial selectMaterialById(Long materialId);

    public int insertMaterial(WmsMaterial material);

    public int updateMaterial(WmsMaterial material);

    public int deleteMaterialByIds(Long[] materialIds);

}
