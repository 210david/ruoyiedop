package com.ruoyi.ai.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import com.ruoyi.ai.core.AiClient;
import com.ruoyi.ai.core.AiResult;
import com.ruoyi.ai.service.IAiBizAlertService;
import com.ruoyi.common.utils.StringUtils;

/**
 * AI行业化预警 Service实现（P3）
 *
 * 两个业务按钮的数据侧（谁需要谁主动查询，不主动推送，天然无噪音）：
 * 1. 临期批次出库建议：批次台账按FEFO（先到期先出）口径排序临期批次，AI生成出库消化建议；
 * 2. 产出率/损耗异常解读：按工单聚合计划量/合格量/不良量计算损耗率，AI解读超阈值异常。
 * 数据查询走JdbcTemplate只读SQL + 强制LIMIT，AI仅做文本解读不碰数据。
 *
 * @author ruoyi
 */
@Service
public class AiBizAlertServiceImpl implements IAiBizAlertService
{
    private static final Logger log = LoggerFactory.getLogger(AiBizAlertServiceImpl.class);

    @Autowired
    private AiClient aiClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public Map<String, Object> expiryAdvice(Integer days)
    {
        int limitDays = (days == null || days < 1) ? 30 : Math.min(days, 365);
        Map<String, Object> result = new HashMap<>();
        // FEFO口径：按到期日升序列出临期/已过期批次（含冻结，状态列区分处置口径），AI按可出库批次给建议
        String sql = "SELECT b.batch_code AS 批次号, m.material_name AS 物料, b.qty AS 批次数量, b.qty_unit AS 单位, "
                + "b.expire_date AS 到期日期, DATEDIFF(b.expire_date, CURDATE()) AS 剩余天数, "
                + "CASE b.batch_status WHEN '0' THEN '待检' WHEN '1' THEN '合格' WHEN '2' THEN '冻结' WHEN '3' THEN '报废' ELSE b.batch_status END AS 状态 "
                + "FROM food_batch b LEFT JOIN wms_material m ON b.material_id = m.material_id "
                + "WHERE b.expire_date IS NOT NULL AND b.del_flag = '0' "
                + "AND DATEDIFF(b.expire_date, CURDATE()) <= " + limitDays + " "
                + "ORDER BY b.expire_date ASC LIMIT 50";
        List<Map<String, Object>> rows;
        try
        {
            org.springframework.jdbc.core.JdbcTemplate tpl = new JdbcTemplate(jdbcTemplate.getDataSource());
            tpl.setQueryTimeout(10);
            rows = tpl.queryForList(sql);
        }
        catch (Exception e)
        {
            result.put("error", "临期批次查询失败：" + e.getMessage());
            return result;
        }
        result.put("rows", rows);
        result.put("total", rows.size());
        // AI解读：生成FEFO出库消化建议（失败静默降级为空，前端仍展示数据）
        if (!rows.isEmpty())
        {
            String prompt = "你是食品仓储出库顾问。以下是临期批次清单（已按到期日升序=FEFO优先出库顺序）：\n" + rowsToString(rows)
                    + "\n请给出简明出库消化建议：1.按FEFO先出哪几批（列批次号）；2.剩余天数<=7的紧迫批次建议（促销/加工优先/报废评估）；"
                    + "3.一句话总结。总字数不超过200字，不要表格。";
            String summary = aiChatQuietly("food", "batch_outbound_advice", prompt);
            if (StringUtils.isNotEmpty(summary))
            {
                result.put("summary", summary);
            }
        }
        return result;
    }

    @Override
    public Map<String, Object> lossInterpret(BigDecimal threshold)
    {
        BigDecimal limitRate = threshold == null || threshold.signum() <= 0 ? new BigDecimal("10") : threshold;
        Map<String, Object> result = new HashMap<>();
        // 损耗率口径：(计划量-合格量)/计划量，含不良+未完工缺口；仅统计有计划量的工单
        String sql = "SELECT w.work_order_no AS 工单号, w.product_name AS 产品, w.plan_qty AS 计划量, "
                + "w.qualified_qty AS 合格量, w.defect_qty AS 不良量, "
                + "ROUND((w.plan_qty - IFNULL(w.qualified_qty, 0)) / w.plan_qty * 100, 2) AS 损耗率, "
                + "w.status AS 工单状态 "
                + "FROM mms_work_order w WHERE w.plan_qty > 0 AND w.del_flag = '0' "
                + "AND (w.plan_qty - IFNULL(w.qualified_qty, 0)) / w.plan_qty * 100 >= " + limitRate + " "
                + "ORDER BY 损耗率 DESC LIMIT 30";
        List<Map<String, Object>> rows;
        try
        {
            org.springframework.jdbc.core.JdbcTemplate tpl = new JdbcTemplate(jdbcTemplate.getDataSource());
            tpl.setQueryTimeout(10);
            rows = tpl.queryForList(sql);
        }
        catch (Exception e)
        {
            result.put("error", "损耗数据查询失败：" + e.getMessage());
            return result;
        }
        result.put("rows", rows);
        result.put("total", rows.size());
        result.put("threshold", limitRate);
        if (!rows.isEmpty())
        {
            BigDecimal avg = BigDecimal.ZERO;
            for (Map<String, Object> r : rows)
            {
                Object v = r.get("损耗率");
                if (v != null)
                {
                    avg = avg.add(new BigDecimal(String.valueOf(v)));
                }
            }
            avg = avg.divide(new BigDecimal(rows.size()), 2, RoundingMode.HALF_UP);
            result.put("avgLossRate", avg);
            String prompt = "你是食品生产损耗分析顾问。损耗率口径=(计划量-合格量)/计划量。以下是损耗率超过阈值" + limitRate
                    + "%的工单（降序）：\n" + rowsToString(rows)
                    + "\n请简明解读：1.损耗最严重的工单及可能原因（修割损耗/发酵失重/不良返工等食品行业常见因素）；2.平均损耗率"
                    + avg + "%水平评价；3.一句话改进建议。总字数不超过200字，不要表格。";
            String summary = aiChatQuietly("mms", "loss_interpret", prompt);
            if (StringUtils.isNotEmpty(summary))
            {
                result.put("summary", summary);
            }
        }
        return result;
    }

    /** AI调用静默降级：失败返回null（按钮场景AI仅为增强，不阻塞数据展示） */
    private String aiChatQuietly(String module, String feature, String prompt)
    {
        try
        {
            AiResult ai = aiClient.chat(module, feature, prompt, 600);
            if (ai != null && ai.isSuccess() && StringUtils.isNotEmpty(ai.getContent()))
            {
                return ai.getContent();
            }
            log.warn("[AI预警解读] 调用未成功 module={} feature={} err={}", module, feature,
                    ai == null ? "null" : ai.getError());
        }
        catch (Exception e)
        {
            log.warn("[AI预警解读] 调用异常 module={} feature={} err={}", module, feature, e.getMessage());
        }
        return null;
    }

    /** 行数据紧凑转文本（限50行以内，AI提示词用） */
    private String rowsToString(List<Map<String, Object>> rows)
    {
        StringBuilder sb = new StringBuilder();
        for (Map<String, Object> r : rows)
        {
            sb.append(r.toString()).append("\n");
        }
        return sb.toString();
    }
}
