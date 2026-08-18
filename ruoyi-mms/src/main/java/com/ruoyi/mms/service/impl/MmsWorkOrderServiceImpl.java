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
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.domain.MmsWorkOrderAuditLog;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.service.IMmsWorkOrderService;

/**
 * 生产工单 Service实现
 *
 * 工单状态机：
 * 0(新建) → 1(已下达) → 2(执行中) → 3(报工中) → 4(待完工质检) → 5(完工入库) → 6(已关闭)
 *               ↓                ↓             ↓
 *           7(已暂停) ⇄ 1     7(已暂停)     8(已作废)
 *
 * @author ruoyi
 */
@Service
public class MmsWorkOrderServiceImpl implements IMmsWorkOrderService
{
    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    // ========== 标准 CRUD ==========

    @Override
    public List<MmsWorkOrder> selectWorkOrderList(MmsWorkOrder workOrder)
    {
        return workOrderMapper.selectWorkOrderList(workOrder);
    }

    @Override
    public MmsWorkOrder selectWorkOrderById(Long workOrderId)
    {
        return workOrderMapper.selectWorkOrderById(workOrderId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertWorkOrder(MmsWorkOrder workOrder)
    {
        // 新建工单默认状态为0(新建)
        if (StringUtils.isEmpty(workOrder.getStatus()))
        {
            workOrder.setStatus("0");
        }
        workOrder.setDelFlag("0");
        // 自动生成工单号（通过编号规则 mms_work_order 生成）
        if (StringUtils.isEmpty(workOrder.getWorkOrderNo()))
        {
            workOrder.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        }
        // 初始化数量字段
        if (workOrder.getFinishedQty() == null)
        {
            workOrder.setFinishedQty(BigDecimal.ZERO);
        }
        if (workOrder.getQualifiedQty() == null)
        {
            workOrder.setQualifiedQty(BigDecimal.ZERO);
        }
        if (workOrder.getDefectQty() == null)
        {
            workOrder.setDefectQty(BigDecimal.ZERO);
        }
        workOrder.setCreateBy(SecurityUtils.getUsername());
        return workOrderMapper.insertWorkOrder(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateWorkOrder(MmsWorkOrder workOrder)
    {
        // 校验：已下达及之后状态不允许修改核心字段
        MmsWorkOrder existing = workOrderMapper.selectWorkOrderById(workOrder.getWorkOrderId());
        if (existing != null && !"0".equals(existing.getStatus()) && !"7".equals(existing.getStatus()))
        {
            throw new ServiceException("当前状态不允许修改工单信息");
        }
        workOrder.setUpdateBy(SecurityUtils.getUsername());
        return workOrderMapper.updateWorkOrder(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteWorkOrderByIds(Long[] workOrderIds)
    {
        // 校验：非新建状态的工单不允许删除
        for (Long id : workOrderIds)
        {
            MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(id);
            if (wo != null && !"0".equals(wo.getStatus()) && !"8".equals(wo.getStatus()))
            {
                throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]非新建/作废状态，不允许删除");
            }
        }
        return workOrderMapper.deleteWorkOrderByIds(workOrderIds);
    }

    // ========== 审核日志 ==========

    @Override
    public List<MmsWorkOrderAuditLog> selectAuditLogByWorkOrderId(Long workOrderId)
    {
        return workOrderMapper.selectAuditLogByWorkOrderId(workOrderId);
    }

    // ========== 业务操作 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int releaseWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：只有新建(0)状态可下达
        if (!"0".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有新建状态可下达");
        }
        // 校验BOM：已下达需要BOM
        if (wo.getBomId() == null)
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]未关联BOM，无法下达");
        }
        // 状态流转：0 → 1
        wo.setStatus("1");
        wo.setReleaseBy(SecurityUtils.getUsername());
        wo.setReleaseTime(new Date());
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        // 记录审核日志
        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "release", "工单下达");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int pauseWorkOrder(Long workOrderId, String pauseReason)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：已下达(1)或执行中(2)可暂停
        if (!"1".equals(wo.getStatus()) && !"2".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有已下达/执行中状态可暂停");
        }
        wo.setStatus("7");
        wo.setPauseReason(pauseReason);
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "pause", "工单暂停" + (StringUtils.isNotEmpty(pauseReason) ? "：" + pauseReason : ""));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int resumeWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：只有已暂停(7)可恢复
        if (!"7".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有已暂停状态可恢复");
        }
        // 恢复到已下达(1)状态
        wo.setStatus("1");
        wo.setPauseReason(null);
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "resume", "工单恢复");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int finishWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：执行中(2)或报工中(3)可完工
        if (!"2".equals(wo.getStatus()) && !"3".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有执行中/报工中状态可完工");
        }
        // 完工条件：合格数 + 不良数 >= 计划数量 * 完工允差（默认100%）
        BigDecimal totalOutput = (wo.getQualifiedQty() == null ? BigDecimal.ZERO : wo.getQualifiedQty())
                .add(wo.getDefectQty() == null ? BigDecimal.ZERO : wo.getDefectQty());
        if (totalOutput.compareTo(wo.getPlanQty()) < 0)
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]产出量(" + totalOutput + ")未达到计划数量(" + wo.getPlanQty() + ")，不允许完工");
        }
        wo.setStatus("4");
        wo.setActualFinish(new Date());
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "finish", "工单完工");
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeWorkOrder(Long workOrderId, String closeRemark)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：待完工质检(4)或完工入库(5)可关闭
        if (!"4".equals(wo.getStatus()) && !"5".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有待完工质检/完工入库状态可关闭");
        }
        wo.setStatus("6");
        wo.setCloseRemark(closeRemark);
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "close", "工单关闭" + (StringUtils.isNotEmpty(closeRemark) ? "：" + closeRemark : ""));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelWorkOrder(Long workOrderId, String cancelReason)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 状态校验：已关闭(6)和已作废(8)不可作废
        if ("6".equals(wo.getStatus()) || "8".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，不允许作废");
        }
        // 作废条件：无在制报工（报工中状态不允许直接作废）
        if ("3".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]处于报工中状态，请先处理报工记录后再作废");
        }
        wo.setStatus("8");
        wo.setCloseRemark(cancelReason);
        wo.setUpdateBy(SecurityUtils.getUsername());
        int rows = workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "cancel", "工单作废" + (StringUtils.isNotEmpty(cancelReason) ? "：" + cancelReason : ""));
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long splitWorkOrder(Long workOrderId, BigDecimal splitQty)
    {
        MmsWorkOrder wo = getAndCheckWorkOrder(workOrderId);
        // 校验：只有新建(0)或已下达(1)状态可拆分
        if (!"0".equals(wo.getStatus()) && !"1".equals(wo.getStatus()))
        {
            throw new ServiceException("工单[" + wo.getWorkOrderNo() + "]当前状态为" + statusName(wo.getStatus()) + "，只有新建/已下达状态可拆分");
        }
        // 校验拆分数量
        if (splitQty == null || splitQty.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("拆分数量必须大于0");
        }
        if (splitQty.compareTo(wo.getPlanQty()) >= 0)
        {
            throw new ServiceException("拆分数量必须小于原工单计划数量(" + wo.getPlanQty() + ")");
        }

        // 创建新工单（拆分出的部分）
        MmsWorkOrder newWo = new MmsWorkOrder();
        newWo.setWorkOrderNo(mkNumberRuleService.generateNumber("mms_work_order"));
        newWo.setOrderType(wo.getOrderType());
        newWo.setMpsId(wo.getMpsId());
        newWo.setMpsNo(wo.getMpsNo());
        newWo.setDemandNo(wo.getDemandNo());
        newWo.setProductId(wo.getProductId());
        newWo.setProductCode(wo.getProductCode());
        newWo.setProductName(wo.getProductName());
        newWo.setSpecModel(wo.getSpecModel());
        newWo.setUnit(wo.getUnit());
        newWo.setPlanQty(splitQty);
        newWo.setBomId(wo.getBomId());
        newWo.setBomNo(wo.getBomNo());
        newWo.setRouteId(wo.getRouteId());
        newWo.setRouteNo(wo.getRouteNo());
        newWo.setResourceId(wo.getResourceId());
        newWo.setResourceName(wo.getResourceName());
        newWo.setPlanStart(wo.getPlanStart());
        newWo.setPlanFinish(wo.getPlanFinish());
        newWo.setPriority(wo.getPriority());
        newWo.setStatus("0"); // 新工单为新建状态
        newWo.setFinishedQty(BigDecimal.ZERO);
        newWo.setQualifiedQty(BigDecimal.ZERO);
        newWo.setDefectQty(BigDecimal.ZERO);
        newWo.setDelFlag("0");
        newWo.setCreateBy(SecurityUtils.getUsername());
        newWo.setRemark("由工单[" + wo.getWorkOrderNo() + "]拆分而来");
        workOrderMapper.insertWorkOrder(newWo);

        // 原工单扣减计划数量
        wo.setPlanQty(wo.getPlanQty().subtract(splitQty));
        wo.setUpdateBy(SecurityUtils.getUsername());
        workOrderMapper.updateWorkOrder(wo);

        insertAuditLog(workOrderId, wo.getWorkOrderNo(), "split",
                "工单拆分：拆出" + splitQty + wo.getUnit() + "至新工单[" + newWo.getWorkOrderNo() + "]");
        insertAuditLog(newWo.getWorkOrderId(), newWo.getWorkOrderNo(), "split",
                "工单拆分产生：来源于工单[" + wo.getWorkOrderNo() + "]，拆分数量" + splitQty + wo.getUnit());

        return newWo.getWorkOrderId();
    }

    // ========== 私有辅助方法 ==========

    /**
     * 获取并校验工单是否存在
     */
    private MmsWorkOrder getAndCheckWorkOrder(Long workOrderId)
    {
        MmsWorkOrder wo = workOrderMapper.selectWorkOrderById(workOrderId);
        if (wo == null)
        {
            throw new ServiceException("工单不存在或已删除");
        }
        return wo;
    }

    /**
     * 记录审核日志
     */
    private void insertAuditLog(Long workOrderId, String workOrderNo, String action, String remark)
    {
        MmsWorkOrderAuditLog log = new MmsWorkOrderAuditLog();
        log.setWorkOrderId(workOrderId);
        log.setAuditBy(SecurityUtils.getUsername());
        log.setAuditAction(action);
        log.setAuditTime(new Date());
        log.setAuditRemark(remark);
        workOrderMapper.insertAuditLog(log);
    }

    /**
     * 状态名称转换
     */
    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "新建";
            case "1": return "已下达";
            case "2": return "执行中";
            case "3": return "报工中";
            case "4": return "待完工质检";
            case "5": return "完工入库";
            case "6": return "已关闭";
            case "7": return "已暂停";
            case "8": return "已作废";
            default: return "未知(" + status + ")";
        }
    }

}
