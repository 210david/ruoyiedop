package com.ruoyi.qms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qms.domain.QmsIndustryTpl;
import com.ruoyi.qms.service.IQmsIndustryTplService;

/**
 * 行业模板配置 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/tpl")
public class QmsIndustryTplController extends BaseController
{
    @Autowired
    private IQmsIndustryTplService qmsIndustryTplService;

    @PreAuthorize("@ss.hasPermi('qms:tpl:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsIndustryTpl industryTpl)
    {
        startPage();
        List<QmsIndustryTpl> list = qmsIndustryTplService.selectIndustryTplList(industryTpl);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('qms:tpl:query')")
    @GetMapping(value = "/{tplId}")
    public AjaxResult getInfo(@PathVariable("tplId") Long tplId)
    {
        return AjaxResult.success(qmsIndustryTplService.selectIndustryTplById(tplId));
    }

    @Log(title = "行业模板", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:tpl:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsIndustryTpl industryTpl)
    {
        return toAjax(qmsIndustryTplService.insertIndustryTpl(industryTpl));
    }

    @Log(title = "行业模板", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:tpl:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsIndustryTpl industryTpl)
    {
        return toAjax(qmsIndustryTplService.updateIndustryTpl(industryTpl));
    }

    @Log(title = "行业模板", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:tpl:remove')")
    @DeleteMapping("/{tplIds}")
    public AjaxResult remove(@PathVariable Long[] tplIds)
    {
        return toAjax(qmsIndustryTplService.deleteIndustryTplByIds(tplIds));
    }
}
