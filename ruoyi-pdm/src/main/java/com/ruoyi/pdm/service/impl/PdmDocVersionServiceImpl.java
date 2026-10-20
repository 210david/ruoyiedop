package com.ruoyi.pdm.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.pdm.domain.PdmDocVersion;
import com.ruoyi.pdm.mapper.PdmDocVersionMapper;
import com.ruoyi.pdm.service.IPdmDocVersionService;

/**
 * 设计资料版本 Service 实现（公共版本机制）
 *
 * 业务规则（设计方案 7.5）：
 * 1. 版本编号系统自动分配，不允许手工改号；
 * 2. 生效动作必须关联审批依据（定稿/变更单），无审批依据不允许生效；
 * 3. 同一资料同一时刻只有一个生效版本，新版本生效后旧版本自动转历史（只读）。
 *
 * @author ruoyi
 */
@Service
public class PdmDocVersionServiceImpl implements IPdmDocVersionService
{
    @Autowired
    private PdmDocVersionMapper versionMapper;

    @Override
    public List<PdmDocVersion> selectVersionList(PdmDocVersion version)
    {
        return versionMapper.selectVersionList(version);
    }

    @Override
    public PdmDocVersion selectVersionById(Long versionId)
    {
        return versionMapper.selectVersionById(versionId);
    }

    @Override
    public PdmDocVersion selectEffectiveVersion(String bizType, Long bizId)
    {
        PdmDocVersion query = new PdmDocVersion();
        query.setBizType(bizType);
        query.setBizId(bizId);
        return versionMapper.selectEffectiveVersion(query);
    }

    @Override
    public PdmDocVersion selectLatestVersion(String bizType, Long bizId)
    {
        PdmDocVersion query = new PdmDocVersion();
        query.setBizType(bizType);
        query.setBizId(bizId);
        return versionMapper.selectMaxVersion(query);
    }

    @Override
    public List<PdmDocVersion> selectVersionsByProduct(Long productId)
    {
        return versionMapper.selectVersionsByProduct(productId);
    }

    @Override
    public int updateVersion(PdmDocVersion version)
    {
        return versionMapper.updateVersion(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int createVersion(PdmDocVersion version, boolean majorUpgrade)
    {
        if (StringUtils.isEmpty(version.getBizType()) || version.getBizId() == null)
        {
            throw new ServiceException("版本归属资料类型与业务ID不能为空");
        }
        version.setVersionNo(nextVersionNo(version, majorUpgrade));
        version.setEffectiveFlag("0");
        return versionMapper.insertVersion(version);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int makeEffective(Long versionId, String effectBasis, String effectRef)
    {
        // 版本生效必须关联审批依据
        if (StringUtils.isEmpty(effectBasis))
        {
            throw new ServiceException("版本生效必须关联审批依据（设计定稿/技术变更），无审批依据不允许生效");
        }
        PdmDocVersion version = versionMapper.selectVersionById(versionId);
        if (version == null)
        {
            throw new ServiceException("版本记录不存在");
        }
        if ("1".equals(version.getEffectiveFlag()))
        {
            throw new ServiceException("该版本已是生效版本，不允许重复生效");
        }
        Date now = new Date();
        // 旧生效版本转历史
        PdmDocVersion current = selectEffectiveVersion(version.getBizType(), version.getBizId());
        if (current != null)
        {
            PdmDocVersion old = new PdmDocVersion();
            old.setVersionId(current.getVersionId());
            old.setEffectiveFlag("0");
            old.setInvalidTime(now);
            versionMapper.updateVersion(old);
        }
        // 新版本生效
        PdmDocVersion update = new PdmDocVersion();
        update.setVersionId(versionId);
        update.setEffectiveFlag("1");
        update.setEffectiveTime(now);
        update.setEffectBasis(effectBasis);
        update.setEffectRef(effectRef);
        return versionMapper.updateVersion(update);
    }

    /** 计算下一版本号：小版本 +0.1，大版本主位 +1 */
    private String nextVersionNo(PdmDocVersion version, boolean majorUpgrade)
    {
        PdmDocVersion max = versionMapper.selectMaxVersion(version);
        if (max == null || StringUtils.isEmpty(max.getVersionNo()))
        {
            return "V1.0";
        }
        String no = max.getVersionNo().replace("V", "");
        String[] parts = no.split("\\.");
        int major = 1;
        int minor = 0;
        try
        {
            major = Integer.parseInt(parts[0]);
            minor = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        }
        catch (NumberFormatException ignored)
        {
        }
        return majorUpgrade ? "V" + (major + 1) + ".0" : "V" + major + "." + (minor + 1);
    }
}
