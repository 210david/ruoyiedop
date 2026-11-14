package com.ruoyi.kingdee.sync;

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
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.service.IWmsSupplierService;

/** 金蝶云星辰 供应商主数据同步 */
@Service
public class SupplierSyncService
{
    private static final Logger log = LoggerFactory.getLogger(SupplierSyncService.class);
    private static final String SUPPLIER_PATH = "/bd/supplier";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private IWmsSupplierService supplierService;

    public SyncResult syncAll()
    {
        SyncResult result = new SyncResult();
        long start = System.currentTimeMillis();
        int page = 1, pageSize = 100;

        while (true)
        {
            log.info("金蝶供应商同步 拉取第 {} 页", page);
            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("pagesize", String.valueOf(pageSize));
            params.put("enable", "-1");

            String raw;
            try { raw = kingdeeClient.getWithRetry(SUPPLIER_PATH, params); }
            catch (Exception e) { result.addSkipReason("拉取失败: " + e.getMessage()); break; }

            List<Map<String, Object>> rows;
            int totalPage = 1;
            try
            {
                Map<String, Object> resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {});
                Integer errcode = (Integer) resp.get("errcode");
                if (errcode == null || errcode != 0)
                {
                    result.addSkipReason("接口报错 errcode=" + errcode + ": " + resp.get("description"));
                    break;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> data = (Map<String, Object>) resp.get("data");
                if (data == null) break;
                @SuppressWarnings("unchecked")
                List<Map<String, Object>> list = (List<Map<String, Object>>) data.get("rows");
                rows = list;
                Object tp = data.get("total_page");
                Object total = data.get("total");
                totalPage = (tp instanceof Number) ? ((Number) tp).intValue() : 1;
                log.info("金蝶供应商第{}页: rows={}, total={}, total_page={}", page, rows == null ? 0 : rows.size(), total, totalPage);
            }
            catch (Exception e) { break; }

            if (rows == null || rows.isEmpty()) break;

            // 先处理当前页数据，再判断是否最后一页（修复：最后一页数据不再丢失）
            for (Map<String, Object> kd : rows)
            {
                try { syncOne(kd, result); }
                catch (Exception e)
                {
                    result.incSkipped();
                    result.addSkipReason(str(kd.get("number")) + ": " + e.getMessage());
                }
            }

            if (page >= totalPage) break;
            page++;
        }

        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶供应商同步完成: {}", result.getStatistics());
        return result;
    }

    private void syncOne(Map<String, Object> kd, SyncResult result)
    {
        String kdNumber   = str(kd.get("number"));
        String kdName     = str(kd.get("name"));
        String kdEnable   = str(kd.get("enable"));
        String kdRemark   = str(kd.get("remark"));
        String kdGroup    = str(kd.get("group_name"));        // 供应商分类 → 供应商类型
        String kdBank     = str(kd.get("bank"));              // 开户银行
        String kdBankAddr = str(kd.get("account_open_addr")); // 开户地址 → 开户行/支行
        String kdInvoice  = str(kd.get("invoice_name"));      // 发票名称 → 发票抬头
        String kdTaxNo    = str(kd.get("taxpayer_no"));       // 纳税人识别号 → 税号
        String kdRate     = str(kd.get("rate"));              // 税率

        if (kdNumber == null || kdNumber.isEmpty()) throw new IllegalArgumentException("金蝶供应商编号为空");

        WmsSupplier q = new WmsSupplier();
        q.setSupplierCode(kdNumber);
        List<WmsSupplier> list = supplierService.selectSupplierList(q);
        WmsSupplier existed = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (existed != null)
        {
            existed.setSupplierName(kdName);
            existed.setSupplierType(kdGroup);
            existed.setBankName(kdBank);
            existed.setBankBranch(kdBankAddr);
            existed.setInvoiceTitle(kdInvoice);
            existed.setTaxNumber(kdTaxNo);
            existed.setRate(kdRate);
            existed.setStatus(isDisabled(kdEnable) ? "1" : "0");
            existed.setRemark(kdRemark);
            supplierService.updateSupplier(existed);
            result.incUpdated();
            result.addSample(kdNumber, kdName, "更新", null);
        }
        else
        {
            WmsSupplier s = new WmsSupplier();
            s.setSupplierCode(kdNumber);
            s.setSupplierName(kdName);
            s.setSupplierShortName(kdName);
            s.setSupplierType(kdGroup);
            s.setBankName(kdBank);
            s.setBankBranch(kdBankAddr);
            s.setInvoiceTitle(kdInvoice);
            s.setTaxNumber(kdTaxNo);
            s.setRate(kdRate);
            s.setStatus(isDisabled(kdEnable) ? "1" : "0");
            s.setRemark(kdRemark);
            s.setDelFlag("0");
            supplierService.insertSupplier(s);
            result.incInserted();
            result.addSample(kdNumber, kdName, "新增", null);
        }
        result.incTotal();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }

    /**
     * 判断金蝶返回的 enable 值是否表示"停用"。
     * 金蝶供应商/客户/物料列表接口通常只返回启用的数据，且不返回 enable 字段（null），
     * 因此只有明确为 "0" 或 "false" 时才视为停用，其余情况（含 null）默认启用。
     */
    private static boolean isDisabled(String enable)
    {
        return "0".equals(enable) || "false".equalsIgnoreCase(enable);
    }
}
