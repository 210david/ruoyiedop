package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import org.apache.ibatis.annotations.Param;

public interface MmsBomMapper {
    public List<MmsBom> selectBomList(MmsBom bom);
    public MmsBom selectBomById(Long bomId);
    public int insertBom(MmsBom bom);
    public int updateBom(MmsBom bom);
    public int deleteBomByIds(Long[] bomIds);
    public List<MmsBomDetail> selectBomDetailByBomId(Long bomId);
    public int insertBomDetail(MmsBomDetail detail);
    public int deleteBomDetailByBomId(Long bomId);
    public int countPublishedByProductId(Long productId);
    public MmsBom selectBomByProductId(Long productId);
    public int updateBomStatus(MmsBom bom);

    /** 根据产品编码查询已存在的BOM（导入时判断重复） */
    public MmsBom selectBomByProductCodeAndVersion(@Param("productCode") String productCode, @Param("version") String version);

    /** 根据物料编码查询物料信息（导入时通过编码获取物料ID） */
    public java.util.Map<String, Object> selectMaterialByCode(@Param("materialCode") String materialCode);
}
