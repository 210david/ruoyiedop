package com.ruoyi.qms.listener;

import java.math.BigDecimal;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.DictUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodCcpOverLimitEvent;
import com.ruoyi.food.domain.FoodCcpRecord;
import com.ruoyi.food.service.IFoodBatchService;
import com.ruoyi.food.service.IFoodCcpService;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.service.IQmsNcrService;

/**
 * CCP超限事件监听（qms域）
 *
 * food域发布 FoodCcpOverLimitEvent（Spring同步事件，同一线程同一事务）：
 * 1. 创建不合格品报告NCR（来源ccp，缺陷等级=严重）
 * 2. 回写CCP记录的联动NCR编号
 * 3. 冻结关联批次（仅待检/合格状态）
 *
 * @author ruoyi
 */
@Component
public class FoodCcpOverLimitListener
{
    private static final Logger log = LoggerFactory.getLogger(FoodCcpOverLimitListener.class);

    @Autowired
    private IQmsNcrService qmsNcrService;

    @Autowired
    private IFoodCcpService ccpService;

    @Autowired
    private IFoodBatchService foodBatchService;

    @EventListener
    @Transactional(rollbackFor = Exception.class)
    public void onCcpOverLimit(FoodCcpOverLimitEvent event)
    {
        FoodCcpRecord record = event.getRecord();
        // 1. 创建NCR（不合格品控制）
        QmsNcr ncr = new QmsNcr();
        ncr.setSourceType("ccp");
        ncr.setSourceId(record.getRecordId());
        ncr.setSourceNo(record.getRecordNo());
        ncr.setMaterialId(record.getMaterialId());
        ncr.setMaterialCode(record.getMaterialCode());
        ncr.setMaterialName(record.getMaterialName());
        ncr.setBatchNo(record.getBatchCode());
        ncr.setDefectLevel("2");
        ncr.setDefectDesc(buildDefectDesc(record));
        ncr.setDefectQty(BigDecimal.ONE);
        ncr.setNcrStatus("0");
        ncr.setIsolateFlag("0");
        ncr.setDiscovererName(record.getRecorder());
        ncr.setDiscoverTime(new Date());
        ncr.setRemark("CCP记录" + record.getRecordNo() + "超限，系统自动创建");
        qmsNcrService.insertNcr(ncr);
        // 2. 回写记录的联动NCR编号
        ccpService.updateOverLink(record.getRecordId(), ncr.getNcrId(), ncr.getNcrNo());
        // 3. 冻结关联批次（仅待检0/合格1状态可冻结）
        if (record.getBatchId() != null)
        {
            FoodBatch batch = foodBatchService.selectBatchById(record.getBatchId());
            if (batch != null && ("0".equals(batch.getBatchStatus()) || "1".equals(batch.getBatchStatus())))
            {
                foodBatchService.freezeBatch(batch.getBatchId(), "CCP超限：" + record.getRecordNo());
            }
        }
        log.info("CCP记录 {} 超限，已联动创建NCR {}", record.getRecordNo(), ncr.getNcrNo());
    }

    private String buildDefectDesc(FoodCcpRecord record)
    {
        return "CCP超限：" + record.getCcpName() + " 实测" + buildMeasuredText(record)
                + "，超出 " + record.getLimitSnapshot();
    }

    /**
     * 按监控项类型拼实测值文本（与限值快照口径一致）
     */
    private String buildMeasuredText(FoodCcpRecord record)
    {
        switch (record.getMonitorType() == null ? "" : record.getMonitorType())
        {
            case "1": return record.getTempValue() + "℃";
            case "2": return record.getHumidityValue() + "%";
            case "3": return record.getSaltRate() + "%";
            case "4":
                String label = DictUtils.getDictLabel("food_mold_status", record.getMoldStatus());
                return StringUtils.isNotEmpty(label) ? label : StringUtils.defaultString(record.getMoldStatus());
            case "5": return record.getMonitorValue() != null ? record.getMonitorValue().toPlainString() : "-";
            default: return "-";
        }
    }
}
