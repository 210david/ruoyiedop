package com.ruoyi.pdm.service;

import java.util.List;
import com.ruoyi.pdm.domain.PdmDesignBom;
import com.ruoyi.pdm.domain.PdmDesignBomItem;

/**
 * 设计BOM Service 接口
 *
 * 业务规则（设计方案 7.4）：
 * 1. 仅"生效版本"的设计BOM允许下发（下发转换 MBOM 于第三批实现，前置校验物料申请全部确认）；
 * 2. 已生效BOM修订自动小版本升版并回到"编制中"，原生效版本保持有效；
 * 3. 明细物料关联 wms 物料主数据，新增物料走"物料申请"由仓库/采购确认编码后回填。
 *
 * @author ruoyi
 */
public interface IPdmDesignBomService
{
    /** 查询设计BOM列表（含明细数量） */
    public List<PdmDesignBom> selectBomList(PdmDesignBom bom);

    /** 查询BOM详情（含明细平铺列表） */
    public PdmDesignBom selectBomById(Long bomId);

    /** 新增BOM（保存即取号 + 保存明细 + 自动创建V1.0版本记录） */
    public int insertBom(PdmDesignBom bom);

    /** 修改BOM（含明细全量替换；已生效BOM修订自动升版回到编制中） */
    public int updateBom(PdmDesignBom bom);

    /** BOM生效（版本生效 + 置为已生效状态） */
    public int effectiveBom(Long bomId, Long versionId, String effectRef);

    /** 手工升版（生成新版本记录） */
    public int upgradeBom(Long bomId, String versionDesc, boolean majorUpgrade);

    /** 批量删除（仅限编制中BOM） */
    public int deleteBomByIds(Long[] bomIds);

    /** 查询BOM明细 */
    public List<PdmDesignBomItem> selectItemsByBomId(Long bomId);

    /** 按技术变更单升版生效（依据=技术变更单号，第三批变更联动） */
    public int effectiveForChange(Long bomId, String changeNo);
}
