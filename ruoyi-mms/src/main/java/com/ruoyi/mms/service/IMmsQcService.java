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
}
