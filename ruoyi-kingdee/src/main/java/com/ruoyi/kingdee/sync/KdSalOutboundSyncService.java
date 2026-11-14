package com.ruoyi.kingdee.sync;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ruoyi.kingdee.adapter.KingdeeHttpClient;
import com.ruoyi.kingdee.domain.KdSalOutbound;
import com.ruoyi.kingdee.service.KdSalOutboundService;

/**
 * 金蝶云星辰 销售出库单列表同步服务
 * <p>
 * 接口：GET /jdy/v2/scm/sal_out_bound_list
 * 每条单据拆分商品分录，写入 kd_sal_outbound（按单据ID + 分录ID 幂等）
 *
 * @author ruoyi
 */
@Service
public class KdSalOutboundSyncService
{
    private static final Logger log = LoggerFactory.getLogger(KdSalOutboundSyncService.class);

    /** 销售出库单列表接口（带 page/pagesize 分页；不传 bill_id 时返回列表） */
    private static final String LIST_PATH = "/scm/sal_out_bound";

    /** 销售出库单详情接口（按 id 或 number 取单张单据完整信息，含商品分录明细） */
    private static final String DETAIL_PATH = "/scm/sal_out_bound_detail";

    private static final int PAGE_SIZE = 100;

    private static final int BATCH_SIZE = 500;

    /** 仅记录一次详情返回样例，便于核对字段 */
    private boolean detailLogged = false;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private KdSalOutboundService salOutboundService;

    /**
     * 全量同步销售出库单
     */
    public SyncResult syncAll()
    {
        SyncResult result = new SyncResult();
        long start = System.currentTimeMillis();
        detailLogged = false;

        List<KdSalOutbound> buffer = new ArrayList<>();
        int page = 1;

        while (true)
        {
            log.info("金蝶销售出库单同步 拉取第 {} 页 pageSize={}", page, PAGE_SIZE);

            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("pagesize", String.valueOf(PAGE_SIZE));
            params.put("total_fields", "allamount,totalunsettleamount,totalinsamount");

            String raw;
            try
            {
                raw = kingdeeClient.getWithRetry(LIST_PATH, params);
            }
            catch (Exception e)
            {
                result.addSkipReason("拉取失败: " + e.getMessage());
                log.error("金蝶销售出库单同步拉取失败: {}", e.getMessage());
                break;
            }

            Map<String, Object> resp;
            try
            {
                resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
            }
            catch (Exception e)
            {
                result.addSkipReason("返回JSON解析失败: " + e.getMessage());
                log.error("金蝶销售出库单同步JSON解析失败: {}", e.getMessage());
                break;
            }

            Integer errcode = asInt(resp.get("errcode"));
            if (errcode == null || errcode != 0)
            {
                String desc = String.valueOf(resp.getOrDefault("description", ""));
                result.addSkipReason("接口报错 errcode=" + errcode + ": " + desc);
                log.error("金蝶销售出库单接口报错: errcode={}, desc={}", errcode, desc);
                break;
            }

            Map<String, Object> data = asMap(resp.get("data"));
            if (data == null)
            {
                break;
            }

            List<Map<String, Object>> rows = asMapList(data.get("rows"));
            if (rows == null)
            {
                rows = asMapList(data.get("list"));
            }
            if (rows == null || rows.isEmpty())
            {
                break;
            }

            for (Map<String, Object> bill : rows)
            {
                try
                {
                    Map<String, Object> detail = fetchDetail(str(pick(bill, "id", "bill_id")), bill);
                    buffer.addAll(toEntities(bill, detail, result));
                }
                catch (Exception e)
                {
                    result.incSkipped();
                    result.addSkipReason(str(bill.get("bill_no")) + ": " + e.getMessage());
                    log.warn("销售出库单同步跳过 billNo={}, err={}", bill.get("bill_no"), e.getMessage());
                }
            }

            if (buffer.size() >= BATCH_SIZE)
            {
                flush(buffer, result);
            }

            Integer totalPage = asInt(data.get("total_page"));
            if (totalPage != null)
            {
                if (page >= totalPage)
                {
                    break;
                }
            }
            else if (rows.size() < PAGE_SIZE)
            {
                break;
            }
            page++;
        }

        flush(buffer, result);

        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶销售出库单同步完成: {}", result.getStatistics());
        return result;
    }

    /** 写入数据库（先删该单旧分录，再批量写入，避免分录变更产生孤儿行） */
    private void flush(List<KdSalOutbound> buffer, SyncResult result)
    {
        if (buffer.isEmpty())
        {
            return;
        }
        int size = buffer.size();
        java.util.Set<String> billIds = new java.util.LinkedHashSet<>();
        for (KdSalOutbound o : buffer)
        {
            billIds.add(o.getKdBillId());
        }
        salOutboundService.deleteByBillIds(new java.util.ArrayList<>(billIds));
        salOutboundService.batchUpsert(buffer);
        for (int i = 0; i < size; i++)
        {
            result.incInserted();
        }
        buffer.clear();
    }

    /**
     * 销售出库单详情（列表接口的分录只有 material_id，商品/仓库/单位/数量等需从详情接口取）
     */
    private Map<String, Object> fetchDetail(String billId, Map<String, Object> billRow)
    {
        if (isBlank(billId))
        {
            return billRow;
        }
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put("id", billId);
            String raw = kingdeeClient.getWithRetry(DETAIL_PATH, params);

            Map<String, Object> resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
            Integer errcode = asInt(resp.get("errcode"));
            if (errcode != null && errcode != 0)
            {
                log.warn("销售出库单详情接口报错 billId={}, errcode={}, desc={}", billId, errcode, resp.get("description"));
                return billRow;
            }
            Map<String, Object> data = asMap(resp.get("data"));
            if (data == null)
            {
                return billRow;
            }

            if (!detailLogged)
            {
                log.info("销售出库单详情返回样例 billId={}, json={}", billId, raw);
                detailLogged = true;
            }

            Map<String, Object> bill = asMap(data.get("bill"));
            if (bill != null)
            {
                return bill;
            }
            List<Map<String, Object>> rows = asMapList(data.get("rows"));
            if (rows != null)
            {
                return rows.get(0);
            }
            return data;
        }
        catch (Exception e)
        {
            log.warn("销售出库单详情拉取失败 billId={}, err={}", billId, e.getMessage());
            return billRow;
        }
    }

    /**
     * 单据 → 分录行集合
     */
    private List<KdSalOutbound> toEntities(Map<String, Object> bill, Map<String, Object> detail, SyncResult result)
    {
        String billNo = str(pickAny(detail, bill, "bill_no", "number", "bill_number"));
        String billId = str(pickAny(detail, bill, "id", "bill_id"));
        if (isBlank(billId))
        {
            billId = billNo;
        }
        if (isBlank(billId))
        {
            throw new IllegalArgumentException("单据ID与单据编号均为空");
        }

        String billDate = normalizeDate(str(pickAny(detail, bill, "bill_date", "date")));
        String customer = str(pickAny(detail, bill, "customer_name", "settle_customer_name", "cust_name"));
        String salesman = str(pickAny(detail, bill, "emp_name", "salesman_name", "seller_name", "salesman", "sales_name"));
        String dept = str(pickAny(detail, bill, "dept_name", "department_name", "dept"));
        String receiptStatus = settleStatusText(str(pickAny(detail, bill, "settle_status", "receipt_status", "receive_status", "pay_status", "rec_status")));
        BigDecimal unpaid = dec(pickAny(detail, bill, "totalunsettleamount", "total_un_settle_amount", "un_settle_amount", "unsettle_amount", "un_receive_amount", "unpaid_amount", "no_receive_amount"));
        BigDecimal paid = dec(pickAny(detail, bill, "totalinsamount", "total_ins_amount", "settle_amount", "receive_amount", "paid_amount", "received_amount", "ins_amount"));
        BigDecimal headerTotal = dec(pickAny(detail, bill, "allamount", "all_amount", "total_amount", "bill_dis_before_amount"));
        // 已收款优先用 价税合计 - 未收款 计算（与页面展示口径一致，totalinsamount 在部分账套不准确）
        if (headerTotal != null && unpaid != null)
        {
            paid = headerTotal.subtract(unpaid);
        }

        List<Map<String, Object>> entries = findEntries(detail != null ? detail : bill);
        if (entries == null || entries.isEmpty())
        {
            entries = Collections.singletonList(detail != null ? detail : bill);
        }

        List<KdSalOutbound> list = new ArrayList<>(entries.size());
        for (int i = 0; i < entries.size(); i++)
        {
            Map<String, Object> entry = entries.get(i);

            KdSalOutbound o = new KdSalOutbound();
            o.setKdBillId(billId);
            String entryId = str(pick(entry, "entry_id", "material_entity_id", "id"));
            o.setKdEntryId(isBlank(entryId) ? "E" + (i + 1) : entryId);
            o.setBillDate(billDate);
            o.setBillNo(billNo);
            o.setCustomerName(customer);
            o.setSalesmanName(salesman);
            o.setDeptName(dept);
            o.setReceiptStatus(receiptStatus);
            o.setUnpaidAmount(unpaid);
            o.setPaidAmount(paid);
            o.setMaterialName(str(pick(entry, "material_name", "goods_name", "product_name")));
            o.setWarehouseName(str(pick(entry, "stock_name", "warehouse_name", "stock")));
            o.setUnitName(str(pick(entry, "unit_name", "unit", "material_unit")));
            o.setQty(dec(pick(entry, "qty", "quantity", "real_qty")));
            o.setPrice(dec(pick(entry, "price", "unit_price", "tax_price")));
            o.setTaxAmount(dec(pick(entry, "tax_amount", "tax", "tax_amt")));
            BigDecimal total = dec(pick(entry, "all_amount", "total_amount", "amount_with_tax", "all_amt"));
            o.setTotalAmount(total != null ? total : headerTotal);
            o.setReturnQty(dec(pick(entry, "return_qty", "return_quantity", "ret_qty", "return_num")));
            o.setSyncTime(new Date());

            list.add(o);
            result.incTotal();
            if (list.size() == 1)
            {
                result.addSample(billNo, customer, "同步", billDate);
            }
        }
        return list;
    }

    /** 从单据中找出商品分录列表 */
    private List<Map<String, Object>> findEntries(Map<String, Object> bill)
    {
        String[] keys = { "material_entity", "bill_entry", "material_entities", "details", "entries" };
        for (String key : keys)
        {
            List<Map<String, Object>> list = asMapList(bill.get(key));
            if (list != null && !list.isEmpty())
            {
                return list;
            }
        }
        return null;
    }

    // ==================== 取值辅助 ====================

    /** 先取主表（详情），取不到再取列表行 */
    private static Object pickAny(Map<String, Object> primary, Map<String, Object> fallback, String... keys)
    {
        Object v = pick(primary, keys);
        return v != null ? v : pick(fallback, keys);
    }

    /** 收款状态：金蝶 settle_status A/B/C */
    private static String settleStatusText(String code)
    {
        if (isBlank(code))
        {
            return null;
        }
        switch (code.trim())
        {
            case "A":
                return "未收款";
            case "B":
                return "部分收款";
            case "C":
                return "全部收款";
            default:
                return code;
        }
    }

    private static Object pick(Map<String, Object> map, String... keys)
    {
        if (map == null)
        {
            return null;
        }
        for (String key : keys)
        {
            Object v = map.get(key);
            if (v != null && !"".equals(String.valueOf(v)))
            {
                return v;
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> asMap(Object o)
    {
        return o instanceof Map ? (Map<String, Object>) o : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> asMapList(Object o)
    {
        if (!(o instanceof List))
        {
            return null;
        }
        List<?> raw = (List<?>) o;
        if (raw.isEmpty())
        {
            return null;
        }
        List<Map<String, Object>> list = new ArrayList<>(raw.size());
        for (Object item : raw)
        {
            if (item instanceof Map)
            {
                list.add((Map<String, Object>) item);
            }
        }
        return list.isEmpty() ? null : list;
    }

    private static Integer asInt(Object o)
    {
        if (o instanceof Number)
        {
            return ((Number) o).intValue();
        }
        try
        {
            return o == null ? null : Integer.valueOf(String.valueOf(o).trim());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static BigDecimal dec(Object o)
    {
        if (o == null || "".equals(String.valueOf(o)))
        {
            return null;
        }
        try
        {
            return new BigDecimal(String.valueOf(o).trim());
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static String str(Object o)
    {
        return o == null ? null : String.valueOf(o);
    }

    private static boolean isBlank(String s)
    {
        return s == null || s.trim().isEmpty();
    }

    /** 金蝶日期可能带时分秒，统一截取 yyyy-MM-dd */
    private static String normalizeDate(String s)
    {
        if (isBlank(s))
        {
            return null;
        }
        String d = s.trim();
        return d.length() > 10 ? d.substring(0, 10) : d;
    }
}
