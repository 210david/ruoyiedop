package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;

/**
 * BOM Service接口
 *
 * @author ruoyi
 */
public interface IMmsBomService
{
    public List<MmsBom> selectBomList(MmsBom bom);
    public MmsBom selectBomById(Long BomId);
    public int insertBom(MmsBom bom);
    public int updateBom(MmsBom bom);
    public int deleteBomByIds(Long[] BomIds);
    public List<MmsBomDetail> selectBomDetailByBomId(Long BomId);
    public int publishBom(Long bomId);
    public int disableBom(Long bomId);
    public int enableBom(Long bomId);
    public MmsBom copyBom(Long bomId);
    public List<MmsBomDetail> selectBomTreeByBomId(Long bomId);
}
