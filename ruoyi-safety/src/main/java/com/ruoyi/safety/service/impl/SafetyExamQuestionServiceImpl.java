package com.ruoyi.safety.service.impl;

import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyExamQuestion;
import com.ruoyi.safety.mapper.SafetyExamQuestionMapper;
import com.ruoyi.safety.service.ISafetyExamQuestionService;

/**
 * 考试题库 Service实现
 *
 * @author ruoyi
 */
@Service
public class SafetyExamQuestionServiceImpl implements ISafetyExamQuestionService
{
    @Autowired
    private SafetyExamQuestionMapper safetyExamQuestionMapper;

    @Override
    public List<SafetyExamQuestion> selectSafetyExamQuestionList(SafetyExamQuestion query)
    {
        return safetyExamQuestionMapper.selectSafetyExamQuestionList(query);
    }

    @Override
    public SafetyExamQuestion selectSafetyExamQuestionById(Long questionId)
    {
        return safetyExamQuestionMapper.selectSafetyExamQuestionById(questionId);
    }

    @Override
    public int insertSafetyExamQuestion(SafetyExamQuestion question)
    {
        validateQuestion(question);
        question.setDelFlag("0");
        if (question.getStatus() == null)
        {
            question.setStatus("0");
        }
        if (question.getDifficulty() == null)
        {
            question.setDifficulty("2");
        }
        return safetyExamQuestionMapper.insertSafetyExamQuestion(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public java.util.Map<String, Integer> importFromAi(Long categoryId, List<SafetyExamQuestion> questions, String operName)
    {
        if (categoryId == null)
        {
            throw new ServiceException("请先选择收编入库的分类");
        }
        if (questions == null || questions.isEmpty())
        {
            throw new ServiceException("AI 生成题目为空，无法收编");
        }
        int inserted = 0;
        int skipped = 0;
        for (int i = 0; i < questions.size(); i++)
        {
            SafetyExamQuestion q = questions.get(i);
            q.setCategoryId(categoryId);
            q.setStatus("0");
            q.setDelFlag("0");
            if (q.getDifficulty() == null)
            {
                q.setDifficulty("2");
            }
            try
            {
                validateQuestion(q);
            }
            catch (ServiceException e)
            {
                throw new ServiceException("第" + (i + 1) + "题不合法：" + e.getMessage());
            }
            // 题干同分类查重（精确匹配则跳过，避免题库重复堆积）
            SafetyExamQuestion dup = new SafetyExamQuestion();
            dup.setCategoryId(categoryId);
            dup.setContent(q.getContent());
            List<SafetyExamQuestion> same = safetyExamQuestionMapper.selectSafetyExamQuestionList(dup);
            boolean exists = false;
            if (same != null)
            {
                for (SafetyExamQuestion s : same)
                {
                    if (q.getContent().equals(s.getContent()))
                    {
                        exists = true;
                        break;
                    }
                }
            }
            if (exists)
            {
                skipped++;
                continue;
            }
            q.setCreateBy(operName);
            safetyExamQuestionMapper.insertSafetyExamQuestion(q);
            inserted++;
        }
        java.util.Map<String, Integer> result = new java.util.HashMap<>();
        result.put("inserted", inserted);
        result.put("skipped", skipped);
        return result;
    }

    @Override
    public int updateSafetyExamQuestion(SafetyExamQuestion question)
    {
        validateQuestion(question);
        return safetyExamQuestionMapper.updateSafetyExamQuestion(question);
    }

    @Override
    public int deleteSafetyExamQuestionByIds(Long[] questionIds)
    {
        return safetyExamQuestionMapper.deleteSafetyExamQuestionByIds(questionIds);
    }

    @Override
    public int changeQuestionStatus(Long questionId, String status)
    {
        SafetyExamQuestion question = new SafetyExamQuestion();
        question.setQuestionId(questionId);
        question.setStatus(status);
        return safetyExamQuestionMapper.updateSafetyExamQuestion(question);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String importQuestion(List<SafetyExamQuestion> questions, String operName)
    {
        if (questions == null || questions.isEmpty())
        {
            throw new ServiceException("导入数据不能为空");
        }
        List<SafetyExamQuestion> validList = new ArrayList<>();
        StringBuilder failMsg = new StringBuilder();
        int failCount = 0;
        // 行级校验（E8：任一行非法则全量回滚，合法行不导入）
        for (int i = 0; i < questions.size(); i++)
        {
            SafetyExamQuestion q = questions.get(i);
            int rowNo = i + 1;
            try
            {
                validateQuestion(q);
                q.setDelFlag("0");
                if (q.getStatus() == null)
                {
                    q.setStatus("0");
                }
                if (q.getDifficulty() == null)
                {
                    q.setDifficulty("2");
                }
                q.setCreateBy(operName);
                validList.add(q);
            }
            catch (ServiceException e)
            {
                failCount++;
                failMsg.append("<br/>第").append(rowNo).append("行：").append(e.getMessage());
            }
        }
        if (failCount > 0)
        {
            throw new ServiceException("导入失败，共 " + failCount + " 行数据不合法，已全部回滚：" + failMsg);
        }
        // 分批插入（每批500）
        for (int i = 0; i < validList.size(); i += 500)
        {
            int end = Math.min(i + 500, validList.size());
            safetyExamQuestionMapper.batchInsertSafetyExamQuestion(validList.subList(i, end));
        }
        return "导入成功，共 " + validList.size() + " 条题目";
    }

    /**
     * 题目业务校验（前后端口径一致）
     */
    private void validateQuestion(SafetyExamQuestion question)
    {
        if (StringUtils.isEmpty(question.getContent()))
        {
            throw new ServiceException("题干不能为空");
        }
        String type = question.getQuestionType();
        if (!"1".equals(type) && !"2".equals(type) && !"3".equals(type))
        {
            throw new ServiceException("题型不合法（仅支持单选/多选/判断）");
        }
        String answer = question.getAnswer();
        if (StringUtils.isEmpty(answer))
        {
            throw new ServiceException("正确答案不能为空");
        }
        if ("3".equals(type))
        {
            // 判断题：答案只能是 对/错
            if (!"对".equals(answer) && !"错".equals(answer))
            {
                throw new ServiceException("判断题答案只能为 对 或 错");
            }
            question.setOptions("[\"正确\",\"错误\"]");
            return;
        }
        // 单选/多选：校验选项与答案
        String options = question.getOptions();
        if (StringUtils.isEmpty(options))
        {
            throw new ServiceException("选项不能为空");
        }
        int optionCount;
        try
        {
            List<?> list = com.alibaba.fastjson2.JSON.parseArray(options);
            optionCount = list.size();
        }
        catch (Exception e)
        {
            throw new ServiceException("选项格式不合法，须为JSON数组");
        }
        if (optionCount < 2)
        {
            throw new ServiceException("选项数量不能少于2个");
        }
        String letters = "ABCDEFGH";
        if (answer.length() > optionCount)
        {
            throw new ServiceException("答案超出选项范围");
        }
        for (char c : answer.toCharArray())
        {
            int idx = letters.indexOf(c);
            if (idx < 0 || idx >= optionCount)
            {
                throw new ServiceException("答案【" + c + "】超出选项范围（共" + optionCount + "个选项）");
            }
        }
        if ("2".equals(type) && answer.length() < 2)
        {
            throw new ServiceException("多选题答案至少2项");
        }
    }
}
