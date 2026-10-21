package com.ruoyi.qms.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.qms.domain.QmsAuditCheckTemplate;
import com.ruoyi.qms.domain.QmsAuditCheckItem;

public interface IQmsAuditCheckService {
    /** 模板列表 */
    public List<QmsAuditCheckTemplate> selectTemplateList(QmsAuditCheckTemplate template);

    /** 按模板生成审核检查表（覆盖该计划已有检查项） */
    public int generateCheckItemsFromTemplate(Long auditPlanId, Long templateId);

    /** 查询计划的检查项 */
    public List<QmsAuditCheckItem> selectCheckItemsByPlanId(Long auditPlanId);

    /** 保存检查项（批量） */
    public int saveCheckItems(Long auditPlanId, List<QmsAuditCheckItem> items);

    /** 生成审核报告Word，返回字节数组 */
    public byte[] generateAuditReport(Long auditPlanId);
}
