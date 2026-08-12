package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsDoc;
import com.ruoyi.qms.mapper.QmsDocMapper;
import com.ruoyi.qms.service.IQmsDocService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsDocServiceImpl implements IQmsDocService {
    @Autowired
    private QmsDocMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsDoc> selectDocList(QmsDoc doc) { return mapper.selectDocList(doc); }
    @Override
    public QmsDoc selectDocById(Long docId) { return mapper.selectDocById(docId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertDoc(QmsDoc doc) {
        if (StringUtils.isEmpty(doc.getDocNo())) {
            doc.setDocNo(mkNumberRuleService.generateNumber("qms_doc"));
        }
        doc.setDelFlag("0"); doc.setStatus("0");
        if (doc.getDocStatus() == null) doc.setDocStatus("0");
        if (doc.getVersionNo() == null) doc.setVersionNo("v1.0");
        return mapper.insertDoc(doc);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateDoc(QmsDoc doc) { return mapper.updateDoc(doc); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteDocByIds(Long[] docIds) { return mapper.deleteDocByIds(docIds); }
}
