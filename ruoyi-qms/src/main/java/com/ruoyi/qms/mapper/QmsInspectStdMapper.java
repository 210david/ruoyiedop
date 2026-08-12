package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsInspectStd;

/**
 * 检验标准库 Mapper
 *
 * @author ruoyi
 */
public interface QmsInspectStdMapper
{
    public List<QmsInspectStd> selectInspectStdList(QmsInspectStd inspectStd);
    public QmsInspectStd selectInspectStdById(Long stdId);
    public QmsInspectStd selectInspectStdByCode(String stdCode);
    public int insertInspectStd(QmsInspectStd inspectStd);
    public int updateInspectStd(QmsInspectStd inspectStd);
    public int deleteInspectStdByIds(Long[] stdIds);
}
