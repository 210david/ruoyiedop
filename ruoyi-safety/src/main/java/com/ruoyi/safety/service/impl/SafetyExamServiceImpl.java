package com.ruoyi.safety.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.safety.domain.SafetyExam;
import com.ruoyi.safety.domain.SafetyExamPaper;
import com.ruoyi.safety.domain.SafetyExamPaperQuestion;
import com.ruoyi.safety.domain.SafetyExamRecord;
import com.ruoyi.safety.domain.SafetyExamUser;
import com.ruoyi.safety.domain.SafetyTrainingAttendee;
import com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive;
import com.ruoyi.safety.domain.vo.SafetyExamQuestionStat;
import com.ruoyi.safety.mapper.SafetyExamAnswerMapper;
import com.ruoyi.safety.mapper.SafetyExamMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperMapper;
import com.ruoyi.safety.mapper.SafetyExamPaperQuestionMapper;
import com.ruoyi.safety.mapper.SafetyExamRecordMapper;
import com.ruoyi.safety.mapper.SafetyExamUserMapper;
import com.ruoyi.safety.mapper.SafetyTrainingAttendeeMapper;
import com.ruoyi.safety.service.ISafetyExamPaperService;
import com.ruoyi.safety.service.ISafetyExamService;

/**
 * 考试场次 Service实现
 *
 * 场次状态机：0草稿 → 1已发布 → 2考试中 → 3已结束；1可撤回为 4已撤回（未开始时）。
 * 2/3 的流转由 updateExamStatusByTime 惰性兜底（定时任务 + 查询前调用）。
 *
 * @author ruoyi
 */
@Service
public class SafetyExamServiceImpl implements ISafetyExamService
{
    @Autowired
    private SafetyExamMapper safetyExamMapper;

    @Autowired
    private SafetyExamPaperMapper safetyExamPaperMapper;

    @Autowired
    private SafetyExamRecordMapper safetyExamRecordMapper;

    @Autowired
    private SafetyTrainingAttendeeMapper safetyTrainingAttendeeMapper;

    @Autowired
    private SafetyExamUserMapper safetyExamUserMapper;

    @Autowired
    private SafetyExamAnswerMapper safetyExamAnswerMapper;

    @Autowired
    private SafetyExamPaperQuestionMapper safetyExamPaperQuestionMapper;

    @Autowired
    private ISafetyExamPaperService safetyExamPaperService;

    @Override
    public List<SafetyExam> selectSafetyExamList(SafetyExam query)
    {
        // 查询前惰性纠正状态（到点未流转的场次）
        safetyExamMapper.updateExamStatusByTime();
        return safetyExamMapper.selectSafetyExamList(query);
    }

    @Override
    public SafetyExam selectSafetyExamById(Long examId)
    {
        SafetyExam exam = safetyExamMapper.selectSafetyExamById(examId);
        if (exam != null && "2".equals(exam.getAttendeeSource()))
        {
            // 手动添加来源：详情回显名单
            exam.setManualUsers(safetyExamUserMapper.selectByExamId(examId));
        }
        return exam;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int insertSafetyExam(SafetyExam exam)
    {
        validateExam(exam);
        exam.setDelFlag("0");
        exam.setExamStatus("0");
        fillPaperName(exam);
        int rows = safetyExamMapper.insertSafetyExam(exam);
        saveManualUsers(exam);
        return rows;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int updateSafetyExam(SafetyExam exam)
    {
        SafetyExam db = requireExam(exam.getExamId());
        if (!"0".equals(db.getExamStatus()))
        {
            throw new ServiceException("仅草稿状态的场次可修改");
        }
        validateExam(exam);
        exam.setExamStatus(null);
        exam.setPublishBy(null);
        exam.setPublishTime(null);
        fillPaperName(exam);
        int rows = safetyExamMapper.updateSafetyExam(exam);
        saveManualUsers(exam);
        return rows;
    }

    /**
     * 保存手动名单（考生来源=2 时按 userIds 全量重建；来源=1 清空旧名单）
     */
    private void saveManualUsers(SafetyExam exam)
    {
        safetyExamUserMapper.deleteByExamId(exam.getExamId());
        if (!"2".equals(exam.getAttendeeSource()) || exam.getUserIds() == null || exam.getUserIds().length == 0)
        {
            return;
        }
        List<Long> ids = new ArrayList<>();
        for (Long userId : exam.getUserIds())
        {
            if (userId != null && !ids.contains(userId))
            {
                ids.add(userId);
            }
        }
        if (ids.isEmpty())
        {
            return;
        }
        // 依据 sys_user 校验并回填显示名/部门；用户已删除或停用的自动剔除
        Map<Long, SafetyExamUser> infoMap = new HashMap<>();
        for (SafetyExamUser info : safetyExamUserMapper.selectUsersByIds(ids))
        {
            infoMap.put(info.getUserId(), info);
        }
        List<SafetyExamUser> list = new ArrayList<>();
        for (Long userId : ids)
        {
            SafetyExamUser info = infoMap.get(userId);
            if (info == null)
            {
                continue;
            }
            SafetyExamUser u = new SafetyExamUser();
            u.setExamId(exam.getExamId());
            u.setUserId(userId);
            u.setUserName(info.getUserName());
            u.setDeptId(info.getDeptId());
            u.setEnterpriseId(exam.getEnterpriseId());
            u.setCreateBy(exam.getCreateBy());
            list.add(u);
        }
        if (list.isEmpty())
        {
            throw new ServiceException("所选考试人员不存在或已停用，请重新选择");
        }
        safetyExamUserMapper.batchInsert(list);
    }

    @Override
    public int deleteSafetyExamByIds(Long[] examIds)
    {
        for (Long examId : examIds)
        {
            SafetyExam db = requireExam(examId);
            if (!"0".equals(db.getExamStatus()) && !"4".equals(db.getExamStatus()))
            {
                throw new ServiceException("场次【" + db.getExamName() + "】未撤回或未结束，不能删除");
            }
        }
        int rows = 0;
        for (Long examId : examIds)
        {
            rows += safetyExamMapper.deleteSafetyExamById(examId);
            safetyExamUserMapper.deleteByExamId(examId);
        }
        return rows;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishExam(Long examId, String operName)
    {
        SafetyExam db = requireExam(examId);
        if (!"0".equals(db.getExamStatus()))
        {
            throw new ServiceException("仅草稿状态的场次可发布");
        }
        if (db.getStartTime() == null || db.getEndTime() == null)
        {
            throw new ServiceException("请先设置考试起止时间");
        }
        if (!db.getEndTime().after(db.getStartTime()))
        {
            throw new ServiceException("考试结束时间必须晚于开始时间");
        }
        // 试卷须为已发布
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(db.getPaperId());
        if (paper == null)
        {
            throw new ServiceException("关联试卷不存在");
        }
        if (!"1".equals(paper.getStatus()))
        {
            throw new ServiceException("关联试卷尚未发布，请先发布试卷");
        }
        // 考生名单：按考生来源生成（1=关联培训参训人 2=手动添加名单）
        List<SafetyExamRecord> records = new ArrayList<>();
        List<SafetyExamUser> newUsers = new ArrayList<>();
        if ("2".equals(db.getAttendeeSource()))
        {
            List<SafetyExamUser> manualUsers = safetyExamUserMapper.selectByExamId(examId);
            for (SafetyExamUser u : manualUsers)
            {
                SafetyExamRecord r = new SafetyExamRecord();
                r.setExamId(examId);
                r.setPaperId(db.getPaperId());
                r.setUserId(u.getUserId());
                r.setUserName(u.getUserName());
                r.setDeptId(u.getDeptId());
                r.setEnterpriseId(db.getEnterpriseId());
                records.add(r);
            }
            if (records.isEmpty())
            {
                throw new ServiceException("手动名单为空，请先编辑场次勾选考试人员");
            }
        }
        else
        {
            if (db.getRecordId() == null)
            {
                throw new ServiceException("请先关联培训记录");
            }
            SafetyTrainingAttendee q = new SafetyTrainingAttendee();
            q.setRecordId(db.getRecordId());
            List<SafetyTrainingAttendee> attendees = safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeList(q);
            for (SafetyTrainingAttendee a : attendees)
            {
                if (a.getUserId() == null)
                {
                    continue;
                }
                SafetyExamRecord r = new SafetyExamRecord();
                r.setExamId(examId);
                r.setPaperId(db.getPaperId());
                r.setUserId(a.getUserId());
                r.setUserName(a.getUserName());
                r.setAttendeeId(a.getAttendeeId());
                r.setDeptId(a.getDeptId());
                r.setEnterpriseId(db.getEnterpriseId());
                records.add(r);
            }
            if (records.isEmpty())
            {
                throw new ServiceException("关联培训记录下没有可参考的系统账号，无法生成考生名单");
            }
        }
        // 重建名单（重复发布时先清旧名单）
        safetyExamRecordMapper.deleteByExamId(examId);
        safetyExamMapper.batchInsertExamRecords(records);
        SafetyExam update = new SafetyExam();
        update.setExamId(examId);
        update.setExamStatus("1");
        update.setPublishBy(operName);
        update.setPublishTime(DateUtils.getNowDate());
        return safetyExamMapper.updateSafetyExam(update);
    }

    /**
     * 发布后增补考生：只增不减。
     * 考生来源=2 按传入 userIds 追加；来源=1 按关联培训参训人重新同步追加（已存在的不重复）。
     * 已开始答题/已交卷的记录一律不动，保证成绩数据完整性。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int addExamUsers(Long examId, Long[] userIds, String operName)
    {
        SafetyExam db = requireExam(examId);
        if (!"1".equals(db.getExamStatus()) && !"2".equals(db.getExamStatus()))
        {
            throw new ServiceException("仅已发布或考试中的场次可增补考生");
        }
        // 现有名单：按 userId 去重，保证只增不减
        SafetyExamRecord q = new SafetyExamRecord();
        q.setExamId(examId);
        List<SafetyExamRecord> existing = safetyExamRecordMapper.selectSafetyExamRecordList(q);
        java.util.Set<Long> present = new java.util.HashSet<>();
        for (SafetyExamRecord r : existing)
        {
            if (r.getUserId() != null)
            {
                present.add(r.getUserId());
            }
        }
        List<SafetyExamRecord> records = new ArrayList<>();
        List<SafetyExamUser> newUsers = new ArrayList<>();
        if ("2".equals(db.getAttendeeSource()))
        {
            if (userIds == null || userIds.length == 0)
            {
                throw new ServiceException("请勾选要增补的考试人员");
            }
            // 依据 sys_user 校验并回填显示名/部门；用户已删除或停用的自动剔除
            List<Long> ids = new ArrayList<>();
            for (Long userId : userIds)
            {
                if (userId != null && !ids.contains(userId))
                {
                    ids.add(userId);
                }
            }
            Map<Long, SafetyExamUser> infoMap = new HashMap<>();
            for (SafetyExamUser info : safetyExamUserMapper.selectUsersByIds(ids))
            {
                infoMap.put(info.getUserId(), info);
            }
            for (Long userId : ids)
            {
                if (present.contains(userId))
                {
                    continue;
                }
                SafetyExamUser info = infoMap.get(userId);
                if (info == null)
                {
                    continue;
                }
                records.add(buildRecord(db, userId, info.getUserName(), info.getDeptId(), null, operName));
                // 同步维护手动名单表，保证详情「考试人员」与列表/考试情况数量一致
                SafetyExamUser u = new SafetyExamUser();
                u.setExamId(examId);
                u.setUserId(userId);
                u.setUserName(info.getUserName());
                u.setDeptId(info.getDeptId());
                u.setEnterpriseId(db.getEnterpriseId());
                u.setCreateBy(operName);
                newUsers.add(u);
            }
            if (records.isEmpty())
            {
                throw new ServiceException("所选人员均已在名单中，或账号不存在/已停用");
            }
        }
        else
        {
            if (db.getRecordId() == null)
            {
                throw new ServiceException("该场次未关联培训记录，无法按参训人同步增补");
            }
            SafetyTrainingAttendee query = new SafetyTrainingAttendee();
            query.setRecordId(db.getRecordId());
            for (SafetyTrainingAttendee a : safetyTrainingAttendeeMapper.selectSafetyTrainingAttendeeList(query))
            {
                if (a.getUserId() == null || present.contains(a.getUserId()))
                {
                    continue;
                }
                records.add(buildRecord(db, a.getUserId(), a.getUserName(), a.getDeptId(), a.getAttendeeId(), operName));
            }
            if (records.isEmpty())
            {
                throw new ServiceException("参训人均已在名单中，无需增补");
            }
        }
        safetyExamMapper.batchInsertExamRecords(records);
        // 手动增补时同步写入考试人员名单表（详情名单与列表数量保持一致）
        if (!newUsers.isEmpty())
        {
            safetyExamUserMapper.batchInsert(newUsers);
        }
        return records.size();
    }

    /** 组装一条考生记录（增补名单 / 补考 / 培训同步共用） */
    private SafetyExamRecord buildRecord(SafetyExam exam, Long userId, String userName, Long deptId, Long attendeeId, String operName)
    {
        SafetyExamRecord r = new SafetyExamRecord();
        r.setExamId(exam.getExamId());
        r.setPaperId(exam.getPaperId());
        r.setUserId(userId);
        r.setUserName(userName);
        r.setAttendeeId(attendeeId);
        r.setDeptId(deptId);
        r.setEnterpriseId(exam.getEnterpriseId());
        r.setCreateBy(operName);
        return r;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelExam(Long examId)
    {
        SafetyExam db = requireExam(examId);
        if (!"1".equals(db.getExamStatus()))
        {
            throw new ServiceException("仅未开始（已发布）的场次可撤回");
        }
        safetyExamRecordMapper.deleteByExamId(examId);
        SafetyExam update = new SafetyExam();
        update.setExamId(examId);
        update.setExamStatus("4");
        return safetyExamMapper.updateSafetyExam(update);
    }

    @Override
    public int forceEndExam(Long examId)
    {
        SafetyExam db = requireExam(examId);
        if (!"1".equals(db.getExamStatus()) && !"2".equals(db.getExamStatus()))
        {
            throw new ServiceException("仅未开始或考试中的场次可强制结束");
        }
        // 未交卷者标记缺考
        safetyExamRecordMapper.markAbsentByExamId(examId);
        SafetyExam update = new SafetyExam();
        update.setExamId(examId);
        update.setExamStatus("3");
        return safetyExamMapper.updateSafetyExam(update);
    }

    @Override
    public List<SafetyExamRecord> selectExamRecords(Long examId, SafetyExamRecord query)
    {
        query.setExamId(examId);
        return safetyExamRecordMapper.selectSafetyExamRecordList(query);
    }

    @Override
    public List<SafetyExamRecord> selectExamRecordsForExport(Long examId)
    {
        SafetyExamRecord query = new SafetyExamRecord();
        query.setExamId(examId);
        return safetyExamRecordMapper.selectSafetyExamRecordList(query);
    }

    @Override
    public java.util.Map<String, Object> selectExamStats(Long examId)
    {
        SafetyExam exam = safetyExamMapper.selectSafetyExamById(examId);
        // 分数段按得分率百分比分桶（试卷总分可变，原始分直分桶无意义）
        java.math.BigDecimal paperTotal = null;
        if (exam != null && exam.getPaperId() != null)
        {
            SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(exam.getPaperId());
            if (paper != null)
            {
                paperTotal = paper.getTotalScore();
            }
        }
        SafetyExamRecord query = new SafetyExamRecord();
        query.setExamId(examId);
        List<SafetyExamRecord> records = safetyExamRecordMapper.selectSafetyExamRecordList(query);
        int total = records.size();
        int notStarted = 0, inProgress = 0, submitted = 0, absent = 0;
        int passCount = 0;
        java.math.BigDecimal scoreSum = java.math.BigDecimal.ZERO;
        java.math.BigDecimal maxScore = null, minScore = null;
        int d1 = 0, d2 = 0, d3 = 0, d4 = 0, d5 = 0;
        for (SafetyExamRecord r : records)
        {
            String result = r.getExamResult();
            if ("0".equals(result)) notStarted++;
            else if ("1".equals(result)) inProgress++;
            else if ("2".equals(result)) submitted++;
            else if ("3".equals(result)) absent++;
            if ("2".equals(result) && r.getTotalScore() != null)
            {
                java.math.BigDecimal s = r.getTotalScore();
                scoreSum = scoreSum.add(s);
                if (maxScore == null || s.compareTo(maxScore) > 0) maxScore = s;
                if (minScore == null || s.compareTo(minScore) < 0) minScore = s;
                if ("1".equals(r.getIsPass())) passCount++;
                double v = paperTotal != null && paperTotal.doubleValue() > 0
                        ? s.doubleValue() * 100.0 / paperTotal.doubleValue()
                        : s.doubleValue();
                if (v < 60) d1++;
                else if (v < 70) d2++;
                else if (v < 80) d3++;
                else if (v < 90) d4++;
                else d5++;
            }
        }
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        stats.put("total", total);
        stats.put("notStarted", notStarted);
        stats.put("inProgress", inProgress);
        stats.put("submitted", submitted);
        stats.put("absent", absent);
        stats.put("passCount", passCount);
        stats.put("passRate", submitted > 0 ? java.math.BigDecimal.valueOf(passCount * 100L / submitted).setScale(1, java.math.RoundingMode.HALF_UP) : java.math.BigDecimal.ZERO);
        stats.put("avgScore", submitted > 0 ? scoreSum.divide(java.math.BigDecimal.valueOf(submitted), 1, java.math.RoundingMode.HALF_UP) : java.math.BigDecimal.ZERO);
        stats.put("maxScore", maxScore);
        stats.put("minScore", minScore);
        java.util.Map<String, Integer> distribution = new java.util.LinkedHashMap<>();
        distribution.put("0-59%", d1);
        distribution.put("60-69%", d2);
        distribution.put("70-79%", d3);
        distribution.put("80-89%", d4);
        distribution.put("90-100%", d5);
        stats.put("distribution", distribution);
        return stats;
    }

    @Override
    public List<SafetyExamQuestionStat> selectQuestionStats(Long examId)
    {
        requireExam(examId);
        return safetyExamAnswerMapper.selectQuestionStats(examId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRemedialPaper(Long examId, java.math.BigDecimal errorRateThreshold, String operName)
    {
        SafetyExam exam = requireExam(examId);
        List<SafetyExamQuestionStat> stats = safetyExamAnswerMapper.selectQuestionStats(examId);
        if (stats.isEmpty())
        {
            throw new ServiceException("该场次暂无已交卷答题数据，无法生成错题复训卷");
        }
        BigDecimal threshold = errorRateThreshold == null ? new BigDecimal("0.5") : errorRateThreshold;
        // 错误率达标的快照题ID集合
        java.util.Set<Long> pickedIds = new java.util.HashSet<>();
        for (SafetyExamQuestionStat s : stats)
        {
            if (s.getAnswerCount() != null && s.getAnswerCount() > 0
                    && s.getErrorRate() != null && s.getErrorRate().compareTo(threshold) >= 0)
            {
                pickedIds.add(s.getPaperQuestionId());
            }
        }
        if (pickedIds.isEmpty())
        {
            throw new ServiceException("没有错误率不低于 " + threshold.multiply(new BigDecimal(100)).stripTrailingZeros().toPlainString() + "% 的题目，无需生成复训卷");
        }
        // 以源试卷快照为蓝本复制错题（快照已冻结题干/答案，不受题库后续变化影响）
        SafetyExamPaper sourcePaper = safetyExamPaperService.selectSafetyExamPaperById(exam.getPaperId());
        if (sourcePaper == null || sourcePaper.getQuestions() == null)
        {
            throw new ServiceException("源试卷不存在或无题目快照");
        }
        List<SafetyExamPaperQuestion> picked = new ArrayList<>();
        BigDecimal totalScore = BigDecimal.ZERO;
        for (SafetyExamPaperQuestion pq : sourcePaper.getQuestions())
        {
            if (pickedIds.contains(pq.getId()))
            {
                pq.setId(null);
                pq.setPaperId(null);
                picked.add(pq);
                totalScore = totalScore.add(pq.getScore() == null ? BigDecimal.ZERO : pq.getScore());
            }
        }
        if (picked.isEmpty())
        {
            throw new ServiceException("错题快照缺失，无法生成复训卷");
        }
        // 组卷：复训卷保持草稿态，由管理员确认后自行发布
        SafetyExamPaper paper = new SafetyExamPaper();
        paper.setPaperName(exam.getExamName() + "-错题复训卷");
        paper.setDuration(exam.getPaperId() != null ? sourcePaper.getDuration() : 30);
        paper.setPassScore(totalScore.multiply(new BigDecimal("0.6")).setScale(1, java.math.RoundingMode.HALF_UP));
        paper.setTotalScore(totalScore);
        paper.setCreateBy(operName);
        safetyExamPaperService.insertSafetyExamPaper(paper);
        for (SafetyExamPaperQuestion pq : picked)
        {
            pq.setPaperId(paper.getPaperId());
        }
        safetyExamPaperQuestionMapper.batchInsert(picked);
        return paper.getPaperId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRetakeExam(Long examId, SafetyExam retakeForm, String operName)
    {
        SafetyExam source = requireExam(examId);
        if (!"3".equals(source.getExamStatus()))
        {
            throw new ServiceException("仅已结束的场次可发起补考");
        }
        if (retakeForm.getStartTime() == null || retakeForm.getEndTime() == null)
        {
            throw new ServiceException("请设置补考起止时间");
        }
        if (!retakeForm.getEndTime().after(retakeForm.getStartTime()))
        {
            throw new ServiceException("补考结束时间必须晚于开始时间");
        }
        List<SafetyExamRecord> targets = safetyExamMapper.selectRetakeTargets(examId);
        if (targets.isEmpty())
        {
            throw new ServiceException("该场次没有不合格或缺考且有系统账号的考生，无需补考");
        }
        SafetyExam retake = new SafetyExam();
        retake.setExamName(source.getExamName() + "(补考" + (source.getRetakeCount() == null ? 1 : source.getRetakeCount() + 1) + ")");
        retake.setPaperId(source.getPaperId());
        retake.setPaperName(source.getPaperName());
        retake.setRecordId(source.getRecordId());
        retake.setRetakeFromId(examId);
        retake.setRetakeCount(source.getRetakeCount() == null ? 1 : source.getRetakeCount() + 1);
        retake.setScoreStrategy(retakeForm.getScoreStrategy() == null ? "2" : retakeForm.getScoreStrategy());
        retake.setStartTime(retakeForm.getStartTime());
        retake.setEndTime(retakeForm.getEndTime());
        retake.setDeptId(source.getDeptId());
        retake.setEnterpriseId(source.getEnterpriseId());
        retake.setExamStatus("1");
        retake.setPublishBy(operName);
        retake.setPublishTime(DateUtils.getNowDate());
        retake.setCreateBy(operName);
        retake.setDelFlag("0");
        safetyExamMapper.insertSafetyExam(retake);
        // 名单：仅不合格/缺考人员
        List<SafetyExamRecord> records = new ArrayList<>();
        for (SafetyExamRecord t : targets)
        {
            SafetyExamRecord r = new SafetyExamRecord();
            r.setExamId(retake.getExamId());
            r.setPaperId(source.getPaperId());
            r.setUserId(t.getUserId());
            r.setUserName(t.getUserName());
            r.setAttendeeId(t.getAttendeeId());
            r.setWorkerId(t.getWorkerId());
            r.setDeptId(t.getDeptId());
            r.setEnterpriseId(source.getEnterpriseId());
            records.add(r);
        }
        safetyExamMapper.batchInsertExamRecords(records);
        return retake.getExamId();
    }

    @Override
    public List<SafetyExamAnswerArchive> selectAnswerArchive(Long examId)
    {
        requireExam(examId);
        return safetyExamAnswerMapper.selectArchiveByExamId(examId);
    }

    /**
     * 按 paperId 回填试卷名快照（前端仅传 paperId，历史数据缺 paper_name 时列表已用试卷表兜底）
     */
    private void fillPaperName(SafetyExam exam)
    {
        if (exam.getPaperId() == null || (exam.getPaperName() != null && !exam.getPaperName().isEmpty()))
        {
            return;
        }
        SafetyExamPaper paper = safetyExamPaperMapper.selectSafetyExamPaperById(exam.getPaperId());
        if (paper != null)
        {
            exam.setPaperName(paper.getPaperName());
        }
    }

    private SafetyExam requireExam(Long examId)
    {
        SafetyExam db = safetyExamMapper.selectSafetyExamById(examId);
        if (db == null)
        {
            throw new ServiceException("考试场次不存在或已删除");
        }
        return db;
    }

    private void validateExam(SafetyExam exam)
    {
        if (exam.getExamName() == null || exam.getExamName().isEmpty())
        {
            throw new ServiceException("场次名称不能为空");
        }
        if (exam.getPaperId() == null)
        {
            throw new ServiceException("请选择关联试卷");
        }
        // 考生来源：默认来自培训；来源=培训须关联培训记录，来源=手动须勾选人员
        if (exam.getAttendeeSource() == null || exam.getAttendeeSource().isEmpty())
        {
            exam.setAttendeeSource("1");
        }
        if (!"1".equals(exam.getAttendeeSource()) && !"2".equals(exam.getAttendeeSource()))
        {
            throw new ServiceException("考生来源不合法");
        }
        if ("1".equals(exam.getAttendeeSource()) && exam.getRecordId() == null)
        {
            throw new ServiceException("考生来源为培训参训人时，请关联培训记录");
        }
        if ("2".equals(exam.getAttendeeSource()))
        {
            exam.setRecordId(null);
            if (exam.getUserIds() == null || exam.getUserIds().length == 0)
            {
                throw new ServiceException("考生来源为手动添加时，请勾选考试人员");
            }
        }
    }
}
