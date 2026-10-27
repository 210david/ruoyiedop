package com.ruoyi.mms.service.impl;

import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.service.IQmsTraceService;
import com.ruoyi.mms.service.IMmsGenealogySyncService;

/**
 * MMS→QMS 谱系联动 Service
 *
 * 将 MMS 生产事件（领料=feeding、完工入库=complete）写入 QMS 谱系表 qms_lot_genealogy，
 * 由 QMS 追溯引擎统一提供正反向追溯、完整度看板、断点清单。
 *
 * 设计约定（批次追溯融合方案 2026-09-19）：
 * 1. 幂等：sourceType + sourceNo 为唯一业务事件键，重复调用自动跳过；
 *    feeding 的 sourceNo = 领料单号-明细序号（如 LL20260919001-1）
 *    complete 的 sourceNo = 完工入库单号
 * 2. 断点：发料明细未录批次号时仍写入谱系并记 breakFlag=1（原因"发料未录批次"），
 *    由完整度看板暴露问题，不在生产流程上强拦截；
 * 3. 隔离：谱系写入失败只记错误日志，不阻断发料/完工主流程（谱系可事后重试/补录）。
 *
 * @author ruoyi
 */
@Service
public class MmsGenealogySyncServiceImpl implements IMmsGenealogySyncService
{
    private static final Logger log = LoggerFactory.getLogger(MmsGenealogySyncServiceImpl.class);

    /** 谱系来源类型：领料（投料） */
    public static final String SOURCE_FEEDING = "feeding";
    /** 谱系来源类型：完工入库 */
    public static final String SOURCE_COMPLETE = "complete";
    /** 谱系来源类型：报工 */
    public static final String SOURCE_REPORT = "report";

    @Autowired
    private IQmsTraceService qmsTraceService;

    /**
     * 领料确认 → 写入投料谱系（一条明细一条谱系）
     */
    @Override
    public void syncFeeding(String issueNo, String workOrderNo, String processId, String processName,
                            Long materialId, String materialCode, String materialName,
                            String batchNo, java.math.BigDecimal issueQty, String operator, int seq)
    {
        try
        {
            QmsLotGenealogy event = new QmsLotGenealogy();
            event.setSourceType(SOURCE_FEEDING);
            event.setSourceNo(seq > 0 ? issueNo + "-" + seq : issueNo);
            event.setParentBatchNo(batchNo);
            event.setChildBatchNo(null); // 产出批次由完工入库回填
            event.setWorkOrderNo(workOrderNo);
            event.setOpId(processId);
            event.setOpName(processName);
            event.setMaterialId(materialId);
            event.setMaterialCode(materialCode);
            event.setMaterialName(materialName);
            event.setQtyIn(issueQty);
            event.setOperator(operator);
            event.setTraceTime(new Date());
            event.setCreateBy(SecurityUtils.getUsername());
            event.setRemark("领料确认自动生成：" + issueNo + (seq > 0 ? " 第" + seq + "行" : ""));
            if (StringUtils.isEmpty(batchNo))
            {
                event.setBreakReason("发料未录批次");
            }
            qmsTraceService.recordGenealogyEvent(event);
        }
        catch (Exception e)
        {
            log.error("领料谱系写入失败（不阻断领料主流程）：领料单 {} 第 {} 行，原因：{}", issueNo, seq, e.getMessage(), e);
        }
    }

    /**
     * 完工入库确认 → 写入完工谱系（产出侧），并回填该工单此前领料谱系的产出批次
     */
    @Override
    public void syncComplete(String finishNo, String workOrderNo, Long productId, String productCode,
                             String productName, String batchNo, java.math.BigDecimal finishQty,
                             String finishBy, Date finishTime)
    {
        try
        {
            // 1. 完工谱系：产出批次 → 下一工单/客户（childBatchNo 指向下游，此处先登记产出事实）
            QmsLotGenealogy event = new QmsLotGenealogy();
            event.setSourceType(SOURCE_COMPLETE);
            event.setSourceNo(finishNo);
            // 完工事件语义：只登记产出批次（child），不写 parent——避免 parent=child 自环
            event.setParentBatchNo(null);
            event.setChildBatchNo(batchNo);
            event.setWorkOrderNo(workOrderNo);
            event.setMaterialId(productId);
            event.setMaterialCode(productCode);
            event.setMaterialName(productName);
            event.setQtyOut(finishQty);
            event.setOperator(finishBy);
            event.setTraceTime(finishTime != null ? finishTime : new Date());
            event.setCreateBy(SecurityUtils.getUsername());
            event.setRemark("完工入库确认自动生成：" + finishNo);
            if (StringUtils.isEmpty(batchNo))
            {
                event.setBreakReason("完工未录批次");
            }
            qmsTraceService.recordGenealogyEvent(event);

            // 2. 回填：把该工单此前所有 feeding 断点谱系的产出批次补齐为本完工批次
            if (StringUtils.isNotEmpty(batchNo))
            {
                // 回填该工单全部断点谱系（feeding + report），产出批次统一为完工批次
                qmsTraceService.backfillChildBatchByWorkOrder(null, workOrderNo, batchNo);
            }
        }
        catch (Exception e)
        {
            log.error("完工谱系写入失败（不阻断完工主流程）：完工单 {}，原因：{}", finishNo, e.getMessage(), e);
        }
    }

    /**
     * 报工审核通过 → 写入报工谱系（report，工序维度核心事件）
     * 记录工序/产能单元/班组/操作员/合格数量，工序维度追溯由此补全。
     * parentBatchNo 为空记断点"报工无批次"，完工入库后按工单回填产出批次。
     */
    @Override
    public void syncReport(String reportNo, String workOrderNo, Long processId, String processName,
                           String resourceName, String teamName, java.math.BigDecimal goodQty,
                           String reportBy, Date reportTime)
    {
        try
        {
            QmsLotGenealogy event = new QmsLotGenealogy();
            event.setSourceType(SOURCE_REPORT);
            event.setSourceNo(reportNo);
            // 报工事件语义：只登记工序事实（工序/产线/数量），无投入批次；
            // parent 留 NULL（禁止用报工单号冒充批次），产出批次由完工入库按工单回填
            event.setParentBatchNo(null);
            event.setChildBatchNo(null);
            event.setWorkOrderNo(workOrderNo);
            event.setOpId(processId != null ? String.valueOf(processId) : null);
            event.setOpName(processName);
            event.setLineName(resourceName); // 产能单元映射谱系产线字段
            event.setOperator(reportBy);
            event.setQtyOut(goodQty); // 合格数记为产出数量
            event.setTraceTime(reportTime != null ? reportTime : new Date());
            event.setCreateBy(SecurityUtils.getUsername());
            event.setRemark("报工审核通过自动生成：" + reportNo
                    + (teamName != null ? "（班组：" + teamName + "）" : ""));
            event.setBreakReason("报工无批次");
            qmsTraceService.recordGenealogyEvent(event);
        }
        catch (Exception e)
        {
            log.error("报工谱系写入失败（不阻断报工主流程）：报工单 {}，原因：{}", reportNo, e.getMessage(), e);
        }
    }

    /**
     * 单据删除/取消 → 撤销对应谱系（按 sourceType+sourceNo 精确或前缀匹配）
     */
    @Override
    public void revoke(String sourceType, String sourceNo)
    {
        try
        {
            qmsTraceService.revokeGenealogyEvent(sourceType, sourceNo);
        }
        catch (Exception e)
        {
            log.error("谱系撤销失败（不阻断主流程）：{}-{}，原因：{}", sourceType, sourceNo, e.getMessage(), e);
        }
    }
}
