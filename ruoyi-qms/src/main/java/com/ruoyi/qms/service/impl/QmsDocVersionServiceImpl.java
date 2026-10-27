package com.ruoyi.qms.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.qms.domain.QmsDocVersion;
import com.ruoyi.qms.mapper.QmsDocVersionMapper;
import com.ruoyi.qms.service.IQmsDocVersionService;

@Service
public class QmsDocVersionServiceImpl implements IQmsDocVersionService {
    @Autowired
    private QmsDocVersionMapper mapper;

    @Override
    public List<QmsDocVersion> selectDocVersionList(QmsDocVersion version) { return mapper.selectDocVersionList(version); }

    @Override
    public List<QmsDocVersion> selectByDocId(Long docId) { return mapper.selectByVersionDocId(docId); }

    @Override
    public QmsDocVersion selectDocVersionById(Long versionId) { return mapper.selectDocVersionById(versionId); }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int publishVersion(QmsDocVersion version) {
        if (version.getVersionId() == null) {
            if (StringUtils.isEmpty(version.getVersionNo())) throw new ServiceException("版本号不能为空");
            if (StringUtils.isEmpty(version.getEsigUser())) throw new ServiceException("电子签名用户不能为空");
            version.setVersionStatus("1");
            version.setPublishTime(new Date());
            version.setEsigTime(new Date());
            return mapper.insertDocVersion(version);
        } else {
            QmsDocVersion existing = mapper.selectDocVersionById(version.getVersionId());
            if (existing == null) throw new ServiceException("版本记录不存在");
            if (StringUtils.isNotEmpty(version.getEsigUser())) {
                version.setEsigTime(new Date());
            }
            version.setVersionStatus("1");
            version.setPublishTime(new Date());
            return mapper.updateDocVersion(version);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int obsoleteVersion(Long versionId, String obsoleteBy) {
        QmsDocVersion existing = mapper.selectDocVersionById(versionId);
        if (existing == null) throw new ServiceException("版本记录不存在");
        if (!"1".equals(existing.getVersionStatus())) throw new ServiceException("只有生效状态的版本才能作废");
        QmsDocVersion update = new QmsDocVersion();
        update.setVersionId(versionId);
        update.setVersionStatus("2");
        update.setObsoleteBy(obsoleteBy);
        update.setObsoleteTime(new Date());
        return mapper.updateDocVersion(update);
    }

    @Override
    public int deleteDocVersionByIds(Long[] versionIds) { return mapper.deleteDocVersionByIds(versionIds); }
}
