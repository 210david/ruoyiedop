package com.ruoyi.wms.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
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

    /**
     * 导入物料数据
     *
     * @param materialList 物料数据列表
     * @param isUpdateSupport 是否更新已存在的数据
     * @param updateKey 更新匹配字段（materialName/materialCode）
     * @param operName 操作人
     * @return 导入结果
     */
    public AjaxResult importMaterial(List<WmsMaterial> materialList, Boolean isUpdateSupport, String updateKey, String operName);
}
