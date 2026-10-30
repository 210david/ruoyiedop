package com.ruoyi.mk.service.impl;

import java.util.Date;
import org.springframework.context.ApplicationEvent;

/**
 * MK→QMS 发货谱系联动事件
 *
 * 营销模块与质量模块存在依赖方向约束（ruoyi-qms 依赖 ruoyi-mk，mk 不可反向依赖），
 * 因此发货确认/删除时通过 Spring 事件发布，由 QMS 侧监听器（MkShipmentGenealogyListener）
 * 消费并写入批次谱系 qms_lot_genealogy（sourceType=shipment），补齐
 * "原料批次→工单→成品批次→发货客户→分销去向"追溯链中的"成品批次→发货客户"一环。
 *
 * QMS 侧监听示例：
 * <pre>
 * &#64;TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
 * public void onShipmentGenealogy(MkShipmentGenealogyEvent event) { ... }
 * </pre>
 *
 * @author ruoyi
 */
public class MkShipmentGenealogyEvent extends ApplicationEvent
{
    private static final long serialVersionUID = 1L;

    /** 事件动作：确认发货（写谱系） */
    public static final String ACTION_CONFIRM = "confirm";
    /** 事件动作：删除发货单（撤销谱系） */
    public static final String ACTION_REVOKE = "revoke";

    /** 动作 */
    private final String action;
    /** 发货单ID */
    private final Long shipmentId;
    /** 发货单号 */
    private final String shipmentNo;
    /** 订单ID */
    private final Long orderId;
    /** 客户ID */
    private final Long customerId;
    /** 客户名称 */
    private final String customerName;
    /** 出库单号（批次来源：wms_outbound_order_detail.batch_no） */
    private final String outboundOrderNo;
    /** 发货人（谱系操作员） */
    private final String operator;
    /** 发货时间（谱系时间） */
    private final Date shipmentDate;

    public MkShipmentGenealogyEvent(Object source, String action, Long shipmentId, String shipmentNo,
                                    Long orderId, Long customerId, String customerName,
                                    String outboundOrderNo, String operator, Date shipmentDate)
    {
        super(source);
        this.action = action;
        this.shipmentId = shipmentId;
        this.shipmentNo = shipmentNo;
        this.orderId = orderId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.outboundOrderNo = outboundOrderNo;
        this.operator = operator;
        this.shipmentDate = shipmentDate;
    }

    public String getAction() { return action; }
    public Long getShipmentId() { return shipmentId; }
    public String getShipmentNo() { return shipmentNo; }
    public Long getOrderId() { return orderId; }
    public Long getCustomerId() { return customerId; }
    public String getCustomerName() { return customerName; }
    public String getOutboundOrderNo() { return outboundOrderNo; }
    public String getOperator() { return operator; }
    public Date getShipmentDate() { return shipmentDate; }
}
