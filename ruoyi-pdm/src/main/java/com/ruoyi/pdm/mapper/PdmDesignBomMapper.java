package com.ruoyi.pdm.mapper;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.domain.PdmDesignBomItem;

/**
 * 设计BOM Mapper 接口
 *
 * @author ruoyi
 */
public interface PdmDesignBomMapper
{
    /** 查询设计BOM列表（含明细数量） */
    public List<PdmDesignBom> selectBomList(PdmDesignBom bom);

    /** 根据ID查询 */
    public PdmDesignBom selectBomById(Long bomId);

    /** 查询BOM明细（平铺，按录入顺序） */
    public List<PdmDesignBomItem> selectItemsByBomId(Long bomId);

    /** 新增 */
    public int insertBom(PdmDesignBom bom);

    /** 修改 */
    public int updateBom(PdmDesignBom bom);

    /** 批量删除 */
    public int deleteBomByIds(Long[] bomIds);

    /** 新增明细 */
    public int insertBomItem(PdmDesignBomItem item);

    /** 修改明细父子关系与层级 */
    public int updateBomItem(PdmDesignBomItem item);

    /** 删除BOM全部明细 */
    public int deleteItemsByBomId(Long bomId);
}
