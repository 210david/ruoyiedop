package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinCostCarryForward;
import com.ruoyi.fin.domain.FinInventoryCost;
import com.ruoyi.fin.mapper.FinCostCarryForwardMapper;
import com.ruoyi.fin.service.IFinInventoryCostService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 存货成本 Controller
 *
 * <p>【菜单 perms】对应 fin_02_menu.sql：
 * fin:cost:list/query/carry/method/tax/valuation/export</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/cost")
public class FinInventoryCostController extends BaseController
{
    @Autowired
    private IFinInventoryCostService costService;

    @Autowired
    private FinCostCarryForwardMapper carryMapper;

    /**
     * 查询成本台账列表
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinInventoryCost cost)
    {
        startPage();
        List<FinInventoryCost> list = costService.selectCostList(cost);
        return getDataTable(list);
    }

    /**
     * 入库计价（扫描已完成入库单）
     */
    @Log(title = "存货入库计价", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:carry')")
    @PostMapping("/priceInbound")
    public AjaxResult priceInbound()
    {
        return AjaxResult.success(costService.priceInbound());
    }

    /**
     * 出库成本结转（扫描已完成出库单）
     */
    @Log(title = "出库成本结转", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:carry')")
    @PostMapping("/carryOutbound")
    public AjaxResult carryOutbound()
    {
        return AjaxResult.success(costService.carryOutbound());
    }

    /**
     * 期末存货计价表
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:valuation')")
    @GetMapping("/valuation")
    public AjaxResult valuation(@RequestParam(required = false) String period)
    {
        List<Map<String, Object>> list = costService.valuation(period);
        BigDecimal total = BigDecimal.ZERO;
        for (Map<String, Object> row : list)
        {
            Object v = row.get("totalCost");
            if (v != null)
            {
                total = total.add(new BigDecimal(v.toString()));
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("totalValue", total);
        return AjaxResult.success(result);
    }

    /**
     * 成本台账总成本（资产负债表存货科目取数）
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:query')")
    @GetMapping("/totalCost")
    public AjaxResult totalCost()
    {
        return AjaxResult.success(costService.totalCost());
    }

    /**
     * 录入期初成本（上线期初数）
     */
    @Log(title = "期初成本录入", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:cost:carry')")
    @PostMapping("/init")
    public AjaxResult initCost(@Validated @RequestBody FinInventoryCost cost)
    {
        return toAjax(costService.initCost(cost));
    }

    /**
     * 查询成本结转流水
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/carryList")
    public TableDataInfo carryList(FinCostCarryForward cf)
    {
        startPage();
        return getDataTable(carryMapper.selectCarryList(cf));
    }

    /**
     * 导出成本台账
     */
    @Log(title = "存货成本台账", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:cost:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinInventoryCost cost)
    {
        List<FinInventoryCost> list = costService.selectCostList(cost);
        ExcelUtil<FinInventoryCost> util = new ExcelUtil<>(FinInventoryCost.class);
        util.exportExcel(response, list, "存货成本台账数据");
    }
}