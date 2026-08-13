package com.ruoyi.qms.service;

import java.util.List;
import com.ruoyi.qms.domain.QmsDocVersion;

public interface IQmsDocVersionService {
    public List<QmsDocVersion> selectDocVersionList(QmsDocVersion version);
    public List<QmsDocVersion> selectByDocId(Long docId);
    public QmsDocVersion selectDocVersionById(Long versionId);
    public int publishVersion(QmsDocVersion version);
    public int obsoleteVersion(Long versionId, String obsoleteBy);
    public int deleteDocVersionByIds(Long[] versionIds);
}
