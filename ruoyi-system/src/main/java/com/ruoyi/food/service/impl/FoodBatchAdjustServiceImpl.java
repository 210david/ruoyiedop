package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodBatchAdjust;
import com.ruoyi.food.domain.FoodBatchAdjustItem;
import com.ruoyi.food.domain.FoodBatchGenealogy;
import com.ruoyi.food.domain.FoodBatchStatusLog;
import com.ruoyi.food.mapper.FoodBatchAdjustMapper;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.service.IFoodBatchAdjustService;
import com.ruoyi.food.service.IFoodBatchRuleService;

/**
 * 食品批次调整单Service业务层处理
 *
 * 调整类型：1冻结 2解冻 3报废 4拆批 5合批 6召回 7效期调整
 *
 * @author ruoyi
 */
@Service
public class FoodBatchAdjustServiceImpl implements IFoodBatchAdjustService
{
    @Autowired
    private FoodBatchAdjustMapper adjustMapper;

    @Autowired
    private FoodBatchMapper batchMapper;

    @Autowired
    private IFoodBatchRuleService ruleService;

    @Override
    public FoodBatchAdjust selectAdjustById(Long adjustId)
    {
        FoodBatchAdjust adjust = adjustMapper.selectAdjustById(adjustId);
        if (adjust != null)
        {
            adjust.setItemList(adjustMapper.selectItemsByAdjustId(adjustId));
        }
        return adjust;
    }

    @Override
    public List<FoodBatchAdjust> selectAdjustList(FoodBatchAdjust adjust)
    {
        return adjustMapper.selectAdjustList(adjust);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAdjust(FoodBatchAdjust adjust)
    {
        if (StringUtils.isEmpty(adjust.getAdjustType()))
        {
            throw new ServiceException("调整类型不能为空");
        }
        String type = adjust.getAdjustType();
        if ("4".equals(type) || "5".equals(type))
        {
            if (adjust.getItemList() == null || adjust.getItemList().isEmpty())
            {
                throw new ServiceException("拆批/合批调整必须包含明细行");
            }
            for (FoodBatchAdjustItem item : adjust.getItemList())
            {
                if (item.getQty() == null || item.getQty().compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("明细调整数量必须大于0");
                }
            }
        }
        if (StringUtils.isEmpty(adjust.getSourceBatchCode()) && adjust.getSourceBatchId() != null)
        {
            FoodBatch batch = batchMapper.selectBatchById(adjust.getSourceBatchId());
            if (batch != null)
            {
                adjust.setSourceBatchCode(batch.getBatchCode());
                adjust.setMaterialId(batch.getMaterialId());
            }
        }
        adjust.setAdjustCode(generateAdjustCode());
        adjust.setStatus("0");
        adjust.setCreateBy(adjust.getCreateBy() == null ? "system" : adjust.getCreateBy());
        int rows = adjustMapper.insertAdjust(adjust);
        if (adjust.getItemList() != null)
        {
            for (FoodBatchAdjustItem item : adjust.getItemList())
            {
                item.setAdjustId(adjust.getAdjustId());
                if (item.getSourceBatchId() == null)
                {
                    item.setSourceBatchId(adjust.getSourceBatchId());
                    item.setSourceBatchCode(adjust.getSourceBatchCode());
                }
                adjustMapper.insertAdjustItem(item);
            }
        }
        return rows;
    }

    /**
     * 执行调整单（状态机迁移 + 批次数量联动 + 谱系 + 状态日志）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeAdjust(Long adjustId)
    {
        FoodBatchAdjust adjust = adjustMapper.selectAdjustById(adjustId);
        if (adjust == null)
        {
            throw new ServiceException("调整单不存在");
        }
        if (!"0".equals(adjust.getStatus()))
        {
            throw new ServiceException("仅待执行状态的调整单可执行");
        }
        List<FoodBatchAdjustItem> items = adjustMapper.selectItemsByAdjustId(adjustId);
        String operator = adjust.getExecuteBy() == null ? adjust.getCreateBy() : adjust.getExecuteBy();
        String type = adjust.getAdjustType();
        switch (type)
        {
            case "1": // 冻结
                doFreeze(adjust, operator, "FREEZE");
                break;
            case "2": // 解冻
                doUnfreeze(adjust, operator);
                break;
            case "3": // 报废
                doScrap(adjust, items, operator);
                break;
            case "4": // 拆批
                doSplit(adjust, items, operator);
                break;
            case "5": // 合批
                doMerge(adjust, items, operator);
                break;
            case "6": // 召回
                doFreeze(adjust, operator, "RECALL");
                break;
            case "7": // 效期调整
                doExpireAdjust(adjust, items, operator);
                break;
            default:
                throw new ServiceException("未知调整类型：" + type);
        }
        FoodBatchAdjust updater = new FoodBatchAdjust();
        updater.setAdjustId(adjustId);
        updater.setStatus("1");
        updater.setExecuteTime(DateUtils.getNowDate());
        updater.setExecuteBy(operator);
        updater.setUpdateBy(operator);
        return adjustMapper.updateAdjust(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelAdjust(Long adjustId)
    {
        FoodBatchAdjust adjust = adjustMapper.selectAdjustById(adjustId);
        if (adjust == null)
        {
            throw new ServiceException("调整单不存在");
        }
        if (!"0".equals(adjust.getStatus()))
        {
            throw new ServiceException("仅待执行状态的调整单可取消");
        }
        FoodBatchAdjust updater = new FoodBatchAdjust();
        updater.setAdjustId(adjustId);
        updater.setStatus("2");
        updater.setUpdateBy(adjust.getUpdateBy() == null ? adjust.getCreateBy() : adjust.getUpdateBy());
        return adjustMapper.updateAdjust(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAdjustByIds(Long[] adjustIds)
    {
        for (Long adjustId : adjustIds)
        {
            FoodBatchAdjust adjust = adjustMapper.selectAdjustById(adjustId);
            if (adjust != null && "1".equals(adjust.getStatus()))
            {
                throw new ServiceException("已执行的调整单【" + adjust.getAdjustCode() + "】不允许删除");
            }
        }
        for (Long adjustId : adjustIds)
        {
            adjustMapper.deleteItemsByAdjustId(adjustId);
        }
        return adjustMapper.deleteAdjustByIds(adjustIds);
    }

    // ---------------- 各类型执行逻辑 ----------------

    private void doFreeze(FoodBatchAdjust adjust, String operator, String operateType)
    {
        List<FoodBatchAdjustItem> items = adjustMapper.selectItemsByAdjustId(adjust.getAdjustId());
        if (items.isEmpty() && adjust.getSourceBatchId() != null)
        {
            changeBatchStatus(adjust.getSourceBatchId(), "2", operateType, adjust.getReason(), adjust.getAdjustId(), operator);
            return;
        }
        for (FoodBatchAdjustItem item : items)
        {
            Long batchId = item.getSourceBatchId() != null ? item.getSourceBatchId() : adjust.getSourceBatchId();
            changeBatchStatus(batchId, "2", operateType, adjust.getReason(), adjust.getAdjustId(), operator);
        }
    }

    private void doUnfreeze(FoodBatchAdjust adjust, String operator)
    {
        List<FoodBatchAdjustItem> items = adjustMapper.selectItemsByAdjustId(adjust.getAdjustId());
        if (items.isEmpty() && adjust.getSourceBatchId() != null)
        {
            changeBatchStatus(adjust.getSourceBatchId(), "1", "UNFREEZE", "解冻复检合格", adjust.getAdjustId(), operator);
            return;
        }
        for (FoodBatchAdjustItem item : items)
        {
            Long batchId = item.getSourceBatchId() != null ? item.getSourceBatchId() : adjust.getSourceBatchId();
            changeBatchStatus(batchId, "1", "UNFREEZE", "解冻复检合格", adjust.getAdjustId(), operator);
        }
    }

    private void doScrap(FoodBatchAdjust adjust, List<FoodBatchAdjustItem> items, String operator)
    {
        if (adjust.getSourceBatchId() != null)
        {
            FoodBatch source = batchMapper.selectBatchById(adjust.getSourceBatchId());
            if ("3".equals(source.getBatchStatus()))
            {
                throw new ServiceException("批次【" + source.getBatchCode() + "】已报废");
            }
            batchMapper.updateBatchStatus(adjust.getSourceBatchId(), null, "3", adjust.getReason());
            insertLog(source, source.getBatchStatus(), "3", "SCRAP", adjust.getReason(), adjust.getAdjustId(), operator);
            deductQty(adjust.getSourceBatchId(), source.getQty(), operator);
        }
        for (FoodBatchAdjustItem item : items)
        {
            if (item.getSourceBatchId() == null || item.getSourceBatchId().equals(adjust.getSourceBatchId()))
            {
                continue;
            }
            FoodBatch source = batchMapper.selectBatchById(item.getSourceBatchId());
            if (source == null || "3".equals(source.getBatchStatus()))
            {
                continue;
            }
            batchMapper.updateBatchStatus(item.getSourceBatchId(), null, "3", adjust.getReason());
            insertLog(source, source.getBatchStatus(), "3", "SCRAP", adjust.getReason(), adjust.getAdjustId(), operator);
            deductQty(item.getSourceBatchId(), source.getQty(), operator);
        }
    }

    /**
     * 拆批：源批次按明细拆出多个子批次（效期沿用源批次）
     */
    private void doSplit(FoodBatchAdjust adjust, List<FoodBatchAdjustItem> items, String operator)
    {
        FoodBatch source = batchMapper.selectBatchById(adjust.getSourceBatchId());
        if (source == null)
        {
            throw new ServiceException("源批次不存在");
        }
        BigDecimal totalSplit = BigDecimal.ZERO;
        for (FoodBatchAdjustItem item : items)
        {
            totalSplit = totalSplit.add(item.getQty());
        }
        if (source.getQty().compareTo(totalSplit) < 0)
        {
            throw new ServiceException("拆批数量合计(" + totalSplit + ")超过源批次数量(" + source.getQty() + ")");
        }
        for (FoodBatchAdjustItem item : items)
        {
            FoodBatch child = new FoodBatch();
            child.setMaterialId(source.getMaterialId());
            child.setMaterialCode(source.getMaterialCode());
            child.setSourceType("3");
            child.setSourceBillType("adjust");
            child.setSourceBillId(adjust.getAdjustId());
            child.setProduceDate(source.getProduceDate());
            child.setReceiveDate(source.getReceiveDate());
            child.setShelfLifeDays(source.getShelfLifeDays());
            child.setExpireDate(source.getExpireDate());
            child.setQty(item.getQty());
            child.setQtyUnit(source.getQtyUnit());
            child.setBatchStatus(source.getBatchStatus());
            child.setExtBatchCode(source.getExtBatchCode());
            child.setBatchCode(ruleService.nextBatchCode(source.getMaterialId(), source.getMaterialCode()));
            child.setRemark("拆批自批次 " + source.getBatchCode());
            child.setCreateBy(operator);
            batchMapper.insertBatch(child);
            batchMapper.refreshBatchExpireStatus(child.getBatchId());
            item.setTargetBatchId(child.getBatchId());
            item.setTargetBatchCode(child.getBatchCode());
            adjustMapper.updateAdjustItem(item);
            // 谱系：源→子（拆分）
            insertGenealogy(source.getBatchId(), child.getBatchId(), "3", item.getQty(), source.getQtyUnit(), "adjust", adjust.getAdjustId(), adjust.getAdjustCode(), operator);
            insertLog(child, source.getBatchStatus(), source.getBatchStatus(), "SPLIT", "拆批生成，源批次 " + source.getBatchCode(), adjust.getAdjustId(), operator);
        }
        BigDecimal newQty = source.getQty().subtract(totalSplit);
        batchMapper.updateBatchQty(source.getBatchId(), newQty);
    }

    /**
     * 合批：多个源批次合并到一个新批次（效期按最早到期批次）
     */
    private void doMerge(FoodBatchAdjust adjust, List<FoodBatchAdjustItem> items, String operator)
    {
        FoodBatch first = batchMapper.selectBatchById(adjust.getSourceBatchId());
        if (first == null)
        {
            throw new ServiceException("主批次不存在");
        }
        BigDecimal totalMerge = BigDecimal.ZERO;
        Date minExpire = null;
        Date minProduce = null;
        for (FoodBatchAdjustItem item : items)
        {
            Long batchId = item.getSourceBatchId() != null ? item.getSourceBatchId() : adjust.getSourceBatchId();
            FoodBatch source = batchMapper.selectBatchById(batchId);
            if (source == null)
            {
                throw new ServiceException("明细源批次不存在：" + item.getSourceBatchCode());
            }
            if (!"3".equals(source.getBatchStatus()))
            {
                totalMerge = totalMerge.add(item.getQty());
                if (minExpire == null || (source.getExpireDate() != null && source.getExpireDate().before(minExpire)))
                {
                    minExpire = source.getExpireDate();
                }
                if (minProduce == null || (source.getProduceDate() != null && source.getProduceDate().before(minProduce)))
                {
                    minProduce = source.getProduceDate();
                }
            }
        }
        if (totalMerge.compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("合批数量必须大于0");
        }
        FoodBatch merged = new FoodBatch();
        merged.setMaterialId(first.getMaterialId());
        merged.setMaterialCode(first.getMaterialCode());
        merged.setSourceType("4");
        merged.setSourceBillType("adjust");
        merged.setSourceBillId(adjust.getAdjustId());
        merged.setProduceDate(minProduce);
        merged.setShelfLifeDays(first.getShelfLifeDays());
        merged.setExpireDate(minExpire);
        merged.setQty(totalMerge);
        merged.setQtyUnit(first.getQtyUnit());
        merged.setBatchStatus(first.getBatchStatus());
        merged.setBatchCode(ruleService.nextBatchCode(first.getMaterialId(), first.getMaterialCode()));
        merged.setRemark("合批生成（含 " + items.size() + " 个源批次）");
        merged.setCreateBy(operator);
        batchMapper.insertBatch(merged);
        batchMapper.refreshBatchExpireStatus(merged.getBatchId());
        for (FoodBatchAdjustItem item : items)
        {
            Long batchId = item.getSourceBatchId() != null ? item.getSourceBatchId() : adjust.getSourceBatchId();
            FoodBatch source = batchMapper.selectBatchById(batchId);
            if (source == null || "3".equals(source.getBatchStatus()))
            {
                continue;
            }
            item.setTargetBatchId(merged.getBatchId());
            item.setTargetBatchCode(merged.getBatchCode());
            adjustMapper.updateAdjustItem(item);
            insertGenealogy(source.getBatchId(), merged.getBatchId(), "4", item.getQty(), source.getQtyUnit(), "adjust", adjust.getAdjustId(), adjust.getAdjustCode(), operator);
            BigDecimal newQty = source.getQty().subtract(item.getQty());
            if (newQty.compareTo(BigDecimal.ZERO) < 0)
            {
                throw new ServiceException("批次【" + source.getBatchCode() + "】可用数量不足，无法合批");
            }
            batchMapper.updateBatchQty(source.getBatchId(), newQty);
        }
        insertLog(merged, first.getBatchStatus(), first.getBatchStatus(), "MERGE", "合批生成", adjust.getAdjustId(), operator);
    }

    /**
     * 效期调整：按明细修正批次到期日期并重算效期状态
     */
    private void doExpireAdjust(FoodBatchAdjust adjust, List<FoodBatchAdjustItem> items, String operator)
    {
        if (items.isEmpty())
        {
            throw new ServiceException("效期调整必须包含明细行");
        }
        for (FoodBatchAdjustItem item : items)
        {
            if (item.getNewExpireDate() == null)
            {
                throw new ServiceException("明细新到期日期不能为空");
            }
            FoodBatch source = batchMapper.selectBatchById(item.getSourceBatchId() != null ? item.getSourceBatchId() : adjust.getSourceBatchId());
            if (source == null)
            {
                throw new ServiceException("源批次不存在：" + item.getSourceBatchCode());
            }
            FoodBatch updater = new FoodBatch();
            updater.setBatchId(source.getBatchId());
            updater.setExpireDate(item.getNewExpireDate());
            updater.setUpdateBy(operator);
            batchMapper.updateBatch(updater);
            batchMapper.refreshBatchExpireStatus(source.getBatchId());
            insertLog(source, source.getExpireStatus(), source.getExpireStatus(), "EXPIRE_ADJUST",
                    "效期调整：" + dateStr(source.getExpireDate()) + " → " + dateStr(item.getNewExpireDate()), adjust.getAdjustId(), operator);
        }
    }

    // ---------------- 通用 ----------------

    private void changeBatchStatus(Long batchId, String toStatus, String operateType, String reason, Long refBillId, String operator)
    {
        FoodBatch batch = batchMapper.selectBatchById(batchId);
        if (batch == null)
        {
            throw new ServiceException("批次不存在：" + batchId);
        }
        if ("3".equals(batch.getBatchStatus()) && !"1".equals(toStatus))
        {
            throw new ServiceException("批次【" + batch.getBatchCode() + "】已报废，不允许调整");
        }
        if ("2".equals(batch.getBatchStatus()) && "2".equals(toStatus))
        {
            return; // 已冻结，幂等跳过
        }
        batchMapper.updateBatchStatus(batchId, null, toStatus, "2".equals(toStatus) ? reason : null);
        insertLog(batch, batch.getBatchStatus(), toStatus, operateType, reason, refBillId, operator);
    }

    /**
     * 报废扣减批次数量至0
     */
    private void deductQty(Long batchId, BigDecimal currentQty, String operator)
    {
        if (currentQty != null && currentQty.compareTo(BigDecimal.ZERO) > 0)
        {
            batchMapper.updateBatchQty(batchId, BigDecimal.ZERO);
        }
    }

    private void insertGenealogy(Long parentBatchId, Long childBatchId, String relationType, BigDecimal qty, String qtyUnit, String billType, Long billId, String billCode, String operator)
    {
        FoodBatchGenealogy genealogy = new FoodBatchGenealogy();
        genealogy.setParentBatchId(parentBatchId);
        genealogy.setChildBatchId(childBatchId);
        genealogy.setRelationType(relationType);
        genealogy.setQty(qty);
        genealogy.setQtyUnit(qtyUnit);
        genealogy.setRefBillType(billType);
        genealogy.setRefBillId(billId);
        genealogy.setRefBillCode(billCode);
        genealogy.setCreateBy(operator);
        batchMapper.insertGenealogy(genealogy);
    }

    private void insertLog(FoodBatch batch, String fromStatus, String toStatus, String operateType, String reason, Long refBillId, String operator)
    {
        FoodBatchStatusLog log = new FoodBatchStatusLog();
        log.setBatchId(batch.getBatchId());
        log.setBatchCode(batch.getBatchCode());
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperateType(operateType);
        log.setReason(reason);
        log.setRefBillType("adjust");
        log.setRefBillId(refBillId);
        log.setOperateBy(operator);
        batchMapper.insertStatusLog(log);
    }

    private String generateAdjustCode()
    {
        return "ADJ" + DateUtils.dateTimeNow("yyyyMMddHHmmss") + String.format("%03d", (int) (Math.random() * 1000));
    }

    private String dateStr(Date date)
    {
        if (date == null)
        {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().toString();
    }
}
