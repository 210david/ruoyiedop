package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsMrAction;

public interface IQmsMrActionService {
    public List<QmsMrAction> selectMrActionList(QmsMrAction action);
    public List<QmsMrAction> selectByMrId(Long mrId);
    public QmsMrAction selectMrActionById(Long actionId);
    public int insertMrAction(QmsMrAction action);
    public int updateMrAction(QmsMrAction action);
    public int closeAction(Long actionId, String actionResult);
    public int deleteMrActionByIds(Long[] ids);
    public boolean checkAllActionsClosed(Long mrId);
}
