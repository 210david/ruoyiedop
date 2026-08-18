package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.mapper.MmsBomMapper;
import com.ruoyi.mms.service.IMmsBomService;

/**
 * BOM Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsBomServiceImpl implements IMmsBomService
{
    @Autowired
    private MmsBomMapper bomMapper;

    @Override
    public List<MmsBom> selectBomList(MmsBom bom)
    {
        return bomMapper.selectBomList(bom);
    }

    @Override
    public MmsBom selectBomById(Long BomId)
    {
        MmsBom bom = bomMapper.selectBomById(BomId);
        if (bom != null)
        {
            bom.setDetailList(bomMapper.selectBomDetailByBomId(BomId));
        }
        return bom;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertBom(MmsBom bom)
    {
        // 初始化默认值
        bom.setDelFlag("0");
        if (bom.getStatus() == null)
        {
            bom.setStatus("0"); // 草稿
        }
        if (bom.getBaseQty() == null)
        {
            bom.setBaseQty(BigDecimal.ONE);
        }
        // 插入主表
        int rows = bomMapper.insertBom(bom);
        // 插入明细
        insertBomDetails(bom);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateBom(MmsBom bom)
    {
        // 已发布的BOM不允许修改
        MmsBom existing = bomMapper.selectBomById(bom.getBomId());
        if (existing != null && "1".equals(existing.getStatus()))
        {
            throw new ServiceException("已发布的BOM不允许修改，请复制新版本后编辑");
        }
        // 删除旧明细，重新插入
        bomMapper.deleteBomDetailByBomId(bom.getBomId());
        // 更新主表
        int rows = bomMapper.updateBom(bom);
        // 插入新明细
        insertBomDetails(bom);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteBomByIds(Long[] BomIds)
    {
        for (Long bomId : BomIds)
        {
            MmsBom bom = bomMapper.selectBomById(bomId);
            if (bom != null && "1".equals(bom.getStatus()))
            {
                throw new ServiceException("BOM[" + bom.getBomNo() + "]已发布，不允许删除");
            }
        }
        // 删除明细
        for (Long bomId : BomIds)
        {
            bomMapper.deleteBomDetailByBomId(bomId);
        }
        return bomMapper.deleteBomByIds(BomIds);
    }

    @Override
    public List<MmsBomDetail> selectBomDetailByBomId(Long BomId)
    {
        return bomMapper.selectBomDetailByBomId(BomId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishBom(Long bomId)
    {
        MmsBom bom = bomMapper.selectBomById(bomId);
        if (bom == null)
        {
            throw new ServiceException("BOM不存在");
        }
        if (!"0".equals(bom.getStatus()))
        {
            throw new ServiceException("只有草稿状态的BOM才能发布");
        }
        // 校验明细不能为空
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details == null || details.isEmpty())
        {
            throw new ServiceException("BOM明细不能为空，无法发布");
        }
        // 校验同物料同时仅一个已发布版本
        int count = bomMapper.countPublishedByProductId(bom.getProductId());
        if (count > 0)
        {
            throw new ServiceException("产品[" + bom.getProductCode() + "]已存在已发布版本的BOM，请先停用旧版本");
        }
        bom.setStatus("1"); // 已发布
        if (bom.getEffectiveDate() == null)
        {
            bom.setEffectiveDate(DateUtils.getNowDate());
        }
        bom.setUpdateBy(SecurityUtils.getUsername());
        bom.setUpdateTime(DateUtils.getNowDate());
        return bomMapper.updateBom(bom);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MmsBom copyBom(Long bomId)
    {
        MmsBom source = bomMapper.selectBomById(bomId);
        if (source == null)
        {
            throw new ServiceException("源BOM不存在");
        }
        // 复制主表
        MmsBom newBom = new MmsBom();
        newBom.setBomNo(source.getBomNo() + "-COPY");
        newBom.setBomName(source.getBomName());
        newBom.setProductId(source.getProductId());
        newBom.setProductCode(source.getProductCode());
        newBom.setProductName(source.getProductName());
        newBom.setBomType(source.getBomType());
        // 版本号递增
        newBom.setVersion(incrementVersion(source.getVersion()));
        newBom.setBaseQty(source.getBaseQty());
        newBom.setBaseUnit(source.getBaseUnit());
        newBom.setStatus("0"); // 草稿
        newBom.setDelFlag("0");
        newBom.setCreateBy(SecurityUtils.getUsername());
        newBom.setCreateTime(DateUtils.getNowDate());
        newBom.setRemark(source.getRemark());
        bomMapper.insertBom(newBom);
        // 复制明细
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details != null)
        {
            for (MmsBomDetail d : details)
            {
                d.setDetailId(null); // 清空ID，走自增
                d.setBomId(newBom.getBomId());
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                bomMapper.insertBomDetail(d);
            }
        }
        newBom.setDetailList(details);
        return newBom;
    }

    @Override
    public List<MmsBomDetail> selectBomTreeByBomId(Long bomId)
    {
        List<MmsBomDetail> result = new ArrayList<>();
        collectBomDetails(bomId, result, 0, BigDecimal.ONE, 0);
        return result;
    }

    /**
     * 递归收集BOM明细（多层级展开，最深3层）
     */
    private void collectBomDetails(Long bomId, List<MmsBomDetail> result, int level, BigDecimal parentQty, int currentLevel)
    {
        if (currentLevel >= 3)
        {
            return;
        }
        List<MmsBomDetail> details = bomMapper.selectBomDetailByBomId(bomId);
        if (details == null)
        {
            return;
        }
        for (MmsBomDetail d : details)
        {
            // 设置层级标识
            d.setTreeLevel(currentLevel);
            // 计算实际用量（考虑上级用量的传递）
            BigDecimal actualQty = d.getUsageQty() != null ? d.getUsageQty().multiply(parentQty) : BigDecimal.ZERO;
            result.add(d);
            // 如果子项有引用BOM（半成品），递归展开
            if (d.getBomRefId() != null && d.getBomRefId() > 0)
            {
                collectBomDetails(d.getBomRefId(), result, level + 1, actualQty, currentLevel + 1);
            }
        }
    }

    /**
     * 批量插入BOM明细
     */
    private void insertBomDetails(MmsBom bom)
    {
        if (bom.getDetailList() != null && !bom.getDetailList().isEmpty())
        {
            int seq = 10;
            for (MmsBomDetail d : bom.getDetailList())
            {
                d.setBomId(bom.getBomId());
                d.setDelFlag("0");
                if (d.getSeq() == null)
                {
                    d.setSeq(seq);
                    seq += 10;
                }
                if (d.getSupplyType() == null)
                {
                    d.setSupplyType("1"); // 默认直接领料
                }
                if (d.getIsKeyMaterial() == null)
                {
                    d.setIsKeyMaterial("0");
                }
                if (d.getIsPhantom() == null)
                {
                    d.setIsPhantom("0");
                }
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                bomMapper.insertBomDetail(d);
            }
        }
    }

    /**
     * 版本号递增（如 1.0 -> 2.0, 1.2 -> 1.3）
     */
    private String incrementVersion(String version)
    {
        if (version == null || version.isEmpty())
        {
            return "1.0";
        }
        try
        {
            String[] parts = version.split("\\.");
            if (parts.length >= 2)
            {
                int major = Integer.parseInt(parts[0]);
                return (major + 1) + ".0";
            }
            return version + "-COPY";
        }
        catch (NumberFormatException e)
        {
            return version + "-COPY";
        }
    }
}
