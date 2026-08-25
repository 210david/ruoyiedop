package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsReturnMaterial;
import com.ruoyi.mms.domain.MmsReturnDetail;
import com.ruoyi.mms.mapper.MmsReturnMaterialMapper;
import com.ruoyi.mms.service.IMmsReturnMaterialService;
import com.ruoyi.mms.domain.MmsIssueDetail;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.service.IMmsIssueService;

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

    @Autowired
    private IMmsIssueService mmsIssueService;

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

        // 校验退料数量不超过可退数量
        validateReturnQty(returnMaterial);

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
     * 确认退料：状态 0(待退料) → 1(已退料)，记录退料时间
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmReturnMaterial(MmsReturnMaterial returnMaterial)
    {
        MmsReturnMaterial existing = returnMaterialMapper.selectReturnMaterialById(returnMaterial.getReturnId());
        if (existing == null)
        {
            throw new ServiceException("退料单不存在或已删除");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("退料单[" + existing.getReturnNo() + "]当前状态为" + statusName(existing.getStatus()) + "，只有待退料状态可确认");
        }
        // 校验退料人必填
        if (StringUtils.isEmpty(returnMaterial.getReturnBy()))
        {
            throw new ServiceException("退料人不能为空");
        }
        // 校验明细
        if (returnMaterial.getDetailList() == null || returnMaterial.getDetailList().isEmpty())
        {
            throw new ServiceException("退料单[" + existing.getReturnNo() + "]没有退料明细，无法确认");
        }
        for (int i = 0; i < returnMaterial.getDetailList().size(); i++)
        {
            MmsReturnDetail d = returnMaterial.getDetailList().get(i);
            if (d.getReturnQty() == null || d.getReturnQty().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (i + 1) + "行退料数量必须大于0");
            }
        }

        // 校验退料数量不超过可退数量（多次退料场景）
        validateReturnQty(returnMaterial);

        // 同步明细数量到主表
        syncFirstDetailToMain(returnMaterial);

        // 先删除旧明细，再插入新明细
        returnMaterialMapper.deleteReturnDetailByReturnId(returnMaterial.getReturnId());
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

        // 保留原有字段，更新状态、退料人、退料时间
        returnMaterial.setReturnNo(existing.getReturnNo());
        returnMaterial.setIssueId(existing.getIssueId());
        returnMaterial.setWorkOrderId(existing.getWorkOrderId());
        returnMaterial.setWorkOrderNo(existing.getWorkOrderNo());
        // 保留物料信息（如前端未传则从已有记录恢复）
        if (returnMaterial.getMaterialId() == null) returnMaterial.setMaterialId(existing.getMaterialId());
        if (StringUtils.isEmpty(returnMaterial.getMaterialCode())) returnMaterial.setMaterialCode(existing.getMaterialCode());
        if (StringUtils.isEmpty(returnMaterial.getMaterialName())) returnMaterial.setMaterialName(existing.getMaterialName());
        if (StringUtils.isEmpty(returnMaterial.getSpecModel())) returnMaterial.setSpecModel(existing.getSpecModel());
        if (StringUtils.isEmpty(returnMaterial.getUnit())) returnMaterial.setUnit(existing.getUnit());
        if (returnMaterial.getReturnQty() == null) returnMaterial.setReturnQty(existing.getReturnQty());
        if (StringUtils.isEmpty(returnMaterial.getReturnReason())) returnMaterial.setReturnReason(existing.getReturnReason());
        returnMaterial.setDelFlag(existing.getDelFlag());
        returnMaterial.setStatus("1");
        returnMaterial.setReturnTime(returnMaterial.getReturnTime() != null ? returnMaterial.getReturnTime() : new Date());
        returnMaterial.setCreateBy(existing.getCreateBy());
        returnMaterial.setCreateTime(existing.getCreateTime());
        returnMaterial.setUpdateBy(SecurityUtils.getUsername());
        returnMaterial.setUpdateTime(DateUtils.getNowDate());
        return returnMaterialMapper.updateReturnMaterial(returnMaterial);
    }

    /**
     * 状态码转中文名
     */
    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待退料";
            case "1": return "已退料";
            default: return "未知(" + status + ")";
        }
    }

    /**
     * 查询某个领料单已退料的明细（按物料汇总退料数量）
     * 只查状态为已退料(1)的退料单中的明细
     */
    @Override
    public List<MmsReturnDetail> selectReturnedQtyByIssueId(Long issueId)
    {
        return returnMaterialMapper.selectReturnedDetailByIssueId(issueId);
    }

    /**
     * 校验退料明细数量不超过可退数量（领料数量-已退数量）
     */
    private void validateReturnQty(MmsReturnMaterial returnMaterial)
    {
        Long issueId = returnMaterial.getIssueId();
        if (issueId == null)
        {
            throw new ServiceException("领料单ID不能为空");
        }
        // 查询领料单详情
        MmsIssue issue = mmsIssueService.selectIssueById(issueId);
        if (issue == null)
        {
            throw new ServiceException("领料单不存在");
        }
        // 查询已退数量
        List<MmsReturnDetail> returnedDetails = selectReturnedQtyByIssueId(issueId);
        java.util.Map<String, BigDecimal> returnedMap = new java.util.HashMap<>();
        if (returnedDetails != null)
        {
            for (MmsReturnDetail rd : returnedDetails)
            {
                if (rd == null || rd.getMaterialCode() == null) continue;
                String key = rd.getMaterialCode();
                BigDecimal qty = rd.getReturnQty() != null ? rd.getReturnQty() : BigDecimal.ZERO;
                BigDecimal existing = returnedMap.get(key);
                returnedMap.put(key, existing == null ? qty : existing.add(qty));
            }
        }
        // 校验每条退料明细
        if (returnMaterial.getDetailList() != null)
        {
            for (int i = 0; i < returnMaterial.getDetailList().size(); i++)
            {
                MmsReturnDetail d = returnMaterial.getDetailList().get(i);
                if (d.getReturnQty() == null || d.getReturnQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("第" + (i + 1) + "行退料数量必须大于0");
                }
                BigDecimal returned = returnedMap.get(d.getMaterialCode());
                if (returned == null) returned = BigDecimal.ZERO;
                BigDecimal available = d.getIssueQty() != null ? d.getIssueQty().subtract(returned) : BigDecimal.ZERO;
                if (available.compareTo(BigDecimal.ZERO) < 0) available = BigDecimal.ZERO;
                if (d.getReturnQty().compareTo(available) > 0)
                {
                    throw new ServiceException("第" + (i + 1) + "行退料数量" + d.getReturnQty()
                        + "超过可退数量" + available + "（领料" + d.getIssueQty() + "-已退" + returned + "）");
                }
            }
        }
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

    @Override
    public List<java.util.Map<String, Object>> selectReturnDetailListByWorkOrderNo(String workOrderNo)
    {
        return returnMaterialMapper.selectReturnDetailListByWorkOrderNo(workOrderNo);
    }
}
