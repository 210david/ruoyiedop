package com.ruoyi.mk.mapper;

import java.util.List;
import com.ruoyi.mk.domain.MkMaterial;
import org.apache.ibatis.annotations.Param;

/**
 * 素材 数据层
 *
 * @author ruoyi
 */
public interface MkMaterialMapper
{
    public MkMaterial selectMaterialById(Long materialId);

    public List<MkMaterial> selectMaterialList(MkMaterial material);

    public int insertMaterial(MkMaterial material);

    public int updateMaterial(MkMaterial material);

    public int deleteMaterialByIds(Long[] materialIds);

    /** 批量调整引用次数（delta 正负） */
    public int updateUseCount(@Param("materialIds") Long[] materialIds, @Param("delta") int delta);
}
