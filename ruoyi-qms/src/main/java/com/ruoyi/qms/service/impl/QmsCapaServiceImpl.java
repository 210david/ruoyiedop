package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsCapa;
import com.ruoyi.qms.domain.QmsNcr;
import com.ruoyi.qms.domain.QmsComplaint;
import com.ruoyi.qms.mapper.QmsCapaMapper;
import com.ruoyi.qms.mapper.QmsNcrMapper;
import com.ruoyi.qms.mapper.QmsComplaintMapper;
import com.ruoyi.qms.service.IQmsCapaService;
import com.ruoyi.mk.service.IMkNumberRuleService;

@Service
public class QmsCapaServiceImpl implements IQmsCapaService {
    @Autowired
    private QmsCapaMapper qmsCapaMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Autowired
    private QmsNcrMapper qmsNcrMapper;

    @Autowired
    private QmsComplaintMapper qmsComplaintMapper;

    @Override
    public List<QmsCapa> selectCapaList(QmsCapa capa) { return qmsCapaMapper.selectCapaList(capa); }

    @Override
    public QmsCapa selectCapaById(Long capaId) { return qmsCapaMapper.selectCapaById(capaId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertCapa(QmsCapa capa) {
        if (StringUtils.isEmpty(capa.getCapaNo())) {
                    // 传递动态前缀参数（CAPA状态），使动态前缀规则可生效
        Map<String, String> params = new HashMap<>();
        if (StringUtils.isNotEmpty(capa.getCapaStatus()))
        {
            params.put("capaStatus", capa.getCapaStatus());
        }
        capa.setCapaNo(mkNumberRuleService.generateNumber("qms_capa", params));
        }
        capa.setDelFlag("0");
        capa.setStatus("0");
        if (capa.getCapaStatus() == null) capa.setCapaStatus("0");
        return qmsCapaMapper.insertCapa(capa);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateCapa(QmsCapa capa) { return qmsCapaMapper.updateCapa(capa); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteCapaByIds(Long[] capaIds) { return qmsCapaMapper.deleteCapaByIds(capaIds); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitCapa(Long capaId) {
        QmsCapa capa = qmsCapaMapper.selectCapaById(capaId);
        if (capa == null) throw new ServiceException("CAPA不存在");
        if (!"0".equals(capa.getCapaStatus()) && !"4".equals(capa.getCapaStatus()))
            throw new ServiceException("当前状态不允许提交操作");
        QmsCapa update = new QmsCapa();
        update.setCapaId(capaId);
        update.setCapaStatus("1");
        return qmsCapaMapper.updateCapa(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int submitVerify(QmsCapa capa) {
        QmsCapa existing = qmsCapaMapper.selectCapaById(capa.getCapaId());
        if (existing == null) throw new ServiceException("CAPA不存在");
        if (!"1".equals(existing.getCapaStatus()))
            throw new ServiceException("当前状态不允许提交验证操作");
        QmsCapa update = new QmsCapa();
        update.setCapaId(capa.getCapaId());
        update.setCapaStatus("2");
        if (StringUtils.isNotEmpty(capa.getVerifyResult())) update.setVerifyResult(capa.getVerifyResult());
        return qmsCapaMapper.updateCapa(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int closeCapa(QmsCapa capa) {
        QmsCapa existing = qmsCapaMapper.selectCapaById(capa.getCapaId());
        if (existing == null) throw new ServiceException("CAPA不存在");
        if (!"2".equals(existing.getCapaStatus()))
            throw new ServiceException("当前状态不允许关闭操作");
        QmsCapa update = new QmsCapa();
        update.setCapaId(capa.getCapaId());
        update.setCapaStatus("3");
        update.setCloseTime(new Date());
        if (StringUtils.isNotEmpty(capa.getPreventAction())) update.setPreventAction(capa.getPreventAction());
        if (StringUtils.isNotEmpty(capa.getCloseRemark())) update.setCloseRemark(capa.getCloseRemark());
        return qmsCapaMapper.updateCapa(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int rejectCapa(Long capaId, String rejectReason) {
        QmsCapa capa = qmsCapaMapper.selectCapaById(capaId);
        if (capa == null) throw new ServiceException("CAPA不存在");
        if (!"2".equals(capa.getCapaStatus()))
            throw new ServiceException("当前状态不允许驳回操作");
        QmsCapa update = new QmsCapa();
        update.setCapaId(capaId);
        update.setCapaStatus("4");
        if (StringUtils.isNotEmpty(rejectReason)) update.setRemark(rejectReason);
        return qmsCapaMapper.updateCapa(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCapaFromNcr(Long ncrId) {
        QmsNcr ncr = qmsNcrMapper.selectNcrById(ncrId);
        if (ncr == null) throw new ServiceException("NCR不存在");
        // 检查是否已有关联CAPA
        QmsCapa query = new QmsCapa();
        query.setSourceType("ncr");
        query.setSourceNo(ncr.getNcrNo());
        List<QmsCapa> existing = qmsCapaMapper.selectCapaList(query);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0).getCapaId();
        }
        QmsCapa capa = new QmsCapa();
        capa.setSourceType("ncr");
        capa.setSourceNo(ncr.getNcrNo());
        capa.setTitle("NCR不合格品 - " + ncr.getNcrNo());
        capa.setProblemDesc(ncr.getDefectDesc());
        capa.setCapaStatus("0");
        capa.setDelFlag("0");
        capa.setStatus("0");
        Map<String, String> params = new HashMap<>();
        capa.setCapaNo(mkNumberRuleService.generateNumber("qms_capa", params));
        qmsCapaMapper.insertCapa(capa);
        return capa.getCapaId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createCapaFromComplaint(Long complaintId) {
        QmsComplaint complaint = qmsComplaintMapper.selectComplaintById(complaintId);
        if (complaint == null) throw new ServiceException("客诉不存在");
        // 检查是否已有关联CAPA
        QmsCapa query = new QmsCapa();
        query.setSourceType("complaint");
        query.setSourceNo(complaint.getComplaintNo());
        List<QmsCapa> existing = qmsCapaMapper.selectCapaList(query);
        if (existing != null && !existing.isEmpty()) {
            return existing.get(0).getCapaId();
        }
        QmsCapa capa = new QmsCapa();
        capa.setSourceType("complaint");
        capa.setSourceNo(complaint.getComplaintNo());
        capa.setTitle("客诉 - " + complaint.getComplaintNo() + " " + (complaint.getCustomerName() != null ? complaint.getCustomerName() : ""));
        capa.setProblemDesc(complaint.getDefectDesc());
        capa.setCapaStatus("0");
        capa.setDelFlag("0");
        capa.setStatus("0");
        Map<String, String> params = new HashMap<>();
        capa.setCapaNo(mkNumberRuleService.generateNumber("qms_capa", params));
        qmsCapaMapper.insertCapa(capa);
        return capa.getCapaId();
    }
}
