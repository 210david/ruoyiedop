package com.ruoyi.mms.service.impl;

import com.ruoyi.common.annotation.DataScope;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsFinishReceipt;
import com.ruoyi.mms.mapper.MmsFinishReceiptMapper;
import com.ruoyi.mms.service.IMmsFinishReceiptService;
import com.ruoyi.mms.service.IMmsGenealogySyncService;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.service.IFoodBatchService;
import com.ruoyi.food.service.IFoodBatchStockService;

/**
 * 完工入库 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsFinishReceiptServiceImpl implements IMmsFinishReceiptService
{
    @Autowired
    private MmsFinishReceiptMapper finishReceiptMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private IMmsGenealogySyncService genealogySyncService;

    @Autowired
    private IFoodBatchService foodBatchService;

    @Autowired
    private IFoodBatchStockService batchStockService;

    @Override
    @DataScope(deptAlias = "mms_finish_receipt")
    public List<MmsFinishReceipt> selectFinishReceiptList(MmsFinishReceipt finishReceipt)
    {
        return finishReceiptMapper.selectFinishReceiptList(finishReceipt);
    }

    @Override
    public MmsFinishReceipt selectFinishReceiptById(Long FinishId)
    {
        return finishReceiptMapper.selectFinishReceiptById(FinishId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        // 自动生成完工入库单号
        if (StringUtils.isEmpty(finishReceipt.getFinishNo()))
        {
            finishReceipt.setFinishNo(mkNumberRuleService.generateNumber("mms_finish_receipt"));
        }
        finishReceipt.setDelFlag("0");
        finishReceipt.setStatus("0");
        finishReceipt.setCreateBy(SecurityUtils.getUsername());
        finishReceipt.setCreateTime(DateUtils.getNowDate());
        return finishReceiptMapper.insertFinishReceipt(finishReceipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        return finishReceiptMapper.updateFinishReceipt(finishReceipt);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmFinishReceipt(MmsFinishReceipt finishReceipt)
    {
        // 校验：只有待入库状态才能确认入库
        MmsFinishReceipt existing = finishReceiptMapper.selectFinishReceiptById(finishReceipt.getFinishId());
        if (existing == null)
        {
            throw new ServiceException("完工入库单不存在");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("只有待入库状态的完工入库单才能进行入库确认");
        }
        // 批次号强制必填：产出批次是追溯谱系的目的地（complete 事件），缺失将导致整条链路无产出节点
        String effBatchNo = StringUtils.isNotEmpty(finishReceipt.getBatchNo()) ? finishReceipt.getBatchNo() : existing.getBatchNo();
        if (StringUtils.isEmpty(effBatchNo))
        {
            throw new ServiceException("完工入库单[" + existing.getFinishNo() + "]批次号必填，请填写完工批次后重新提交");
        }
        // 设置入库信息
        finishReceipt.setStatus("1");
        finishReceipt.setInboundTime(finishReceipt.getInboundTime() != null ? finishReceipt.getInboundTime() : DateUtils.getNowDate());
        finishReceipt.setInboundBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateBy(SecurityUtils.getUsername());
        finishReceipt.setUpdateTime(DateUtils.getNowDate());
        // 如果前端传了批次号，同步更新
        int rows = finishReceiptMapper.updateFinishReceipt(finishReceipt);

        // ===== 谱系联动：完工入库确认 → 写入完工谱系（complete）并回填该工单领料谱系产出批次（幂等，失败不阻断） =====
        MmsFinishReceipt merged = finishReceiptMapper.selectFinishReceiptById(finishReceipt.getFinishId());
        if (merged != null)
        {
            // 批次库存联动：成品批次登记（不存在则创建，来源=生产完工，效期按产品保质期自完工日起推算）
            registerProductBatch(merged);
            // 生产投入谱系桥接：原料批次（领料明细）→ 成品批次 写入 food 谱系（多对多，幂等）
            FoodBatch productBatch = foodBatchService.selectBatchByCode(merged.getBatchNo());
            if (productBatch != null)
            {
                foodBatchService.bridgeProductionEdges(merged.getWorkOrderNo(), productBatch.getBatchId(),
                        merged.getFinishId(), merged.getFinishNo(), SecurityUtils.getUsername());
            }
            genealogySyncService.syncComplete(
                    merged.getFinishNo(), merged.getWorkOrderNo(),
                    merged.getProductId(), merged.getProductCode(), merged.getProductName(),
                    merged.getBatchNo(), merged.getFinishQty(),
                    merged.getFinishBy(), merged.getFinishTime() != null ? merged.getFinishTime() : merged.getInboundTime());
        }
        return rows;
    }

    /**
     * 完工成品批次登记：批次已存在则直接同步批次库存；不存在则按生产完工来源创建
     * （批次状态=待检，待检放行走检验任务；效期=完工日期+产品保质期，保质期缺失则效期留空人工补录）
     */
    private void registerProductBatch(MmsFinishReceipt merged)
    {
        if (StringUtils.isEmpty(merged.getBatchNo()) || merged.getProductId() == null
                || merged.getFinishQty() == null || merged.getFinishQty().compareTo(java.math.BigDecimal.ZERO) <= 0)
        {
            return;
        }
        FoodBatch exist = foodBatchService.selectBatchByCode(merged.getBatchNo());
        if (exist != null && !merged.getProductId().equals(exist.getMaterialId()))
        {
            throw new ServiceException("完工批次【" + merged.getBatchNo() + "】归属物料与完工产品不一致，请核对");
        }
        if (exist == null)
        {
            FoodBatch batch = new FoodBatch();
            batch.setBatchCode(merged.getBatchNo());
            batch.setMaterialId(merged.getProductId());
            batch.setMaterialCode(merged.getProductCode());
            batch.setMaterialName(merged.getProductName());
            batch.setSourceType("2"); // 生产完工
            batch.setSourceBillType("finish");
            batch.setSourceBillId(merged.getFinishId());
            batch.setProduceDate(merged.getFinishTime() != null ? merged.getFinishTime() : merged.getInboundTime());
            batch.setReceiveDate(DateUtils.getNowDate());
            batch.setQtyUnit(merged.getUnit());
            // 保质期天数供 insertBatch 推算效期（完工日+保质期），未配置则效期留空人工补录
            batch.setShelfLifeDays(foodBatchService.selectShelfLifeDays(merged.getProductId()));
            batch.setCreateBy(SecurityUtils.getUsername());
            foodBatchService.insertBatch(batch);
        }
        // 批次库存同步（无仓库上下文，跨仓登记：优先复用该批次现有库存行仓库）
        batchStockService.addAcrossWarehouse(merged.getProductId(), merged.getBatchNo(), merged.getFinishQty(),
                SecurityUtils.getUsername(), null, null,
                merged.getFinishTime() != null ? merged.getFinishTime() : merged.getInboundTime(), null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteFinishReceiptByIds(Long[] FinishIds)
    {
        // ===== 谱系联动：撤销对应 complete 谱系（失败不阻断） =====
        for (Long id : FinishIds)
        {
            MmsFinishReceipt receipt = finishReceiptMapper.selectFinishReceiptById(id);
            if (receipt != null && "1".equals(receipt.getStatus()))
            {
                genealogySyncService.revoke("complete", receipt.getFinishNo());
            }
        }
        return finishReceiptMapper.deleteFinishReceiptByIds(FinishIds);
    }
}
