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

            String raw;
            try { raw = kingdeeClient.getWithRetry(SUPPLIER_PATH, params); }
            catch (Exception e) { result.addSkipReason("拉取失败: " + e.getMessage()); break; }

            List<Map<String, Object>> rows;
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
                Object totalPage = data.get("total_page");
                boolean lastPage = (totalPage instanceof Number && page >= ((Number) totalPage).intValue());
                if (rows == null || rows.isEmpty() || lastPage) break;
            }
            catch (Exception e) { break; }

            for (Map<String, Object> kd : rows)
            {
                try { syncOne(kd, result); }
                catch (Exception e)
                {
                    result.incSkipped();
                    result.addSkipReason(str(kd.get("number")) + ": " + e.getMessage());
                }
            }
            page++;
        }

        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶供应商同步完成: {}", result.getStatistics());
        return result;
    }

    private void syncOne(Map<String, Object> kd, SyncResult result)
    {
        String kdNumber = str(kd.get("number"));
        String kdName   = str(kd.get("name"));
        String kdEnable = str(kd.get("enable"));
        String kdRemark = str(kd.get("remark"));

        if (kdNumber == null || kdNumber.isEmpty()) throw new IllegalArgumentException("金蝶供应商编号为空");

        WmsSupplier q = new WmsSupplier();
        q.setSupplierCode(kdNumber);
        List<WmsSupplier> list = supplierService.selectSupplierList(q);
        WmsSupplier existed = (list != null && !list.isEmpty()) ? list.get(0) : null;
        if (existed != null)
        {
            existed.setSupplierName(kdName);
            existed.setStatus("1".equals(kdEnable) ? "0" : "1");
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
            s.setStatus("1".equals(kdEnable) ? "0" : "1");
            s.setRemark(kdRemark);
            s.setDelFlag("0");
            supplierService.insertSupplier(s);
            result.incInserted();
            result.addSample(kdNumber, kdName, "新增", null);
        }
        result.incTotal();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
}
