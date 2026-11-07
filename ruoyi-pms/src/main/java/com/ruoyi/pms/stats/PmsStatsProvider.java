package com.ruoyi.pms.stats;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.stats.IModuleStatsProvider;
import com.ruoyi.common.stats.MetricValue;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.mapper.PmsPurchaseOrderMapper;

/**
 * 采购模块统计指标提供者
 *
 * 复用 PMS 现有 Mapper 与看板口径（供应商采购金额排行、订单交期），
 * 不新建平行统计实现（统一开发规范 2.8）。
 *
 * 直连 Mapper 而非 Service：快照任务无登录上下文，
 * 避免经过带 @DataScope 注解的 Service 列表方法（规范 3.5.3）。
 *
 * 覆盖指标：PMS_ONTIME_RATE / PMS_TOP_SUP
 *
 * @author ruoyi
 */
@Component
public class PmsStatsProvider implements IModuleStatsProvider
{
    @Autowired
    private PmsPurchaseOrderMapper purchaseOrderMapper;

    @Override
    public String getModuleCode()
    {
        return "pms";
    }

    @Override
    public List<String> supportedMetricCodes()
    {
        return Arrays.asList("PMS_ONTIME_RATE", "PMS_TOP_SUP");
    }

    @Override
    public MetricValue getMetric(String metricCode, Date metricDate, Long deptId)
    {
        // 部门维度（PRD F5）：订单主表有 dept_id，按归属部门过滤
        List<PmsPurchaseOrder> orders = purchaseOrderMapper.selectPurchaseOrderList(new PmsPurchaseOrder());
        if (deptId != null)
        {
            List<PmsPurchaseOrder> scoped = new ArrayList<>();
            for (PmsPurchaseOrder o : orders)
            {
                if (o.getDeptId() != null && o.getDeptId().equals(deptId))
                {
                    scoped.add(o);
                }
            }
            orders = scoped;
        }

        BigDecimal value = null;
        String unit = null;
        switch (metricCode)
        {
            case "PMS_ONTIME_RATE":
                // 口径【已确认 v1】：到货及时率 = 按期到货订单数 / 已到货订单数。
                // "按期"基准 = 订单交期（expect_delivery_date，即合同/订单要求交期）；
                // 交期在主表故为订单粒度（订单行级交期字段缺失，字典口径已按订单行约定，此处为当前数据模型下的实现约束）。
                int arrived = 0;
                int ontime = 0;
                for (PmsPurchaseOrder o : orders)
                {
                    if (o.getActualDeliveryDate() == null)
                    {
                        continue;
                    }
                    arrived++;
                    if (o.getExpectDeliveryDate() == null
                            || !o.getActualDeliveryDate().after(o.getExpectDeliveryDate()))
                    {
                        ontime++;
                    }
                }
                value = rate(ontime, arrived);
                unit = "%";
                break;

            case "PMS_TOP_SUP":
                // 口径【已确认 v1】：供应商集中度 = 当月采购金额 Top5 供应商金额合计 / 当月采购总额。
                // 采购额取已生效订单 total_amount（status 非草稿/驳回的订单由列表口径过滤）。
                Map<Long, BigDecimal> supAmount = new HashMap<>();
                BigDecimal monthTotal = BigDecimal.ZERO;
                for (PmsPurchaseOrder o : orders)
                {
                    // 仅统计订单日期在当月、有供应商且金额有效的订单
                    if (o.getOrderDate() == null || o.getSupplierId() == null
                            || !inSameMonth(o.getOrderDate(), metricDate))
                    {
                        continue;
                    }
                    BigDecimal amt = o.getTotalAmount() == null ? BigDecimal.ZERO : o.getTotalAmount();
                    supAmount.merge(o.getSupplierId(), amt, BigDecimal::add);
                    monthTotal = monthTotal.add(amt);
                }
                if (monthTotal.compareTo(BigDecimal.ZERO) <= 0)
                {
                    value = null; // 当月无采购数据，返回 null（前端展示"—"）
                }
                else
                {
                    List<BigDecimal> top = new ArrayList<>(supAmount.values());
                    top.sort(Comparator.reverseOrder());
                    BigDecimal top5 = BigDecimal.ZERO;
                    for (int i = 0; i < Math.min(5, top.size()); i++)
                    {
                        top5 = top5.add(top.get(i));
                    }
                    value = top5.multiply(BigDecimal.valueOf(100))
                            .divide(monthTotal, 2, RoundingMode.HALF_UP);
                }
                unit = "%";
                break;

            default:
                break;
        }
        return new MetricValue(metricCode, metricDate, deptId, value, unit, "real");
    }

    private boolean inSameMonth(Date a, Date b)
    {
        if (a == null || b == null)
        {
            return false;
        }
        Calendar ca = Calendar.getInstance();
        ca.setTime(a);
        Calendar cb = Calendar.getInstance();
        cb.setTime(b);
        return ca.get(Calendar.YEAR) == cb.get(Calendar.YEAR)
                && ca.get(Calendar.MONTH) == cb.get(Calendar.MONTH);
    }

    private BigDecimal rate(int part, int total)
    {
        if (total <= 0)
        {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(part * 100.0 / total).setScale(2, RoundingMode.HALF_UP);
    }
}
