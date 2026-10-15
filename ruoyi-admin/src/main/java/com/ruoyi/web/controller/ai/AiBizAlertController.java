package com.ruoyi.web.controller.ai;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.ai.service.IAiBizAlertService;
import com.ruoyi.common.core.domain.AjaxResult;

/**
 * AI行业化预警 Controller（P3业务按钮）
 *
 * 临期批次出库建议、产出率/损耗异常解读——用户在业务页面主动点击查询，
 * 遵循"谁接收谁处置谁闭环"原则，不做主动推送，推送噪音天然受控。
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ai/bizAlert")
public class AiBizAlertController
{
    @Autowired
    private IAiBizAlertService bizAlertService;

    /**
     * 临期批次出库建议（FEFO口径）
     */
    @PreAuthorize("@ss.hasPermi('food:batch:list')")
    @GetMapping("/expiryAdvice")
    public AjaxResult expiryAdvice(Integer days)
    {
        return AjaxResult.success(bizAlertService.expiryAdvice(days));
    }

    /**
     * 产出率/损耗异常解读
     */
    @PreAuthorize("@ss.hasPermi('mms:workorder:list')")
    @GetMapping("/lossInterpret")
    public AjaxResult lossInterpret(java.math.BigDecimal threshold)
    {
        return AjaxResult.success(bizAlertService.lossInterpret(threshold));
    }
}
