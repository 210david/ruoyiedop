package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsInspectStd;

/**
 * 检验标准库 Service接口
 *
 * @author ruoyi
 */
public interface IQmsInspectStdService
{
    public List<QmsInspectStd> selectInspectStdList(QmsInspectStd inspectStd);
    public QmsInspectStd selectInspectStdById(Long stdId);
    public int insertInspectStd(QmsInspectStd inspectStd);
    public int updateInspectStd(QmsInspectStd inspectStd);
    public int deleteInspectStdByIds(Long[] stdIds);
}
