package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.mk.service.IMkNumberRuleService;
import com.ruoyi.mms.domain.MmsIssue;
import com.ruoyi.mms.domain.MmsIssueDetail;
import com.ruoyi.mms.mapper.MmsIssueMapper;
import com.ruoyi.mms.service.IMmsIssueService;

/**
 * 领料 Service实现
 *
 * @author ruoyi
 */
@Service
public class MmsIssueServiceImpl implements IMmsIssueService
{
    @Autowired
    private MmsIssueMapper issueMapper;

    @Autowired
    private IMkNumberRuleService mkNumberRuleService;

    @Override
    public List<MmsIssue> selectIssueList(MmsIssue issue)
    {
        return issueMapper.selectIssueList(issue);
    }

    @Override
    public MmsIssue selectIssueById(Long IssueId)
    {
        return issueMapper.selectIssueById(IssueId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertIssue(MmsIssue issue)
    {
        // 自动生成发料单号
        if (StringUtils.isEmpty(issue.getIssueNo()))
        {
            issue.setIssueNo(mkNumberRuleService.generateNumber("mms_issue"));
        }
        issue.setDelFlag("0");
        issue.setCreateBy(SecurityUtils.getUsername());
        issue.setCreateTime(DateUtils.getNowDate());
        // 新建领料默认状态为待领料
        if (StringUtils.isEmpty(issue.getStatus()))
        {
            issue.setStatus("0");
        }

        // 从明细列表中提取首条物料信息到主表（兼容列表显示）
        syncFirstDetailToMain(issue);

        int rows = issueMapper.insertIssue(issue);

        // 插入明细
        if (issue.getDetailList() != null && !issue.getDetailList().isEmpty())
        {
            int seq = 1;
            for (MmsIssueDetail d : issue.getDetailList())
            {
                d.setIssueId(issue.getIssueId());
                d.setSeq(seq++);
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                issueMapper.insertIssueDetail(d);
            }
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateIssue(MmsIssue issue)
    {
        // 从明细列表中提取首条物料信息到主表（兼容列表显示）
        syncFirstDetailToMain(issue);

        // 先删除旧明细，再插入新明细
        issueMapper.deleteIssueDetailByIssueId(issue.getIssueId());
        if (issue.getDetailList() != null && !issue.getDetailList().isEmpty())
        {
            int seq = 1;
            for (MmsIssueDetail d : issue.getDetailList())
            {
                d.setIssueId(issue.getIssueId());
                d.setSeq(seq++);
                d.setDelFlag("0");
                d.setCreateBy(SecurityUtils.getUsername());
                d.setCreateTime(DateUtils.getNowDate());
                issueMapper.insertIssueDetail(d);
            }
        }
        return issueMapper.updateIssue(issue);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteIssueByIds(Long[] IssueIds)
    {
        // 软删除明细
        for (Long id : IssueIds)
        {
            issueMapper.deleteIssueDetailByIssueId(id);
        }
        return issueMapper.deleteIssueByIds(IssueIds);
    }

    /**
     * 确认领料：状态 0(待领料) → 1(已领料)，记录领料时间，可修改领料人和明细数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int confirmIssue(MmsIssue issue)
    {
        MmsIssue existing = issueMapper.selectIssueById(issue.getIssueId());
        if (existing == null)
        {
            throw new ServiceException("领料单不存在或已删除");
        }
        if (!"0".equals(existing.getStatus()))
        {
            throw new ServiceException("领料单[" + existing.getIssueNo() + "]当前状态为" + statusName(existing.getStatus()) + "，只有待领料状态可确认");
        }
        // 校验领料人必填
        if (StringUtils.isEmpty(issue.getIssueBy()))
        {
            throw new ServiceException("领料人不能为空");
        }
        // 校验明细
        if (issue.getDetailList() == null || issue.getDetailList().isEmpty())
        {
            throw new ServiceException("领料单[" + existing.getIssueNo() + "]没有领料明细，无法确认");
        }
        for (int i = 0; i < issue.getDetailList().size(); i++)
        {
            MmsIssueDetail d = issue.getDetailList().get(i);
            if (d.getIssueQty() == null || d.getIssueQty().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (i + 1) + "行领料数量必须大于0");
            }
        }

        // 同步明细数量到主表（兼容列表显示）
        syncFirstDetailToMain(issue);

        // 先删除旧明细，再插入新明细（保存修改后的数量）
        issueMapper.deleteIssueDetailByIssueId(issue.getIssueId());
        int seq = 1;
        for (MmsIssueDetail d : issue.getDetailList())
        {
            d.setIssueId(issue.getIssueId());
            d.setSeq(seq++);
            d.setDelFlag("0");
            d.setCreateBy(SecurityUtils.getUsername());
            d.setCreateTime(DateUtils.getNowDate());
            issueMapper.insertIssueDetail(d);
        }

        // 保留原有字段，更新状态、领料人、领料时间
        issue.setIssueNo(existing.getIssueNo());
        issue.setWorkOrderId(existing.getWorkOrderId());
        issue.setWorkOrderNo(existing.getWorkOrderNo());
        issue.setStatus("1");
        // 优先使用前端传入的领料时间，未传则用当前时间
        issue.setIssueTime(issue.getIssueTime() != null ? issue.getIssueTime() : new Date());
        issue.setUpdateBy(SecurityUtils.getUsername());
        issue.setUpdateTime(DateUtils.getNowDate());
        return issueMapper.updateIssue(issue);
    }

    /**
     * 状态码转中文名
     */
    private String statusName(String status)
    {
        switch (status)
        {
            case "0": return "待领料";
            case "1": return "已领料";
            default: return "未知(" + status + ")";
        }
    }

    /**
     * 将明细列表中第一条的物料信息同步到主表，兼容列表页显示
     */
    private void syncFirstDetailToMain(MmsIssue issue)
    {
        if (issue.getDetailList() != null && !issue.getDetailList().isEmpty())
        {
            MmsIssueDetail first = issue.getDetailList().get(0);
            issue.setMaterialId(first.getMaterialId());
            issue.setMaterialCode(first.getMaterialCode());
            issue.setMaterialName(first.getMaterialName());
            issue.setSpecModel(first.getSpecModel());
            issue.setUnit(first.getUnit());
            issue.setIssueQty(first.getIssueQty());
            issue.setBatchNo(first.getBatchNo());

            // 计算总数量
            BigDecimal totalQty = BigDecimal.ZERO;
            for (MmsIssueDetail d : issue.getDetailList())
            {
                if (d.getIssueQty() != null)
                {
                    totalQty = totalQty.add(d.getIssueQty());
                }
            }
            // 如果只有一条明细，主表数量就等于该条数量；多条则用总量
            if (issue.getDetailList().size() == 1)
            {
                issue.setIssueQty(first.getIssueQty());
            }
            else
            {
                issue.setIssueQty(totalQty);
            }
        }
    }
}
