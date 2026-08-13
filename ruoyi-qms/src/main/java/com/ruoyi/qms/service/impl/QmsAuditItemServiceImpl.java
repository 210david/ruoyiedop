package com.ruoyi.qms.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsAuditItem;
import com.ruoyi.qms.domain.QmsCapa;
import com.ruoyi.qms.mapper.QmsAuditItemMapper;
import com.ruoyi.qms.mapper.QmsCapaMapper;
import com.ruoyi.qms.service.IQmsAuditItemService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsAuditItemServiceImpl implements IQmsAuditItemService {
    @Autowired
    private QmsAuditItemMapper mapper;
    @Autowired
    private QmsCapaMapper capaMapper;
    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsAuditItem> selectAuditItemList(QmsAuditItem item) { return mapper.selectAuditItemList(item); }

    @Override
    public List<QmsAuditItem> selectByAuditPlanId(Long auditPlanId) { return mapper.selectByAuditPlanId(auditPlanId); }

    @Override
    public QmsAuditItem selectAuditItemById(Long itemId) { return mapper.selectAuditItemById(itemId); }

    @Override
    public int insertAuditItem(QmsAuditItem item) {
        if (item.getItemStatus() == null) item.setItemStatus("0");
        return mapper.insertAuditItem(item);
    }

    @Override
    public int updateAuditItem(QmsAuditItem item) { return mapper.updateAuditItem(item); }

    @Override
    public int deleteAuditItemByIds(Long[] ids) { return mapper.deleteAuditItemByIds(ids); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCapaFromAuditItem(Long itemId) {
        QmsAuditItem item = mapper.selectAuditItemById(itemId);
        if (item == null) throw new ServiceException("不符合项不存在");
        if (item.getCapaId() != null) throw new ServiceException("该不符合项已关联CAPA");

        QmsCapa capa = new QmsCapa();
        capa.setSourceType("audit");
        capa.setSourceNo(item.getAuditPlanNo());
        capa.setTitle("内审不符合项 - " + (StringUtils.isNotEmpty(item.getClauseRef()) ? item.getClauseRef() : ""));
        capa.setProblemDesc(item.getNcDesc());
        capa.setCapaStatus("0");
        capa.setDelFlag("0");
        capa.setStatus("0");
        Map<String, String> params = new HashMap<>();
        capa.setCapaNo(mkNumberRuleService.generateNumber("qms_capa", params));
        capaMapper.insertCapa(capa);

        // 回写不符合项的capaId
        QmsAuditItem update = new QmsAuditItem();
        update.setItemId(itemId);
        update.setCapaId(capa.getCapaId());
        update.setItemStatus("1");
        mapper.updateAuditItem(update);

        return capa.getCapaId();
    }
}
