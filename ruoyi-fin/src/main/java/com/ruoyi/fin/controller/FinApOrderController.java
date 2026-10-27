package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.fin.domain.FinApOrder;
import com.ruoyi.fin.mapper.FinApWriteoffMapper;
import com.ruoyi.fin.service.IFinApOrderService;
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
 * 应付管理 Controller
 *
 * <p><b>内控要点</b></p>
 * <ul>
 *   <li>应付单由发票自动生成，不提供手工新增接口</li>
 *   <li>核销仅财务经理（fin:ap:writeoff），会计无此权限</li>
 *   <li>已付金额从 PMS 同步，财务侧不重复登记付款</li>
 * </ul>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/ap")
public class FinApOrderController extends BaseController
{
    @Autowired
    private IFinApOrderService apService;

    @Autowired
    private FinApWriteoffMapper writeoffMapper;

    /**
     * 查询应付单列表
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:list')")
    @GetMapping("/list")
    public TableDataInfo list(FinApOrder ap)
    {
        startPage();
        return getDataTable(apService.selectApList(ap));
    }

    /**
     * 查询应付单详情
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:query')")
    @GetMapping("/{apId}")
    public AjaxResult getInfo(@PathVariable("apId") Long apId)
    {
        FinApOrder ap = apService.selectApById(apId);
        if (ap == null)
        {
            return AjaxResult.error("应付单不存在或已删除");
        }
        Map<String, Object> result = new HashMap<>();
        result.put("ap", ap);
        result.put("unpaidAmount", ap.getUnpaidAmount());
        result.put("writeoffs", writeoffMapper.selectByApId(apId));
        return AjaxResult.success(result);
    }

    /**
     * 从已审核发票生成应付单
     */
    @Log(title = "应付单生成", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ap:add')")
    @PostMapping("/generate")
    public AjaxResult generate()
    {
        return AjaxResult.success(apService.generateFromInvoice());
    }

    /**
     * 同步已付金额（从 PMS）
     */
    @Log(title = "应付已付金额同步", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ap:pay')")
    @PostMapping("/syncPaid")
    public AjaxResult syncPaid()
    {
        int updated = apService.syncPaidAmount();
        return AjaxResult.success(updated);
    }

    /**
     * 付款核销（仅财务经理）
     */
    @Log(title = "应付核销", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:ap:writeoff')")
    @PostMapping("/writeoff/{apId}")
    public AjaxResult writeoff(@PathVariable("apId") Long apId,
                               @RequestParam BigDecimal amount,
                               @RequestParam(required = false) String remark)
    {
        return toAjax(apService.writeoff(apId, amount, remark));
    }

    /**
     * 作废应付单
     */
    @Log(title = "应付单作废", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:ap:edit')")
    @PutMapping("/void/{apId}")
    public AjaxResult voidAp(@PathVariable("apId") Long apId,
                             @RequestParam(required = false) String reason)
    {
        return toAjax(apService.voidAp(new Long[]{apId}, reason));
    }

    /**
     * 账龄分析
     */
    @PreAuthorize("@ss.hasPermi('fin:report:aging')")
    @GetMapping("/aging")
    public AjaxResult aging()
    {
        return AjaxResult.success(apService.aging(null));
    }

    /**
     * 逾期未付预警
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:query')")
    @GetMapping("/overdue")
    public AjaxResult overdue()
    {
        return AjaxResult.success(apService.overdue(null));
    }

    /**
     * 应付汇总
     */
    @PreAuthorize("@ss.hasPermi('fin:ap:query')")
    @GetMapping("/summary")
    public AjaxResult summary(@RequestParam(required = false) String period)
    {
        return AjaxResult.success(apService.summary(period));
    }

    /**
     * 导出应付单
     */
    @Log(title = "应付单", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('fin:ap:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, FinApOrder ap)
    {
        ExcelUtil<FinApOrder> util = new ExcelUtil<>(FinApOrder.class);
        util.exportExcel(response, apService.selectApList(ap), "应付单数据");
    }
}