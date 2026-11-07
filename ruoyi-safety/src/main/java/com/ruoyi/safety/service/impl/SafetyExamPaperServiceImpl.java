package com.ruoyi.safety.service.impl;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.safety.domain.SafetyExamPaper;
import com.ruoyi.safety.domain.SafetyExamPaperQuestion;
import com.ruoyi.safety.domain.SafetyExamQuestion;
import com.ruoyi.safety.mapper.SafetyExamMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperQuestionMapper;
import com.ruoyi.safety.mapper.SafetyExamQuestionMapper;
import com.ruoyi.safety.service.ISafetyExamPaperService;

/**
 * 考试试卷 Service实现
 *
 * 快照策略：保存选题时即把题干/选项/答案/解析复制进 biz_safety_exam_paper_question；
 * 发布后禁止再保存题目集合，源题后续修改/停用不影响已发布试卷的判分一致性。
 *
 * @author ruoyi
 */
@Service
public class SafetyExamPaperServiceImpl implements ISafetyExamPaperService
{
    @Autowired
    private SafetyExamPaperMapper safetyExamPaperMapper;

    @Autowired
    private SafetyExamPaperQuestionMapper safetyExamPaperQuestionMapper;

    @Autowired
    private SafetyExamQuestionMapper safetyExamQuestionMapper;

    @Autowired
    private SafetyExamMapper safetyExamMapper;

    @Override
    public List<SafetyExamPaper> selectSafetyExamPaperList(SafetyExamPaper query)
    {
        return safetyExamPaperMapper.selectSafetyExamPaperList(query);
    }

    @Override
    public SafetyExamPaper selectSafetyExamPaperById(Long paperId)
    {
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(paperId);
        if (paper != null)
        {
            paper.setQuestions(safetyExamPaperQuestionMapper.selectByPaperId(paperId));
        }
        return paper;
    }

    @Override
    public int insertSafetyExamPaper(SafetyExamPaper paper)
    {
        validatePaper(paper);
        paper.setDelFlag("0");
        if (paper.getStatus() == null)
        {
            paper.setStatus("0");
        }
        return safetyExamPaperMapper.insertSafetyExamPaper(paper);
    }

    @Override
    public int updateSafetyExamPaper(SafetyExamPaper paper)
    {
        SafetyExamPaper db = requirePaper(paper.getPaperId());
        if ("1".equals(db.getStatus()))
        {
            throw new ServiceException("试卷已发布，不允许修改基础信息，请先撤回");
        }
        validatePaper(paper);
        // 发布信息不允许通过普通修改接口覆盖
        paper.setStatus(null);
        paper.setPublishBy(null);
        paper.setPublishTime(null);
        return safetyExamPaperMapper.updateSafetyExamPaper(paper);
    }

    @Override
    public int deleteSafetyExamPaperByIds(Long[] paperIds)
    {
        for (Long paperId : paperIds)
        {
            SafetyExamPaper db = requirePaper(paperId);
            if ("1".equals(db.getStatus()))
            {
                throw new ServiceException("试卷【" + db.getPaperName() + "】已发布，请先撤回再删除");
            }
            if (safetyExamMapper.countByPaperId(paperId) > 0)
            {
                throw new ServiceException("试卷【" + db.getPaperName() + "】已被考试场次引用，不能删除");
            }
        }
        int rows = 0;
        for (Long paperId : paperIds)
        {
            rows += safetyExamPaperMapper.deleteSafetyExamPaperById(paperId);
            safetyExamPaperQuestionMapper.deleteByPaperId(paperId);
        }
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int savePaperQuestions(SafetyExamPaper paper)
    {
        if (paper.getPaperId() == null)
        {
            throw new ServiceException("试卷ID不能为空");
        }
        SafetyExamPaper db = requirePaper(paper.getPaperId());
        if (!"0".equals(db.getStatus()))
        {
            throw new ServiceException("仅草稿状态的试卷可编辑题目集合");
        }
        List<SafetyExamPaperQuestion> questions = paper.getQuestions();
        if (questions == null || questions.isEmpty())
        {
            throw new ServiceException("试卷题目不能为空");
        }
        BigDecimal totalScore = BigDecimal.ZERO;
        for (int i = 0; i < questions.size(); i++)
        {
            SafetyExamPaperQuestion pq = questions.get(i);
            if (pq.getQuestionId() == null)
            {
                throw new ServiceException("第" + (i + 1) + "题缺少题目ID");
            }
            SafetyExamQuestion source = safetyExamQuestionMapper.selectSafetyExamQuestionById(pq.getQuestionId());
            if (source == null)
            {
                throw new ServiceException("第" + (i + 1) + "题在题库中不存在或已删除");
            }
            if (pq.getScore() == null || pq.getScore().compareTo(BigDecimal.ZERO) <= 0)
            {
                throw new ServiceException("第" + (i + 1) + "题分值必须大于0");
            }
            // 快照：题干/选项/答案/解析以保存时刻题库内容为准
            pq.setPaperId(paper.getPaperId());
            pq.setQuestionType(source.getQuestionType());
            pq.setContent(source.getContent());
            pq.setOptions(source.getOptions());
            pq.setAnswer(source.getAnswer());
            pq.setAnalysis(source.getAnalysis());
            pq.setSortNo(i + 1);
            totalScore = totalScore.add(pq.getScore());
        }
        safetyExamPaperQuestionMapper.deleteByPaperId(paper.getPaperId());
        for (int i = 0; i < questions.size(); i += 500)
        {
            int end = Math.min(i + 500, questions.size());
            safetyExamPaperQuestionMapper.batchInsert(questions.subList(i, end));
        }
        // 回写总分
        SafetyExamPaper update = new SafetyExamPaper();
        update.setPaperId(paper.getPaperId());
        update.setTotalScore(totalScore);
        return safetyExamPaperMapper.updateSafetyExamPaper(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishPaper(Long paperId, String operName)
    {
        SafetyExamPaper db = requirePaper(paperId);
        if (!"0".equals(db.getStatus()))
        {
            throw new ServiceException("仅草稿状态的试卷可发布");
        }
        List<SafetyExamPaperQuestion> questions = safetyExamPaperQuestionMapper.selectByPaperId(paperId);
        if (questions == null || questions.isEmpty())
        {
            throw new ServiceException("试卷暂无题目，请先完成组卷再发布");
        }
        BigDecimal totalScore = BigDecimal.ZERO;
        for (SafetyExamPaperQuestion pq : questions)
        {
            totalScore = totalScore.add(pq.getScore());
        }
        if (db.getPassScore() == null)
        {
            throw new ServiceException("请先设置及格分再发布");
        }
        if (db.getPassScore().compareTo(totalScore) > 0)
        {
            throw new ServiceException("及格分（" + db.getPassScore() + "）不能大于总分（" + totalScore + "）");
        }
        SafetyExamPaper update = new SafetyExamPaper();
        update.setPaperId(paperId);
        update.setStatus("1");
        update.setPublishBy(operName);
        update.setPublishTime(DateUtils.getNowDate());
        return safetyExamPaperMapper.updateSafetyExamPaper(update);
    }

    @Override
    public int unpublishPaper(Long paperId)
    {
        SafetyExamPaper db = requirePaper(paperId);
        if (!"1".equals(db.getStatus()))
        {
            throw new ServiceException("仅已发布的试卷可撤回");
        }
        if (safetyExamMapper.countByPaperId(paperId) > 0)
        {
            throw new ServiceException("试卷已被考试场次引用，不能撤回");
        }
        SafetyExamPaper update = new SafetyExamPaper();
        update.setPaperId(paperId);
        update.setStatus("0");
        return safetyExamPaperMapper.updateSafetyExamPaper(update);
    }

    @Override
    public List<SafetyExamQuestion> randomPick(Long categoryId, String questionType, String difficulty, Integer count)
    {
        if (count == null || count <= 0)
        {
            count = 10;
        }
        if (count > 100)
        {
            count = 100;
        }
        return safetyExamQuestionMapper.selectRandomQuestions(questionType, difficulty, categoryId, count);
    }

    private SafetyExamPaper requirePaper(Long paperId)
    {
        SafetyExamPaper db = safetyExamPaperMapper.selectSafetyExamPaperById(paperId);
        if (db == null)
        {
            throw new ServiceException("试卷不存在或已删除");
        }
        return db;
    }

    private void validatePaper(SafetyExamPaper paper)
    {
        if (StringUtils.isEmpty(paper.getPaperName()))
        {
            throw new ServiceException("试卷名称不能为空");
        }
        if (paper.getDuration() == null || paper.getDuration() <= 0)
        {
            throw new ServiceException("考试时长必须大于0分钟");
        }
        if (paper.getPassScore() != null && paper.getPassScore().compareTo(BigDecimal.ZERO) <= 0)
        {
            throw new ServiceException("及格分必须大于0");
        }
    }
}
