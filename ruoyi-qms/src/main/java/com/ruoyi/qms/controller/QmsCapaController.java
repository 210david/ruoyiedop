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
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.qms.domain.QmsCapa;
import com.ruoyi.qms.service.IQmsCapaService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/capa")
public class QmsCapaController extends BaseController {
    @Autowired
    private IQmsCapaService qmsCapaService;

    @PreAuthorize("@ss.hasPermi('qms:capa:list')")
    @GetMapping("/list")
    public TableDataInfo list(QmsCapa capa) {
        startPage();
        List<QmsCapa> list = qmsCapaService.selectCapaList(capa);
        return getDataTable(list);
    }

    @Log(title = "CAPA", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:capa:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, QmsCapa capa) {
        List<QmsCapa> list = qmsCapaService.selectCapaList(capa);
        ExcelUtil<QmsCapa> util = new ExcelUtil<>(QmsCapa.class);
        util.exportExcel(response, list, "CAPA数据");
    }

    @PreAuthorize("@ss.hasPermi('qms:capa:query')")
    @GetMapping("/{capaId}")
    public AjaxResult getInfo(@PathVariable Long capaId) {
        return AjaxResult.success(qmsCapaService.selectCapaById(capaId));
    }

    @Log(title = "CAPA", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:capa:add')")
    @PostMapping
    public AjaxResult add(@Validated @RequestBody QmsCapa capa) {
        return toAjax(qmsCapaService.insertCapa(capa));
    }

    @Log(title = "CAPA", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody QmsCapa capa) {
        return toAjax(qmsCapaService.updateCapa(capa));
    }

    @Log(title = "CAPA", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:capa:remove')")
    @DeleteMapping("/{capaIds}")
    public AjaxResult remove(@PathVariable Long[] capaIds) {
        return toAjax(qmsCapaService.deleteCapaByIds(capaIds));
    }

    @Log(title = "CAPA关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:close')")
    @PutMapping("/close/{capaId}")
    public AjaxResult close(@PathVariable Long capaId) {
        return toAjax(qmsCapaService.closeCapa(capaId));
    }
}
