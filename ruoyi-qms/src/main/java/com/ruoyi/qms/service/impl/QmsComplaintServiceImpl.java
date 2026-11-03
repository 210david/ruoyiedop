package com.ruoyi.qms.service.impl;

import com.ruoyi.common.annotation.DataScope;
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
import com.ruoyi.wms.domain.WmsSupplier;
import com.ruoyi.wms.service.IWmsSupplierService;

@Service
public class QmsComplaintServiceImpl implements IQmsComplaintService {
    @Autowired
    private QmsComplaintMapper mapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private IWmsSupplierService wmsSupplierService;

    @Override
    @DataScope(deptAlias = "qms_complaint")
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
        validateSupplier(complaint);
        return mapper.insertComplaint(complaint);
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateComplaint(QmsComplaint complaint) { validateSupplier(complaint); return mapper.updateComplaint(complaint); }
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
        validateComplete(complaint, existing);
        return mapper.updateComplaint(update);
    }

    /**
     * 客诉处理校验：退货/索赔金额字段按处理方式联动校验（与前端 requiredByHandleResult 口径一致）。
     * 1=退货退款：退货数量、退货金额必填
     * 2=换货处理：退货数量必填
     * 3/4/5：均选填
     */
    private void validateComplete(QmsComplaint complaint, QmsComplaint existing) {
        String handleResult = complaint.getHandleResult();
        if (handleResult == null || handleResult.isEmpty()) throw new RuntimeException("处理结果不能为空");
        if (complaint.getHandleDesc() == null || complaint.getHandleDesc().trim().isEmpty()) throw new RuntimeException("处理描述不能为空");
        switch (handleResult) {
            case "1":
                if (complaint.getReturnQty() == null) throw new RuntimeException("退货退款方式下必须填写退货数量");
                if (complaint.getReturnAmt() == null) throw new RuntimeException("退货退款方式下必须填写退货金额");
                break;
            case "2":
                if (complaint.getReturnQty() == null) throw new RuntimeException("换货处理方式下必须填写退货数量");
                break;
            default:
                break;
        }
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeComplaint(Long complaintId) {
        QmsComplaint complaint = mapper.selectComplaintById(complaintId);
        if (complaint == null) throw new RuntimeException("客诉不存在");
        if (!"2".equals(complaint.getComplaintStatus())) throw new RuntimeException("当前状态不允许关闭，需为「待确认」");
        // KR-02：责任类型为「待定」时，关闭前须补充判定依据（填写于备注）
        if ("4".equals(complaint.getResponsibilityType()) && (complaint.getRemark() == null || complaint.getRemark().trim().isEmpty())) {
            throw new RuntimeException("责任类型为「待定」，关闭前须在备注中补充判定依据后再关闭（KR-02）");
        }
        QmsComplaint update = new QmsComplaint();
        update.setComplaintId(complaintId);
        update.setComplaintStatus("3");
        update.setCloseTime(new Date());
        return mapper.updateComplaint(update);
    }

    /**
     * 需求二/KR-01 供应商信息校验：
     * 1. 供应商必须从供应商档案（wms_supplier）选择，不允许手工填写名称；
     * 2. 责任类型为「供应商责任」时必须关联供应商；
     * 3. 已选供应商时，名称/联系人/电话以档案主数据为准自动带出，保证主数据一致。
     */
    private void validateSupplier(QmsComplaint complaint) {
        if (complaint.getSupplierName() != null && !complaint.getSupplierName().isEmpty() && complaint.getSupplierId() == null) {
            throw new RuntimeException("供应商必须从供应商档案中选择，不允许手工填写（KR-01）");
        }
        if ("0".equals(complaint.getResponsibilityType()) && complaint.getSupplierId() == null) {
            throw new RuntimeException("责任类型为「供应商责任」时必须关联供应商");
        }
        if (complaint.getSupplierId() != null) {
            WmsSupplier supplier = wmsSupplierService.selectSupplierById(complaint.getSupplierId());
            if (supplier == null) throw new RuntimeException("所选供应商不存在，请重新从供应商档案中选择");
            complaint.setSupplierName(supplier.getSupplierName());
            complaint.setSupplierContact(supplier.getContactPerson());
            complaint.setSupplierPhone(supplier.getContactPhone());
        }
    }
}
