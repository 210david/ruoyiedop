package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsTraceSn;

public interface QmsTraceSnMapper {
    public List<QmsTraceSn> selectSnList(QmsTraceSn sn);
    public QmsTraceSn selectSnById(Long id);
    public QmsTraceSn selectBySnCode(String snCode);
    public List<QmsTraceSn> selectByBatchNo(String batchNo);
    public int insertSn(QmsTraceSn sn);
    public int updateSn(QmsTraceSn sn);
    public int deleteSnByIds(Long[] ids);
}
