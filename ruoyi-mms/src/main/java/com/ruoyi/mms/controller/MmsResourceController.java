package com.ruoyi.mms.controller;

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
import com.ruoyi.mms.domain.MmsResource;
import com.ruoyi.mms.service.IMmsResourceService;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Resource Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/mms/resource")
public class MmsResourceController extends BaseController
{
    @Autowired
    private IMmsResourceService mmsResourceService;

    @PreAuthorize("@ss.hasPermi('mms:resource:list')")
    @GetMapping("/list")
    public TableDataInfo list(MmsResource resource)
    {
        startPage();
        List<MmsResource> list = mmsResourceService.selectResourceList(resource);
        return getDataTable(list);
    }

    @Log(title = "Resource", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('mms:resource:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, MmsResource resource)
    {
        List<MmsResource> list = mmsResourceService.selectResourceList(resource);
        ExcelUtil<MmsResource> util = new ExcelUtil<>(MmsResource.class);
        util.exportExcel(response, list, "Resource");
    }

    @PreAuthorize("@ss.hasPermi('mms:resource:query')")
    @GetMapping(value = "/{ResourceId}")
    public AjaxResult getInfo(@PathVariable("ResourceId") Long ResourceId)
    {
        return AjaxResult.success(mmsResourceService.selectResourceById(ResourceId));
    }

    @Log(title = "Resource", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('mms:resource:add')")
    @PostMapping
    public AjaxResult add(@RequestBody MmsResource resource)
    {
        return toAjax(mmsResourceService.insertResource(resource));
    }

    @Log(title = "Resource", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('mms:resource:edit')")
    @PutMapping
    public AjaxResult edit(@RequestBody MmsResource resource)
    {
        return toAjax(mmsResourceService.updateResource(resource));
    }

    @Log(title = "Resource", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('mms:resource:remove')")
    @DeleteMapping("/{ResourceIds}")
    public AjaxResult remove(@PathVariable Long[] ResourceIds)
    {
        return toAjax(mmsResourceService.deleteResourceByIds(ResourceIds));
    }
}
