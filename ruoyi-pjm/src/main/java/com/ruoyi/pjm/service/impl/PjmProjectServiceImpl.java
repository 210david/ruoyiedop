package com.ruoyi.pjm.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.annotation.DataScope;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.pjm.domain.PjmProject;
import com.ruoyi.pjm.domain.PjmProjectAttachment;
import com.ruoyi.pjm.domain.PjmProjectMember;
import com.ruoyi.pjm.domain.PjmProjectProcurement;
import com.ruoyi.pjm.domain.PjmWbsTask;
import com.ruoyi.pjm.mapper.PjmProjectMapper;
import com.ruoyi.pjm.mapper.PjmProjectAttachmentMapper;
import com.ruoyi.pjm.mapper.PjmProjectMemberMapper;
import com.ruoyi.pjm.mapper.PjmProjectProcurementMapper;
import com.ruoyi.pjm.mapper.PjmWbsTaskMapper;
import com.ruoyi.pjm.service.IPjmProjectService;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.domain.PmsPurchaseRequestDetail;
import com.ruoyi.pms.service.IPmsPurchaseRequestService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.wms.domain.WmsOutboundOrder;
import com.ruoyi.wms.domain.WmsOutboundOrderDetail;
import com.ruoyi.wms.domain.WmsWarehouse;
import com.ruoyi.wms.service.IWmsInventoryService;
import com.ruoyi.wms.service.IWmsOutboundOrderService;
import com.ruoyi.wms.service.IWmsWarehouseService;

/**
 * 项目立项 Service 实现
 */
@Service
public class PjmProjectServiceImpl implements IPjmProjectService
{
    private static final Logger log = LoggerFactory.getLogger(PjmProjectServiceImpl.class);

    @Autowired
    private PjmProjectMapper projectMapper;

    @Autowired
    private PjmWbsTaskMapper wbsTaskMapper;

    @Autowired
    private IMkNumberRuleService numberRuleService;

    @Autowired
    private IPmsPurchaseRequestService pmsRequestService;

    @Autowired
    private PjmProjectAttachmentMapper attachmentMapper;

    @Autowired
    private PjmProjectProcurementMapper procurementMapper;

    @Autowired
    private PjmProjectMemberMapper memberMapper;

    @Autowired
    private ISysUserService sysUserService;

    @Autowired
    private IWmsOutboundOrderService wmsOutboundOrderService;

    @Autowired
    private IWmsInventoryService wmsInventoryService;

    @Autowired
    private IWmsWarehouseService wmsWarehouseService;

    @Override
    public PjmProject selectProjectById(Long projectId)
    {
        PjmProject p = projectMapper.selectProjectById(projectId);
        if (p != null)
        {
            p.setAttachmentList(attachmentMapper.selectByProjectId(projectId));
            p.setProcurementList(procurementMapper.selectByProjectId(projectId));
            p.setMemberList(memberMapper.selectByProjectId(projectId));
            PjmWbsTask q = new PjmWbsTask();
            q.setProjectId(projectId);
            p.setWbsTaskList(wbsTaskMapper.selectTaskList(q));
        }
        return p;
    }

    @Override
    @DataScope(deptAlias = "pjm_project")
    public List<PjmProject> selectProjectList(PjmProject project)
    {
        return projectMapper.selectProjectList(project);
    }

    @Override
    @Transactional
    public int insertProject(PjmProject project)
    {
        if (project.getProjectNo() == null || project.getProjectNo().isEmpty())
        {
            try
            {
                String no = numberRuleService.generateNumber("PJM_PROJECT");
                project.setProjectNo(no);
            }
            catch (Exception e)
            {
                log.error("生成项目编号失败", e);
                throw new RuntimeException("生成项目编号失败，请检查编号规则配置", e);
            }
        }
        project.setStatus("0");
        project.setDelFlag("0");
        project.setCreateTime(new Date());
        project.setCreateBy(SecurityUtils.getUsername());
        int rows = projectMapper.insertProject(project);

        // ★ 级联保存附件
        if (project.getAttachmentList() != null)
        {
            for (PjmProjectAttachment att : project.getAttachmentList())
            {
                att.setProjectId(project.getProjectId());
                if (att.getAttachmentType() == null) att.setAttachmentType("1");
                if (att.getDelFlag() == null) att.setDelFlag("0");
                if (att.getCreateBy() == null || att.getCreateBy().isEmpty()) att.setCreateBy(SecurityUtils.getUsername());
                attachmentMapper.insertPjmProjectAttachment(att);
            }
        }
        // ★ 级联保存采购清单
        if (project.getProcurementList() != null)
        {
            int idx = 1;
            for (PjmProjectProcurement proc : project.getProcurementList())
            {
                proc.setProjectId(project.getProjectId());
                if (proc.getItemNo() == null) proc.setItemNo(idx++);
                if (proc.getDelFlag() == null) proc.setDelFlag("0");
                if (proc.getCreateBy() == null || proc.getCreateBy().isEmpty()) proc.setCreateBy(SecurityUtils.getUsername());
                if (proc.getQty() != null && proc.getUnitPrice() != null)
                {
                    proc.setAmount(proc.getQty().multiply(proc.getUnitPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                procurementMapper.insertPjmProjectProcurement(proc);
            }
        }
        // ★ 级联保存项目团队
        if (project.getMemberList() != null)
        {
            for (PjmProjectMember m : project.getMemberList())
            {
                m.setProjectId(project.getProjectId());
                if (m.getRoleType() == null || m.getRoleType().isEmpty()) m.setRoleType("3");
                if (m.getDelFlag() == null) m.setDelFlag("0");
                if (m.getCreateBy() == null || m.getCreateBy().isEmpty()) m.setCreateBy(SecurityUtils.getUsername());
                // ★ 若只有姓名没有 userId，按姓名匹配 sys_user 自动回填 userId/deptId/deptName
                enrichMemberByUserName(m);
                memberMapper.insertMember(m);
            }
        }
        // ★ 级联保存 WBS 任务分解
        if (project.getWbsTaskList() != null)
        {
            for (PjmWbsTask t : project.getWbsTaskList())
            {
                t.setTaskId(null);
                t.setProjectId(project.getProjectId());
                if (t.getTaskType() == null) t.setTaskType("1");
                if (t.getStatus() == null || t.getStatus().isEmpty()) t.setStatus("0");
                if (t.getProgress() == null) t.setProgress(BigDecimal.ZERO);
                if (t.getDelFlag() == null || t.getDelFlag().isEmpty()) t.setDelFlag("0");
                if (t.getCreateBy() == null || t.getCreateBy().isEmpty()) t.setCreateBy(SecurityUtils.getUsername());
                wbsTaskMapper.insertTask(t);
            }
        }
        return rows;
    }

    /**
     * 根据成员姓名自动匹配系统用户，回填 userId/deptId/deptName。
     * 匹配失败保持原值（允许只存姓名文本）。
     */
    private void enrichMemberByUserName(PjmProjectMember m)
    {
        if (m.getUserId() != null || m.getUserName() == null || m.getUserName().isEmpty())
        {
            return;
        }
        try
        {
            SysUser q = new SysUser();
            q.setNickName(m.getUserName().trim());
            List<SysUser> users = sysUserService.selectUserList(q);
            if (users != null && !users.isEmpty())
            {
                SysUser hit = users.stream()
                        .filter(u -> m.getUserName().trim().equals(u.getNickName()))
                        .findFirst().orElse(users.get(0));
                m.setUserId(hit.getUserId());
                if (hit.getDeptId() != null)
                {
                    m.setDeptId(hit.getDeptId());
                    if (hit.getDept() != null && hit.getDept().getDeptName() != null)
                    {
                        m.setDeptName(hit.getDept().getDeptName());
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.warn("按姓名[{}]匹配系统用户失败：{}", m.getUserName(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public int updateProject(PjmProject project)
    {
        project.setUpdateTime(new Date());
        project.setUpdateBy(SecurityUtils.getUsername());
        int rows = projectMapper.updateProject(project);

        // ★ 级联保存附件：清旧存新
        if (project.getAttachmentList() != null)
        {
            attachmentMapper.deleteByProjectId(project.getProjectId());
            for (PjmProjectAttachment att : project.getAttachmentList())
            {
                att.setAttachmentId(null);
                att.setProjectId(project.getProjectId());
                if (att.getDelFlag() == null) att.setDelFlag("0");
                attachmentMapper.insertPjmProjectAttachment(att);
            }
        }
        // ★ 级联保存采购清单：清旧存新
        if (project.getProcurementList() != null)
        {
            procurementMapper.deleteByProjectId(project.getProjectId());
            int idx = 1;
            for (PjmProjectProcurement proc : project.getProcurementList())
            {
                proc.setProcurementId(null);
                proc.setProjectId(project.getProjectId());
                if (proc.getItemNo() == null) proc.setItemNo(idx++);
                if (proc.getDelFlag() == null) proc.setDelFlag("0");
                if (proc.getQty() != null && proc.getUnitPrice() != null)
                {
                    proc.setAmount(proc.getQty().multiply(proc.getUnitPrice()).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                procurementMapper.insertPjmProjectProcurement(proc);
            }
        }
        // ★ 级联保存项目团队：清旧存新
        if (project.getMemberList() != null)
        {
            memberMapper.deleteByProjectId(project.getProjectId());
            for (PjmProjectMember m : project.getMemberList())
            {
                m.setMemberId(null);
                m.setProjectId(project.getProjectId());
                if (m.getRoleType() == null || m.getRoleType().isEmpty()) m.setRoleType("3");
                if (m.getDelFlag() == null) m.setDelFlag("0");
                enrichMemberByUserName(m);
                memberMapper.insertMember(m);
            }
        }
        // ★ 级联保存 WBS 任务分解：清旧存新
        if (project.getWbsTaskList() != null)
        {
            wbsTaskMapper.deleteByProjectId(project.getProjectId());
            for (PjmWbsTask t : project.getWbsTaskList())
            {
                t.setTaskId(null);
                t.setParentTaskId(null); // 立项阶段暂不处理父子关系
                t.setProjectId(project.getProjectId());
                if (t.getTaskType() == null) t.setTaskType("1");
                if (t.getStatus() == null || t.getStatus().isEmpty()) t.setStatus("0");
                if (t.getProgress() == null) t.setProgress(BigDecimal.ZERO);
                if (t.getDelFlag() == null || t.getDelFlag().isEmpty()) t.setDelFlag("0");
                if (t.getCreateBy() == null || t.getCreateBy().isEmpty()) t.setCreateBy(SecurityUtils.getUsername());
                wbsTaskMapper.insertTask(t);
            }
        }
        return rows;
    }

    @Override
    @Transactional
    public int deleteProjectByIds(Long[] projectIds)
    {
        return projectMapper.deleteProjectByIds(projectIds);
    }

    @Override
    @Transactional
    public int auditProject(Long projectId, String status, String auditOpinion)
    {
        PjmProject p = new PjmProject();
        p.setProjectId(projectId);
        p.setStatus(status);
        p.setAuditBy(SecurityUtils.getUsername());
        p.setAuditTime(new Date());
        p.setAuditOpinion(auditOpinion);
        int rows = projectMapper.updateProject(p);

        // ★ 审批通过后自动刷新采购汇总
        if ("2".equals(status))
        {
            refreshPurchaseSummary(projectId);
        }
        return rows;
    }

    @Override
    @Transactional
    public int closeProject(Long projectId)
    {
        PjmProject existing = projectMapper.selectProjectById(projectId);
        if (existing == null)
        {
            throw new ServiceException("项目不存在");
        }
        if (!"3".equals(existing.getStatus()))
        {
            throw new ServiceException("仅执行中的项目可以结项");
        }
        PjmProject p = new PjmProject();
        p.setProjectId(projectId);
        p.setStatus("4");
        p.setActualEndDate(new Date());
        p.setUpdateBy(SecurityUtils.getUsername());
        p.setUpdateTime(new Date());
        // 结项时进度置为 100%
        p.setProgress(new BigDecimal("100"));
        return projectMapper.updateProject(p);
    }

    @Override
    public void refreshPurchaseSummary(Long projectId)
    {
        projectMapper.refreshPurchaseSummary(projectId);
    }

    @Override
    public Map<String, Object> selectPurchaseSummary(Long projectId)
    {
        PjmProject p = projectMapper.selectProjectById(projectId);
        if (p == null)
        {
            throw new ServiceException("项目不存在");
        }
        Map<String, Object> summary = new HashMap<>();
        summary.put("projectId", projectId);
        summary.put("projectNo", p.getProjectNo());
        summary.put("projectName", p.getProjectName());
        summary.put("budgetAmount", p.getBudgetAmount());
        summary.put("plannedPurchase", p.getPlannedPurchase());
        summary.put("actualPurchase", p.getActualPurchase());
        summary.put("receivedAmount", p.getReceivedAmount());
        summary.put("invoiceAmount", p.getInvoiceAmount());
        summary.put("paidAmount", p.getPaidAmount());
        summary.put("purchaseCount", p.getPurchaseCount());
        return summary;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long buildPurchaseRequest(Long projectId, Long taskId, String title, String purpose,
                                      List<Map<String, Object>> details)
    {
        PjmProject project = projectMapper.selectProjectById(projectId);
        if (project == null)
        {
            throw new ServiceException("项目不存在");
        }

        PjmWbsTask task = null;
        if (taskId != null)
        {
            task = wbsTaskMapper.selectTaskById(taskId);
            if (task == null || !task.getProjectId().equals(projectId))
            {
                throw new ServiceException("WBS 任务不存在或不属于该项目");
            }
        }

        // 组装 PMS 采购申请
        PmsPurchaseRequest req = new PmsPurchaseRequest();
        req.setTitle(title != null ? title : project.getProjectName() + (task != null ? " - " + task.getTaskName() : " - 采购申请"));
        req.setPurpose(purpose != null ? purpose : (task != null ? "WBS任务[" + task.getTaskName() + "]采购需求" : "项目[" + project.getProjectName() + "]采购需求"));
        req.setPriority(task != null ? task.getPriority() : "1");
        req.setSourceProjectId(projectId);
        req.setSourceProjectNo(project.getProjectNo());
        req.setRequesterId(SecurityUtils.getUserId());
        req.setRequesterName(SecurityUtils.getUsername());
        req.setDeptId(project.getDeptId());
        req.setDeptName(project.getDeptName());
        req.setStatus("0");

        // 明细
        List<PmsPurchaseRequestDetail> detailList = new ArrayList<>();
        if (details != null && !details.isEmpty())
        {
            for (Map<String, Object> d : details)
            {
                PmsPurchaseRequestDetail det = new PmsPurchaseRequestDetail();
                det.setMaterialId(toLong(d.get("materialId")));
                det.setMaterialCode((String) d.get("materialCode"));
                det.setMaterialName((String) d.get("materialName"));
                det.setSpecModel((String) d.get("specModel"));
                det.setUnit((String) d.get("unit"));
                det.setQty(toBigDecimal(d.get("qty")));
                det.setEstimatedPrice(toBigDecimal(d.get("estimatedPrice")));
                det.setRemark((String) d.get("remark"));
                detailList.add(det);
            }
        }
        req.setDetailList(detailList);

        pmsRequestService.insertPurchaseRequest(req);
        Long requestId = req.getRequestId();
        log.info("PJM 发起采购成功: projectId={} taskId={} requestId={}", projectId, taskId, requestId);

        // ★ 回写 WBS 任务关联
        if (task != null)
        {
            PjmWbsTask update = new PjmWbsTask();
            update.setTaskId(taskId);
            update.setNeedPurchase("1");
            update.setPurchaseRequestId(requestId);
            update.setUpdateBy(SecurityUtils.getUsername());
            update.setUpdateTime(new Date());
            wbsTaskMapper.updateTask(update);
        }

        return requestId;
    }

    @Override
    public List<Map<String, Object>> stockCheck(List<Long> materialIds)
    {
        return wmsInventoryService.selectStockSummaryByMaterialIds(materialIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @SuppressWarnings("unchecked")
    public Map<String, Object> executeProjectPurchase(Map<String, Object> payload)
    {
        Long projectId = toLong(payload.get("projectId"));
        PjmProject project = projectMapper.selectProjectById(projectId);
        if (project == null)
        {
            throw new ServiceException("项目不存在");
        }
        Map<String, Object> result = new HashMap<>();
        String username = SecurityUtils.getUsername();

        // ========== ① 有库存部分 → 生成 WMS 出库单（领料出库，草稿），支持跨仓库拆分 ==========
        List<Map<String, Object>> outboundItems = (List<Map<String, Object>>) payload.get("outboundItems");
        if (outboundItems != null && !outboundItems.isEmpty())
        {
            Long defaultWarehouseId = toLong(payload.get("warehouseId"));
            List<Long> materialIds = new ArrayList<>();
            for (Map<String, Object> item : outboundItems)
            {
                Long mid = toLong(item.get("materialId"));
                if (mid != null && !materialIds.contains(mid)) materialIds.add(mid);
            }
            List<Map<String, Object>> summary = wmsInventoryService.selectStockSummaryByMaterialIds(materialIds);

            // 按仓库分组（明细行指定仓库优先，未指定时使用默认仓库）
            Map<Long, List<Map<String, Object>>> groups = new LinkedHashMap<>();
            for (Map<String, Object> item : outboundItems)
            {
                Long materialId = toLong(item.get("materialId"));
                BigDecimal qty = toBigDecimal(item.get("qty"));
                if (materialId == null || qty == null || qty.compareTo(BigDecimal.ZERO) <= 0)
                {
                    throw new ServiceException("出库明细数据不完整（物料或数量缺失）");
                }
                Long warehouseId = toLong(item.get("warehouseId"));
                if (warehouseId == null) warehouseId = defaultWarehouseId;
                if (warehouseId == null)
                {
                    throw new ServiceException("物料【" + item.get("materialName") + "】未指定出库仓库，请为明细分配出库仓库");
                }
                groups.computeIfAbsent(warehouseId, k -> new ArrayList<>()).add(item);
            }

            // 逐仓库校验库存并生成出库单
            List<Map<String, Object>> outboundOrders = new ArrayList<>();
            for (Map.Entry<Long, List<Map<String, Object>>> entry : groups.entrySet())
            {
                Long warehouseId = entry.getKey();
                WmsWarehouse warehouse = wmsWarehouseService.selectWarehouseById(warehouseId);
                if (warehouse == null)
                {
                    throw new ServiceException("出库仓库不存在（ID:" + warehouseId + "）");
                }
                // 同仓库内按物料聚合校验，避免同物料多行累加超出可用库存
                Map<Long, BigDecimal> needByMaterial = new LinkedHashMap<>();
                for (Map<String, Object> item : entry.getValue())
                {
                    Long materialId = toLong(item.get("materialId"));
                    needByMaterial.merge(materialId, toBigDecimal(item.get("qty")), BigDecimal::add);
                }
                for (Map.Entry<Long, BigDecimal> need : needByMaterial.entrySet())
                {
                    BigDecimal avail = BigDecimal.ZERO;
                    for (Map<String, Object> s : summary)
                    {
                        if (need.getKey().equals(toLong(s.get("materialId"))) && warehouseId.equals(toLong(s.get("warehouseId"))))
                        {
                            avail = avail.add(toBigDecimal(s.get("availQty")));
                        }
                    }
                    if (avail.compareTo(need.getValue()) < 0)
                    {
                        throw new ServiceException("物料在仓库【" + warehouse.getWarehouseName() + "】可用库存不足（可用 "
                                + avail + "，需出库 " + need.getValue() + "），请调整出库数量或更换仓库");
                    }
                }

                WmsOutboundOrder order = new WmsOutboundOrder();
                order.setOrderType("1"); // 领料出库
                order.setWarehouseId(warehouseId);
                order.setWarehouseName(warehouse.getWarehouseName());
                order.setStatus("0"); // 草稿
                order.setSourceProjectId(projectId);
                order.setSourceProjectNo(project.getProjectNo());
                order.setRemark("项目[" + project.getProjectNo() + "]" + project.getProjectName() + " 领料出库");
                order.setCreateBy(username);
                List<WmsOutboundOrderDetail> details = new ArrayList<>();
                for (Map<String, Object> item : entry.getValue())
                {
                    WmsOutboundOrderDetail d = new WmsOutboundOrderDetail();
                    d.setMaterialId(toLong(item.get("materialId")));
                    d.setMaterialCode((String) item.get("materialCode"));
                    d.setMaterialName((String) item.get("materialName"));
                    d.setSpecModel((String) item.get("specModel"));
                    d.setUnit(item.get("unit") != null ? item.get("unit").toString() : null);
                    d.setPlanQty(toBigDecimal(item.get("qty")));
                    details.add(d);
                }
                order.setDetailList(details);
                wmsOutboundOrderService.insertOutboundOrder(order);

                Map<String, Object> orderInfo = new HashMap<>();
                orderInfo.put("outboundOrderId", order.getOrderId());
                orderInfo.put("outboundOrderNo", order.getOrderNo());
                orderInfo.put("warehouseId", warehouseId);
                orderInfo.put("warehouseName", warehouse.getWarehouseName());
                outboundOrders.add(orderInfo);
                log.info("PJM 项目采购-生成出库单: projectId={} warehouse={} orderNo={}", projectId, warehouse.getWarehouseName(), order.getOrderNo());
            }
            result.put("outboundOrders", outboundOrders);
            // 兼容旧前端：保留首张出库单信息
            Map<String, Object> first = outboundOrders.get(0);
            result.put("outboundOrderId", first.get("outboundOrderId"));
            result.put("outboundOrderNo", first.get("outboundOrderNo"));
        }

        // ========== ② 无库存部分 → 生成 PMS 采购申请（草稿） ==========
        List<Map<String, Object>> purchaseItems = (List<Map<String, Object>>) payload.get("purchaseItems");
        if (purchaseItems != null && !purchaseItems.isEmpty())
        {
            String purchaseMode = payload.get("purchaseMode") != null ? payload.get("purchaseMode").toString() : null;
            if (!"1".equals(purchaseMode) && !"2".equals(purchaseMode))
            {
                throw new ServiceException("请选择采购模式（入库后再出库 / 直送项目现场）");
            }
            String modeText = "2".equals(purchaseMode) ? "直送项目现场" : "入库后再出库";
            PmsPurchaseRequest req = new PmsPurchaseRequest();
            String title = payload.get("purchaseTitle") != null ? payload.get("purchaseTitle").toString() : null;
            req.setTitle(StringUtils.isNotEmpty(title) ? title : project.getProjectName() + " - 物料采购（" + modeText + "）");
            req.setPurpose("项目[" + project.getProjectNo() + "]" + project.getProjectName() + " 采购需求，采购模式：" + modeText);
            req.setPurchaseType(purchaseMode);
            req.setSourceProjectId(projectId);
            req.setSourceProjectNo(project.getProjectNo());
            req.setRequesterId(SecurityUtils.getUserId());
            req.setRequesterName(username);
            req.setDeptId(project.getDeptId());
            req.setDeptName(project.getDeptName());
            req.setStatus("0");
            Object expectDate = payload.get("expectDate");
            if (expectDate != null && StringUtils.isNotEmpty(expectDate.toString()))
            {
                try
                {
                    req.setExpectDate(DateUtils.parseDate(expectDate.toString().substring(0, 10)));
                }
                catch (Exception e)
                {
                    log.warn("期望交货日期解析失败: {}", expectDate);
                }
            }
            List<PmsPurchaseRequestDetail> detailList = new ArrayList<>();
            for (Map<String, Object> item : purchaseItems)
            {
                PmsPurchaseRequestDetail det = new PmsPurchaseRequestDetail();
                det.setMaterialId(toLong(item.get("materialId")));
                det.setMaterialCode((String) item.get("materialCode"));
                det.setMaterialName((String) item.get("materialName"));
                det.setSpecModel((String) item.get("specModel"));
                det.setUnit(item.get("unit") != null ? item.get("unit").toString() : null);
                det.setQty(toBigDecimal(item.get("qty")));
                det.setEstimatedPrice(toBigDecimal(item.get("estimatedPrice")));
                detailList.add(det);
            }
            req.setDetailList(detailList);
            pmsRequestService.insertPurchaseRequest(req);
            result.put("requestId", req.getRequestId());
            result.put("requestNo", req.getRequestNo());
            log.info("PJM 项目采购-生成采购申请: projectId={} requestNo={} mode={}", projectId, req.getRequestNo(), purchaseMode);
        }

        if (result.isEmpty())
        {
            throw new ServiceException("没有可执行的出库或采购明细");
        }
        // ★ 生成采购申请后刷新项目采购汇总（计划采购额/采购单数）
        refreshPurchaseSummary(projectId);
        return result;
    }

    private static Long toLong(Object obj)
    {
        if (obj == null) return null;
        if (obj instanceof Long) return (Long) obj;
        if (obj instanceof Integer) return ((Integer) obj).longValue();
        return Long.valueOf(obj.toString());
    }

    private static BigDecimal toBigDecimal(Object obj)
    {
        if (obj == null) return null;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        if (obj instanceof Number) return new BigDecimal(obj.toString());
        return new BigDecimal(obj.toString());
    }
}
