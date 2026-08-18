package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsReturnMaterial;

public interface MmsReturnMaterialMapper {
    public List<MmsReturnMaterial> selectReturnMaterialList(MmsReturnMaterial m);
    public MmsReturnMaterial selectReturnMaterialById(Long returnId);
    public int insertReturnMaterial(MmsReturnMaterial m);
    public int updateReturnMaterial(MmsReturnMaterial m);
    public int deleteReturnMaterialByIds(Long[] returnIds);
}