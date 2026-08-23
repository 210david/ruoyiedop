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
}
