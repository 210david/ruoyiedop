package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    // 传递动态前缀参数（客诉类型），使动态前缀规则可生效
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(complaint.getComplaintType()))
        {
            params.put("complaintType", complaint.getComplaintType());
        }
        complaint.setComplaintNo(mkNumberRuleService.generateNumber("qms_complaint", params));
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
    public int acceptComplaint(Long complaintId) {
        QmsComplaint complaint = mapper.selectComplaintById(complaintId);
        if (complaint == null) throw new RuntimeException("客诉不存在");
        if (!"0".equals(complaint.getComplaintStatus())) throw new RuntimeException("当前状态不允许受理，需为「已登记」");
        QmsComplaint update = new QmsComplaint();
        update.setComplaintId(complaintId);
        update.setComplaintStatus("1");
        return mapper.updateComplaint(update);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int completeComplaint(QmsComplaint complaint) {
        if (complaint.getComplaintId() == null) throw new RuntimeException("客诉ID不能为空");
        QmsComplaint existing = mapper.selectComplaintById(complaint.getComplaintId());
        if (existing == null) throw new RuntimeException("客诉不存在");
        if (!"1".equals(existing.getComplaintStatus())) throw new RuntimeException("当前状态不允许完成处理，需为「处理中」");
        QmsComplaint update = new QmsComplaint();
        update.setComplaintId(complaint.getComplaintId());
        update.setComplaintStatus("2");
        update.setHandleDesc(complaint.getHandleDesc());
        update.setHandleResult(complaint.getHandleResult());
        update.setReturnQty(complaint.getReturnQty());
        update.setReturnAmt(complaint.getReturnAmt());
        update.setClaimAmt(complaint.getClaimAmt());
        update.setRemark(complaint.getRemark());
        return mapper.updateComplaint(update);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeComplaint(Long complaintId) {
        QmsComplaint complaint = mapper.selectComplaintById(complaintId);
        if (complaint == null) throw new RuntimeException("客诉不存在");
        if (!"2".equals(complaint.getComplaintStatus())) throw new RuntimeException("当前状态不允许关闭，需为「待确认」");
        QmsComplaint update = new QmsComplaint();
        update.setComplaintId(complaintId);
        update.setComplaintStatus("3");
        update.setCloseTime(new Date());
        return mapper.updateComplaint(update);
    }
}
