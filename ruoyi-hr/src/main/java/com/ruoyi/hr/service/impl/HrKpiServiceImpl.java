package com.ruoyi.hr.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.hr.domain.HrKpiItem;
import com.ruoyi.hr.domain.HrKpiRecord;
import com.ruoyi.hr.domain.HrKpiScheme;
import com.ruoyi.hr.domain.HrSalesProfile;
import com.ruoyi.hr.mapper.HrKpiMapper;
import com.ruoyi.hr.mapper.HrSalesPerfMapper;
import com.ruoyi.hr.service.IHrKpiService;

/**
 * KPI考核 Service业务层处理（需求三）
 *
 * 线上流程：发起—自评—评分—确认—申诉（简化审批，状态机控制）
 */
@Service
public class HrKpiServiceImpl implements IHrKpiService {
    @Autowired
    private HrKpiMapper mapper;

    @Autowired
    private HrSalesPerfMapper profileMapper;

    // ---- 方案 ----
    @Override
    public List<HrKpiScheme> selectSchemeList(HrKpiScheme scheme) { return mapper.selectSchemeList(scheme); }

    @Override
    public HrKpiScheme selectSchemeById(Long schemeId) {
        HrKpiScheme scheme = mapper.selectSchemeById(schemeId);
        if (scheme != null) scheme.setItems(mapper.selectItemsByScheme(schemeId));
        return scheme;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertScheme(HrKpiScheme scheme) {
        validateItems(scheme);
        scheme.setSchemeStatus("0");
        scheme.setDelFlag("0");
        scheme.setCreateBy(SecurityUtils.getUsername());
        int rows = mapper.insertScheme(scheme);
        saveItems(scheme);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateScheme(HrKpiScheme scheme) {
        HrKpiScheme existing = mapper.selectSchemeById(scheme.getSchemeId());
        if (existing == null) throw new ServiceException("考核方案不存在");
        validateItems(scheme);
        scheme.setUpdateBy(SecurityUtils.getUsername());
        int rows = mapper.updateScheme(scheme);
        if (scheme.getItems() != null) {
            mapper.deleteItemsByScheme(scheme.getSchemeId());
            saveItems(scheme);
        }
        return rows;
    }

    /** 指标校验：非空且权重合计=100 */
    private void validateItems(HrKpiScheme scheme) {
        if (scheme.getItems() == null || scheme.getItems().isEmpty()) throw new ServiceException("请至少配置一个考核指标");
        BigDecimal total = BigDecimal.ZERO;
        for (HrKpiItem item : scheme.getItems()) {
            if (StringUtils.isEmpty(item.getIndicatorName())) throw new ServiceException("指标名称不能为空");
            if (item.getWeight() == null) throw new ServiceException("指标权重不能为空");
            total = total.add(item.getWeight());
        }
        if (total.compareTo(BigDecimal.valueOf(100)) != 0) throw new ServiceException("指标权重合计必须等于100");
    }

    private void saveItems(HrKpiScheme scheme) {
        for (HrKpiItem item : scheme.getItems()) {
            item.setSchemeId(scheme.getSchemeId());
            mapper.insertItem(item);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSchemeByIds(Long[] schemeIds) { return mapper.deleteSchemeByIds(schemeIds); }

    // ---- 考核记录 ----
    @Override
    public List<HrKpiRecord> selectRecordList(HrKpiRecord record) { return mapper.selectRecordList(record); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initiateRecords(Long schemeId, String period, List<Long> employeeIds) {
        HrKpiScheme scheme = mapper.selectSchemeById(schemeId);
        if (scheme == null) throw new ServiceException("考核方案不存在");
        if (!"1".equals(scheme.getSchemeStatus())) throw new ServiceException("仅「启用」状态的考核方案可发起考核");
        if (StringUtils.isEmpty(period) || !period.matches("\\d{4}-\\d{2}")) throw new ServiceException("考核周期格式应为 yyyy-MM");
        if (employeeIds == null || employeeIds.isEmpty()) throw new ServiceException("请选择考核对象");
        String oper = SecurityUtils.getUsername();
        int rows = 0;
        for (Long employeeId : employeeIds) {
            HrKpiRecord record = new HrKpiRecord();
            record.setSchemeId(schemeId);
            record.setPeriod(period);
            record.setEmployeeId(employeeId);
            HrSalesProfile profile = profileMapper.selectProfileByEmployeeId(employeeId);
            record.setEmployeeName(profile != null ? profile.getEmployeeName() : null);
            record.setKpiStatus("0");
            record.setCreateBy(oper);
            rows += mapper.insertRecord(record);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int selfScore(HrKpiRecord record) {
        HrKpiRecord existing = mapper.selectRecordById(record.getRecordId());
        if (existing == null) throw new ServiceException("考核记录不存在");
        if (!"0".equals(existing.getKpiStatus())) throw new ServiceException("当前状态不允许自评");
        if (record.getSelfScore() == null) throw new ServiceException("请填写自评分");
        HrKpiRecord update = new HrKpiRecord();
        update.setRecordId(record.getRecordId());
        update.setSelfScore(record.getSelfScore());
        update.setSelfComment(record.getSelfComment());
        update.setKpiStatus("1");
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateRecord(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int superiorScore(HrKpiRecord record) {
        HrKpiRecord existing = mapper.selectRecordById(record.getRecordId());
        if (existing == null) throw new ServiceException("考核记录不存在");
        if (!"1".equals(existing.getKpiStatus())) throw new ServiceException("当前状态不允许评分，需员工完成自评");
        if (record.getSuperiorScore() == null) throw new ServiceException("请填写上级评分");
        HrKpiRecord update = new HrKpiRecord();
        update.setRecordId(record.getRecordId());
        update.setSuperiorScore(record.getSuperiorScore());
        update.setSuperiorComment(record.getSuperiorComment());
        update.setFinalScore(record.getSuperiorScore());
        update.setKpiStatus("2");
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateRecord(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmRecord(Long recordId) {
        HrKpiRecord existing = mapper.selectRecordById(recordId);
        if (existing == null) throw new ServiceException("考核记录不存在");
        if (!"2".equals(existing.getKpiStatus())) throw new ServiceException("当前状态不允许确认");
        HrKpiRecord update = new HrKpiRecord();
        update.setRecordId(recordId);
        update.setKpiStatus("3");
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateRecord(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int appealRecord(Long recordId, String reason) {
        HrKpiRecord existing = mapper.selectRecordById(recordId);
        if (existing == null) throw new ServiceException("考核记录不存在");
        if (!"3".equals(existing.getKpiStatus())) throw new ServiceException("仅已确认的考核结果可申诉");
        if (StringUtils.isEmpty(reason)) throw new ServiceException("请填写申诉理由");
        HrKpiRecord update = new HrKpiRecord();
        update.setRecordId(recordId);
        update.setKpiStatus("4");
        update.setAppealReason(reason);
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateRecord(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int handleAppeal(Long recordId, String result, BigDecimal finalScore) {
        HrKpiRecord existing = mapper.selectRecordById(recordId);
        if (existing == null) throw new ServiceException("考核记录不存在");
        if (!"4".equals(existing.getKpiStatus())) throw new ServiceException("当前状态不允许处理申诉");
        if (StringUtils.isEmpty(result)) throw new ServiceException("请填写申诉处理结果");
        HrKpiRecord update = new HrKpiRecord();
        update.setRecordId(recordId);
        update.setKpiStatus("5");
        update.setAppealResult(result);
        if (finalScore != null) update.setFinalScore(finalScore);
        update.setUpdateBy(SecurityUtils.getUsername());
        return mapper.updateRecord(update);
    }

    @Override
    public List<HrKpiRecord> selectRecordRank(String period) { return mapper.selectRecordRank(period); }
}
