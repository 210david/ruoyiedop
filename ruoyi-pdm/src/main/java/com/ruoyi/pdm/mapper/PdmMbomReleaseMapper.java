package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmMbomRelease;

/**
 * BOM下发记录 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmMbomReleaseMapper
{
    /** 查询下发记录列表 */
    public List<PdmMbomRelease> selectReleaseList(PdmMbomRelease release);

    /** 根据ID查询下发记录 */
    public PdmMbomRelease selectReleaseById(Long releaseId);

    /** 新增下发记录 */
    public int insertRelease(PdmMbomRelease release);

    /** 修改下发记录 */
    public int updateRelease(PdmMbomRelease release);

    /** 将产品下已下发的旧记录置为已替代（D3口径） */
    public int markReplacedByProductId(PdmMbomRelease release);
}
