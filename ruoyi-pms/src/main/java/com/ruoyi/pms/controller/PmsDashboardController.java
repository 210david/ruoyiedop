package com.ruoyi.pms.controller;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pms.domain.PmsPurchaseOrder;
import com.ruoyi.pms.domain.PmsPurchaseRequest;
import com.ruoyi.pms.domain.PmsContract;
import com.ruoyi.pms.domain.PmsInquiry;
import com.ruoyi.pms.service.IPmsPurchaseOrderService;
import com.ruoyi.pms.service.IPmsPurchaseRequestService;
import com.ruoyi.pms.service.IPmsContractService;
import com.ruoyi.pms.service.IPmsInquiryService;

/**
 * 采购看板 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pms/dashboard")
public class PmsDashboardController extends BaseController
{
    @Autowired
    private IPmsPurchaseRequestService pmsPurchaseRequestService;

    @Autowired
    private IPmsPurchaseOrderService pmsPurchaseOrderService;

    @Autowired
    private IPmsContractService pmsContractService;

    @Autowired
    private IPmsInquiryService pmsInquiryService;

    /**
     * 看板汇总数据
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/summary")
    public AjaxResult summary()
    {
        Map<String, Object> result = new HashMap<>();

        // 采购申请统计
        PmsPurchaseRequest reqQuery = new PmsPurchaseRequest();
        List<PmsPurchaseRequest> reqList = pmsPurchaseRequestService.selectPurchaseRequestList(reqQuery);
        int reqPending = 0;
        int reqApproved = 0;
        BigDecimal reqTotalAmount = BigDecimal.ZERO;
        for (PmsPurchaseRequest r : reqList)
        {
            if ("1".equals(r.getStatus())) reqPending++;
            if ("2".equals(r.getStatus())) reqApproved++;
            if (r.getTotalAmount() != null) reqTotalAmount = reqTotalAmount.add(r.getTotalAmount());
        }
        result.put("reqTotal", reqList.size());
        result.put("reqPending", reqPending);
        result.put("reqApproved", reqApproved);
        result.put("reqTotalAmount", reqTotalAmount);

        // 采购订单统计
        PmsPurchaseOrder ordQuery = new PmsPurchaseOrder();
        List<PmsPurchaseOrder> ordList = pmsPurchaseOrderService.selectPurchaseOrderList(ordQuery);
        int ordPending = 0;
        int ordCompleted = 0;
        BigDecimal ordTotalAmount = BigDecimal.ZERO;
        BigDecimal ordReceivedAmount = BigDecimal.ZERO;
        for (PmsPurchaseOrder o : ordList)
        {
            if ("1".equals(o.getStatus())) ordPending++;
            if ("5".equals(o.getStatus())) ordCompleted++;
            if (o.getTotalAmount() != null) ordTotalAmount = ordTotalAmount.add(o.getTotalAmount());
            if (o.getReceivedAmount() != null) ordReceivedAmount = ordReceivedAmount.add(o.getReceivedAmount());
        }
        result.put("ordTotal", ordList.size());
        result.put("ordPending", ordPending);
        result.put("ordCompleted", ordCompleted);
        result.put("ordTotalAmount", ordTotalAmount);
        result.put("ordReceivedAmount", ordReceivedAmount);

        // 采购合同统计
        PmsContract contractQuery = new PmsContract();
        List<PmsContract> contractList = pmsContractService.selectContractList(contractQuery);
        int contractSigned = 0;
        int contractPending = 0;
        BigDecimal contractTotalAmount = BigDecimal.ZERO;
        for (PmsContract c : contractList)
        {
            if ("1".equals(c.getStatus())) contractSigned++;
            if ("5".equals(c.getStatus())) contractPending++;
            if (c.getContractAmount() != null) contractTotalAmount = contractTotalAmount.add(c.getContractAmount());
        }
        result.put("contractTotal", contractList.size());
        result.put("contractSigned", contractSigned);
        result.put("contractPending", contractPending);
        result.put("contractTotalAmount", contractTotalAmount);

        // 询比价统计
        PmsInquiry inquiryQuery = new PmsInquiry();
        List<PmsInquiry> inquiryList = pmsInquiryService.selectPmsInquiryList(inquiryQuery);
        int inquiryActive = 0;
        int inquiryAwarded = 0;
        BigDecimal inquiryTotalAmount = BigDecimal.ZERO;
        for (PmsInquiry inq : inquiryList)
        {
            if ("1".equals(inq.getStatus())) inquiryActive++;
            if ("4".equals(inq.getStatus())) inquiryAwarded++;
            if (inq.getAwardAmount() != null) inquiryTotalAmount = inquiryTotalAmount.add(inq.getAwardAmount());
        }
        result.put("inquiryTotal", inquiryList.size());
        result.put("inquiryActive", inquiryActive);
        result.put("inquiryAwarded", inquiryAwarded);
        result.put("inquiryTotalAmount", inquiryTotalAmount);

        return AjaxResult.success(result);
    }

    /**
     * 采购订单状态分布
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/orderStatusDist")
    public AjaxResult orderStatusDist()
    {
        PmsPurchaseOrder query = new PmsPurchaseOrder();
        List<PmsPurchaseOrder> ordList = pmsPurchaseOrderService.selectPurchaseOrderList(query);
        String[] statusCodes = {"0", "1", "2", "3", "4", "5", "6"};
        String[] statusNames = {"草稿", "待审批", "已审批", "已下单", "部分到货", "已完成", "已作废"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < statusCodes.length; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", statusCodes[i]);
            item.put("name", statusNames[i]);
            int count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            for (PmsPurchaseOrder o : ordList)
            {
                if (statusCodes[i].equals(o.getStatus()))
                {
                    count++;
                    if (o.getTotalAmount() != null) amount = amount.add(o.getTotalAmount());
                }
            }
            item.put("count", count);
            item.put("amount", amount);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 采购合同状态分布
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/contractStatusDist")
    public AjaxResult contractStatusDist()
    {
        PmsContract query = new PmsContract();
        List<PmsContract> list = pmsContractService.selectContractList(query);
        String[] statusCodes = {"0", "1", "2", "3", "4", "5", "6"};
        String[] statusNames = {"草稿", "已签订", "变更审批中", "已到期", "已终止", "待审核", "驳回"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < statusCodes.length; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", statusCodes[i]);
            item.put("name", statusNames[i]);
            int count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            for (PmsContract c : list)
            {
                if (statusCodes[i].equals(c.getStatus()))
                {
                    count++;
                    if (c.getContractAmount() != null) amount = amount.add(c.getContractAmount());
                }
            }
            item.put("count", count);
            item.put("amount", amount);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 采购月度趋势（近12个月）
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/monthlyTrend")
    public AjaxResult monthlyTrend()
    {
        PmsPurchaseOrder query = new PmsPurchaseOrder();
        List<PmsPurchaseOrder> ordList = pmsPurchaseOrderService.selectPurchaseOrderList(query);
        // 按月份分组
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");
        // 生成近12个月的月份列表
        List<String> months = new ArrayList<>();
        for (int i = 11; i >= 0; i--)
        {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            months.add(sdf.format(cal.getTime()));
        }
        Map<String, Object[]> monthData = new LinkedHashMap<>();
        for (String m : months)
        {
            monthData.put(m, new Object[]{0, BigDecimal.ZERO}); // count, amount
        }
        for (PmsPurchaseOrder o : ordList)
        {
            if (o.getOrderDate() != null)
            {
                String m = sdf.format(o.getOrderDate());
                if (monthData.containsKey(m))
                {
                    Object[] data = monthData.get(m);
                    data[0] = (int) data[0] + 1;
                    if (o.getTotalAmount() != null)
                    {
                        data[1] = ((BigDecimal) data[1]).add(o.getTotalAmount());
                    }
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (String m : months)
        {
            Object[] data = monthData.get(m);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("date", m);
            item.put("count", data[0]);
            item.put("amount", data[1]);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 供应商采购金额排行 TOP10
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/supplierRanking")
    public AjaxResult supplierRanking()
    {
        PmsPurchaseOrder query = new PmsPurchaseOrder();
        List<PmsPurchaseOrder> ordList = pmsPurchaseOrderService.selectPurchaseOrderList(query);
        // 按供应商分组汇总
        Map<String, BigDecimal> supplierAmount = new HashMap<>();
        Map<String, Integer> supplierCount = new HashMap<>();
        for (PmsPurchaseOrder o : ordList)
        {
            String name = o.getSupplierName();
            if (name == null || name.isEmpty()) name = "未知供应商";
            supplierAmount.merge(name, o.getTotalAmount() != null ? o.getTotalAmount() : BigDecimal.ZERO, BigDecimal::add);
            supplierCount.merge(name, 1, Integer::sum);
        }
        // 排序取TOP10
        List<Map<String, Object>> result = new ArrayList<>();
        supplierAmount.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(10)
                .forEach(e -> {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("name", e.getKey());
                    item.put("amount", e.getValue());
                    item.put("count", supplierCount.get(e.getKey()));
                    result.add(item);
                });
        return AjaxResult.success(result);
    }

    /**
     * 询比价状态分布
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/inquiryStatusDist")
    public AjaxResult inquiryStatusDist()
    {
        PmsInquiry query = new PmsInquiry();
        List<PmsInquiry> list = pmsInquiryService.selectPmsInquiryList(query);
        String[] statusCodes = {"0", "1", "2", "3", "4", "5"};
        String[] statusNames = {"草稿", "询价中", "已截止", "已比价", "已定标", "待审核"};
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < statusCodes.length; i++)
        {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("code", statusCodes[i]);
            item.put("name", statusNames[i]);
            int count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            for (PmsInquiry inq : list)
            {
                if (statusCodes[i].equals(inq.getStatus()))
                {
                    count++;
                    if (inq.getEstimateAmount() != null) amount = amount.add(inq.getEstimateAmount());
                }
            }
            item.put("count", count);
            item.put("amount", amount);
            result.add(item);
        }
        return AjaxResult.success(result);
    }

    /**
     * 最近采购订单
     */
    @PreAuthorize("@ss.hasPermi('pms:dashboard:view')")
    @GetMapping("/recentOrders")
    public AjaxResult recentOrders()
    {
        PmsPurchaseOrder query = new PmsPurchaseOrder();
        List<PmsPurchaseOrder> all = pmsPurchaseOrderService.selectPurchaseOrderList(query);
        // 取前10条
        int limit = Math.min(10, all.size());
        List<PmsPurchaseOrder> recent = all.subList(0, limit);
        return AjaxResult.success(recent);
    }
}
