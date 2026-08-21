package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.domain.MmsKitCheckDetail;
import com.ruoyi.mms.domain.MmsWorkOrder;
import com.ruoyi.mms.mapper.MmsBomMapper;
import com.ruoyi.mms.mapper.MmsKitCheckMapper;
import com.ruoyi.mms.mapper.MmsWorkOrderMapper;
import com.ruoyi.mms.service.IMmsKitCheckService;

/**
 * 齐套检查 Service实现
 *
 * 齐套检查逻辑：
 * 1. 根据工单关联的BOM展开物料需求（单件用量 × 工单计划数量 + 损耗）
 * 2. 对比库存可用量计算缺口
 * 3. 计算齐套率 = 满足项数 / 总项数 * 100%
 * 4. 全部满足 → 1(已通过)，否则 → 2(缺料)
 *
 * @author ruoyi
 */
@Service
public class MmsKitCheckServiceImpl implements IMmsKitCheckService
{
    @Autowired
    private MmsKitCheckMapper kitCheckMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private MmsWorkOrderMapper workOrderMapper;

    @Autowired
    private MmsBomMapper bomMapper;

    @Override
    public List<MmsKitCheck> selectKitCheckList(MmsKitCheck kitCheck)
    {
        return kitCheckMapper.selectKitCheckList(kitCheck);
    }

    @Override
    public MmsKitCheck selectKitCheckById(Long kitId)
    {
        return kitCheckMapper.selectKitCheckById(kitId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertKitCheck(MmsKitCheck kitCheck)
    {
        // 自动生成齐套检查单号
        if (StringUtils.isEmpty(kitCheck.getKitNo()))
        {
            kitCheck.setKitNo(mkNumberRuleService.generateNumber("mms_kit_check"));
        }
        // 默认状态为待检查
        if (StringUtils.isEmpty(kitCheck.getStatus()))
        {
            kitCheck.setStatus("0");
        }
        kitCheck.setDelFlag("0");
        kitCheck.setCreateBy(SecurityUtils.getUsername());
        return kitCheckMapper.insertKitCheck(kitCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateKitCheck(MmsKitCheck kitCheck)
    {
        // 已通过/缺料状态的检查不允许修改
        MmsKitCheck existing = kitCheckMapper.selectKitCheckById(kitCheck.getKitId());
        if (existing != null && ("1".equals(existing.getStatus()) || "2".equals(existing.getStatus())))
        {
            throw new ServiceException("齐套检查[" + existing.getKitNo() + "]已执行，不允许修改");
        }
        kitCheck.setUpdateBy(SecurityUtils.getUsername());
        return kitCheckMapper.updateKitCheck(kitCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteKitCheckByIds(Long[] kitIds)
    {
        // 已通过的检查不允许删除
        for (Long id : kitIds)
        {
            MmsKitCheck check = kitCheckMapper.selectKitCheckById(id);
            if (check != null && "1".equals(check.getStatus()))
            {
                throw new ServiceException("齐套检查[" + check.getKitNo() + "]已通过，不允许删除");
            }
        }
        return kitCheckMapper.deleteKitCheckByIds(kitIds);
    }

    @Override
    public List<MmsKitCheckDetail> selectKitCheckDetailByKitId(Long kitId)
    {
        return kitCheckMapper.selectKitCheckDetailByKitId(kitId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeKitCheck(Long kitId)
    {
        MmsKitCheck kitCheck = kitCheckMapper.selectKitCheckById(kitId);
        if (kitCheck == null)
        {
            throw new ServiceException("齐套检查记录不存在或已删除");
        }
        // 状态校验：只有待检查(0)可执行
        if (!"0".equals(kitCheck.getStatus()))
        {
            throw new ServiceException("齐套检查[" + kitCheck.getKitNo() + "]当前状态为" + statusName(kitCheck.getStatus()) + "，只有待检查状态可执行");
        }

        // 如果明细为空，尝试自动从BOM生成明细
        List<MmsKitCheckDetail> details = kitCheckMapper.selectKitCheckDetailByKitId(kitId);
        if (details == null || details.isEmpty())
        {
            // 尝试从工单BOM自动展开
            details = generateDetailsFromBom(kitCheck);
            if (details.isEmpty())
            {
                throw new ServiceException("齐套检查明细为空且无法从BOM自动生成，请检查工单是否关联了BOM");
            }
            // 先删除旧明细（如果有）
            kitCheckMapper.deleteKitCheckDetailByKitId(kitId);
            // 写入新明细
            for (MmsKitCheckDetail detail : details)
            {
                detail.setKitId(kitId);
                kitCheckMapper.insertKitCheckDetail(detail);
            }
        }

        // 计算齐套情况
        int totalItems = details.size();
        int completeItems = 0;
        boolean allComplete = true;

        for (MmsKitCheckDetail detail : details)
        {
            // 重新查询库存可用量
            BigDecimal available = kitCheckMapper.selectAvailableQtyByMaterialId(detail.getMaterialId());
            if (available == null)
            {
                available = BigDecimal.ZERO;
            }
            detail.setAvailableQty(available);

            // 计算缺口量 = 需求数量 - 可用数量
            BigDecimal required = detail.getRequiredQty() == null ? BigDecimal.ZERO : detail.getRequiredQty();
            BigDecimal shortage = required.subtract(available);
            if (shortage.compareTo(BigDecimal.ZERO) < 0)
            {
                shortage = BigDecimal.ZERO;
            }
            detail.setShortageQty(shortage);

            // 判断单项是否齐套
            if (shortage.compareTo(BigDecimal.ZERO) == 0)
            {
                detail.setIsComplete("1");
                completeItems++;
            }
            else
            {
                detail.setIsComplete("0");
                allComplete = false;
            }

            // 更新明细
            detail.setUpdateBy(SecurityUtils.getUsername());
            kitCheckMapper.updateKitCheckDetail(detail);
        }

        // 计算齐套率
        BigDecimal kitRate = BigDecimal.valueOf(completeItems)
                .divide(BigDecimal.valueOf(totalItems), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 更新主表
        kitCheck.setKitRate(kitRate);
        kitCheck.setIsComplete(allComplete ? "1" : "0");
        kitCheck.setStatus(allComplete ? "1" : "2");
        kitCheck.setCheckTime(new Date());
        kitCheck.setUpdateBy(SecurityUtils.getUsername());

        return kitCheckMapper.updateKitCheck(kitCheck);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoKitCheckByWorkOrderId(Long workOrderId)
    {
        if (workOrderId == null)
        {
            throw new ServiceException("工单ID不能为空");
        }

        MmsWorkOrder workOrder = workOrderMapper.selectWorkOrderById(workOrderId);
        if (workOrder == null)
        {
            throw new ServiceException("工单不存在或已删除");
        }

        return doAutoKitCheck(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long autoKitCheckByWorkOrderNo(String workOrderNo)
    {
        if (StringUtils.isEmpty(workOrderNo))
        {
            throw new ServiceException("工单号不能为空");
        }

        MmsWorkOrder queryParam = new MmsWorkOrder();
        queryParam.setWorkOrderNo(workOrderNo);
        List<MmsWorkOrder> workOrders = workOrderMapper.selectWorkOrderList(queryParam);
        if (workOrders == null || workOrders.isEmpty())
        {
            throw new ServiceException("工单[" + workOrderNo + "]不存在");
        }
        MmsWorkOrder workOrder = workOrders.get(0);

        return doAutoKitCheck(workOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchAutoKitCheck(Long[] workOrderIds)
    {
        if (workOrderIds == null || workOrderIds.length == 0)
        {
            throw new ServiceException("请选择至少一个工单");
        }

        int successCount = 0;
        List<String> errors = new ArrayList<>();

        for (Long workOrderId : workOrderIds)
        {
            try
            {
                autoKitCheckByWorkOrderId(workOrderId);
                successCount++;
            }
            catch (ServiceException e)
            {
                errors.add(e.getMessage());
            }
        }

        if (successCount == 0 && !errors.isEmpty())
        {
            throw new ServiceException("批量齐套检查失败：" + String.join("；", errors));
        }

        return successCount;
    }

    // ========== 核心私有方法 ==========

    /**
     * 执行一键齐套检查的核心逻辑
     * 1. 校验工单状态
     * 2. 获取工单关联的BOM
     * 3. 展开BOM物料需求
     * 4. 查询库存可用量
     * 5. 生成明细记录
     * 6. 计算齐套率
     * 7. 创建齐套检查主表+明细
     */
    private Long doAutoKitCheck(MmsWorkOrder workOrder)
    {
        // 校验工单状态
        if (!isValidWorkOrderStatus(workOrder.getStatus()))
        {
            throw new ServiceException("工单[" + workOrder.getWorkOrderNo() + "]当前状态为"
                    + workOrderStatusName(workOrder.getStatus()) + "，不可进行齐套检查");
        }

        // 获取BOM：优先使用工单关联的BOM，若未关联则根据产品ID自动查找已发布的BOM
        MmsBom bom = null;
        Long bomId = workOrder.getBomId();

        if (bomId != null)
        {
            // 工单已关联BOM，直接使用
            bom = bomMapper.selectBomById(bomId);
            if (bom == null)
            {
                throw new ServiceException("工单[" + workOrder.getWorkOrderNo() + "]关联的BOM(ID:" + bomId + ")不存在");
            }
        }
        else if (workOrder.getProductId() != null)
        {
            // 工单未关联BOM，根据产品ID自动查找已发布的标准BOM
            bom = bomMapper.selectBomByProductId(workOrder.getProductId());
            if (bom == null)
            {
                throw new ServiceException("工单[" + workOrder.getWorkOrderNo() + "]未关联BOM，且产品[" + workOrder.getProductCode() + "]没有已发布的BOM，无法进行齐套检查");
            }
            // 自动关联到工单
            bomId = bom.getBomId();
        }
        else
        {
            throw new ServiceException("工单[" + workOrder.getWorkOrderNo() + "]未关联BOM，且缺少产品信息，无法进行齐套检查");
        }

        if (!"1".equals(bom.getStatus()))
        {
            throw new ServiceException("BOM[" + bom.getBomNo() + "]未发布，无法进行齐套检查");
        }

        // 获取BOM明细
        List<MmsBomDetail> bomDetails = bomMapper.selectBomDetailByBomId(bomId);
        if (bomDetails == null || bomDetails.isEmpty())
        {
            throw new ServiceException("BOM[" + bom.getBomNo() + "]没有明细数据，无法进行齐套检查");
        }

        // 展开BOM物料需求，生成齐套检查明细
        List<MmsKitCheckDetail> details = generateDetailsFromBom(workOrder, bom, bomDetails);

        // 计算齐套情况
        int totalItems = details.size();
        int completeItems = 0;
        boolean allComplete = true;

        for (MmsKitCheckDetail detail : details)
        {
            // 查询库存可用量
            BigDecimal available = kitCheckMapper.selectAvailableQtyByMaterialId(detail.getMaterialId());
            if (available == null)
            {
                available = BigDecimal.ZERO;
            }
            detail.setAvailableQty(available);

            // 计算缺口量
            BigDecimal required = detail.getRequiredQty();
            BigDecimal shortage = required.subtract(available);
            if (shortage.compareTo(BigDecimal.ZERO) < 0)
            {
                shortage = BigDecimal.ZERO;
            }
            detail.setShortageQty(shortage);

            // 判断单项是否齐套
            if (shortage.compareTo(BigDecimal.ZERO) == 0)
            {
                detail.setIsComplete("1");
                completeItems++;
            }
            else
            {
                detail.setIsComplete("0");
                allComplete = false;
            }
        }

        // 计算齐套率
        BigDecimal kitRate = BigDecimal.valueOf(completeItems)
                .divide(BigDecimal.valueOf(totalItems), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);

        // 创建齐套检查主表
        MmsKitCheck kitCheck = new MmsKitCheck();
        kitCheck.setKitNo(mkNumberRuleService.generateNumber("mms_kit_check"));
        kitCheck.setWorkOrderId(workOrder.getWorkOrderId());
        kitCheck.setWorkOrderNo(workOrder.getWorkOrderNo());
        kitCheck.setMpsId(workOrder.getMpsId());
        kitCheck.setMpsNo(workOrder.getMpsNo());
        kitCheck.setBomId(bomId);
        kitCheck.setCheckTime(new Date());
        kitCheck.setKitRate(kitRate);
        kitCheck.setIsComplete(allComplete ? "1" : "0");
        kitCheck.setStatus(allComplete ? "1" : "2"); // 直接为已检查状态
        kitCheck.setDelFlag("0");
        kitCheck.setCreateBy(SecurityUtils.getUsername());
        kitCheck.setCreateTime(new Date());

        kitCheckMapper.insertKitCheck(kitCheck);

        // 写入明细
        for (MmsKitCheckDetail detail : details)
        {
            detail.setKitId(kitCheck.getKitId());
            detail.setDelFlag("0");
            detail.setCreateBy(SecurityUtils.getUsername());
            detail.setCreateTime(new Date());
            kitCheckMapper.insertKitCheckDetail(detail);
        }

        return kitCheck.getKitId();
    }

    /**
     * 根据齐套检查主表（已关联工单/BOM）生成明细列表
     * 用于executeKitCheck中明细为空时自动补充
     */
    private List<MmsKitCheckDetail> generateDetailsFromBom(MmsKitCheck kitCheck)
    {
        List<MmsKitCheckDetail> details = new ArrayList<>();

        if (kitCheck.getWorkOrderId() == null && StringUtils.isEmpty(kitCheck.getWorkOrderNo()))
        {
            return details;
        }

        MmsWorkOrder workOrder = null;
        if (kitCheck.getWorkOrderId() != null)
        {
            workOrder = workOrderMapper.selectWorkOrderById(kitCheck.getWorkOrderId());
        }
        else
        {
            MmsWorkOrder queryParam = new MmsWorkOrder();
            queryParam.setWorkOrderNo(kitCheck.getWorkOrderNo());
            List<MmsWorkOrder> list = workOrderMapper.selectWorkOrderList(queryParam);
            if (list != null && !list.isEmpty())
            {
                workOrder = list.get(0);
            }
        }

        if (workOrder == null)
        {
            return details;
        }

        // 获取BOM：优先使用工单关联的BOM，若未关联则根据产品ID自动查找
        Long bomId = workOrder.getBomId();
        MmsBom bom = null;

        if (bomId != null)
        {
            bom = bomMapper.selectBomById(bomId);
        }
        else if (workOrder.getProductId() != null)
        {
            // 工单未关联BOM，根据产品ID自动查找已发布的BOM
            bom = bomMapper.selectBomByProductId(workOrder.getProductId());
            if (bom != null)
            {
                bomId = bom.getBomId();
            }
        }

        if (bom == null || bomId == null)
        {
            return details;
        }

        List<MmsBomDetail> bomDetails = bomMapper.selectBomDetailByBomId(bomId);
        if (bomDetails == null || bomDetails.isEmpty())
        {
            return details;
        }

        return generateDetailsFromBom(workOrder, bom, bomDetails);
    }

    /**
     * 根据工单+BOM+BOM明细，计算物料需求并生成齐套检查明细列表
     *
     * 需求量 = 单件用量 × (工单计划数量 / BOM基准数量) × (1 + 损耗率/100)
     */
    private List<MmsKitCheckDetail> generateDetailsFromBom(MmsWorkOrder workOrder, MmsBom bom, List<MmsBomDetail> bomDetails)
    {
        List<MmsKitCheckDetail> details = new ArrayList<>();

        // 工单计划数量
        BigDecimal planQty = workOrder.getPlanQty() == null ? BigDecimal.ZERO : workOrder.getPlanQty();
        // BOM基准数量
        BigDecimal baseQty = bom.getBaseQty() == null ? BigDecimal.ONE : bom.getBaseQty();
        if (baseQty.compareTo(BigDecimal.ZERO) == 0)
        {
            baseQty = BigDecimal.ONE;
        }
        // 数量倍率 = 工单计划数量 / BOM基准数量
        BigDecimal qtyRatio = planQty.divide(baseQty, 6, RoundingMode.HALF_UP);

        for (MmsBomDetail bomDetail : bomDetails)
        {
            // 跳过虚拟件（虚拟件需要展开子BOM，这里暂不处理多层级）
            if ("1".equals(bomDetail.getIsPhantom()))
            {
                continue;
            }

            MmsKitCheckDetail detail = new MmsKitCheckDetail();
            detail.setMaterialId(bomDetail.getMaterialId());
            detail.setMaterialCode(bomDetail.getMaterialCode());
            detail.setMaterialName(bomDetail.getMaterialName());
            detail.setSpecModel(bomDetail.getSpecModel());
            detail.setUnit(bomDetail.getUnit());

            // 计算需求数量 = 单件用量 × 倍率 × (1 + 损耗率/100)
            BigDecimal usageQty = bomDetail.getUsageQty() == null ? BigDecimal.ZERO : bomDetail.getUsageQty();
            BigDecimal requiredQty = usageQty.multiply(qtyRatio);

            // 加损耗
            BigDecimal lossRate = bomDetail.getLossRate();
            if (lossRate != null && lossRate.compareTo(BigDecimal.ZERO) > 0)
            {
                BigDecimal lossFactor = BigDecimal.ONE.add(lossRate.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP));
                requiredQty = requiredQty.multiply(lossFactor);
            }
            requiredQty = requiredQty.setScale(3, RoundingMode.HALF_UP);
            detail.setRequiredQty(requiredQty);

            // 初始值
            detail.setAvailableQty(BigDecimal.ZERO);
            detail.setShortageQty(BigDecimal.ZERO);
            detail.setIsComplete("0");

            details.add(detail);
        }

        return details;
    }

    // ========== 辅助方法 ==========

    private boolean isValidWorkOrderStatus(String status)
    {
        // 新建(0)、已下达(1)、执行中(2) 状态可做齐套检查
        return "0".equals(status) || "1".equals(status) || "2".equals(status);
    }

    private String workOrderStatusName(String status)
    {
        if (status == null) return "未知";
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

    private String statusName(String status)
    {
        if (status == null) return "未知";
        switch (status)
        {
            case "0": return "待检查";
            case "1": return "已通过";
            case "2": return "缺料";
            default: return "未知(" + status + ")";
        }
    }

}
