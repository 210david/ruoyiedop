package com.ruoyi.mk.service;

import java.util.List;
import com.ruoyi.mk.domain.MkMaterial;

/**
 * 素材库 Service接口
 *
 * @author ruoyi
 */
public interface IMkMaterialService
{
    public List<MkMaterial> selectMaterialList(MkMaterial material);

    public MkMaterial selectMaterialById(Long materialId);

    public int insertMaterial(MkMaterial material);

    public int updateMaterial(MkMaterial material);

    public int deleteMaterialByIds(Long[] materialIds);

    /**
     * 引用次数+1（按素材ID数组）
     */
    public int increaseUseCount(Long[] materialIds);

    /**
     * 引用次数-1（按素材ID数组）
     */
    public int decreaseUseCount(Long[] materialIds);
}
