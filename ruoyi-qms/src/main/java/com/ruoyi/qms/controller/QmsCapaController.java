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
import com.ruoyi.qms.domain.QmsCapaRootCause;
import com.ruoyi.qms.domain.QmsCapaEffectiveness;
import com.ruoyi.qms.service.IQmsCapaService;
import com.ruoyi.qms.service.IQmsCapaRootCauseService;
import com.ruoyi.qms.service.IQmsCapaEffectivenessService;
import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/qms/capa")
public class QmsCapaController extends BaseController {
    @Autowired
    private IQmsCapaService qmsCapaService;

    @Autowired
    private IQmsCapaRootCauseService rootCauseService;

    @Autowired
    private IQmsCapaEffectivenessService effectivenessService;

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

    @Log(title = "CAPA提交", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PutMapping("/submit/{capaId}")
    public AjaxResult submit(@PathVariable Long capaId) {
        return toAjax(qmsCapaService.submitCapa(capaId));
    }

    @Log(title = "CAPA提交验证", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PutMapping("/submitVerify")
    public AjaxResult submitVerify(@RequestBody QmsCapa capa) {
        return toAjax(qmsCapaService.submitVerify(capa));
    }

    @Log(title = "CAPA关闭", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:close')")
    @PutMapping("/close")
    public AjaxResult close(@RequestBody QmsCapa capa) {
        return toAjax(qmsCapaService.closeCapa(capa));
    }

    @Log(title = "CAPA驳回", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:close')")
    @PutMapping("/reject/{capaId}")
    public AjaxResult reject(@PathVariable Long capaId, @RequestParam(required = false) String rejectReason) {
        return toAjax(qmsCapaService.rejectCapa(capaId, rejectReason));
    }

    // ==================== 根因分析 ====================

    @PreAuthorize("@ss.hasPermi('qms:capa:query')")
    @GetMapping("/rootCause/{capaId}")
    public AjaxResult getRootCause(@PathVariable Long capaId) {
        return AjaxResult.success(rootCauseService.selectByCapaId(capaId));
    }

    @Log(title = "根因分析保存", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PostMapping("/rootCause")
    public AjaxResult saveRootCause(@RequestBody QmsCapaRootCause rootCause) {
        return toAjax(rootCauseService.saveRootCause(rootCause));
    }

    // ==================== 有效性追踪 ====================

    @PreAuthorize("@ss.hasPermi('qms:capa:query')")
    @GetMapping("/effectiveness/list")
    public TableDataInfo effectivenessList(QmsCapaEffectiveness effectiveness) {
        startPage();
        return getDataTable(effectivenessService.selectEffectivenessList(effectiveness));
    }

    @PreAuthorize("@ss.hasPermi('qms:capa:query')")
    @GetMapping("/effectiveness/{capaId}")
    public AjaxResult getEffectivenessByCapaId(@PathVariable Long capaId) {
        return AjaxResult.success(effectivenessService.selectByCapaId(capaId));
    }

    @Log(title = "有效性追踪新增", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PostMapping("/effectiveness")
    public AjaxResult addEffectiveness(@RequestBody QmsCapaEffectiveness effectiveness) {
        return toAjax(effectivenessService.insertEffectiveness(effectiveness));
    }

    @Log(title = "有效性追踪更新", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:capa:edit')")
    @PutMapping("/effectiveness")
    public AjaxResult editEffectiveness(@RequestBody QmsCapaEffectiveness effectiveness) {
        return toAjax(effectivenessService.updateEffectiveness(effectiveness));
    }

    @Log(title = "有效性追踪删除", businessType = BusinessType.DELETE)
    @PreAuthorize("@ss.hasPermi('qms:capa:remove')")
    @DeleteMapping("/effectiveness/{ids}")
    public AjaxResult removeEffectiveness(@PathVariable Long[] ids) {
        return toAjax(effectivenessService.deleteEffectivenessByIds(ids));
    }
}
