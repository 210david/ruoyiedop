package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsDefectCode;

/**
 * 缺陷代码 Service接口
 *
 * @author ruoyi
 */
public interface IQmsDefectCodeService
{
    public List<QmsDefectCode> selectDefectCodeList(QmsDefectCode defectCode);
    public QmsDefectCode selectDefectCodeById(Long defectId);
    public int insertDefectCode(QmsDefectCode defectCode);
    public int updateDefectCode(QmsDefectCode defectCode);
    public int deleteDefectCodeByIds(Long[] defectIds);
}
