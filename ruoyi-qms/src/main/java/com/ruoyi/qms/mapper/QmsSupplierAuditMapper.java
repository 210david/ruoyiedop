package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsSupplierAudit;

public interface QmsSupplierAuditMapper {
    public List<QmsSupplierAudit> selectAuditList(QmsSupplierAudit audit);
    public QmsSupplierAudit selectAuditById(Long auditId);
    public int insertAudit(QmsSupplierAudit audit);
    public int updateAudit(QmsSupplierAudit audit);
    public int deleteAuditByIds(Long[] auditIds);
}
