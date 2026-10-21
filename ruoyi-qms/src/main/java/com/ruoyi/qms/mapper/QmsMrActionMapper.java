package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsMrAction;

public interface QmsMrActionMapper {
    public List<QmsMrAction> selectMrActionList(QmsMrAction action);
    public List<QmsMrAction> selectByMrId(Long mrId);
    public QmsMrAction selectMrActionById(Long actionId);
    public int insertMrAction(QmsMrAction action);
    public int updateMrAction(QmsMrAction action);
    public int deleteMrActionByIds(Long[] ids);
    public int countByMrIdAndStatus(Long mrId, String excludeStatus);
}
