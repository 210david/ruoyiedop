package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsAuditCheckTemplate;
import com.ruoyi.qms.domain.QmsAuditCheckTemplateItem;
import com.ruoyi.qms.domain.QmsAuditCheckItem;
import org.apache.ibatis.annotations.Param;

public interface QmsAuditCheckMapper {
    public List<QmsAuditCheckTemplate> selectTemplateList(QmsAuditCheckTemplate template);
    public QmsAuditCheckTemplate selectTemplateById(Long templateId);
    public List<QmsAuditCheckTemplateItem> selectTemplateItems(Long templateId);

    public List<QmsAuditCheckItem> selectCheckItemsByPlanId(Long auditPlanId);
    public QmsAuditCheckItem selectCheckItemById(Long checkId);
    public int insertCheckItem(QmsAuditCheckItem item);
    public int updateCheckItem(QmsAuditCheckItem item);
    public int deleteCheckItemsByPlanId(@Param("auditPlanId") Long auditPlanId);
    public int countCheckItemsByPlanId(@Param("auditPlanId") Long auditPlanId);
}
