package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDocVersion;

/**
 * 设计资料版本 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmDocVersionMapper
{
    /** 查询版本列表（按资料） */
    public List<PdmDocVersion> selectVersionList(PdmDocVersion version);

    /** 根据ID查询 */
    public PdmDocVersion selectVersionById(Long versionId);

    /** 查询资料当前生效版本 */
    public PdmDocVersion selectEffectiveVersion(PdmDocVersion version);

    /** 查询资料最大版本序号（用于自动分配版本号） */
    public PdmDocVersion selectMaxVersion(PdmDocVersion version);

    /** 查询产品档案聚合的全部资料版本（方案1/图纸2/BOM3，档案"版本记录"数据源） */
    public List<PdmDocVersion> selectVersionsByProduct(Long productId);

    /** 新增 */
    public int insertVersion(PdmDocVersion version);

    /** 修改 */
    public int updateVersion(PdmDocVersion version);

    /** 批量删除 */
    public int deleteVersionByIds(Long[] versionIds);
}
