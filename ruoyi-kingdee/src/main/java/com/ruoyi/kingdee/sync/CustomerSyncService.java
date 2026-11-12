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
import com.ruoyi.mk.domain.MkCustomer;
import com.ruoyi.mk.service.IMkCustomerService;

/**
 * 金蝶云星辰 客户主数据同步服务
 */
@Service
public class CustomerSyncService
{
    private static final Logger log = LoggerFactory.getLogger(CustomerSyncService.class);

    private static final String CUSTOMER_PATH = "/bd/customer";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private KingdeeHttpClient kingdeeClient;
    @Autowired private IMkCustomerService customerService;

    /** 全量同步客户（支持分页） */
    public SyncResult syncAll()
    {
        SyncResult result = new SyncResult();
        long start = System.currentTimeMillis();
        int page = 1;
        int pageSize = 100;

        while (true)
        {
            log.info("金蝶客户同步 拉取第 {} 页 pageSize={}", page, pageSize);

            Map<String, String> params = new HashMap<>();
            params.put("page", String.valueOf(page));
            params.put("pagesize", String.valueOf(pageSize));

            String raw;
            try { raw = kingdeeClient.getWithRetry(CUSTOMER_PATH, params); }
            catch (Exception e) { result.addSkipReason("拉取失败: " + e.getMessage()); break; }

            Map<String, Object> resp;
            try { resp = objectMapper.readValue(raw, new TypeReference<Map<String, Object>>() {}); }
            catch (Exception e) { log.error("金蝶返回JSON解析失败: {}", e.getMessage()); break; }

            Integer errcode = (Integer) resp.get("errcode");
            if (errcode == null || errcode != 0)
            {
                String desc = String.valueOf(resp.getOrDefault("description", ""));
                log.error("金蝶客户同步接口报错: errcode={}, desc={}", errcode, desc);
                result.addSkipReason("接口报错 errcode=" + errcode + ": " + desc);
                break;
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) resp.get("data");
            if (data == null) break;

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rows = (List<Map<String, Object>>) data.get("rows");
            if (rows == null || rows.isEmpty()) break;

            for (Map<String, Object> kd : rows)
            {
                try { syncOne(kd, result); }
                catch (Exception e)
                {
                    result.incSkipped();
                    String reason = str(kd.get("number")) + ": " + e.getMessage();
                    result.addSkipReason(reason);
                    log.warn("客户同步跳过 kdNumber={}, err={}", kd.get("number"), e.getMessage());
                }
            }

            // 判断是否最后一页
            Object totalPage = data.get("total_page");
            if (totalPage instanceof Number && page >= ((Number) totalPage).intValue()) break;
            page++;
        }

        result.setElapsedMs(System.currentTimeMillis() - start);
        log.info("金蝶客户同步完成: {}", result.getStatistics());
        return result;
    }

    private void syncOne(Map<String, Object> kd, SyncResult result)
    {
        String kdNumber = str(kd.get("number"));
        String kdName   = str(kd.get("name"));
        String kdEnable = str(kd.get("enable"));
        String kdRemark = str(kd.get("remark"));

        if (kdNumber == null || kdNumber.isEmpty())
        {
            throw new IllegalArgumentException("金蝶客户编号为空");
        }

        MkCustomer query = new MkCustomer();
        query.setCustomerNo(kdNumber);
        List<MkCustomer> existed = customerService.selectCustomerListPlain(query);
        MkCustomer target = (existed != null && !existed.isEmpty()) ? existed.get(0) : null;

        if (target != null)
        {
            target.setCustomerName(kdName);
            target.setCustomerStatus("1".equals(kdEnable) ? "0" : "1");
            target.setRemark(kdRemark);
            customerService.updateCustomer(target);
            result.incUpdated();
            result.addSample(kdNumber, kdName, "更新", null);
        }
        else
        {
            MkCustomer c = new MkCustomer();
            c.setCustomerNo(kdNumber);
            c.setCustomerName(kdName);
            c.setCustomerLevel("3");
            c.setCustomerSource("0");
            c.setCustomerStatus("1".equals(kdEnable) ? "0" : "1");
            c.setRemark(kdRemark);
            customerService.insertCustomer(c);
            result.incInserted();
            result.addSample(kdNumber, kdName, "新增", null);
        }
        result.incTotal();
    }

    private static String str(Object o) { return o == null ? null : String.valueOf(o); }
}
