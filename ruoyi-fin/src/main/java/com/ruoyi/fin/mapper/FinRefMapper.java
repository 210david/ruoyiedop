package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.vo.ApInvoiceRefVO;
import com.ruoyi.fin.domain.vo.ArReceiptRefVO;
import com.ruoyi.fin.domain.vo.ArShipmentRefVO;
import com.ruoyi.fin.domain.vo.InboundCostRefVO;
import com.ruoyi.fin.domain.vo.InvoiceRefVO;
import com.ruoyi.fin.domain.vo.OutboundCostRefVO;
import com.ruoyi.fin.domain.vo.PayrollBatchRefVO;
import com.ruoyi.fin.domain.vo.ShipmentRefVO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 跨模块只读数据访问 Mapper
 *
 * <p>【强制约束】本接口及对应 XML 中，对既有模块的表只允许 SELECT，
 * 严禁出现 INSERT / UPDATE / DELETE。交付前用以下脚本校验：</p>
 * <pre>
 *   grep -rlE "(INSERT INTO|UPDATE|DELETE FROM)\s+(pms_|wms_|mk_|mms_|qms_|biz_hr_)" \
 *     ruoyi-fin/src/main/resources/mapper/ &amp;&amp; echo "违规" || echo "通过"
 * </pre>
 *
 * <p>严禁通过 @Autowired 注入既有模块的 Service 或 Mapper Bean，
 * 以避免编译期耦合与误写风险。统一在本 Mapper 内以 SQL 只读访问。</p>
 *
 * @author ruoyi
 */
public interface FinRefMapper
{
    /**
     * 扫描已审核但尚未记账的进项发票。
     *
     * <p>对应 pms_invoice.status='2'（已审核）。</p>
     *
     * @param limit 单批上限
     * @return 待记账发票列表
     */
    List<InvoiceRefVO> selectUnbookedInvoices(@Param("limit") int limit);

    /**
     * 扫描已发货但尚未记账的发货单。
     *
     * <p>对应 mk_shipment.status='1'（已发货）。</p>
     *
     * @param limit 单批上限
     * @return 待记账发货单列表
     */
    List<ShipmentRefVO> selectUnbookedShipments(@Param("limit") int limit);

    /**
     * 扫描已发放但尚未记账的薪酬批次。
     *
     * <p>对应 biz_hr_payroll_batch.audit_status='3'（已发放）。</p>
     *
     * @param limit 单批上限
     * @return 待记账薪酬批次列表
     */
    List<PayrollBatchRefVO> selectUnbookedPayrolls(@Param("limit") int limit);

    /**
     * 查询供应商主数据（只读 wms_supplier，含银行/账期/税号）。
     *
     * @param supplierId 供应商ID
     * @return 供应商信息
     */
    InvoiceRefVO selectSupplierInfo(@Param("supplierId") Long supplierId);

    /**
     * 查询物料税率（只读 wms_material 与既有字典，用于成本兜底）。
     *
     * @param materialCode 物料编码
     * @return 物料信息
     */
    InvoiceRefVO selectMaterialInfo(@Param("materialCode") String materialCode);

    /**
     * 扫描已入库但尚未计价的入库明细（成本入口）。
     *
     * <p>对应 wms_inbound_order.status='3'（已完成）。</p>
     * <p>幂等：用 NOT EXISTS 判断该明细是否已在成本台账建档。</p>
     *
     * @param limit 单批上限
     * @return 待计价明细
     */
    List<InboundCostRefVO> selectUnpricedInbound(@Param("limit") int limit);

    /**
     * 扫描已出库但尚未结转成本的出库明细。
     *
     * <p>对应 wms_outbound_order.status='3'（已完成）。</p>
     * <p>幂等：用 NOT EXISTS 判断是否已在成本结转流水中。</p>
     *
     * @param limit 单批上限
     * @return 待结转明细
     */
    List<OutboundCostRefVO> selectUncarriedOutbound(@Param("limit") int limit);

    /**
     * 扫描已审核但尚未生成应付单的进项发票。
     *
     * <p>对应 pms_invoice.status='2'（已审核）。</p>
     * <p>幂等：NOT EXISTS 判断该发票是否已生成应付单。</p>
     *
     * @param limit 单批上限
     * @return 待生成应付单的发票
     */
    List<ApInvoiceRefVO> selectUnmatchedInvoice(@Param("limit") int limit);

    /**
     * 查询供应商付款汇总（只读 pms_invoice_payment）。
     *
     * <p>用于把已付金额同步到应付单，避免财务重复登记付款。</p>
     *
     * @param invoiceIds 发票ID集合
     * @return 发票ID与已付金额的对应关系
     */
    List<ApInvoiceRefVO> selectInvoicePaidAmount(@Param("invoiceIds") List<Long> invoiceIds);

    /**
     * 扫描已审核但尚未生成应收单的发货单。
     *
     * <p>对应 mk_shipment.status IN ('2','3','4')（审核通过/已出库/已签收）。</p>
     * <p>幂等：NOT EXISTS 判断是否已生成应收单。</p>
     *
     * @param limit 单批上限
     * @return 待生成应收单的发货单
     */
    List<ArShipmentRefVO> selectUnmatchedShipment(@Param("limit") int limit);

    /**
     * 查询客户收款汇总（只读 mk_payment_record + mk_payment_plan）。
     *
     * <p>用于把已收金额同步到应收单，避免财务重复登记收款。
     * 仅取 confirm_status='1'（已确认）的收款记录。</p>
     *
     * @param customerIds 客户ID集合
     * @return 客户ID与已收金额的对应关系
     */
    List<ArReceiptRefVO> selectCustomerReceivedAmount(@Param("customerIds") List<Long> customerIds);
}
