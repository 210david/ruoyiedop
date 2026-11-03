package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsComplaint;
import com.ruoyi.qms.domain.QmsSupplierRectify;
import com.ruoyi.qms.mapper.QmsComplaintMapper;
import com.ruoyi.qms.mapper.QmsSupplierRectifyMapper;
import com.ruoyi.qms.service.IQmsSupplierRectifyService;
import com.ruoyi.mk.service.IMkNumberRuleService;

/**
 * 供应商整改单（客诉供应商责任处理）Service业务层处理
 *
 * 业务规则：
 * KR-01 供应商信息从采购管理供应商档案选择，不允许手工新建供应商
 * KR-03 供应商整改单逾期未回复，消息中心自动升级提醒采购负责人
 */
@Service
public class QmsSupplierRectifyServiceImpl implements IQmsSupplierRectifyService {
    @Autowired
    private QmsSupplierRectifyMapper mapper;

    @Autowired
    private QmsComplaintMapper complaintMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<QmsSupplierRectify> selectRectifyList(QmsSupplierRectify rectify) { return mapper.selectRectifyList(rectify); }

    @Override
    public QmsSupplierRectify selectRectifyById(Long rectifyId) { return mapper.selectRectifyById(rectifyId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertRectify(QmsSupplierRectify rectify) {
        if (rectify.getComplaintId() == null) throw new RuntimeException("客诉ID不能为空");
        QmsComplaint complaint = complaintMapper.selectComplaintById(rectify.getComplaintId());
        if (complaint == null) throw new RuntimeException("客诉不存在");
        // 仅处理中/待确认状态可发起供应商整改单
        if (!"1".equals(complaint.getComplaintStatus()) && !"2".equals(complaint.getComplaintStatus())) {
            throw new RuntimeException("当前客诉状态不允许发起供应商整改单，需为「处理中」或「待确认」");
        }
        // 整改单面向供应商责任，客诉单需已选择供应商（KR-01 数据源约束）
        if (complaint.getSupplierId() == null) {
            throw new RuntimeException("客诉单未关联供应商，请先在客诉单中补充供应商信息");
        }
        if (rectify.getRectifyRequirement() == null || rectify.getRectifyRequirement().trim().isEmpty()) {
            throw new RuntimeException("整改要求不能为空");
        }
        if (rectify.getReplyDeadline() == null) {
            throw new RuntimeException("供应商回复时限不能为空");
        }
        // 编号自动生成；客诉与供应商/物料信息从客诉单带出（保证主数据一致）
        if (StringUtils.isEmpty(rectify.getRectifyNo())) {
            rectify.setRectifyNo(mkNumberRuleService.generateNumber("qms_supplier_rectify"));
        }
        rectify.setComplaintNo(complaint.getComplaintNo());
        rectify.setSupplierId(complaint.getSupplierId());
        rectify.setSupplierName(complaint.getSupplierName());
        rectify.setMaterialName(complaint.getMaterialName());
        rectify.setBatchNo(complaint.getBatchNo());
        rectify.setDelFlag("0");
        if (rectify.getRectifyStatus() == null) rectify.setRectifyStatus("0");
        // TODO 消息中心：发送提醒至供应商对接人（后续接入消息中心组件后补充推送）
        return mapper.insertRectify(rectify);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateRectify(QmsSupplierRectify rectify) {
        if (rectify.getRectifyId() == null) throw new RuntimeException("整改单ID不能为空");
        QmsSupplierRectify existing = mapper.selectRectifyById(rectify.getRectifyId());
        if (existing == null) throw new RuntimeException("整改单不存在");
        // 供应商首次回复：状态 待回复 → 已回复
        if (StringUtils.isNotEmpty(rectify.getSupplierReply()) && "0".equals(existing.getRectifyStatus())) {
            rectify.setRectifyStatus("1");
            rectify.setReplyTime(new Date());
        }
        return mapper.updateRectify(rectify);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteRectifyByIds(Long[] rectifyIds) { return mapper.deleteRectifyByIds(rectifyIds); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeRectify(Long rectifyId) {
        QmsSupplierRectify existing = mapper.selectRectifyById(rectifyId);
        if (existing == null) throw new RuntimeException("整改单不存在");
        if (!"1".equals(existing.getRectifyStatus()) && !"2".equals(existing.getRectifyStatus())) {
            throw new RuntimeException("当前状态不允许关闭，需供应商已回复（含逾期升级）");
        }
        QmsSupplierRectify update = new QmsSupplierRectify();
        update.setRectifyId(rectifyId);
        update.setRectifyStatus("3");
        return mapper.updateRectify(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleOverdue() {
        int rows = mapper.markOverdue();
        // TODO 消息中心：对本次升级的整改单推送消息至采购负责人（KR-03，接入消息中心组件后补充推送）
        return rows;
    }
}
