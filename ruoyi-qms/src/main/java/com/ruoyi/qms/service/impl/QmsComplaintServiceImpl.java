package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsComplaint;
import com.ruoyi.qms.mapper.QmsComplaintMapper;
import com.ruoyi.qms.service.IQmsComplaintService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsComplaintServiceImpl implements IQmsComplaintService {
    @Autowired
    private QmsComplaintMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsComplaint> selectComplaintList(QmsComplaint complaint) { return mapper.selectComplaintList(complaint); }
    @Override
    public QmsComplaint selectComplaintById(Long complaintId) { return mapper.selectComplaintById(complaintId); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertComplaint(QmsComplaint complaint) {
        if (StringUtils.isEmpty(complaint.getComplaintNo())) {
            complaint.setComplaintNo(mkNumberRuleService.generateNumber("qms_complaint"));
        }
        complaint.setDelFlag("0"); complaint.setStatus("0");
        if (complaint.getComplaintStatus() == null) complaint.setComplaintStatus("0");
        return mapper.insertComplaint(complaint);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateComplaint(QmsComplaint complaint) { return mapper.updateComplaint(complaint); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteComplaintByIds(Long[] complaintIds) { return mapper.deleteComplaintByIds(complaintIds); }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeComplaint(Long complaintId) {
        QmsComplaint update = new QmsComplaint();
        update.setComplaintId(complaintId);
        update.setComplaintStatus("3");
        update.setCloseTime(new Date());
        return mapper.updateComplaint(update);
    }
}
