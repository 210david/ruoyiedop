package com.ruoyi.safety.service;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExam;
import com.ruoyi.safety.domain.SafetyExamRecord;
import com.ruoyi.safety.domain.vo.SafetyExamAnswerArchive;
import com.ruoyi.safety.domain.vo.SafetyExamQuestionStat;

/**
 * 考试场次 Service接口
 *
 * @author ruoyi
 */
public interface ISafetyExamService
{
    public List<SafetyExam> selectSafetyExamList(SafetyExam query);

    /** 场次详情（含考生名单） */
    public SafetyExam selectSafetyExamById(Long examId);

    public int insertSafetyExam(SafetyExam exam);

    public int updateSafetyExam(SafetyExam exam);

    public int deleteSafetyExamByIds(Long[] examIds);

    /** 发布场次（校验试卷已发布，生成考生名单） */
    public int publishExam(Long examId, String operName);

    /** 发布后增补考生（只增不减，返回新增人数） */
    public int addExamUsers(Long examId, Long[] userIds, String operName);

    /** 撤回场次（仅未开始，名单作废） */
    public int cancelExam(Long examId);

    /** 强制结束（考试中→已结束，未交卷者标记缺考） */
    public int forceEndExam(Long examId);

    /** 考生名单 */
    public List<SafetyExamRecord> selectExamRecords(Long examId, SafetyExamRecord query);

    /** 成绩导出用全量名单（不分页） */
    public List<SafetyExamRecord> selectExamRecordsForExport(Long examId);

    /** 成绩统计（应考/交卷/缺考/及格率/平均分/分数段分布） */
    public java.util.Map<String, Object> selectExamStats(Long examId);

    /** FR-7：错题率分析（按题目聚合，错误率降序） */
    public List<SafetyExamQuestionStat> selectQuestionStats(Long examId);

    /** FR-7：按高错误率题目一键生成复训/补考试卷草稿，返回新试卷ID */
    public Long createRemedialPaper(Long examId, java.math.BigDecimal errorRateThreshold, String operName);

    /** FR-8：一键创建补考场次（名单=不合格+缺考且有账号者），返回新场次ID */
    public Long createRetakeExam(Long examId, SafetyExam retakeForm, String operName);

    /** FR-12：完整答卷存档明细（每题考生原始作答） */
    public List<SafetyExamAnswerArchive> selectAnswerArchive(Long examId);
}
