package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsReturnMaterial;

/**
 * 退料 Service接口
 *
 * @author ruoyi
 */
public interface IMmsReturnMaterialService
{
    public List<MmsReturnMaterial> selectReturnMaterialList(MmsReturnMaterial returnMaterial);
    public MmsReturnMaterial selectReturnMaterialById(Long ReturnId);
    public int insertReturnMaterial(MmsReturnMaterial returnMaterial);
    public int updateReturnMaterial(MmsReturnMaterial returnMaterial);
    public int deleteReturnMaterialByIds(Long[] ReturnIds);
}
