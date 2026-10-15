package com.ruoyi.food.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.food.domain.FoodBatch;
import com.ruoyi.food.domain.FoodRecall;
import com.ruoyi.food.domain.FoodRecallBatch;
import com.ruoyi.food.mapper.FoodBatchMapper;
import com.ruoyi.food.mapper.FoodRecallMapper;
import com.ruoyi.food.service.IFoodBatchService;
import com.ruoyi.food.service.IFoodRecallService;
import com.ruoyi.system.utils.MessageHelper;

/**
 * 食品批次召回Service实现
 *
 * 谱系来源双体系融合：
 * 1. food_batch_genealogy（batchId维度）：拆批/合批/生产投入等批次级边
 * 2. qms_lot_genealogy（batchNo维度）：领料feeding/完工complete生产事件边，按 batchNo 映射到 food_batch 参与遍历
 *
 * 圈批规则：BFS 防环（visited集合），最大深度10层兜底；
 * 冻结规则：仅待检(0)/合格(1)可冻结（与批次操作一致），冻结/报废批次记入圈批清单但跳过冻结动作
 *
 * @author ruoyi
 */
@Service
public class FoodRecallServiceImpl implements IFoodRecallService
{
    private static final Logger log = LoggerFactory.getLogger(FoodRecallServiceImpl.class);

    /** BFS最大深度防环兜底 */
    private static final int MAX_DEPTH = 10;

    @Autowired
    private FoodRecallMapper recallMapper;

    @Autowired
    private FoodBatchMapper batchMapper;

    @Autowired
    private IFoodBatchService batchService;

    @Autowired
    private MessageHelper messageHelper;

    @Override
    public FoodRecall selectRecallById(Long recallId)
    {
        return recallMapper.selectRecallById(recallId);
    }

    @Override
    public List<FoodRecall> selectRecallList(FoodRecall recall)
    {
        return recallMapper.selectRecallList(recall);
    }

    @Override
    public List<FoodRecallBatch> selectRecallBatchList(Long recallId)
    {
        return recallMapper.selectRecallBatchList(recallId);
    }

    @Override
    public List<Map<String, Object>> circles(Long triggerBatchId, String scope)
    {
        FoodBatch trigger = batchMapper.selectBatchById(triggerBatchId);
        if (trigger == null)
        {
            throw new ServiceException("触发批次不存在或已删除");
        }
        boolean forward = "1".equals(scope) || "3".equals(scope) || scope == null;
        boolean backward = "2".equals(scope) || "3".equals(scope) || scope == null;
        // 构建双方向邻接表：food谱系边 + qms谱系边（batchNo映射）统一为批次ID边
        Map<Long, List<Edge>> parentToChild = new HashMap<>();
        Map<Long, List<Edge>> childToParent = new HashMap<>();
        buildAdjacency(parentToChild, childToParent);
        Map<String, Object> self = buildCircleItem(trigger, 0, "触发批次");
        List<Map<String, Object>> result = new ArrayList<>();
        result.add(self);
        Set<Long> visited = new HashSet<>();
        visited.add(triggerBatchId);
        if (forward)
        {
            bfs(triggerBatchId, parentToChild, true, visited, result);
        }
        if (backward)
        {
            bfs(triggerBatchId, childToParent, false, visited, result);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRecall(FoodRecall recall)
    {
        if (recall.getTriggerBatchId() == null)
        {
            throw new ServiceException("触发批次不能为空");
        }
        if (StringUtils.isEmpty(recall.getRecallReason()))
        {
            throw new ServiceException("召回原因不能为空");
        }
        FoodBatch trigger = batchMapper.selectBatchById(recall.getTriggerBatchId());
        if (trigger == null)
        {
            throw new ServiceException("触发批次不存在或已删除");
        }
        recall.setTriggerBatchCode(trigger.getBatchCode());
        if (StringUtils.isEmpty(recall.getRecallScope()))
        {
            recall.setRecallScope("3");
        }
        if (StringUtils.isEmpty(recall.getRecallLevel()))
        {
            recall.setRecallLevel("2");
        }
        recall.setStatus("0");
        recall.setRecallCode(nextRecallCode());
        recall.setCreateBy(SecurityUtils.getUsername());
        recall.setCreateTime(DateUtils.getNowDate());
        return recallMapper.insertRecall(recall);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int executeRecall(Long recallId)
    {
        FoodRecall recall = recallMapper.selectRecallById(recallId);
        if (recall == null)
        {
            throw new ServiceException("召回单不存在或已删除");
        }
        if (!"0".equals(recall.getStatus()))
        {
            throw new ServiceException("召回单[" + recall.getRecallCode() + "]当前状态不允许执行（仅待执行可执行）");
        }
        String operator = SecurityUtils.getUsername();
        // 1. 圈批
        List<Map<String, Object>> circles = circles(recall.getTriggerBatchId(), recall.getRecallScope());
        // 2. 逐批冻结留痕
        recallMapper.deleteRecallBatchByRecallId(recallId);
        int frozen = 0;
        for (Map<String, Object> c : circles)
        {
            FoodRecallBatch rb = new FoodRecallBatch();
            rb.setRecallId(recallId);
            rb.setBatchId(((Number) c.get("batchId")).longValue());
            rb.setBatchCode((String) c.get("batchCode"));
            rb.setMaterialId(((Number) c.get("materialId")).longValue());
            rb.setDepth((Integer) c.get("depth"));
            rb.setRelationPath((String) c.get("relationPath"));
            rb.setCreateTime(DateUtils.getNowDate());
            FoodBatch batch = batchMapper.selectBatchById(rb.getBatchId());
            if (batch == null)
            {
                continue;
            }
            rb.setMaterialId(batch.getMaterialId());
            rb.setPrevStatus(batch.getBatchStatus());
            if ("0".equals(batch.getBatchStatus()) || "1".equals(batch.getBatchStatus()))
            {
                // 待检/合格 → 冻结隔离（复用批次操作状态机留痕）
                batchService.freezeBatch(batch.getBatchId(), "召回隔离：" + recall.getRecallCode()
                        + "（" + recall.getRecallReason() + "）");
                rb.setFrozenFlag("1");
                frozen++;
            }
            else
            {
                rb.setFrozenFlag("0");
            }
            recallMapper.insertRecallBatch(rb);
        }
        // 3. 更新召回单
        FoodRecall updater = new FoodRecall();
        updater.setRecallId(recallId);
        updater.setStatus("1");
        updater.setAffectedCount(circles.size());
        updater.setExecuteTime(DateUtils.getNowDate());
        updater.setExecuteBy(operator);
        updater.setUpdateBy(operator);
        updater.setRemark("本次冻结 " + frozen + " 批，圈批合计 " + circles.size() + " 批（含已是冻结/报废批次）");
        recallMapper.updateRecall(updater);
        // 4. 消息通知（召回通知，紧急级）
        try
        {
            messageHelper.sendMessage(
                "批次召回隔离已执行：" + recall.getRecallCode(),
                "触发批次：" + recall.getTriggerBatchCode() + "，原因：" + recall.getRecallReason()
                        + "，圈批 " + circles.size() + " 批，本次冻结 " + frozen + " 批，请核实隔离现场",
                "2", "3".equals(recall.getRecallLevel()) ? "3" : "2", "food", recallId,
                "/wms/batch/recall", "food:recall:list", "1", "召回隔离");
        }
        catch (Exception e)
        {
            log.error("召回通知推送失败（不阻断召回执行）：{}", recall.getRecallCode(), e);
        }
        return frozen;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelRecall(Long recallId)
    {
        FoodRecall recall = recallMapper.selectRecallById(recallId);
        if (recall == null)
        {
            throw new ServiceException("召回单不存在或已删除");
        }
        if (!"0".equals(recall.getStatus()))
        {
            throw new ServiceException("召回单[" + recall.getRecallCode() + "]仅待执行状态可取消；已执行召回的隔离解除请走批次操作解冻留痕");
        }
        FoodRecall updater = new FoodRecall();
        updater.setRecallId(recallId);
        updater.setStatus("2");
        updater.setUpdateBy(SecurityUtils.getUsername());
        return recallMapper.updateRecall(updater);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRecallByIds(Long[] recallIds)
    {
        for (Long recallId : recallIds)
        {
            FoodRecall recall = recallMapper.selectRecallById(recallId);
            if (recall != null && "0".equals(recall.getStatus()))
            {
                // 草稿一并删除圈批预览，避免残留
                recallMapper.deleteRecallBatchByRecallId(recallId);
            }
        }
        return recallMapper.deleteRecallByIds(recallIds);
    }

    // ---------------- 私有方法 ----------------

    /**
     * 统一邻接表：food谱系（batchId）+ qms谱系（batchNo映射batchId）
     * parentToChild：key=投入/父批次 → 下游产出；childToParent：key=产出/子批次 → 上游投入
     */
    private void buildAdjacency(Map<Long, List<Edge>> parentToChild, Map<Long, List<Edge>> childToParent)
    {
        // 1. food 谱系边
        List<FoodBatch> allBatches = batchMapper.selectBatchList(new FoodBatch());
        Map<String, Long> codeToId = new HashMap<>();
        for (FoodBatch b : allBatches)
        {
            codeToId.put(b.getBatchCode(), b.getBatchId());
        }
        for (FoodBatch b : allBatches)
        {
            for (com.ruoyi.food.domain.FoodBatchGenealogy g : batchMapper.selectGenealogyByParent(b.getBatchId()))
            {
                addEdge(parentToChild, childToParent, g.getParentBatchId(), g.getChildBatchId(), relationText(g.getRelationType()),
                        g.getRefBillCode(), g.getQty());
            }
        }
        // 2. qms 生产谱系边（batchNo → batchId 映射，仅映射已登记批次）
        for (Map<String, Object> e : recallMapper.selectQmsGenealogyEdges())
        {
            Long parentId = codeToId.get(String.valueOf(e.get("parentNo")));
            Long childId = codeToId.get(String.valueOf(e.get("childNo")));
            if (parentId == null || childId == null)
            {
                continue;
            }
            String label = "feeding".equals(String.valueOf(e.get("sourceType"))) ? "生产投入" : "生产产出";
            java.math.BigDecimal qty = e.get("qtyIn") == null ? null : new java.math.BigDecimal(String.valueOf(e.get("qtyIn")));
            addEdge(parentToChild, childToParent, parentId, childId, label,
                    e.get("workOrderNo") == null ? null : String.valueOf(e.get("workOrderNo")), qty);
        }
    }

    private void addEdge(Map<Long, List<Edge>> parentToChild, Map<Long, List<Edge>> childToParent,
                         Long parent, Long child, String label, String billCode, java.math.BigDecimal qty)
    {
        if (parent.equals(child))
        {
            return;
        }
        parentToChild.computeIfAbsent(parent, k -> new ArrayList<>()).add(new Edge(child, label, billCode, qty));
        childToParent.computeIfAbsent(child, k -> new ArrayList<>()).add(new Edge(parent, label, billCode, qty));
    }

    /**
     * BFS 遍历：direction=true 正向（沿 parent→child 找下游产出），false 反向（沿 child→parent 找上游投入）
     */
    private void bfs(Long start, Map<Long, List<Edge>> adjacency, boolean direction,
                     Set<Long> visited, List<Map<String, Object>> result)
    {
        Queue<long[]> queue = new LinkedList<>();
        Queue<String> paths = new LinkedList<>();
        queue.add(new long[] { start, 0 });
        paths.add("");
        while (!queue.isEmpty())
        {
            long[] cur = queue.poll();
            String path = paths.poll();
            int depth = (int) cur[1];
            if (depth >= MAX_DEPTH)
            {
                continue;
            }
            List<Edge> edges = adjacency.get(cur[0]);
            if (edges == null)
            {
                continue;
            }
            for (Edge e : edges)
            {
                FoodBatch nb = batchMapper.selectBatchById(e.other);
                if (nb == null || visited.contains(e.other))
                {
                    continue;
                }
                visited.add(e.other);
                String arrow = direction ? "→" : "←";
                String step = arrow + e.label + (e.billCode != null ? "[" + e.billCode + "]" : "") + arrow + nb.getBatchCode();
                String newPath = path.isEmpty() ? step : path + " " + step;
                result.add(buildCircleItem(nb, depth + 1, newPath));
                queue.add(new long[] { e.other, depth + 1 });
                paths.add(newPath);
            }
        }
    }

    private Map<String, Object> buildCircleItem(FoodBatch batch, int depth, String relationPath)
    {
        Map<String, Object> item = new HashMap<>();
        item.put("batchId", batch.getBatchId());
        item.put("batchCode", batch.getBatchCode());
        item.put("materialId", batch.getMaterialId());
        item.put("materialName", batch.getMaterialName());
        item.put("depth", depth);
        item.put("relationPath", relationPath);
        item.put("batchStatus", batch.getBatchStatus());
        item.put("qty", batch.getQty());
        item.put("expireDate", batch.getExpireDate());
        return item;
    }

    /**
     * 召回单号：ZH+yyyyMMdd+4位流水（同日内按已有单号递增）
     */
    private synchronized String nextRecallCode()
    {
        String prefix = "ZH" + DateUtils.parseDateToStr("yyyyMMdd", new Date());
        FoodRecall query = new FoodRecall();
        query.setRecallCode(prefix);
        List<FoodRecall> exist = recallMapper.selectRecallList(query);
        int seq = 1;
        for (FoodRecall r : exist)
        {
            String code = r.getRecallCode();
            if (code != null && code.length() > prefix.length())
            {
                try
                {
                    int n = Integer.parseInt(code.substring(prefix.length()));
                    seq = Math.max(seq, n + 1);
                }
                catch (NumberFormatException ignore)
                {
                }
            }
        }
        return prefix + String.format("%04d", seq);
    }

    private String relationText(String relationType)
    {
        if ("1".equals(relationType)) return "投入";
        if ("2".equals(relationType)) return "派生";
        if ("3".equals(relationType)) return "拆分";
        if ("4".equals(relationType)) return "合并";
        if ("5".equals(relationType)) return "退回";
        return "关联";
    }

    /** 邻接边：other=对端批次ID；放入 parentToChild 时 other=child，放入 childToParent 时 other=parent */
    private static class Edge
    {
        final Long other;
        final String label;
        final String billCode;
        final java.math.BigDecimal qty;

        Edge(Long other, String label, String billCode, java.math.BigDecimal qty)
        {
            this.other = other;
            this.label = label;
            this.billCode = billCode;
            this.qty = qty;
        }
    }
}
