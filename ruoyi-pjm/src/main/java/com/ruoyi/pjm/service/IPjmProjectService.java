package com.ruoyi.pjm.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.pjm.domain.PjmProject;
import com.ruoyi.pjm.domain.PjmWbsTask;

/**
 * 项目立项 Service 接口
 */
public interface IPjmProjectService
{
    PjmProject selectProjectById(Long projectId);
    List<PjmProject> selectProjectList(PjmProject project);
    int insertProject(PjmProject project);
    int updateProject(PjmProject project);
    int deleteProjectByIds(Long[] projectIds);

    /** 审核项目 */
    int auditProject(Long projectId, String status, String auditOpinion);

    /** 项目结项归档（执行中 → 已完成） */
    int closeProject(Long projectId);

    /** ★ 刷新项目采购汇总（来自 PMS） */
    void refreshPurchaseSummary(Long projectId);

    /** ★ 项目采购汇总（读接口） */
    Map<String, Object> selectPurchaseSummary(Long projectId);

    /** ★ 从项目/任务发起采购 */
    Long buildPurchaseRequest(Long projectId, Long taskId, String title, String purpose,
                              List<Map<String, Object>> details);

    /** ★ 项目采购库存检查：按物料批量返回各仓库可用库存 */
    List<Map<String, Object>> stockCheck(List<Long> materialIds);

    /**
     * ★ 项目采购执行：有库存部分生成 WMS 出库单（领料出库），无库存部分生成 PMS 采购申请
     * @param payload { projectId, warehouseId, warehouseName, outboundItems[], purchaseMode(1入库再出库/2直送项目点), purchaseTitle, expectDate, purchaseItems[] }
     * @return { outboundOrderId, outboundOrderNo, requestId, requestNo }
     */
    Map<String, Object> executeProjectPurchase(Map<String, Object> payload);
}
