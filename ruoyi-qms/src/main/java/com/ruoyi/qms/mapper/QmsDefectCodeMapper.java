package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsDefectCode;

/**
 * 缺陷代码 Mapper
 *
 * @author ruoyi
 */
public interface QmsDefectCodeMapper
{
    public List<QmsDefectCode> selectDefectCodeList(QmsDefectCode defectCode);
    public QmsDefectCode selectDefectCodeById(Long defectId);
    public QmsDefectCode selectDefectCodeByCode(String defectCode);
    public int insertDefectCode(QmsDefectCode defectCode);
    public int updateDefectCode(QmsDefectCode defectCode);
    public int deleteDefectCodeByIds(Long[] defectIds);
}
