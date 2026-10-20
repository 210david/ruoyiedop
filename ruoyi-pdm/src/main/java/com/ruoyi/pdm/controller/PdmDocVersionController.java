package com.ruoyi.pdm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.service.IPdmDocVersionService;

/**
 * 设计资料版本 Controller（版本时间轴数据源，只读）
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/pdm/docVersion")
public class PdmDocVersionController extends BaseController
{
    @Autowired
    private IPdmDocVersionService versionService;

    /**
     * 查询资料版本列表（按资料类型+业务ID，时间轴展示）
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:query')")
    @GetMapping("/list")
    public AjaxResult list(@RequestParam String bizType, @RequestParam Long bizId)
    {
        PdmDocVersion query = new PdmDocVersion();
        query.setBizType(bizType);
        query.setBizId(bizId);
        List<PdmDocVersion> list = versionService.selectVersionList(query);
        return success(list);
    }

    /**
     * 查询资料当前生效版本
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:query')")
    @GetMapping("/effective")
    public AjaxResult effective(@RequestParam String bizType, @RequestParam Long bizId)
    {
        return success(versionService.selectEffectiveVersion(bizType, bizId));
    }

    /**
     * 查询产品档案聚合的全部资料版本（方案1/图纸2/BOM3，档案"版本记录"数据源）
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:query')")
    @GetMapping("/listByProduct")
    public AjaxResult listByProduct(@RequestParam Long productId)
    {
        return success(versionService.selectVersionsByProduct(productId));
    }

    /**
     * 获取版本详情
     */
    @PreAuthorize("@ss.hasPermi('pdm:product:query')")
    @GetMapping(value = "/{versionId}")
    public AjaxResult getInfo(@PathVariable("versionId") Long versionId)
    {
        return success(versionService.selectVersionById(versionId));
    }
}
