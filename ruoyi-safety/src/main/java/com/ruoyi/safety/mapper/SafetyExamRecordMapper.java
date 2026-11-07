package com.ruoyi.safety.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.ruoyi.safety.domain.SafetyExamRecord;

/**
 * 考生考试记录 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamRecordMapper
{
    public SafetyExamRecord selectSafetyExamRecordById(Long recordId);

    public List<SafetyExamRecord> selectSafetyExamRecordList(SafetyExamRecord query);

    public int insertSafetyExamRecord(SafetyExamRecord record);

    public int updateSafetyExamRecord(SafetyExamRecord record);

    public int deleteSafetyExamRecordById(Long recordId);

    public int deleteSafetyExamRecordByIds(Long[] recordIds);

    /** 按考试+考生精确查询（幂等提交校验） */
    public SafetyExamRecord selectByExamAndUser(@Param("examId") Long examId, @Param("userId") Long userId);

    /** 查询本人考试记录列表（考生端） */
    public List<SafetyExamRecord> selectMyExamRecords(@Param("userId") Long userId);

    /** 考生端「我的考试」聚合视图（record + exam 窗口 + paper 规则） */
    public List<java.util.Map<String, Object>> selectMyExamList(@Param("userId") Long userId);

    /** 批量标记缺考（窗口结束未交卷） */
    public int markAbsentByExamId(@Param("examId") Long examId);

    /** 按场次清空考生名单（发布重建 / 撤回作废） */
    public int deleteByExamId(@Param("examId") Long examId);
}
