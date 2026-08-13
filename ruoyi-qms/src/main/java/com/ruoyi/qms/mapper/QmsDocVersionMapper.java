package com.ruoyi.qms.mapper;

import java.util.List;
import com.ruoyi.qms.domain.QmsDocVersion;

public interface QmsDocVersionMapper {
    public List<QmsDocVersion> selectDocVersionList(QmsDocVersion version);
    public List<QmsDocVersion> selectByVersionDocId(Long docId);
    public QmsDocVersion selectDocVersionById(Long versionId);
    public int insertDocVersion(QmsDocVersion version);
    public int updateDocVersion(QmsDocVersion version);
    public int deleteDocVersionByIds(Long[] versionIds);
}
