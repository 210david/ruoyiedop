package com.ruoyi.mms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.mapper.MmsReturnMaterialMapper;
import com.ruoyi.mms.service.IMmsReturnMaterialService;

/**
 * 退料 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsReturnMaterialServiceImpl implements IMmsReturnMaterialService
{
    @Autowired
    private MmsReturnMaterialMapper returnMaterialMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsReturnMaterial> selectReturnMaterialList(MmsReturnMaterial returnMaterial)
    {
        return returnMaterialMapper.selectReturnMaterialList(returnMaterial);
    }

    @Override
    public MmsReturnMaterial selectReturnMaterialById(Long ReturnId)
    {
        return returnMaterialMapper.selectReturnMaterialById(ReturnId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertReturnMaterial(MmsReturnMaterial returnMaterial)
    {
        // 自动生成退料单号
        if (StringUtils.isEmpty(returnMaterial.getReturnNo()))
        {
            returnMaterial.setReturnNo(mkNumberRuleService.generateNumber("mms_return_material"));
        }
        returnMaterial.setDelFlag("0");
        returnMaterial.setCreateBy(SecurityUtils.getUsername());
        returnMaterial.setCreateTime(DateUtils.getNowDate());
        return returnMaterialMapper.insertReturnMaterial(returnMaterial);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReturnMaterial(MmsReturnMaterial returnMaterial)
    {
        return returnMaterialMapper.updateReturnMaterial(returnMaterial);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReturnMaterialByIds(Long[] ReturnIds)
    {
        return returnMaterialMapper.deleteReturnMaterialByIds(ReturnIds);
    }
}
