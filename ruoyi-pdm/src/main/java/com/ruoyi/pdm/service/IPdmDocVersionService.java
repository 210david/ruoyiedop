package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDocVersion;

/**
 * 设计资料版本 Service 接口（公共版本机制，被方案/文件/BOM 复用）
 *
 * @author ruoyi
 */
public interface IPdmDocVersionService
{
    /** 查询版本列表（版本时间轴数据源） */
    public List<PdmDocVersion> selectVersionList(PdmDocVersion version);

    /** 根据ID查询 */
    public PdmDocVersion selectVersionById(Long versionId);

    /** 查询资料当前生效版本 */
    public PdmDocVersion selectEffectiveVersion(String bizType, Long bizId);

    /** 查询资料最大版本记录（定稿/升版默认取最新版本） */
    public PdmDocVersion selectLatestVersion(String bizType, Long bizId);

    /** 查询产品档案聚合的全部资料版本（方案1/图纸2/BOM3） */
    public List<PdmDocVersion> selectVersionsByProduct(Long productId);

    /** 修改版本记录（用于定稿时同步文件等有限场景） */
    public int updateVersion(PdmDocVersion version);

    /** 新建版本（版本号自动分配：小版本 +0.1，大版本 +1.0） */
    public int createVersion(PdmDocVersion version, boolean majorUpgrade);

    /** 版本生效（同事务：新版本生效 + 旧版本转历史，须关联审批依据） */
    public int makeEffective(Long versionId, String effectBasis, String effectRef);
}
