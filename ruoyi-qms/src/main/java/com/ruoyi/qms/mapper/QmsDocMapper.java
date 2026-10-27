package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsDoc;

public interface QmsDocMapper {
    public List<QmsDoc> selectDocList(QmsDoc doc);
    public QmsDoc selectDocById(Long docId);
    public int insertDoc(QmsDoc doc);
    public int updateDoc(QmsDoc doc);
    public int deleteDocByIds(Long[] docIds);
}
