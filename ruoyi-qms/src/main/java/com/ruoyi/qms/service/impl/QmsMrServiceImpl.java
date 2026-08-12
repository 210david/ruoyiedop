package com.ruoyi.qms.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsMr;
import com.ruoyi.qms.mapper.QmsMrMapper;
import com.ruoyi.qms.service.IQmsMrService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsMrServiceImpl implements IQmsMrService {
    @Autowired
    private QmsMrMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsMr> selectMrList(QmsMr mr) { return mapper.selectMrList(mr); }
    @Override
    public QmsMr selectMrById(Long mrId) { return mapper.selectMrById(mrId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertMr(QmsMr mr) {
        if (StringUtils.isEmpty(mr.getMrNo())) {
            mr.setMrNo(mkNumberRuleService.generateNumber("qms_mr"));
        }
        mr.setDelFlag("0"); mr.setStatus("0");
        if (mr.getMrStatus() == null) mr.setMrStatus("0");
        return mapper.insertMr(mr);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateMr(QmsMr mr) { return mapper.updateMr(mr); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteMrByIds(Long[] mrIds) { return mapper.deleteMrByIds(mrIds); }
}
