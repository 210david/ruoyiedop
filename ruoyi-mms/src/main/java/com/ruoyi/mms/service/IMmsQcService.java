package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsQc;

/**
 * 过程质检 Service接口
 *
 * @author ruoyi
 */
public interface IMmsQcService
{
    public List<MmsQc> selectQcList(MmsQc qc);
    public MmsQc selectQcById(Long QcId);
    public int insertQc(MmsQc qc);
    public int updateQc(MmsQc qc);
    public int deleteQcByIds(Long[] QcIds);

    /**
     * 执行质检（提交质检结果）
     * 质检员确认检验数据后，提交检验结论、缺陷类型等信息
     */
    public int inspectQc(MmsQc qc);
}
