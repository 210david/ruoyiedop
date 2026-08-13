package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsAuditItem;

public interface IQmsAuditItemService {
    public List<QmsAuditItem> selectAuditItemList(QmsAuditItem item);
    public List<QmsAuditItem> selectByAuditPlanId(Long auditPlanId);
    public QmsAuditItem selectAuditItemById(Long itemId);
    public int insertAuditItem(QmsAuditItem item);
    public int updateAuditItem(QmsAuditItem item);
    public int deleteAuditItemByIds(Long[] ids);
    public Long createCapaFromAuditItem(Long itemId);
}
