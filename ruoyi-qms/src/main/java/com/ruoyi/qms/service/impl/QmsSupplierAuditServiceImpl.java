package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsSupplierAudit;
import com.ruoyi.qms.mapper.QmsSupplierAuditMapper;
import com.ruoyi.qms.service.IQmsSupplierAuditService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsSupplierAuditServiceImpl implements IQmsSupplierAuditService {
    @Autowired
    private QmsSupplierAuditMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsSupplierAudit> selectAuditList(QmsSupplierAudit audit) { return mapper.selectAuditList(audit); }
    @Override
    public QmsSupplierAudit selectAuditById(Long auditId) { return mapper.selectAuditById(auditId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertAudit(QmsSupplierAudit audit) {
        if (StringUtils.isEmpty(audit.getAuditNo())) {
            audit.setAuditNo(mkNumberRuleService.generateNumber("qms_supplier_audit"));
        }
        audit.setDelFlag("0"); audit.setStatus("0");
        if (audit.getAuditStatus() == null) audit.setAuditStatus("0");
        return mapper.insertAudit(audit);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateAudit(QmsSupplierAudit audit) { return mapper.updateAudit(audit); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteAuditByIds(Long[] auditIds) { return mapper.deleteAuditByIds(auditIds); }
}
