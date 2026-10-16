package com.ruoyi.mk.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkPricePolicy;
import com.ruoyi.mk.service.IMkPricePolicyService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 价格政策 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mk/pricePolicy")
public class MkPricePolicyController extends BaseController
{
    @Autowired
    private IMkPricePolicyService mkPricePolicyService;

    @PreAuthorize("@ss.hasPermi('marketing:price:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkPricePolicy policy)
    {
        startPage();
        List<MkPricePolicy> list = mkPricePolicyService.selectPolicyList(policy);
        return getDataTable(list);
    }

    @Log(title = "价格政策", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('marketing:price:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkPricePolicy policy)
    {
        List<MkPricePolicy> list = mkPricePolicyService.selectPolicyList(policy);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, policy);
        ExcelUtil<MkPricePolicy> util = new ExcelUtil<>(MkPricePolicy.class);
        util.exportExcel(response, list, "价格政策数据");
    }

    @PreAuthorize("@ss.hasPermi('marketing:price:query')")
    @GetMapping("/{policyId}")
    public AjaxResult getInfo(@PathVariable Long policyId)
    {
        return AjaxResult.success(mkPricePolicyService.selectPolicyById(policyId));
    }

    /** 下单取价：客户专价→等级价→无 */
    @GetMapping("/apply")
    public AjaxResult apply(@RequestParam Long materialId, @RequestParam(required = false) Long customerId)
    {
        return AjaxResult.success(mkPricePolicyService.applyPrice(customerId, materialId));
    }

    @Log(title = "价格政策", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('marketing:price:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MkPricePolicy policy)
    {
        return toAjax(mkPricePolicyService.insertPolicy(policy));
    }

    @Log(title = "价格政策", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('marketing:price:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MkPricePolicy policy)
    {
        return toAjax(mkPricePolicyService.updatePolicy(policy));
    }

    @Log(title = "价格政策", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('marketing:price:remove')")
    @DeleteMapping("/{policyIds}")
    public AjaxResult remove(@PathVariable Long[] policyIds)
    {
        return toAjax(mkPricePolicyService.deletePolicyByIds(policyIds));
    }
}
