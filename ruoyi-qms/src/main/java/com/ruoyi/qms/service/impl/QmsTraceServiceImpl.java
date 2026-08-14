package com.ruoyi.qms.service.impl;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsLotGenealogy;
import com.ruoyi.qms.domain.QmsInspTask;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.domain.vo.TraceFallbackVO;
import com.ruoyi.qms.mapper.QmsLotGenealogyMapper;
import com.ruoyi.qms.mapper.QmsInspTaskMapper;
import com.ruoyi.qms.mapper.QmsNcrMapper;
import com.ruoyi.qms.mapper.QmsTraceFallbackMapper;
import com.ruoyi.qms.service.IQmsTraceService;

/**
 * 质量追溯 Service实现
 *
 * 正向追溯：原料批次 → 递归查找下游产出 → 成品 → 客户/订单
 * 反向追溯：成品批次 → 递归查找上游投入 → 原料 → 供应商/检验记录/NCR
 *
 * 追溯引擎采用递归遍历谱系表，最大递归深度10层防环与性能兜底。
 *
 * @author ruoyi
 */
@Service
public class QmsTraceServiceImpl implements IQmsTraceService
{
    private static final Logger log = LoggerFactory.getLogger(QmsTraceServiceImpl.class);

    /** 最大递归深度 */
    private static final int MAX_DEPTH = 10;

    @Autowired
    private QmsLotGenealogyMapper qmsLotGenealogyMapper;

    @Autowired
    private QmsInspTaskMapper qmsInspTaskMapper;

    @Autowired
    private QmsNcrMapper qmsNcrMapper;

    @Autowired
    private QmsTraceFallbackMapper traceFallbackMapper;

    @Override
    public List<QmsLotGenealogy> selectGenealogyList(QmsLotGenealogy genealogy)
    {
        return qmsLotGenealogyMapper.selectGenealogyList(genealogy);
    }

    @Override
    public QmsLotGenealogy selectGenealogyById(Long id)
    {
        return qmsLotGenealogyMapper.selectGenealogyById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertGenealogy(QmsLotGenealogy genealogy)
    {
        if (StringUtils.isEmpty(genealogy.getParentBatchNo()))
        {
            throw new ServiceException("投入批次号不能为空");
        }
        // 环检测：如果 child→parent 已存在，再加 parent→child 会成环
        if (StringUtils.isNotEmpty(genealogy.getChildBatchNo()))
        {
            int cycle = qmsLotGenealogyMapper.checkCycle(genealogy.getParentBatchNo(), genealogy.getChildBatchNo());
            if (cycle > 0)
            {
                throw new ServiceException("谱系成环检测：" + genealogy.getChildBatchNo() + " → " + genealogy.getParentBatchNo() + " 已存在，添加会形成环");
            }
        }
        // 断点判断：产出批次为空 → 断点
        if (StringUtils.isEmpty(genealogy.getChildBatchNo()))
        {
            genealogy.setBreakFlag(1);
            if (StringUtils.isEmpty(genealogy.getBreakReason()))
            {
                genealogy.setBreakReason("产出批次未登记");
            }
        }
        else
        {
            genealogy.setBreakFlag(0);
        }
        if (genealogy.getStatus() == null)
        {
            genealogy.setStatus("0");
        }
        if (genealogy.getSourceType() == null)
        {
            genealogy.setSourceType("manual");
        }
        if (genealogy.getTraceTime() == null)
        {
            genealogy.setTraceTime(new Date());
        }
        return qmsLotGenealogyMapper.insertGenealogy(genealogy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateGenealogy(QmsLotGenealogy genealogy)
    {
        // 断点重算：与 insertGenealogy 保持一致
        // 产出批次为空 → 断点（breakFlag=1）；产出批次非空 → 正常（breakFlag=0）
        if (StringUtils.isEmpty(genealogy.getChildBatchNo()))
        {
            genealogy.setBreakFlag(1);
            if (StringUtils.isEmpty(genealogy.getBreakReason()))
            {
                genealogy.setBreakReason("产出批次未登记");
            }
        }
        else
        {
            genealogy.setBreakFlag(0);
            // 产出批次已补录，清除断点原因
            genealogy.setBreakReason(null);
        }
        return qmsLotGenealogyMapper.updateGenealogy(genealogy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteGenealogyByIds(Long[] ids)
    {
        return qmsLotGenealogyMapper.deleteGenealogyByIds(ids);
    }

    /**
     * 正向追溯：从指定批次向下递归，找到所有下游成品批次
     */
    @Override
    public QmsLotGenealogy forwardTrace(String batchNo)
    {
        if (StringUtils.isEmpty(batchNo))
        {
            throw new ServiceException("批次号不能为空");
        }
        log.info("正向追溯开始：{}", batchNo);
        // 构建根节点
        QmsLotGenealogy root = new QmsLotGenealogy();
        root.setParentBatchNo(batchNo);
        root.setSourceType("trace_root");
        root.setTraceTime(new Date());
        root.setRemark("正向追溯起点");
        // 递归构建下游树
        Set<String> visited = new HashSet<>();
        visited.add(batchNo);
        List<QmsLotGenealogy> children = buildForwardTree(batchNo, 1, visited);
        root.setChildren(children);
        log.info("正向追溯完成：{}，找到 {} 条直接下游", batchNo, children.size());
        return root;
    }

    /**
     * 递归构建正向追溯树
     */
    private List<QmsLotGenealogy> buildForwardTree(String batchNo, int depth, Set<String> visited)
    {
        if (depth > MAX_DEPTH)
        {
            log.warn("正向追溯超过最大深度 {}，批次：{}", MAX_DEPTH, batchNo);
            return Collections.emptyList();
        }
        List<QmsLotGenealogy> list = qmsLotGenealogyMapper.selectByParentBatchNo(batchNo);
        for (QmsLotGenealogy node : list)
        {
            // 关联检验记录
            if (StringUtils.isNotEmpty(node.getChildBatchNo()))
            {
                QmsInspTask taskQuery = new QmsInspTask();
                taskQuery.setBatchNo(node.getChildBatchNo());
                List<QmsInspTask> tasks = qmsInspTaskMapper.selectInspTaskList(taskQuery);
                node.setInspectTasks(tasks);

                // 关联NCR
                QmsNcr ncrQuery = new QmsNcr();
                ncrQuery.setBatchNo(node.getChildBatchNo());
                List<QmsNcr> ncrs = qmsNcrMapper.selectNcrList(ncrQuery);
                node.setNcrList(ncrs);
            }
            // 递归查找下游
            if (StringUtils.isNotEmpty(node.getChildBatchNo()) && !visited.contains(node.getChildBatchNo()))
            {
                visited.add(node.getChildBatchNo());
                node.setChildren(buildForwardTree(node.getChildBatchNo(), depth + 1, visited));
            }
        }
        return list;
    }

    /**
     * 反向追溯：从指定批次向上递归，找到所有上游原料批次
     */
    @Override
    public QmsLotGenealogy backwardTrace(String batchNo)
    {
        if (StringUtils.isEmpty(batchNo))
        {
            throw new ServiceException("批次号不能为空");
        }
        log.info("反向追溯开始：{}", batchNo);
        // 构建根节点
        QmsLotGenealogy root = new QmsLotGenealogy();
        root.setChildBatchNo(batchNo);
        root.setSourceType("trace_root");
        root.setTraceTime(new Date());
        root.setRemark("反向追溯起点");
        // 关联当前批次的检验记录和NCR
        QmsInspTask taskQuery = new QmsInspTask();
        taskQuery.setBatchNo(batchNo);
        root.setInspectTasks(qmsInspTaskMapper.selectInspTaskList(taskQuery));

        QmsNcr ncrQuery = new QmsNcr();
        ncrQuery.setBatchNo(batchNo);
        root.setNcrList(qmsNcrMapper.selectNcrList(ncrQuery));

        // 递归构建上游树
        Set<String> visited = new HashSet<>();
        visited.add(batchNo);
        List<QmsLotGenealogy> parents = buildBackwardTree(batchNo, 1, visited);
        root.setChildren(parents);
        log.info("反向追溯完成：{}，找到 {} 条直接上游", batchNo, parents.size());
        return root;
    }

    /**
     * 递归构建反向追溯树
     */
    private List<QmsLotGenealogy> buildBackwardTree(String batchNo, int depth, Set<String> visited)
    {
        if (depth > MAX_DEPTH)
        {
            log.warn("反向追溯超过最大深度 {}，批次：{}", MAX_DEPTH, batchNo);
            return Collections.emptyList();
        }
        List<QmsLotGenealogy> list = qmsLotGenealogyMapper.selectByChildBatchNo(batchNo);
        for (QmsLotGenealogy node : list)
        {
            // 关联检验记录
            if (StringUtils.isNotEmpty(node.getParentBatchNo()))
            {
                QmsInspTask taskQuery = new QmsInspTask();
                taskQuery.setBatchNo(node.getParentBatchNo());
                List<QmsInspTask> tasks = qmsInspTaskMapper.selectInspTaskList(taskQuery);
                node.setInspectTasks(tasks);

                // 关联NCR
                QmsNcr ncrQuery = new QmsNcr();
                ncrQuery.setBatchNo(node.getParentBatchNo());
                List<QmsNcr> ncrs = qmsNcrMapper.selectNcrList(ncrQuery);
                node.setNcrList(ncrs);
            }
            // 递归查找上游
            if (StringUtils.isNotEmpty(node.getParentBatchNo()) && !visited.contains(node.getParentBatchNo()))
            {
                visited.add(node.getParentBatchNo());
                node.setChildren(buildBackwardTree(node.getParentBatchNo(), depth + 1, visited));
            }
        }
        return list;
    }

    /**
     * 谱系完整度统计
     */
    @Override
    public Map<String, Object> traceCompleteness()
    {
        Map<String, Object> result = new HashMap<>();
        int linked = qmsLotGenealogyMapper.countLinkedOrders();
        int breakCount = qmsLotGenealogyMapper.countBreakOrders();
        int total = linked + breakCount;
        double rate = total > 0 ? (double) linked / total * 100 : 0;
        result.put("linkedOrders", linked);
        result.put("breakOrders", breakCount);
        result.put("totalOrders", total);
        result.put("completenessRate", Math.round(rate * 100) / 100.0);
        result.put("targetRate", 98.0);
        result.put("gap", Math.max(0, 98.0 - Math.round(rate * 100) / 100.0));
        return result;
    }

    @Override
    public List<QmsLotGenealogy> selectBreakList(QmsLotGenealogy genealogy)
    {
        return qmsLotGenealogyMapper.selectBreakList(genealogy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int batchInsertGenealogy(List<QmsLotGenealogy> list)
    {
        for (QmsLotGenealogy genealogy : list)
        {
            if (StringUtils.isEmpty(genealogy.getParentBatchNo()))
            {
                throw new ServiceException("投入批次号不能为空");
            }
            if (StringUtils.isEmpty(genealogy.getChildBatchNo()))
            {
                genealogy.setBreakFlag(1);
                if (StringUtils.isEmpty(genealogy.getBreakReason()))
                {
                    genealogy.setBreakReason("产出批次未登记");
                }
            }
            else
            {
                genealogy.setBreakFlag(0);
            }
            if (genealogy.getStatus() == null) genealogy.setStatus("0");
            if (genealogy.getSourceType() == null) genealogy.setSourceType("manual");
            if (genealogy.getTraceTime() == null) genealogy.setTraceTime(new Date());
        }
        return qmsLotGenealogyMapper.batchInsertGenealogy(list);
    }

    /**
     * 降级追溯：当谱系表无数据时，利用已有业务单据中的 batch_no 做辅助关联追溯
     */
    @Override
    public TraceFallbackVO fallbackTrace(String batchNo, String direction)
    {
        if (StringUtils.isEmpty(batchNo))
        {
            throw new ServiceException("批次号不能为空");
        }
        log.info("降级追溯开始：{}，方向：{}", batchNo, direction);

        TraceFallbackVO result = new TraceFallbackVO();
        result.setBatchNo(batchNo);
        result.setDirection(direction);
        result.setTraceTime(new Date());
        result.setFallback(true);

        // 1. 检查谱系表是否有数据
        List<QmsLotGenealogy> genealogyList;
        if ("forward".equals(direction))
        {
            genealogyList = qmsLotGenealogyMapper.selectByParentBatchNo(batchNo);
        }
        else
        {
            genealogyList = qmsLotGenealogyMapper.selectByChildBatchNo(batchNo);
        }
        result.setGenealogyNodeCount(genealogyList != null ? genealogyList.size() : 0);

        // 2. 查询检验记录
        List<TraceFallbackVO.InspTaskSummary> inspTasks = traceFallbackMapper.selectInspTasksByBatchNo(batchNo);
        result.setInspectTasks(inspTasks != null ? inspTasks : Collections.emptyList());

        // 3. 查询 NCR
        List<TraceFallbackVO.NcrSummary> ncrs = traceFallbackMapper.selectNcrsByBatchNo(batchNo);
        result.setNcrList(ncrs != null ? ncrs : Collections.emptyList());

        // 4. 查询仓库库存流水
        List<TraceFallbackVO.InventoryLogSummary> invLogs = traceFallbackMapper.selectInventoryLogsByBatchNo(batchNo);
        result.setInventoryLogs(invLogs != null ? invLogs : Collections.emptyList());

        // 5. 查询仓库库存快照
        List<TraceFallbackVO.InventorySummary> invList = traceFallbackMapper.selectInventoryByBatchNo(batchNo);
        result.setInventoryList(invList != null ? invList : Collections.emptyList());

        // 6. 查询采购收货记录
        List<TraceFallbackVO.ReceiveSummary> recvList = traceFallbackMapper.selectReceiveDetailsByBatchNo(batchNo);
        result.setReceiveList(recvList != null ? recvList : Collections.emptyList());

        // 7. 查询客诉记录
        List<TraceFallbackVO.ComplaintSummary> complaintList = traceFallbackMapper.selectComplaintsByBatchNo(batchNo);
        result.setComplaintList(complaintList != null ? complaintList : Collections.emptyList());

        // 8. 查询销售发货记录（通过出库单明细 batch_no 关联）
        List<TraceFallbackVO.ShipmentSummary> shipmentList = traceFallbackMapper.selectShipmentsByBatchNo(batchNo);
        result.setShipmentList(shipmentList != null ? shipmentList : Collections.emptyList());

        // 9. 构建提示消息
        int totalRecords = result.getInspectTasks().size() + result.getNcrList().size()
                + result.getInventoryLogs().size() + result.getInventoryList().size()
                + result.getReceiveList().size() + result.getComplaintList().size()
                + result.getShipmentList().size();

        if (result.getGenealogyNodeCount() > 0)
        {
            result.setMessage(String.format("谱系表已有 %d 条记录，可使用正常追溯。降级追溯补充发现 %d 条关联业务记录。",
                    result.getGenealogyNodeCount(), totalRecords));
        }
        else if (totalRecords > 0)
        {
            result.setMessage(String.format("谱系表无该批次数据（谱系未建立）。降级追溯从业务单据中找到 %d 条关联记录，建议补录谱系数据以获得完整追溯链。", totalRecords));
        }
        else
        {
            result.setMessage("谱系表无该批次数据，且业务单据中也未找到该批次的关联记录。请检查批次号是否正确，或该批次可能尚未在任何业务环节中使用。");
        }

        log.info("降级追溯完成：{}，找到检验{}条/NCR{}条/库存流水{}条/库存{}条/收货{}条/客诉{}条/发货{}条",
                batchNo,
                result.getInspectTasks().size(),
                result.getNcrList().size(),
                result.getInventoryLogs().size(),
                result.getInventoryList().size(),
                result.getReceiveList().size(),
                result.getComplaintList().size(),
                result.getShipmentList().size());
        return result;
    }
}
