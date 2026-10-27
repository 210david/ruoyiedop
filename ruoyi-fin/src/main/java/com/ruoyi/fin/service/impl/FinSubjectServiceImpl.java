package com.ruoyi.fin.service.impl;

import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.fin.domain.FinSubject;
import com.ruoyi.fin.exception.FinException;
import com.ruoyi.fin.mapper.FinSubjectMapper;
import com.ruoyi.fin.service.IFinSubjectService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 会计科目 Service 实现
 *
 * <p>【科目树维护的三条硬规则】</p>
 * <ol>
 *   <li>层级最多 4 级（与企业会计准则科目表一致）</li>
 *   <li>有子科目的节点不能删除</li>
 *   <li>已有凭证分录的科目不能删除，只能停用——保证历史报表可追溯</li>
 * </ol>
 *
 * @author ruoyi
 */
@Service
public class FinSubjectServiceImpl implements IFinSubjectService
{
    private static final Logger log = LoggerFactory.getLogger(FinSubjectServiceImpl.class);

    /** 科目最大层级 */
    private static final int MAX_LEVEL = 4;

    @Autowired
    private FinSubjectMapper subjectMapper;

    @Override
    public List<FinSubject> selectSubjectList(FinSubject subject)
    {
        return subjectMapper.selectSubjectList(subject);
    }

    @Override
    public List<FinSubject> selectAllEnabled()
    {
        return subjectMapper.selectAllEnabled();
    }

    @Override
    public FinSubject selectSubjectById(Long subjectId)
    {
        return subjectMapper.selectSubjectById(subjectId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSubject(FinSubject subject)
    {
        applyAndValidate(subject);

        if (subjectMapper.checkCodeUnique(subject.getSubjectCode(), null) > 0)
        {
            throw FinException.subjectCodeDuplicated(subject.getSubjectCode());
        }

        subject.setCreateBy(SecurityUtils.getUsername());
        subject.setStatus(subject.getStatus() == null ? "0" : subject.getStatus());
        subject.setIsLeaf(subject.getIsLeaf() == null ? "1" : subject.getIsLeaf());
        subject.setSortOrder(subject.getSortOrder() == null ? 0 : subject.getSortOrder());

        return subjectMapper.insertSubject(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSubject(FinSubject subject)
    {
        FinSubject exist = subjectMapper.selectSubjectById(subject.getSubjectId());
        if (exist == null)
        {
            throw new IllegalArgumentException("科目不存在或已删除");
        }

        // 防止把自己设为自己的上级，会造成树结构死循环
        if (subject.getParentId() != null && subject.getParentId().equals(subject.getSubjectId()))
        {
            throw FinException.subjectParentSelf();
        }

        applyAndValidate(subject);

        // 编码变更时需重新校验唯一性
        if (subject.getSubjectCode() != null
                && !subject.getSubjectCode().equals(exist.getSubjectCode()))
        {
            if (subjectMapper.checkCodeUnique(subject.getSubjectCode(), subject.getSubjectId()) > 0)
            {
                throw FinException.subjectCodeDuplicated(subject.getSubjectCode());
            }
        }

        subject.setUpdateBy(SecurityUtils.getUsername());
        return subjectMapper.updateSubject(subject);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int deleteSubjectByIds(Long[] subjectIds)
    {
        for (Long subjectId : subjectIds)
        {
            FinSubject subject = subjectMapper.selectSubjectById(subjectId);
            if (subject == null)
            {
                continue;
            }
            // 规则2：有子科目不能删
            if (subjectMapper.countChildren(subjectId) > 0)
            {
                throw FinException.subjectHasChildren(subject.getSubjectName());
            }
            // 规则3：已有凭证分录不能删，只能停用
            if (subjectMapper.countVoucherEntry(subject.getSubjectCode()) > 0)
            {
                throw FinException.subjectHasVoucher(subject.getSubjectName());
            }
        }

        if (subjectIds == null || subjectIds.length == 0)
        {
            return 0;
        }
        return subjectMapper.deleteSubjectByIds(subjectIds);
    }

    @Override
    public boolean checkCodeUnique(String subjectCode, Long subjectId)
    {
        return subjectMapper.checkCodeUnique(subjectCode, subjectId) == 0;
    }

    /**
     * 公共校验：编码格式、层级计算、父科目有效性。
     *
     * @param subject 科目
     */
    private void applyAndValidate(FinSubject subject)
    {
        // 科目编码必须为数字（会计科目编码惯例，如 1001 / 2202 / 6001）
        if (subject.getSubjectCode() == null || !subject.getSubjectCode().matches("\\d{1,20}"))
        {
            throw FinException.subjectCodeNotNumeric(subject.getSubjectCode());
        }

        // 层级：无上级为 1 级，有上级则父级 +1，并校验不超过 4 级
        if (subject.getParentId() == null || subject.getParentId() == 0L)
        {
            subject.setParentId(null);
            subject.setParentCode(null);
            subject.setLevel(1);
        }
        else
        {
            FinSubject parent = subjectMapper.selectSubjectById(subject.getParentId());
            if (parent == null || "1".equals(parent.getStatus()))
            {
                throw FinException.subjectParentInvalid(subject.getParentCode());
            }
            int parentLevel = parent.getLevel() == null ? 1 : parent.getLevel();
            if (parentLevel >= MAX_LEVEL)
            {
                throw FinException.subjectLevelExceeded(parent.getSubjectCode());
            }
            subject.setLevel(parentLevel + 1);
            subject.setParentCode(parent.getSubjectCode());
        }
    }

    /**
     * Excel 批量导入科目（D-01 修复）。
     *
     * <p>全部成功或全部失败（异常触发事务回滚，不留半批数据）。</p>
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importSubjects(java.util.List<FinSubject> subjects, String operName)
    {
        if (subjects == null || subjects.isEmpty())
        {
            throw new IllegalArgumentException("导入数据为空");
        }
        java.util.List<String> errors = new java.util.ArrayList<>();
        int success = 0;

        for (int i = 0; i < subjects.size(); i++)
        {
            FinSubject s = subjects.get(i);
            int row = i + 2;   // Excel 行号（第1行是表头）
            if (s.getSubjectCode() == null || s.getSubjectCode().trim().isEmpty())
            {
                errors.add("第" + row + "行：科目编码为空");
                continue;
            }
            s.setSubjectCode(s.getSubjectCode().trim());
            if (s.getSubjectName() == null || s.getSubjectName().trim().isEmpty())
            {
                errors.add("第" + row + "行：科目名称为空");
                continue;
            }
            if (subjectMapper.checkCodeUnique(s.getSubjectCode(), null) > 0)
            {
                errors.add("第" + row + "行：科目编码 " + s.getSubjectCode() + " 已存在");
                continue;
            }
            if (s.getCategory() == null
                    || !"ASSET,LIABILITY,EQUITY,COST,INCOME".contains(s.getCategory()))
            {
                errors.add("第" + row + "行：科目类别非法（" + s.getCategory()
                        + "，应为 ASSET/LIABILITY/EQUITY/COST/INCOME）");
                continue;
            }
            if (s.getDirection() == null
                    || !"DEBIT,CREDIT".contains(s.getDirection()))
            {
                errors.add("第" + row + "行：余额方向非法（" + s.getDirection() + "，应为 DEBIT/CREDIT）");
                continue;
            }

            // 父科目：库里有或本批先导入
            if (s.getParentCode() != null && !s.getParentCode().trim().isEmpty())
            {
                s.setParentCode(s.getParentCode().trim());
                FinSubject parent = subjectMapper.selectSubjectByCode(s.getParentCode());
                if (parent == null)
                {
                    errors.add("第" + row + "行：上级科目 " + s.getParentCode() + " 不存在（先导入父科目）");
                    continue;
                }
                s.setParentId(parent.getSubjectId());
                s.setLevel(parent.getLevel() == null ? 2 : parent.getLevel() + 1);
                // 父科目转为非明细
                if ("1".equals(parent.getIsLeaf()))
                {
                    parent.setIsLeaf("0");
                    parent.setUpdateBy(operName);
                    subjectMapper.updateSubject(parent);
                }
            }
            else
            {
                s.setLevel(1);
            }
            s.setIsLeaf("1");
            s.setStatus("0");
            s.setCreateBy(operName);
            subjectMapper.insertSubject(s);
            success++;
        }

        if (!errors.isEmpty())
        {
            // 任一行失败则整体回滚，避免半批数据
            throw new IllegalArgumentException("导入失败，共 " + errors.size() + " 处错误：" + String.join("；", errors));
        }
        return success;
    }
}
