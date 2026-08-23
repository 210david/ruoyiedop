package com.ruoyi.mms.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
