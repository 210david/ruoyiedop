package com.ruoyi.safety.mapper;

import java.util.List;
import com.ruoyi.safety.domain.SafetyExamPaper;

/**
 * 考试试卷 Mapper接口
 *
 * @author ruoyi
 */
public interface SafetyExamPaperMapper
{
    public SafetyExamPaper selectSafetyExamPaperById(Long paperId);

    public List<SafetyExamPaper> selectSafetyExamPaperList(SafetyExamPaper query);

    public int insertSafetyExamPaper(SafetyExamPaper paper);

    public int updateSafetyExamPaper(SafetyExamPaper paper);

    public int deleteSafetyExamPaperById(Long paperId);

    public int deleteSafetyExamPaperByIds(Long[] paperIds);
}
