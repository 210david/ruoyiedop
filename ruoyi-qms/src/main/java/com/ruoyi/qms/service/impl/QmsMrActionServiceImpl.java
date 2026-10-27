package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.qms.domain.QmsMrAction;
import com.ruoyi.qms.mapper.QmsMrActionMapper;
import com.ruoyi.qms.service.IQmsMrActionService;

@Service
public class QmsMrActionServiceImpl implements IQmsMrActionService {
    @Autowired
    private QmsMrActionMapper mapper;

    @Override
    public List<QmsMrAction> selectMrActionList(QmsMrAction action) { return mapper.selectMrActionList(action); }

    @Override
    public List<QmsMrAction> selectByMrId(Long mrId) { return mapper.selectByMrId(mrId); }

    @Override
    public QmsMrAction selectMrActionById(Long actionId) { return mapper.selectMrActionById(actionId); }

    @Override
    public int insertMrAction(QmsMrAction action) {
        if (action.getActionStatus() == null) action.setActionStatus("0");
        return mapper.insertMrAction(action);
    }

    @Override
    public int updateMrAction(QmsMrAction action) { return mapper.updateMrAction(action); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeAction(Long actionId, String actionResult) {
        QmsMrAction existing = mapper.selectMrActionById(actionId);
        if (existing == null) throw new ServiceException("改进项不存在");
        QmsMrAction update = new QmsMrAction();
        update.setActionId(actionId);
        update.setActionStatus("2");
        update.setActualCloseDate(new Date());
        update.setActionResult(actionResult);
        return mapper.updateMrAction(update);
    }

    @Override
    public int deleteMrActionByIds(Long[] ids) { return mapper.deleteMrActionByIds(ids); }

    @Override
    public boolean checkAllActionsClosed(Long mrId) {
        int count = mapper.countByMrIdAndStatus(mrId, "2");
        return count == 0;
    }
}
