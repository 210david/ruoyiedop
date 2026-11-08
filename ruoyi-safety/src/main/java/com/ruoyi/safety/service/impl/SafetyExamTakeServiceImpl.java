package com.ruoyi.safety.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.alibaba.fastjson2.JSON;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.safety.domain.SafetyExam;
import com.ruoyi.safety.domain.SafetyExamAnswer;
import com.ruoyi.safety.domain.SafetyExamPaper;
import com.ruoyi.safety.domain.SafetyExamPaperQuestion;
import com.ruoyi.safety.domain.SafetyExamRecord;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;
import com.ruoyi.safety.domain.SafetyTrainingCert;
import com.ruoyi.safety.domain.SafetyTrainingRecord;
import com.ruoyi.safety.mapper.SafetyExamAnswerMapper;
import com.ruoyi.safety.mapper.SafetyExamMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperQuestionMapper;
import com.ruoyi.safety.mapper.SafetyExamRecordMapper;
import com.ruoyi.safety.mapper.SafetyTrainingAttendeeMapper;
import com.ruoyi.safety.mapper.SafetyTrainingRecordMapper;
import com.ruoyi.safety.service.ISafetyExamTakeService;
import com.ruoyi.safety.service.ISafetyTrainingCertService;

/**
 * 考生端考试 Service实现
 *
 * 判分规则（与 PRD §6.1 FR-5 一致）：
 * - 单选/判断：答案完全一致得满分，否则 0 分；
 * - 多选：全对得满分；漏选（答案为所选的真子集）得该题分值 × 50%（保留 1 位小数）；错选得 0 分。
 *
 * 乱序说明：题目/选项乱序仅在前端展示层打乱（字母标签与内容绑定一起乱序），
 * 答案按字母比对不受影响，后端无需维护 per-user 乱序映射。
 *
 * @author ruoyi
 */
@Service
public class SafetyExamTakeServiceImpl implements ISafetyExamTakeService
{
    private static final Logger log = LoggerFactory.getLogger(SafetyExamTakeServiceImpl.class);

    /** 交卷窗口宽限秒数（容忍网络延迟） */
    private static final long SUBMIT_GRACE_SECONDS = 30L;

    @Autowired
    private SafetyExamMapper safetyExamMapper;

    @Autowired
    private ISafetyTrainingCertService safetyTrainingCertService;

    @Autowired
    private SafetyTrainingRecordMapper safetyTrainingRecordMapper;

    @Autowired
    private SafetyExamPaperMapper safetyExamPaperMapper;

    @Autowired
    private SafetyExamPaperQuestionMapper safetyExamPaperQuestionMapper;

    @Autowired
    private SafetyExamRecordMapper safetyExamRecordMapper;

    @Autowired
    private SafetyExamAnswerMapper safetyExamAnswerMapper;

    @Autowired
    private SafetyTrainingAttendeeMapper safetyTrainingAttendeeMapper;

    @Override
    public List<Map<String, Object>> selectMyExamList(Long userId)
    {
        safetyExamMapper.updateExamStatusByTime();
        return safetyExamRecordMapper.selectMyExamList(userId);
    }

    @Override
    public Map<String, Object> startExam(Long recordId, Long userId)
    {
        SafetyExamRecord record = requireOwnedRecord(recordId, userId);
        SafetyExam exam = requireExam(record.getExamId());
        // 场次须为考试中（惰性流转已由 list 调用；此处再次兜底）
        if ("1".equals(exam.getExamStatus()) && exam.getStartTime() != null && !exam.getStartTime().after(new Date()))
        {
            SafetyExam u = new SafetyExam();
            u.setExamId(exam.getExamId());
            u.setExamStatus("2");
            safetyExamMapper.updateSafetyExam(u);
            exam.setExamStatus("2");
        }
        if (!"2".equals(exam.getExamStatus()))
        {
            throw new ServiceException("当前不在考试时间窗口内");
        }
        if ("2".equals(record.getExamResult()))
        {
            throw new ServiceException("本场考试已交卷，无需重复进入");
        }
        if ("3".equals(record.getExamResult()))
        {
            throw new ServiceException("本场考试已标记缺考，无法进入");
        }
        // 首次进入：开始计时
        Date now = DateUtils.getNowDate();
        if ("0".equals(record.getExamResult()) || record.getStartTime() == null)
        {
            SafetyExamRecord u = new SafetyExamRecord();
            u.setRecordId(recordId);
            u.setExamResult("1");
            u.setStartTime(now);
            safetyExamRecordMapper.updateSafetyExamRecord(u);
            record.setStartTime(now);
        }
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(record.getPaperId());
        List<SafetyExamPaperQuestion> questions = safetyExamPaperQuestionMapper.selectByPaperId(record.getPaperId());
        List<SafetyExamAnswer> drafts = safetyExamAnswerMapper.selectByRecordId(recordId);
        // 组装脱敏题目（剥离 answer/analysis，防抓包作弊）
        List<Map<String, Object>> questionList = new ArrayList<>();
        for (SafetyExamPaperQuestion pq : questions)
        {
            Map<String, Object> q = new HashMap<>();
            q.put("paperQuestionId", pq.getId());
            q.put("questionType", pq.getQuestionType());
            q.put("content", pq.getContent());
            try
            {
                q.put("options", JSON.parseArray(pq.getOptions()));
            }
            catch (Exception e)
            {
                q.put("options", new ArrayList<>());
            }
            questionList.add(q);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", recordId);
        result.put("examName", exam.getExamName());
        result.put("paperName", paper.getPaperName());
        result.put("passScore", paper.getPassScore());
        result.put("switchLimit", paper.getSwitchLimit() == null ? 0 : paper.getSwitchLimit());
        result.put("shuffleQuestion", paper.getShuffleQuestion());
        result.put("shuffleOption", paper.getShuffleOption());
        result.put("questions", questionList);
        // 剩余秒数 = min(窗口结束, 开始时间 + 时长) - now，两者取先到
        long windowRemain = (exam.getEndTime().getTime() - now.getTime()) / 1000L;
        long durationRemain = Long.MAX_VALUE;
        if (record.getStartTime() != null && paper.getDuration() != null)
        {
            durationRemain = (record.getStartTime().getTime() + paper.getDuration() * 60_000L - now.getTime()) / 1000L;
        }
        result.put("remainSeconds", Math.max(0L, Math.min(windowRemain, durationRemain)));
        // 已有草稿回显（断网续答）
        Map<Long, String> draftMap = new HashMap<>();
        for (SafetyExamAnswer a : drafts)
        {
            draftMap.put(a.getPaperQuestionId(), a.getUserAnswer());
        }
        result.put("drafts", draftMap);
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int saveDraft(Long recordId, List<Map<String, Object>> answers, Long userId)
    {
        SafetyExamRecord record = requireOwnedRecord(recordId, userId);
        if (!"1".equals(record.getExamResult()))
        {
            throw new ServiceException("仅进行中的考试可保存草稿");
        }
        if (answers == null || answers.isEmpty())
        {
            return 0;
        }
        List<SafetyExamAnswer> list = buildAnswers(recordId, answers);
        return safetyExamAnswerMapper.batchUpsert(list);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> submitExam(Long recordId, List<Map<String, Object>> answers, Integer switchCount, String submitType, Long userId)
    {
        SafetyExamRecord record = requireOwnedRecord(recordId, userId);
        SafetyExam exam = requireExam(record.getExamId());
        if ("2".equals(record.getExamResult()))
        {
            throw new ServiceException("本场考试已交卷，请勿重复提交");
        }
        // 窗口校验（带宽限）：超窗仍收卷，交卷方式记为超时自动提交
        Date now = DateUtils.getNowDate();
        // 开考前不允许交卷（防止绕过 start 直接提交空白卷脏写成绩）
        if (exam.getStartTime() != null && now.before(exam.getStartTime()))
        {
            throw new ServiceException("考试尚未开始，不能交卷");
        }
        long nowSec = now.getTime() / 1000L;
        long endSec = exam.getEndTime() != null ? exam.getEndTime().getTime() / 1000L + SUBMIT_GRACE_SECONDS : Long.MAX_VALUE;
        if (nowSec > endSec && (submitType == null || "1".equals(submitType)))
        {
            submitType = "2";
        }
        // 落答案（未答题保持无记录，判分按空处理）
        if (answers != null && !answers.isEmpty())
        {
            safetyExamAnswerMapper.batchUpsert(buildAnswers(recordId, answers));
        }
        // 取快照判分
        List<SafetyExamPaperQuestion> questions = safetyExamPaperQuestionMapper.selectByPaperId(record.getPaperId());
        List<SafetyExamAnswer> saved = safetyExamAnswerMapper.selectByRecordId(recordId);
        Map<Long, SafetyExamAnswer> answerMap = new HashMap<>();
        for (SafetyExamAnswer a : saved)
        {
            answerMap.put(a.getPaperQuestionId(), a);
        }
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(record.getPaperId());
        BigDecimal totalScore = BigDecimal.ZERO;
        for (SafetyExamPaperQuestion pq : questions)
        {
            SafetyExamAnswer a = answerMap.get(pq.getId());
            String userAnswer = (a == null || a.getUserAnswer() == null) ? "" : a.getUserAnswer().trim();
            BigDecimal score = judgeQuestion(pq.getQuestionType(), pq.getAnswer(), userAnswer, pq.getScore());
            totalScore = totalScore.add(score);
            // 回填判分结果
            SafetyExamAnswer u = new SafetyExamAnswer();
            u.setRecordId(recordId);
            u.setPaperQuestionId(pq.getId());
            u.setUserAnswer(userAnswer);
            u.setIsCorrect(score.compareTo(BigDecimal.ZERO) > 0 ? "1" : "0");
            u.setScore(score);
            safetyExamAnswerMapper.updateJudgeResult(u);
        }
        boolean pass = paper.getPassScore() != null && totalScore.compareTo(paper.getPassScore()) >= 0;
        // 回写考试记录
        long usedSeconds = 0L;
        if (record.getStartTime() != null)
        {
            usedSeconds = Math.max(0L, (now.getTime() - record.getStartTime().getTime()) / 1000L);
        }
        SafetyExamRecord ru = new SafetyExamRecord();
        ru.setRecordId(recordId);
        ru.setSubmitTime(now);
        ru.setUsedSeconds((int) usedSeconds);
        ru.setSubmitType(submitType == null ? "1" : submitType);
        ru.setTotalScore(totalScore);
        ru.setIsPass(pass ? "1" : "0");
        ru.setJudgeStatus("1");
        ru.setExamResult("2");
        if (switchCount != null)
        {
            ru.setSwitchCount(switchCount);
        }
        safetyExamRecordMapper.updateSafetyExamRecord(ru);
        // 回写参训人成绩（与培训模块闭环）；补考成绩覆盖策略（FR-8）：1=取最高 2=取最新
        String strategy = exam.getScoreStrategy() == null ? "2" : exam.getScoreStrategy();
        if (record.getAttendeeId() != null)
        {
            SafetyTrainingAttendee cur = safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeById(record.getAttendeeId());
            BigDecimal finalScore = totalScore;
            String finalPass = pass ? "1" : "0";
            if (cur != null && "1".equals(strategy) && cur.getExamScore() != null
                    && cur.getExamScore().compareTo(totalScore) > 0)
            {
                finalScore = cur.getExamScore();
                finalPass = "1".equals(cur.getIsPass()) ? "1" : finalPass;
            }
            SafetyTrainingAttendee au = new SafetyTrainingAttendee();
            au.setAttendeeId(record.getAttendeeId());
            au.setExamScore(finalScore);
            au.setIsPass(finalPass);
            au.setExamTime(now);
            safetyTrainingAttendeeMapper.updateSafetyTrainingAttendee(au);
            // FR-11：考试合格且培训学时达标 → 自动生成培训证书（幂等，同人不重复发）
            if ("1".equals(finalPass))
            {
                tryIssueCert(record, cur, exam);
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("totalScore", totalScore);
        result.put("isPass", pass);
        result.put("passScore", paper.getPassScore());
        result.put("questionCount", questions.size());
        return result;
    }

    @Override
    public Map<String, Object> getResult(Long recordId, Long userId)
    {
        SafetyExamRecord record = requireOwnedRecord(recordId, userId);
        if (!"2".equals(record.getExamResult()))
        {
            throw new ServiceException("考试尚未交卷判分");
        }
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(record.getPaperId());
        boolean showAnalysis = "Y".equals(paper.getShowAnalysis());
        List<SafetyExamPaperQuestion> questions = safetyExamPaperQuestionMapper.selectByPaperId(record.getPaperId());
        List<SafetyExamAnswer> answers = safetyExamAnswerMapper.selectByRecordId(recordId);
        Map<Long, SafetyExamAnswer> answerMap = new HashMap<>();
        for (SafetyExamAnswer a : answers)
        {
            answerMap.put(a.getPaperQuestionId(), a);
        }
        List<Map<String, Object>> detail = new ArrayList<>();
        for (SafetyExamPaperQuestion pq : questions)
        {
            Map<String, Object> q = new HashMap<>();
            q.put("paperQuestionId", pq.getId());
            q.put("questionType", pq.getQuestionType());
            q.put("content", pq.getContent());
            try
            {
                q.put("options", JSON.parseArray(pq.getOptions()));
            }
            catch (Exception e)
            {
                q.put("options", new ArrayList<>());
            }
            SafetyExamAnswer a = answerMap.get(pq.getId());
            q.put("userAnswer", a == null ? "" : a.getUserAnswer());
            q.put("isCorrect", a != null && "1".equals(a.getIsCorrect()));
            q.put("score", a == null ? BigDecimal.ZERO : a.getScore());
            // 答案/解析仅按试卷配置在交卷后可见
            if (showAnalysis)
            {
                q.put("answer", pq.getAnswer());
                q.put("analysis", pq.getAnalysis());
            }
            detail.add(q);
        }
        Map<String, Object> result = new HashMap<>();
        result.put("recordId", recordId);
        SafetyExam exam = safetyExamMapper.selectSafetyExamById(record.getExamId());
        result.put("examName", exam != null ? exam.getExamName() : "");
        result.put("paperName", paper.getPaperName());
        result.put("totalScore", record.getTotalScore());
        result.put("isPass", record.getIsPass());
        result.put("passScore", paper.getPassScore());
        result.put("submitTime", record.getSubmitTime());
        result.put("usedSeconds", record.getUsedSeconds());
        result.put("showAnalysis", showAnalysis);
        result.put("questions", detail);
        return result;
    }

    /**
     * 单题判分
     */
    private BigDecimal judgeQuestion(String questionType, String standardAnswer, String userAnswer, BigDecimal fullScore)
    {
        if (userAnswer == null || userAnswer.isEmpty())
        {
            return BigDecimal.ZERO;
        }
        // 单选/判断：完全一致得满分
        if ("1".equals(questionType) || "3".equals(questionType))
        {
            return userAnswer.equals(standardAnswer) ? fullScore : BigDecimal.ZERO;
        }
        // 多选
        if ("2".equals(questionType))
        {
            if (userAnswer.equals(standardAnswer))
            {
                return fullScore;
            }
            // 无错选且非空（答案为所选真子集）→ 漏选得 50%
            boolean hasWrong = false;
            for (char c : userAnswer.toCharArray())
            {
                if (standardAnswer.indexOf(c) < 0)
                {
                    hasWrong = true;
                    break;
                }
            }
            if (!hasWrong)
            {
                return fullScore.multiply(new BigDecimal("0.5")).setScale(1, RoundingMode.UP);
            }
            return BigDecimal.ZERO;
        }
        return BigDecimal.ZERO;
    }

    private List<SafetyExamAnswer> buildAnswers(Long recordId, List<Map<String, Object>> answers)
    {
        List<SafetyExamAnswer> list = new ArrayList<>();
        for (Map<String, Object> item : answers)
        {
            Object pqId = item.get("paperQuestionId");
            Object ua = item.get("userAnswer");
            if (pqId == null)
            {
                continue;
            }
            SafetyExamAnswer a = new SafetyExamAnswer();
            a.setRecordId(recordId);
            a.setPaperQuestionId(Long.valueOf(String.valueOf(pqId)));
            a.setUserAnswer(ua == null ? "" : String.valueOf(ua));
            list.add(a);
        }
        return list;
    }

    /** 校验考试记录存在且属于当前用户（防代考） */
    private SafetyExamRecord requireOwnedRecord(Long recordId, Long userId)
    {
        SafetyExamRecord record = safetyExamRecordMapper.selectSafetyExamRecordById(recordId);
        if (record == null)
        {
            throw new ServiceException("考试记录不存在");
        }
        if (!userId.equals(record.getUserId()))
        {
            throw new ServiceException("无权操作该考试记录");
        }
        return record;
    }

    private SafetyExam requireExam(Long examId)
    {
        SafetyExam exam = safetyExamMapper.selectSafetyExamById(examId);
        if (exam == null)
        {
            throw new ServiceException("考试场次不存在");
        }
        return exam;
    }

    /**
     * FR-11：成绩与培训证书联动。
     * 联动条件：考试合格，且关联培训记录的学时已登记（>0，即"学时达标"口径，见 PRD 开放问题 Q2）。
     * 幂等：同一用户 + 同一培训记录只发一张证书。
     */
    private void tryIssueCert(SafetyExamRecord record, SafetyTrainingAttendee attendee, SafetyExam exam)
    {
        try
        {
            if (attendee == null || attendee.getRecordId() == null)
            {
                return;
            }
            // 幂等校验：该用户该培训记录已发证则跳过
            SafetyTrainingCert q = new SafetyTrainingCert();
            q.setUserId(record.getUserId());
            List<SafetyTrainingCert> existed = safetyTrainingCertService.selectSafetyTrainingCertList(q);
            if (existed != null)
            {
                for (SafetyTrainingCert c : existed)
                {
                    if (attendee.getRecordId().equals(c.getRecordId()))
                    {
                        return;
                    }
                }
            }
            // 学时达标口径：培训记录学时已登记且大于 0
            SafetyTrainingRecord trainingRecord = safetyTrainingRecordMapper.selectSafetyTrainingRecordById(attendee.getRecordId());
            if (trainingRecord == null || trainingRecord.getHours() == null
                    || trainingRecord.getHours().compareTo(java.math.BigDecimal.ZERO) <= 0)
            {
                return;
            }
            SafetyTrainingCert cert = new SafetyTrainingCert();
            cert.setCertNo("AQ" + DateUtils.dateTimeNow("yyyyMMddHHmmssSSS"));
            String courseName = trainingRecord.getCourseName();
            cert.setCertName((courseName == null || courseName.isEmpty() ? exam.getExamName() : courseName) + "培训合格证书");
            cert.setUserId(record.getUserId());
            cert.setUserName(record.getUserName());
            cert.setDeptId(record.getDeptId());
            cert.setRecordId(attendee.getRecordId());
            cert.setCourseId(trainingRecord.getCourseId());
            cert.setIssueDate(DateUtils.parseDate(DateUtils.getDate()));
            cert.setStatus("0");
            cert.setDelFlag("0");
            cert.setCreateBy("exam-auto");
            safetyTrainingCertService.insertSafetyTrainingCert(cert);
        }
        catch (Exception e)
        {
            // 证书联动为增强能力，失败不阻断判分主流程（记日志由管理员补发）
            log.warn("考试合格自动发证失败 recordId={}", record.getRecordId(), e);
        }
    }
}
