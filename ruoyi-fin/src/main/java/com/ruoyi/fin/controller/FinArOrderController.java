package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinArOrder;
import com.ruoyi.fin.domain.FinBadDebt;
import com.ruoyi.fin.mapper.FinArWriteoffMapper;
import com.ruoyi.fin.service.IFinArOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 应收管理 Controller
 *
 * <p><b>内控要点</b>（同 P3 应付）</p>
 * <ul>
 *   <li>应收单由发货单自动生成，不提供手工新增接口</li>
 *   <li>核销仅财务经理（fin:ar:writeoff），会计无此权限</li>
 *   <li>已收金额从 MK 同步，财务侧不重复登记收款</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/ar")
public class FinArOrderController extends BaseController
{
    @Autowired
    private IFinArOrderService arService;

    @Autowired
    private FinArWriteoffMapper writeoffMapper;

    /**
     * 查询应收单列表
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinArOrder ar)
    {
        startPage();
        return getDataTable(arService.selectArList(ar));
    }

    /**
     * 查询应收单详情（含核销记录与未收余额）
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:query')")
    @GetMapping("/{arId}")
    public AjaxResult getInfo(@PathVariable("arId") Long arId)
    {
        FinArOrder ar = arService.selectArById(arId);
        if (ar == null)
        {
            return AjaxResult.error("应收单不存在或已删除");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ar", ar);
        result.put("unreceivedAmount", ar.getUnreceivedAmount());
        result.put("writeoffs", writeoffMapper.selectByArId(arId));
        return AjaxResult.success(result);
    }

    /**
     * 从已审核发货单生成应收单
     */
    @Log(title = "应收单生成", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ar:add')")
    @PostMapping("/generate")
    public AjaxResult generate()
    {
        return AjaxResult.success(arService.generateFromShipment());
    }

    /**
     * 同步已收金额（从 MK）
     */
    @Log(title = "应收已收金额同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ar:receipt')")
    @PostMapping("/syncReceived")
    public AjaxResult syncReceived()
    {
        return AjaxResult.success(arService.syncReceivedAmount());
    }

    /**
     * 收款核销（仅财务经理）
     */
    @Log(title = "应收核销", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ar:writeoff')")
    @PostMapping("/writeoff/{arId}")
    public AjaxResult writeoff(@PathVariable("arId") Long arId,
                               @RequestParam java.math.BigDecimal amount,
                               @RequestParam(required = false) String remark)
    {
        return toAjax(arService.writeoff(arId, amount, remark));
    }

    /**
     * 登记销项发票
     */
    @Log(title = "销项发票", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ar:invoice')")
    @PutMapping("/invoice/{arId}")
    public AjaxResult invoice(@PathVariable("arId") Long arId,
                              @RequestParam String invoiceNo,
                              @RequestParam(required = false) String invoiceCode,
                              @RequestParam(required = false)
                              @DateTimeFormat(pattern = "yyyy-MM-dd") Date invoiceDate)
    {
        return toAjax(arService.invoice(arId, invoiceNo, invoiceCode, invoiceDate));
    }

    /**
     * 作废应收单
     */
    @Log(title = "应收单作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ar:edit')")
    @PutMapping("/void/{arId}")
    public AjaxResult voidAr(@PathVariable("arId") Long arId,
                             @RequestParam(required = false) String reason)
    {
        return toAjax(arService.voidAr(new Long[]{arId}, reason));
    }

    /**
     * 账龄分析
     */
    @PreAuthorize("@ss.hasPermi('fin:report:aging')")
    @GetMapping("/aging")
    public AjaxResult aging()
    {
        return AjaxResult.success(arService.aging(null));
    }

    /**
     * 逾期未收预警
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:query')")
    @GetMapping("/overdue")
    public AjaxResult overdue()
    {
        return AjaxResult.success(arService.overdue(null));
    }

    /**
     * 应收汇总
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:query')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) String period)
    {
        return AjaxResult.success(arService.summary(period));
    }

    /**
     * 客户维度应收余额
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:query')")
    @GetMapping("/balanceByCustomer")
    public AjaxResult balanceByCustomer()
    {
        return AjaxResult.success(arService.balanceByCustomer());
    }

    /**
     * 坏账计提（按账龄区间比例）
     */
    @Log(title = "坏账计提", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ar:badDebt')")
    @PostMapping("/badDebt")
    public AjaxResult badDebt(@RequestParam String period)
    {
        List<FinBadDebt> list = arService.provisionBadDebt(period, new Date());
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (FinBadDebt bd : list)
        {
            total = total.add(bd.getProvisionAmount());
        }
        Map<String, Object> r = new HashMap<>();
        r.put("count", list.size());
        r.put("totalAmount", total);
        r.put("list", list);
        return AjaxResult.success(r);
    }

    /**
     * 查询坏账计提记录
     */
    @PreAuthorize("@ss.hasPermi('fin:ar:query')")
    @GetMapping("/badDebt/list")
    public TableDataInfo badDebtList(@RequestParam(required = false) String period)
    {
        startPage();
        return getDataTable(writeoffMapper.selectBadDebtList(period));
    }

    /**
     * 导出应收单
     */
    @Log(title = "应收单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:ar:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinArOrder ar)
    {
        ExcelUtil<FinArOrder> util = new ExcelUtil<>(FinArOrder.class);
        util.exportExcel(response, arService.selectArList(ar), "应收单数据");
    }
}