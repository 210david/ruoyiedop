package com.ruoyi.mms.service;

import java.util.Date;

/**
 * MMS→QMS 谱系联动 Service接口
 *
 * 生产事件（领料/完工入库）写入 QMS 谱系表，实现批次追溯融合：
 * 领料确认 → feeding 谱系；完工入库确认 → complete 谱系 + 回填产出批次。
 * 所有方法均为尽力而为语义：谱系写入失败不阻断生产主流程。
 *
 * @author ruoyi
 */
public interface IMmsGenealogySyncService
{
    /**
     * 领料确认 → 写入投料谱系（幂等：领料单号-明细序号）
     */
    void syncFeeding(String issueNo, String workOrderNo, String processId, String processName,
                     Long materialId, String materialCode, String materialName,
                     String batchNo, java.math.BigDecimal issueQty, String operator, int seq);

    /**
     * 完工入库确认 → 写入完工谱系并回填该工单领料谱系的产出批次
     */
    void syncComplete(String finishNo, String workOrderNo, Long productId, String productCode,
                      String productName, String batchNo, java.math.BigDecimal finishQty,
                      String finishBy, Date finishTime);

    /**
     * 报工审核通过 → 写入报工谱系（report，工序维度：工序/产能单元/班组/合格数）
     */
    void syncReport(String reportNo, String workOrderNo, Long processId, String processName,
                    String resourceName, String teamName, java.math.BigDecimal goodQty,
                    String reportBy, Date reportTime);

    /**
     * 单据删除/取消 → 撤销对应谱系
     */
    void revoke(String sourceType, String sourceNo);
}
