package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Anonymous;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.mk.domain.*;
import com.ruoyi.mk.service.*;

/**
 * 营销管理(MK) 开放接口
 * 路径前缀：/openapi/v1/mk
 * 鉴权方式：请求头 X-API-Key（由OpenApiInterceptor统一校验）
 *
 * @author ruoyi
 */
@Anonymous
@RestController
@RequestMapping("/openapi/v1/mk")
public class OpenMkController extends BaseController
{
    @Autowired
    private IMkCustomerService customerService;
    @Autowired
    private IMkLeadService leadService;
    @Autowired
    private IMkOpportunityService opportunityService;
    @Autowired
    private IMkContractService contractService;
    @Autowired
    private IMkOrderService orderService;
    @Autowired
    private IMkContactService contactService;
    @Autowired
    private IMkActivityService activityService;
    @Autowired
    private IMkInteractionService interactionService;
    @Autowired
    private IMkDashboardService dashboardService;
    @Autowired
    private IMkPaymentPlanService paymentPlanService;

    // ==================== 客户 ====================

    @GetMapping("/customer/list")
    public TableDataInfo customerList(MkCustomer query)
    {
        startPage();
        List<MkCustomer> list = customerService.selectCustomerList(query);
        return getDataTable(list);
    }

    @GetMapping("/customer/{customerId}")
    public AjaxResult customerById(@PathVariable Long customerId)
    {
        return AjaxResult.success(customerService.selectCustomerById(customerId));
    }

    // ==================== 线索 ====================

    @GetMapping("/lead/list")
    public TableDataInfo leadList(MkLead query)
    {
        startPage();
        List<MkLead> list = leadService.selectLeadList(query);
        return getDataTable(list);
    }

    @GetMapping("/lead/{leadId}")
    public AjaxResult leadById(@PathVariable Long leadId)
    {
        return AjaxResult.success(leadService.selectLeadById(leadId));
    }

    // ==================== 商机 ====================

    @GetMapping("/opportunity/list")
    public TableDataInfo opportunityList(MkOpportunity query)
    {
        startPage();
        List<MkOpportunity> list = opportunityService.selectOpportunityList(query);
        return getDataTable(list);
    }

    @GetMapping("/opportunity/{opportunityId}")
    public AjaxResult opportunityById(@PathVariable Long opportunityId)
    {
        return AjaxResult.success(opportunityService.selectOpportunityById(opportunityId));
    }

    // ==================== 合同 ====================

    @GetMapping("/contract/list")
    public TableDataInfo contractList(MkContract query)
    {
        startPage();
        List<MkContract> list = contractService.selectContractList(query);
        return getDataTable(list);
    }

    @GetMapping("/contract/{contractId}")
    public AjaxResult contractById(@PathVariable Long contractId)
    {
        return AjaxResult.success(contractService.selectContractById(contractId));
    }

    // ==================== 订单 ====================

    @GetMapping("/order/list")
    public TableDataInfo orderList(MkOrder query)
    {
        startPage();
        List<MkOrder> list = orderService.selectOrderList(query);
        return getDataTable(list);
    }

    @GetMapping("/order/{orderId}")
    public AjaxResult orderById(@PathVariable Long orderId)
    {
        return AjaxResult.success(orderService.selectOrderById(orderId));
    }

    // ==================== 联系人 ====================

    @GetMapping("/contact/list")
    public TableDataInfo contactList(MkContact query)
    {
        startPage();
        List<MkContact> list = contactService.selectContactList(query);
        return getDataTable(list);
    }

    @GetMapping("/contact/{contactId}")
    public AjaxResult contactById(@PathVariable Long contactId)
    {
        return AjaxResult.success(contactService.selectContactById(contactId));
    }

    // ==================== 营销活动 ====================

    @GetMapping("/activity/list")
    public TableDataInfo activityList(MkActivity query)
    {
        startPage();
        List<MkActivity> list = activityService.selectActivityList(query);
        return getDataTable(list);
    }

    @GetMapping("/activity/{activityId}")
    public AjaxResult activityById(@PathVariable Long activityId)
    {
        return AjaxResult.success(activityService.selectActivityById(activityId));
    }

    // ==================== 互动记录 ====================

    @GetMapping("/interaction/list")
    public TableDataInfo interactionList(MkInteraction query)
    {
        startPage();
        List<MkInteraction> list = interactionService.selectInteractionList(query);
        return getDataTable(list);
    }

    // ==================== 统计看板 ====================

    @GetMapping("/dashboard/overview")
    public AjaxResult dashboardOverview()
    {
        return AjaxResult.success(dashboardService.selectOverview());
    }

    @GetMapping("/dashboard/leadFunnel")
    public AjaxResult dashboardLeadFunnel()
    {
        return AjaxResult.success(dashboardService.selectLeadFunnel());
    }

    @GetMapping("/dashboard/opportunityPipeline")
    public AjaxResult dashboardOpportunityPipeline()
    {
        return AjaxResult.success(dashboardService.selectOpportunityPipeline());
    }

    @GetMapping("/dashboard/salesRanking")
    public AjaxResult dashboardSalesRanking()
    {
        return AjaxResult.success(dashboardService.selectSalesRanking());
    }

    @GetMapping("/dashboard/revenueTrend")
    public AjaxResult dashboardRevenueTrend()
    {
        return AjaxResult.success(dashboardService.selectRevenueTrend());
    }

    // ==================== 回款统计 ====================

    @GetMapping("/payment/statistics")
    public AjaxResult paymentStatistics()
    {
        return AjaxResult.success(paymentPlanService.selectPaymentStatistics());
    }

    @GetMapping("/payment/list")
    public TableDataInfo paymentList(MkPaymentPlan query)
    {
        startPage();
        List<MkPaymentPlan> list = paymentPlanService.selectPaymentPlanList(query);
        return getDataTable(list);
    }
}
