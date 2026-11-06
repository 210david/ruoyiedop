package com.ruoyi.pjm.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
import com.ruoyi.pjm.domain.PjmProjectProcurement;
import com.ruoyi.pjm.service.IPjmProjectProcurementService;

/**
 * 项目立项采购清单 Controller
 */
@RestController
@RequestMapping("/pjm/project/procurement")
public class PjmProjectProcurementController extends BaseController
{
    @Autowired
    private IPjmProjectProcurementService procurementService;

    @PreAuthorize("@ss.hasPermi('pjm:project:list')")
    @GetMapping("/list")
    public TableDataInfo list(PjmProjectProcurement procurement)
    {
        startPage();
        List<PjmProjectProcurement> list = procurementService.selectPjmProjectProcurementList(procurement);
        return getDataTable(list);
    }

    /** 按项目ID查询 */
    @GetMapping("/project/{projectId}")
    public AjaxResult listByProject(@PathVariable Long projectId)
    {
        return success(procurementService.selectByProjectId(projectId));
    }

    @GetMapping("/{procurementId}")
    public AjaxResult getInfo(@PathVariable Long procurementId)
    {
        return success(procurementService.selectPjmProjectProcurementById(procurementId));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:add')")
    @Log(title = "采购清单", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody PjmProjectProcurement procurement)
    {
        procurement.setCreateBy(getUsername());
        return toAjax(procurementService.insertPjmProjectProcurement(procurement));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:edit')")
    @Log(title = "采购清单", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody PjmProjectProcurement procurement)
    {
        procurement.setUpdateBy(getUsername());
        return toAjax(procurementService.updatePjmProjectProcurement(procurement));
    }

    @PreAuthorize("@ss.hasPermi('pjm:project:remove')")
    @Log(title = "采购清单", businessType = BusinessType.DELETE)
    @DeleteMapping("/{procurementIds}")
    public AjaxResult remove(@PathVariable Long[] procurementIds)
    {
        return toAjax(procurementService.deletePjmProjectProcurementByIds(procurementIds));
    }
}
