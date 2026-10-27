package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmScheme;
import com.ruoyi.pdm.domain.PdmSchemeFormula;

/**
 * 设计方案 Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmSchemeMapper
{
    /** 查询设计方案列表 */
    public List<PdmScheme> selectSchemeList(PdmScheme scheme);

    /** 根据ID查询 */
    public PdmScheme selectSchemeById(Long schemeId);

    /** 新增 */
    public int insertScheme(PdmScheme scheme);

    /** 修改 */
    public int updateScheme(PdmScheme scheme);

    /** 批量删除 */
    public int deleteSchemeByIds(Long[] schemeIds);

    /** 查询方案配方配比明细 */
    public List<PdmSchemeFormula> selectFormulaBySchemeId(Long schemeId);

    /** 批量插入配方明细 */
    public int batchInsertFormula(@org.apache.ibatis.annotations.Param("list") List<PdmSchemeFormula> formulaList);

    /** 删除方案配方明细 */
    public int deleteFormulaBySchemeId(Long schemeId);
}
