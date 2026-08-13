package com.ruoyi.qms.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        // 文档编号始终由系统自动生成（忽略前端传入的值）
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(doc.getDocCategory()))
        {
            params.put("docCategory", doc.getDocCategory());
        }
        doc.setDocNo(mkNumberRuleService.generateNumber("qms_doc", params));
        doc.setDelFlag("0");
        doc.setStatus("0");
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
