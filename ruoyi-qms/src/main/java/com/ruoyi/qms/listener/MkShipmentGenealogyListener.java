package com.ruoyi.qms.listener;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.impl.MkShipmentGenealogyEvent;
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.domain.vo.TraceFallbackVO;
import com.ruoyi.qms.mapper.QmsTraceFallbackMapper;
import com.ruoyi.qms.service.IQmsTraceService;

/**
 * MK 发货谱系联动监听器
 *
 * 消费营销模块发货事件（MkShipmentGenealogyEvent），写入批次谱系 qms_lot_genealogy
 * （sourceType=shipment），补齐追溯链"原料批次→工单→成品批次→发货客户→分销去向"中的
 * "成品批次→发货客户"一环：
 *
 * 1. confirm：按出库单号查明细批次（wms_outbound_order_detail.batch_no），
 *    逐条写入"成品批次(parent)→发货客户"谱系；childBatchNo 为空属终端节点语义（不算断点）。
 *    sourceNo 幂等键 = 发货单号（多明细时 发货单号-序号，与 feeding 明细谱系规则一致）。
 * 2. revoke：删除发货单时按 sourceType=shipment + 发货单号（前缀匹配）撤销谱系。
 *
 * 隔离约定（与 MMS 谱系联动一致）：谱系写入失败只记错误日志，不阻断发货/删除主流程。
 *
 * @author ruoyi
 */
@Component
public class MkShipmentGenealogyListener implements ApplicationListener<MkShipmentGenealogyEvent>
{
    private static final Logger log = LoggerFactory.getLogger(MkShipmentGenealogyListener.class);

    /** 谱系来源类型：销售发货 */
    public static final String SOURCE_SHIPMENT = "shipment";

    @Autowired
    private IQmsTraceService qmsTraceService;

    @Autowired
    private QmsTraceFallbackMapper traceFallbackMapper;

    @Override
    public void onApplicationEvent(MkShipmentGenealogyEvent event)
    {
        try
        {
            if (MkShipmentGenealogyEvent.ACTION_REVOKE.equals(event.getAction()))
            {
                handleRevoke(event);
            }
            else
            {
                handleConfirm(event);
            }
        }
        catch (Exception e)
        {
            log.error("发货谱系联动失败（不阻断主流程）：发货单 {} action {}，原因：{}",
                    event.getShipmentNo(), event.getAction(), e.getMessage(), e);
        }
    }

    /**
     * 确认发货 → 按出库明细批次写入发货谱系（成品批次 → 发货客户）
     */
    private void handleConfirm(MkShipmentGenealogyEvent event)
    {
        if (StringUtils.isEmpty(event.getShipmentNo()))
        {
            log.warn("发货谱系联动缺少发货单号，跳过");
            return;
        }
        if (StringUtils.isEmpty(event.getOutboundOrderNo()))
        {
            log.warn("发货谱系联动缺少出库单号，跳过：发货单 {}", event.getShipmentNo());
            return;
        }
        List<TraceFallbackVO.OutboundDetailSummary> details = traceFallbackMapper.selectOutboundDetailsByOrderNo(event.getOutboundOrderNo());
        if (details == null || details.isEmpty())
        {
            log.warn("出库单 {} 未找到明细，发货谱系未写入：发货单 {}", event.getOutboundOrderNo(), event.getShipmentNo());
            return;
        }
        boolean multi = details.size() > 1;
        int seq = 0;
        int written = 0;
        for (TraceFallbackVO.OutboundDetailSummary d : details)
        {
            seq++;
            // 出库明细未录批次：无法建立批次关联，跳过（由降级追溯/完整度看板兜底暴露）
            if (d == null || StringUtils.isEmpty(d.getBatchNo()))
            {
                log.warn("出库单 {} 第 {} 行未录批次，跳过谱系写入：发货单 {}", event.getOutboundOrderNo(), seq, event.getShipmentNo());
                continue;
            }
            QmsLotGenealogy node = new QmsLotGenealogy();
            node.setSourceType(SOURCE_SHIPMENT);
            node.setSourceNo(multi ? event.getShipmentNo() + "-" + seq : event.getShipmentNo());
            // 投入侧=成品批次；产出侧为发货客户（非批次），childBatchNo 保持空（终端节点不算断点）
            node.setParentBatchNo(d.getBatchNo());
            node.setChildBatchNo(null);
            node.setMaterialId(d.getMaterialId());
            node.setMaterialCode(d.getMaterialCode());
            node.setMaterialName(d.getMaterialName());
            node.setQtyOut(d.getActualQty());
            node.setOperator(event.getOperator());
            node.setCustomerId(event.getCustomerId());
            node.setCustomerName(event.getCustomerName());
            node.setTraceTime(event.getShipmentDate() != null ? event.getShipmentDate() : new java.util.Date());
            node.setCreateBy(StringUtils.isNotEmpty(event.getOperator()) ? event.getOperator() : "shipment");
            node.setRemark("发货确认自动生成：" + event.getShipmentNo()
                    + (StringUtils.isNotEmpty(event.getCustomerName()) ? " → " + event.getCustomerName() : ""));
            qmsTraceService.recordGenealogyEvent(node);
            written++;
        }
        log.info("发货谱系写入完成：发货单 {} 出库单 {} 客户 {}，写入 {} 条",
                event.getShipmentNo(), event.getOutboundOrderNo(), event.getCustomerName(), written);
    }

    /**
     * 删除发货单 → 撤销该发货单写入的 shipment 谱系（前缀匹配：发货单号 / 发货单号-序号）
     */
    private void handleRevoke(MkShipmentGenealogyEvent event)
    {
        if (StringUtils.isEmpty(event.getShipmentNo()))
        {
            return;
        }
        int revoked = qmsTraceService.revokeGenealogyEvent(SOURCE_SHIPMENT, event.getShipmentNo());
        if (revoked > 0)
        {
            log.info("发货谱系撤销完成：发货单 {}，撤销 {} 条", event.getShipmentNo(), revoked);
        }
    }
}
