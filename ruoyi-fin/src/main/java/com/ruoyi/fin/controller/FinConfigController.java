package com.ruoyi.fin.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RepeatSubmit;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.fin.domain.FinCostMethodConfig;
import com.ruoyi.fin.domain.FinFiscalConfig;
import com.ruoyi.fin.domain.FinPeriod;
import com.ruoyi.fin.domain.FinVoucherRule;
import com.ruoyi.fin.mapper.FinVoucherRuleMapper;
import com.ruoyi.fin.service.IFinConfigService;
import com.ruoyi.fin.service.impl.FinClosingServiceImpl;
import com.ruoyi.fin.service.impl.FinVoucherRuleServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 财务配置 Controller（结账 + 凭证规则）
 *
 * <p>对应菜单 FinConfig（fin/config/index），权限项已在 fin_02_menu.sql 预置：
 * fin:config:list / query / edit / periodInit / periodUnlock</p>
 *
 * <p><b>内控</b>：结账与反结账是财务经理的专属权限
 * （fin:config:edit 与 fin:config:periodUnlock），会计无权操作。</p>
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/fin/config")
public class FinConfigController extends BaseController
{
    @Autowired
    private FinVoucherRuleServiceImpl ruleService;

    @Autowired
    private FinVoucherRuleMapper ruleMapper;

    @Autowired
    private FinClosingServiceImpl closingService;

    @Autowired
    private IFinConfigService finConfigService;

    // ==================== 会计期间 ====================

    /**
     * 查询期间列表
     */
    @PreAuthorize("@ss.hasPermi('fin:config:list')")
    @GetMapping("/periods")
    public TableDataInfo periods()
    {
        List<FinPeriod> list = ruleMapper.selectPeriodList();
        startPage();
        return getDataTable(list);
    }

    /**
     * 结账前置检查（预检，不落库）
     */
    @PreAuthorize("@ss.hasPermi('fin:config:list')")
    @GetMapping("/closing/check")
    public AjaxResult closingCheck(@RequestParam String yearMonth)
    {
        return AjaxResult.success(closingService.checkBeforeClose(yearMonth));
    }

    /**
     * 执行结账
     *
     * <p>流程：前置检查 → 结转损益 → 生成账簿 → 封期。</p>
     */
    @Log(title = "期末结账", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PostMapping("/closing/close")
    public AjaxResult closePeriod(@RequestParam String yearMonth)
    {
        return AjaxResult.success(closingService.closePeriod(yearMonth));
    }

    /**
     * 反结账（须填原因，留痕）
     */
    @Log(title = "反结账", businessType = BusinessType.FORCE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:config:periodUnlock')")
    @PostMapping("/closing/unlock")
    public AjaxResult unlockPeriod(@RequestParam String yearMonth,
                                   @RequestParam String reason)
    {
        return toAjax(closingService.unlockPeriod(yearMonth, reason));
    }

    /**
     * 查询结账检查记录
     */
    @PreAuthorize("@ss.hasPermi('fin:config:query')")
    @GetMapping("/closing/records")
    public AjaxResult closingRecords(@RequestParam String yearMonth)
    {
        return AjaxResult.success(closingService.selectClosingList(yearMonth));
    }

    // ==================== 凭证规则 ====================

    /**
     * 查询全部凭证规则
     */
    @PreAuthorize("@ss.hasPermi('fin:config:list')")
    @GetMapping("/voucherRules")
    public AjaxResult voucherRules()
    {
        return AjaxResult.success(ruleService.selectAllRules());
    }

    /**
     * 修改凭证规则
     */
    @Log(title = "凭证规则", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PutMapping("/voucherRule")
    public AjaxResult updateRule(@RequestBody FinVoucherRule rule)
    {
        return toAjax(ruleService.updateRule(rule));
    }

    // ==================== 自动凭证生成 ====================

    /**
     * 由业务单据生成凭证（单张）
     */
    @Log(title = "自动凭证生成", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:entry:add')")
    @PostMapping("/voucherRule/generate")
    public AjaxResult generate(@RequestParam String sourceModule,
                               @RequestParam String sourceType,
                               @RequestParam Long sourceId)
    {
        return AjaxResult.success(
                ruleService.generateVoucher(sourceModule, sourceType, sourceId));
    }

    /**
     * 批量生成凭证（扫描已审核业务单据）
     */
    @Log(title = "批量凭证生成", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:gl:entry:add')")
    @PostMapping("/voucherRule/generateBatch")
    public AjaxResult generateBatch(@RequestParam String sourceModule,
                                    @RequestParam String sourceType,
                                    @RequestParam(defaultValue = "50") int limit)
    {
        return AjaxResult.success(
                ruleService.generateBatch(sourceModule, sourceType, limit));
    }

    // ==================== 财务参数配置 ====================

    /**
     * 查询财务参数（键值配置）
     */
    @PreAuthorize("@ss.hasPermi('fin:config:list')")
    @GetMapping("/fiscal/list")
    public TableDataInfo fiscalList(FinFiscalConfig config)
    {
        startPage();
        return getDataTable(finConfigService.selectConfigList(config));
    }

    /**
     * 更新财务参数值
     */
    @Log(title = "财务参数配置", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PutMapping("/fiscal/{configKey}")
    public AjaxResult updateFiscal(@PathVariable("configKey") String configKey,
                                   @RequestParam String configValue)
    {
        return toAjax(finConfigService.updateConfig(configKey, configValue));
    }

    /**
     * 新增财务参数项
     */
    @Log(title = "财务参数配置", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PostMapping("/fiscal")
    public AjaxResult addFiscal(@RequestBody FinFiscalConfig config)
    {
        return toAjax(finConfigService.insertConfig(config));
    }

    /**
     * 删除财务参数项（内置项不允许删除）
     */
    @Log(title = "财务参数配置", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @DeleteMapping("/fiscal/{configKey}")
    public AjaxResult removeFiscal(@PathVariable("configKey") String configKey)
    {
        return toAjax(finConfigService.deleteConfig(configKey));
    }

    // ==================== 成本方法配置 ====================

    /**
     * 查询成本方法配置
     */
    @PreAuthorize("@ss.hasPermi('fin:cost:list')")
    @GetMapping("/costMethod/list")
    public TableDataInfo costMethodList(FinCostMethodConfig config)
    {
        startPage();
        return getDataTable(finConfigService.selectCostMethodList(config));
    }

    /**
     * 新增成本方法配置
     */
    @Log(title = "成本方法配置", businessType = BusinessType.INSERT)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PostMapping("/costMethod")
    public AjaxResult addCostMethod(@RequestBody FinCostMethodConfig config)
    {
        return toAjax(finConfigService.insertCostMethod(config));
    }

    /**
     * 修改成本方法配置
     */
    @Log(title = "成本方法配置", businessType = BusinessType.UPDATE)
    @RepeatSubmit()
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @PutMapping("/costMethod")
    public AjaxResult editCostMethod(@RequestBody FinCostMethodConfig config)
    {
        return toAjax(finConfigService.updateCostMethod(config));
    }

    /**
     * 删除成本方法配置（全局默认不可删）
     */
    @Log(title = "成本方法配置", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('fin:config:edit')")
    @DeleteMapping("/costMethod/{ids}")
    public AjaxResult removeCostMethod(@PathVariable Long[] ids)
    {
        return toAjax(finConfigService.deleteCostMethodByIds(ids));
    }
}