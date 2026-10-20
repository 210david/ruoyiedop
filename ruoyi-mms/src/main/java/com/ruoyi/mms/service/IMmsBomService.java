package com.ruoyi.mms.service;

import java.util.List;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.mms.domain.MmsBom;
import com.ruoyi.mms.domain.MmsBomDetail;
import com.ruoyi.mms.domain.MmsBomImport;

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

    /**
     * BOM批量导入
     *
     * @param importList 导入数据列表
     * @param isUpdateSupport 是否更新已存在的BOM
     * @param operName 操作人
     * @return 导入结果
     */
    public AjaxResult importBom(List<MmsBomImport> importList, Boolean isUpdateSupport, String operName);

    /**
     * 接收产品设计模块下发的制造BOM（跨模块衔接点一，D1决策：mms 纯新增方法）
     * 校验产品物料有效 → 同产品旧已发布BOM置停用（D3口径）→ 事务内落库 mms_bom(status=1,bomType=0) + 明细
     *
     * @param bom 下发的BOM（productId=物料ID、version=EBOM生效版本号、明细需已确认物料编码）
     * @return 接收生成的 mms_bom.bomId
     */
    public Long receiveMbom(MmsBom bom);
}
