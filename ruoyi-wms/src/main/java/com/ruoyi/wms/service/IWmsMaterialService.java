package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.wms.domain.WmsMaterial;

public interface IWmsMaterialService
{
    public List<WmsMaterial> selectMaterialList(WmsMaterial material);
    public WmsMaterial selectMaterialById(Long materialId);
    public WmsMaterial selectMaterialByCode(String materialCode);
    public int insertMaterial(WmsMaterial material);
    public int updateMaterial(WmsMaterial material);
    public int deleteMaterialByIds(Long[] materialIds);
    public boolean checkMaterialCodeUnique(WmsMaterial material);
}
