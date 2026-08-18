package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsMps;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsMpsMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.service.IMmsMpsService;

/**
 * 主生产计划 Service实现
 *
 * 计划状态机：
 * 0(草稿) → 1(已确认) → 2(已发布) → 3(已下达)
 *                ↓                       ↓
 *           4(已取消)              4(已取消)
 *
 * @author ruoyi
 */
@Service
public class MmsMpsServiceImpl implements IMmsMpsService
{
    @Autowired
    private MmsMpsMapper mpsMapper;

    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsMps> selectMpsList(MmsMps mps)
    {
        return mpsMapper.selectMpsList(mps);
    }

    @Override
    public MmsMps selectMpsById(Long mpsId)
    {
        return mpsMapper.selectMpsById(mpsId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMps(MmsMps mps)
    {
        // 自动生成计划编号：MPS + yyyyMMdd + 4位
        if (StringUtils.isEmpty(mps.getMpsNo()))
        {
            mps.setMpsNo(mkNumberRuleService.generateNumber("mms_mps"));
        }
        // 默认状态为草稿
        if (StringUtils.isEmpty(mps.getStatus()))
        {
            mps.setStatus("0");
        }
        mps.setDelFlag("0");
        mps.setCreateBy(SecurityUtils.getUsername());
        return mpsMapper.insertMps(mps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMps(MmsMps mps)
    {
        // 校验：已确认及之后状态不允许修改
        MmsMps existing = mpsMapper.selectMpsById(mps.getMpsId());
        if (existing != null && !"0".equals(existing.getStatus()))
        {
            throw new ServiceException("计划[" + existing.getMpsNo() + "]当前状态为" + statusName(existing.getStatus()) + "，不允许修改");
        }
        mps.setUpdateBy(SecurityUtils.getUsername());
        return mpsMapper.updateMps(mps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMpsByIds(Long[] mpsIds)
    {
        // 校验：非草稿/取消状态不允许删除
        for (Long id : mpsIds)
        {
            MpsEntityCheck(id);
        }
        return mpsMapper.deleteMpsByIds(mpsIds);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmMps(Long mpsId)
    {
        MmsMps mps = getAndCheckMps(mpsId);
        // 状态校验：只有草稿(0)可确认
        if (!"0".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]当前状态为" + statusName(mps.getStatus()) + "，只有草稿状态可确认");
        }
        // 校验关键字段
        if (mps.getProductId() == null)
        {
            throw new ServiceException("计划未关联产品，无法确认");
        }
        if (mps.getPlanQty() == null || mps.getPlanQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("计划产量必须大于0");
        }
        mps.setStatus("1");
        mps.setUpdateBy(SecurityUtils.getUsername());
        return mpsMapper.updateMps(mps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int auditMps(Long mpsId, String status, String auditOpinion)
    {
        MmsMps mps = getAndCheckMps(mpsId);
        // 状态校验：只有已确认(1)可审批
        if (!"1".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]当前状态为" + statusName(mps.getStatus()) + "，只有已确认状态可审批");
        }
        // status: "2"=通过(已发布), "0"=驳回(草稿)
        if ("2".equals(status))
        {
            mps.setStatus("2"); // 已发布
        }
        else if ("0".equals(status))
        {
            mps.setStatus("0"); // 驳回到草稿
        }
        else
        {
            throw new ServiceException("无效的审批状态值，2=通过，0=驳回");
        }
        mps.setAuditBy(SecurityUtils.getUsername());
        mps.setAuditTime(new Date());
        mps.setAuditOpinion(auditOpinion);
        mps.setUpdateBy(SecurityUtils.getUsername());
        return mpsMapper.updateMps(mps);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long releaseMps(Long mpsId)
    {
        MmsMps mps = getAndCheckMps(mpsId);
        // 状态校验：只有已发布(2)可下达
        if (!"2".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]当前状态为" + statusName(mps.getStatus()) + "，只有已发布状态可下达");
        }
        // 计划状态流转：2 → 3(已下达)
        mps.setStatus("3");
        mps.setUpdateBy(SecurityUtils.getUsername());
        mpsMapper.updateMps(mps);

        // 自动生成工单
        MmsWorkOrder wo = new MmsWorkOrder();
        wo.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        wo.setOrderType("0"); // 标准生产
        wo.setMpsId(mps.getMpsId());
        wo.setMpsNo(mps.getMpsNo());
        wo.setDemandNo(mps.getDemandNo());
        wo.setProductId(mps.getProductId());
        wo.setProductCode(mps.getProductCode());
        wo.setProductName(mps.getProductName());
        wo.setSpecModel(mps.getSpecModel());
        wo.setUnit(mps.getUnit());
        wo.setPlanQty(mps.getPlanQty());
        wo.setResourceId(mps.getResourceId());
        wo.setResourceName(mps.getResourceName());
        wo.setPlanStart(mps.getPeriodStart());
        wo.setPlanFinish(mps.getPeriodEnd());
        wo.setPriority(mps.getPriority());
        wo.setStatus("0"); // 新建工单
        wo.setFinishedQty(BigDecimal.ZERO);
        wo.setQualifiedQty(BigDecimal.ZERO);
        wo.setDefectQty(BigDecimal.ZERO);
        wo.setDelFlag("0");
        wo.setCreateBy(SecurityUtils.getUsername());
        wo.setRemark("由计划[" + mps.getMpsNo() + "]自动生成");
        workOrderMapper.insertWorkOrder(wo);

        return wo.getWorkOrderId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelMps(Long mpsId, String cancelReason)
    {
        MmsMps mps = getAndCheckMps(mpsId);
        // 状态校验：已下达(3)不允许取消
        if ("3".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]已下达，不能直接取消，请先处理关联工单");
        }
        if ("4".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]已是取消状态");
        }
        mps.setStatus("4");
        mps.setAuditOpinion(cancelReason);
        mps.setUpdateBy(SecurityUtils.getUsername());
        return mpsMapper.updateMps(mps);
    }

    // ========== 私有辅助方法 ==========

    private MmsMps getAndCheckMps(Long mpsId)
    {
        MmsMps mps = mpsMapper.selectMpsById(mpsId);
        if (mps == null)
        {
            throw new ServiceException("生产计划不存在或已删除");
        }
        return mps;
    }

    private void MpsEntityCheck(Long mpsId)
    {
        MmsMps mps = mpsMapper.selectMpsById(mpsId);
        if (mps != null && !"0".equals(mps.getStatus()) && !"4".equals(mps.getStatus()))
        {
            throw new ServiceException("计划[" + mps.getMpsNo() + "]非草稿/取消状态，不允许删除");
        }
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "草稿";
            case "1": return "已确认";
            case "2": return "已发布";
            case "3": return "已下达";
            case "4": return "已取消";
            default: return "未知(" + status + ")";
        }
    }

}
