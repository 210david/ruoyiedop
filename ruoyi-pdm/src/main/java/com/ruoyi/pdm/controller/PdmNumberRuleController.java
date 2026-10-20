package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.mk.domain.MkNumberRule;
import com.ruoyi.mk.service.IMkNumberRuleService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 产品设计 - 编码规则 Controller
 *
 * 复用统一的编号规则服务（mk_number_rule 表），通过 module='pdm' 过滤本模块的规则。
 * 权限标识使用 pdm:numberRule:* 与其他模块保持一致。
 */
@RestController
@RequestMapping("/pdm/numberRule")
public class PdmNumberRuleController extends BaseController
{
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @PreAuthorize("@ss.hasPermi('pdm:numberRule:list')")
    @GetMapping("/list")
    public TableDataInfo list(MkNumberRule rule)
    {
        startPage();
        List<MkNumberRule> list = mkNumberRuleService.selectRuleList(rule);
        return getDataTable(list);
    }

    @Log(title = "编号规则", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('pdm:numberRule:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MkNumberRule rule)
    {
        List<MkNumberRule> list = mkNumberRuleService.selectRuleList(rule);
        list = com.ruoyi.common.utils.ExportIdFilter.filter(list, rule);
        ExcelUtil<MkNumberRule> util = new ExcelUtil<>(MkNumberRule.class);
        util.exportExcel(response, list, "产品设计编号规则数据");
    }

    @PreAuthorize("@ss.hasPermi('pdm:numberRule:query')")
    @GetMapping("/{ruleId}")
    public AjaxResult getInfo(@PathVariable Long ruleId)
    {
        return AjaxResult.success(mkNumberRuleService.selectRuleById(ruleId));
    }

    /**
     * 根据规则编码获取预览编号（不消耗序列号）
     */
    @PreAuthorize("@ss.hasPermi('pdm:numberRule:query')")
    @GetMapping("/preview/{ruleCode}")
    public AjaxResult preview(@PathVariable String ruleCode)
    {
        MkNumberRule rule = mkNumberRuleService.selectRuleByCode(ruleCode);
        if (rule == null)
        {
            return AjaxResult.error("编号规则不存在");
        }
        return AjaxResult.success("操作成功", mkNumberRuleService.previewNumber(rule));
    }

    @Log(title = "编号规则", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('pdm:numberRule:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody MkNumberRule rule)
    {
        if (!mkNumberRuleService.checkRuleCodeUnique(rule))
        {
            return error("新增规则'" + rule.getRuleCode() + "'失败，规则编码已存在");
        }
        return toAjax(mkNumberRuleService.insertRule(rule));
    }

    @Log(title = "编号规则", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('pdm:numberRule:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody MkNumberRule rule)
    {
        return toAjax(mkNumberRuleService.updateRule(rule));
    }

    @Log(title = "编号规则", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('pdm:numberRule:remove')")
    @DeleteMapping("/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return toAjax(mkNumberRuleService.deleteRuleByIds(ruleIds));
    }
}
