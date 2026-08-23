package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.domain.MmsReturnDetail;
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
        // 新建退料默认状态为待退料
        if (StringUtils.isEmpty(returnMaterial.getStatus()))
        {
            returnMaterial.setStatus("0");
        }

        // 从明细列表中提取首条物料信息到主表（兼容列表显示）
        syncFirstDetailToMain(returnMaterial);

        int rows = returnMaterialMapper.insertReturnMaterial(returnMaterial);

        // 插入明细
        if (returnMaterial.getDetailList() != null && !returnMaterial.getDetailList().isEmpty())
        {
            int seq = 1;
            for (MmsReturnDetail d : returnMaterial.getDetailList())
            {
                d.setReturnId(returnMaterial.getReturnId());
                d.setSeq(seq++);
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                returnMaterialMapper.insertReturnDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateReturnMaterial(MmsReturnMaterial returnMaterial)
    {
        // 从明细列表中提取首条物料信息到主表（兼容列表显示）
        syncFirstDetailToMain(returnMaterial);

        // 先删除旧明细，再插入新明细
        returnMaterialMapper.deleteReturnDetailByReturnId(returnMaterial.getReturnId());
        if (returnMaterial.getDetailList() != null && !returnMaterial.getDetailList().isEmpty())
        {
            int seq = 1;
            for (MmsReturnDetail d : returnMaterial.getDetailList())
            {
                d.setReturnId(returnMaterial.getReturnId());
                d.setSeq(seq++);
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                returnMaterialMapper.insertReturnDetail(d);
            }
        }
        return returnMaterialMapper.updateReturnMaterial(returnMaterial);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteReturnMaterialByIds(Long[] ReturnIds)
    {
        // 软删除明细
        for (Long id : ReturnIds)
        {
            returnMaterialMapper.deleteReturnDetailByReturnId(id);
        }
        return returnMaterialMapper.deleteReturnMaterialByIds(ReturnIds);
    }

    /**
     * 将明细列表中第一条的物料信息同步到主表，兼容列表页显示
     */
    private void syncFirstDetailToMain(MmsReturnMaterial returnMaterial)
    {
        if (returnMaterial.getDetailList() != null && !returnMaterial.getDetailList().isEmpty())
        {
            MmsReturnDetail first = returnMaterial.getDetailList().get(0);
            returnMaterial.setMaterialId(first.getMaterialId());
            returnMaterial.setMaterialCode(first.getMaterialCode());
            returnMaterial.setMaterialName(first.getMaterialName());
            returnMaterial.setSpecModel(first.getSpecModel());
            returnMaterial.setUnit(first.getUnit());

            // 计算总数量
            BigDecimal totalQty = BigDecimal.ZERO;
            for (MmsReturnDetail d : returnMaterial.getDetailList())
            {
                if (d.getReturnQty() != null)
                {
                    totalQty = totalQty.add(d.getReturnQty());
                }
            }
            if (returnMaterial.getDetailList().size() == 1)
            {
                returnMaterial.setReturnQty(first.getReturnQty());
            }
            else
            {
                returnMaterial.setReturnQty(totalQty);
            }
        }
    }
}
