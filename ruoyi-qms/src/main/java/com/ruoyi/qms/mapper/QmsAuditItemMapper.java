package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsAuditItem;

public interface QmsAuditItemMapper {
    public List<QmsAuditItem> selectAuditItemList(QmsAuditItem item);
    public List<QmsAuditItem> selectByAuditPlanId(Long auditPlanId);
    public QmsAuditItem selectAuditItemById(Long itemId);
    public int insertAuditItem(QmsAuditItem item);
    public int updateAuditItem(QmsAuditItem item);
    public int deleteAuditItemByIds(Long[] ids);
    public int countByAuditPlanId(Long auditPlanId);
}
