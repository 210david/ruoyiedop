package com.ruoyi.mms.mapper;

import java.math.BigDecimal;
import java.util.List;
import com.ruoyi.mms.domain.MmsKitCheck;
import com.ruoyi.mms.domain.MmsKitCheckDetail;

public interface MmsKitCheckMapper {
    public List<MmsKitCheck> selectKitCheckList(MmsKitCheck kitCheck);
    public MmsKitCheck selectKitCheckById(Long kitId);
    public int insertKitCheck(MmsKitCheck kitCheck);
    public int updateKitCheck(MmsKitCheck kitCheck);
    public int deleteKitCheckByIds(Long[] kitIds);
    public List<MmsKitCheckDetail> selectKitCheckDetailByKitId(Long kitId);
    public int insertKitCheckDetail(MmsKitCheckDetail detail);
    public int updateKitCheckDetail(MmsKitCheckDetail detail);
    public int deleteKitCheckDetailByKitId(Long kitId);

    /** 查询物料可用库存总量（跨模块查询 wms_inventory） */
    public BigDecimal selectAvailableQtyByMaterialId(Long materialId);
}