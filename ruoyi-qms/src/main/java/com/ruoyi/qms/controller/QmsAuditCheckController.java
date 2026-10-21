package com.ruoyi.qms.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.qms.domain.QmsAuditCheckItem;
import com.ruoyi.qms.domain.QmsAuditCheckTemplate;
import com.ruoyi.qms.service.IQmsAuditCheckService;

@RestController
@RequestMapping("/qms/auditCheck")
public class QmsAuditCheckController extends BaseController {
    @Autowired
    private IQmsAuditCheckService service;

    /** 检查表模板列表 */
    @PreAuthorize("@ss.hasPermi('qms:audit:list')")
    @GetMapping("/templates")
    public AjaxResult templates(QmsAuditCheckTemplate template) {
        return AjaxResult.success(service.selectTemplateList(template));
    }

    /** 按模板生成检查表（覆盖式） */
    @Log(title = "内审检查表生成", businessType = BusinessType.INSERT)
    @PreAuthorize("@ss.hasPermi('qms:audit:check')")
    @PostMapping("/generate/{auditPlanId}/{templateId}")
    public AjaxResult generate(@PathVariable Long auditPlanId, @PathVariable Long templateId) {
        int count = service.generateCheckItemsFromTemplate(auditPlanId, templateId);
        return AjaxResult.success("已按模板生成 " + count + " 项检查项", count);
    }

    /** 查询计划的检查项 */
    @PreAuthorize("@ss.hasPermi('qms:audit:list')")
    @GetMapping("/items/{auditPlanId}")
    public AjaxResult items(@PathVariable Long auditPlanId) {
        return AjaxResult.success(service.selectCheckItemsByPlanId(auditPlanId));
    }

    /** 保存检查项（批量） */
    @Log(title = "内审检查表保存", businessType = BusinessType.UPDATE)
    @PreAuthorize("@ss.hasPermi('qms:audit:check')")
    @PutMapping("/items/{auditPlanId}")
    public AjaxResult saveItems(@PathVariable Long auditPlanId, @RequestBody List<QmsAuditCheckItem> items) {
        return toAjax(service.saveCheckItems(auditPlanId, items));
    }

    /** 生成审核报告（Word下载） */
    @Log(title = "内审报告生成", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('qms:audit:report')")
    @PostMapping("/report/{auditPlanId}")
    public void report(@PathVariable Long auditPlanId, jakarta.servlet.http.HttpServletResponse response) {
        byte[] data = service.generateAuditReport(auditPlanId);
        try {
            response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            String filename = java.net.URLEncoder.encode("内部审核报告_" + auditPlanId + ".docx", "UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + filename);
            response.getOutputStream().write(data);
            response.getOutputStream().flush();
        } catch (Exception e) {
            throw new com.ruoyi.common.exception.ServiceException("报告下载失败：" + e.getMessage());
        }
    }
}
