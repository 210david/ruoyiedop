package com.ruoyi.food.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodWip;
import com.ruoyi.food.domain.FoodWipTransfer;
import com.ruoyi.food.mapper.FoodWipMapper;
import com.ruoyi.food.mapper.FoodWipTransferMapper;
import com.ruoyi.food.service.IFoodWipTransferService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 工序转移单 Service实现
 *
 * 转移即生效：校验垛在制、目标工序必须大于当前工序且在路线内；
 * 转移后推进垛的当前工序并按目标工序标准时长重算预计出柜；
 * 转移到最后一道工序自动完成；损耗率超工序阈值推送告警（不阻断转移）。
 *
 * @author ruoyi
 */
@Service
public class FoodWipTransferServiceImpl implements IFoodWipTransferService
{
    private static final Logger log = LoggerFactory.getLogger(FoodWipTransferServiceImpl.class);

    @Autowired
    private FoodWipTransferMapper transferMapper;

    @Autowired
    private FoodWipMapper wipMapper;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public List<FoodWipTransfer> selectTransferList(FoodWipTransfer transfer)
    {
        return transferMapper.selectTransferList(transfer);
    }

    @Override
    public FoodWipTransfer selectTransferById(Long transferId)
    {
        return transferMapper.selectTransferById(transferId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertTransfer(FoodWipTransfer transfer)
    {
        if (transfer.getWipId() == null)
        {
            throw new ServiceException("垛不能为空");
        }
        FoodWip wip = wipMapper.selectWipById(transfer.getWipId());
        if (wip == null)
        {
            throw new ServiceException("垛不存在或已删除");
        }
        if (!"0".equals(wip.getWipStatus()))
        {
            throw new ServiceException("垛[" + wip.getWipNo() + "]当前状态为" + statusName(wip.getWipStatus()) + "，仅在制垛可工序转移");
        }
        if (transfer.getToStepSeq() == null || transfer.getToStepSeq() <= 0)
        {
            throw new ServiceException("转入工序序号不能为空");
        }
        if (wip.getCurrentStepSeq() != null && transfer.getToStepSeq() <= wip.getCurrentStepSeq())
        {
            throw new ServiceException("转入工序序号(" + transfer.getToStepSeq() + ")必须大于当前工序序号(" + wip.getCurrentStepSeq() + ")，工序不可回退");
        }
        if (transfer.getTransferQty() == null || transfer.getTransferQty().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("转移数量必须大于0");
        }
        BigDecimal lossQty = transfer.getLossQty() == null ? BigDecimal.ZERO : transfer.getLossQty();
        if (lossQty.compareTo(BigDecimal.ZERO) < 0)
        {
            throw new ServiceException("损耗数量不能为负数");
        }
        if (wip.getQty() != null && lossQty.add(transfer.getTransferQty()).compareTo(wip.getQty()) > 0)
        {
            throw new ServiceException("转移数量+损耗数量(" + lossQty.add(transfer.getTransferQty()) + ")不能超过垛现存数量(" + wip.getQty() + ")");
        }
        if (lossQty.compareTo(BigDecimal.ZERO) > 0 && StringUtils.isEmpty(transfer.getLossType()))
        {
            throw new ServiceException("有损耗时必须选择损耗类型");
        }
        if (wip.getRouteId() == null)
        {
            throw new ServiceException("垛[" + wip.getWipNo() + "]未挂工艺路线，无法按工序推进");
        }
        Map<String, Object> step = wipMapper.selectStepInfo(wip.getRouteId(), transfer.getToStepSeq());
        if (step == null)
        {
            throw new ServiceException("路线下不存在序号" + transfer.getToStepSeq() + "的工序，请核对工艺路线");
        }
        // 转移即生效
        Date transferTime = transfer.getTransferTime() != null ? transfer.getTransferTime() : DateUtils.getNowDate();
        transfer.setTransferTime(transferTime);
        transfer.setTransferBy(SecurityUtils.getUsername());
        transfer.setTransferNo(transferMapper.selectNextTransferNo());
        transfer.setWorkOrderId(wip.getWorkOrderId());
        transfer.setWorkOrderNo(wip.getWorkOrderNo());
        transfer.setWipNo(wip.getWipNo());
        transfer.setBatchId(wip.getBatchId());
        transfer.setBatchCode(wip.getBatchCode());
        transfer.setMaterialId(wip.getMaterialId());
        transfer.setMaterialCode(wip.getMaterialCode());
        transfer.setMaterialName(wip.getMaterialName());
        transfer.setFromStepSeq(wip.getCurrentStepSeq());
        transfer.setFromProcessName(wip.getCurrentProcessName());
        transfer.setToProcessName((String) step.get("process_name"));
        transfer.setLossQty(lossQty);
        // 损耗率 = 损耗 / (转入+损耗) × 100
        BigDecimal outTotal = lossQty.add(transfer.getTransferQty());
        BigDecimal lossRate = outTotal.compareTo(BigDecimal.ZERO) > 0
                ? lossQty.multiply(new BigDecimal(100)).divide(outTotal, 2, RoundingMode.HALF_UP) : BigDecimal.ZERO;
        transfer.setLossRate(lossRate);
        Object stdDaysObj = step.get("std_days");
        if (stdDaysObj != null)
        {
            BigDecimal stdDays = new BigDecimal(stdDaysObj.toString());
            transfer.setStdDays(stdDays);
            if (stdDays.compareTo(BigDecimal.ZERO) > 0)
            {
                transfer.setPlanStepEnd(DateUtils.addHours(transferTime, stdDays.multiply(new BigDecimal(24)).intValue()));
            }
        }
        transfer.setStatus("1");
        transfer.setCreateBy(SecurityUtils.getUsername());
        transfer.setCreateTime(DateUtils.getNowDate());
        int rows = transferMapper.insertTransfer(transfer);

        // 推进垛：当前工序/入柜时间/预计出柜/数量
        FoodWip updater = new FoodWip();
        updater.setWipId(wip.getWipId());
        updater.setCurrentStepSeq(transfer.getToStepSeq());
        updater.setCurrentProcessName(transfer.getToProcessName());
        updater.setStepStartTime(transferTime);
        updater.setPlanStepEnd(transfer.getPlanStepEnd());
        updater.setQty(transfer.getTransferQty());
        // 转移到最后一道工序（成品入库）自动完成
        Object maxSeqObj = step.get("max_seq");
        if (maxSeqObj != null && transfer.getToStepSeq() >= ((Number) maxSeqObj).intValue())
        {
            updater.setWipStatus("1");
            updater.setFinishTime(transferTime);
        }
        updater.setUpdateBy(SecurityUtils.getUsername());
        updater.setUpdateTime(DateUtils.getNowDate());
        wipMapper.updateWip(updater);

        // 损耗率超工序阈值告警（不阻断转移）
        Object lossMaxObj = step.get("loss_rate_max");
        if (lossQty.compareTo(BigDecimal.ZERO) > 0 && lossMaxObj != null)
        {
            BigDecimal lossMax = new BigDecimal(lossMaxObj.toString());
            if (lossRate.compareTo(lossMax) > 0)
            {
                try
                {
                    messageHelper.sendMessage(
                        "工序转移损耗超阈值：" + transfer.getTransferNo(),
                        "垛[" + wip.getWipNo() + "]转入[" + transfer.getToProcessName() + "]，损耗率" + lossRate
                                + "%超过工序阈值" + lossMax + "%，请核实修割/腌制操作",
                        "2", "2", "food", transfer.getTransferId(), "/mms/execution/wipTransfer", "food:wipTransfer:list", "1", "产出率损耗");
                }
                catch (Exception e)
                {
                    log.error("工序转移损耗告警推送失败（不阻断转移）：{}", transfer.getTransferNo(), e);
                }
            }
        }
        return rows;
    }

    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "在制";
            case "1": return "已完成";
            case "9": return "已关闭";
            default: return "未知(" + status + ")";
        }
    }
}
