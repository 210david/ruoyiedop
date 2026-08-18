package com.ruoyi.mms.mapper;

import java.util.List;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;

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
}
