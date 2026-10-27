package com.ruoyi.qms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qms.domain.QmsWorkshop;
import com.ruoyi.qms.service.IQmsWorkshopService;

/**
 * 车间/产线/工位 Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/qms/workshop")
public class QmsWorkshopController extends BaseController
{
    @Autowired
    private IQmsWorkshopService qmsWorkshopService;

    /**
     * 列表查询（平表）
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:list')")
    @GetMapping("/list")
    public AjaxResult list(QmsWorkshop workshop)
    {
        List<QmsWorkshop> list = qmsWorkshopService.selectWorkshopList(workshop);
        return AjaxResult.success(list);
    }

    /**
     * 树形结构查询
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:list')")
    @GetMapping("/tree")
    public AjaxResult tree(QmsWorkshop workshop)
    {
        List<QmsWorkshop> list = qmsWorkshopService.selectWorkshopList(workshop);
        return AjaxResult.success(qmsWorkshopService.buildWorkshopTree(list));
    }

    /**
     * 详情
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:query')")
    @GetMapping("/{workshopId}")
    public AjaxResult getInfo(@PathVariable Long workshopId)
    {
        return AjaxResult.success(qmsWorkshopService.selectWorkshopById(workshopId));
    }

    /**
     * 新增
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:add')")
    @Log(title = "车间/产线/工位", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody QmsWorkshop workshop)
    {
        return toAjax(qmsWorkshopService.insertWorkshop(workshop));
    }

    /**
     * 修改
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:edit')")
    @Log(title = "车间/产线/工位", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody QmsWorkshop workshop)
    {
        return toAjax(qmsWorkshopService.updateWorkshop(workshop));
    }

    /**
     * 删除
     */
    @PreAuthorize("@ss.hasPermi('qms:workshop:remove')")
    @Log(title = "车间/产线/工位", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workshopIds}")
    public AjaxResult remove(@PathVariable Long[] workshopIds)
    {
        return toAjax(qmsWorkshopService.deleteWorkshopByIds(workshopIds));
    }
}
