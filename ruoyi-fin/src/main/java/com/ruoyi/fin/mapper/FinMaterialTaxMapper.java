package com.ruoyi.fin.mapper;

import com.ruoyi.fin.domain.FinMaterialTax;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 物料税率档案 Mapper
 *
 * <p><b>为什么必须显式维护</b>：FinCostEngine 取税率时若查不到档案
 * 会直接抛 10008materialTaxNotFound，<b>不做 13% 默认回落</b>——
 * 因为默认回落会让零税率物料（如农产品免税、出口零税）被错按13% 计税，
 * 造成成本失真且难以察觉。</p>
 *
 * @author ruoyi
 */
public interface FinMaterialTaxMapper
{
    /**
     * 按物料编码查询当前生效的税率档案。
     *
     * @param materialCode 物料编码
     * @return 生效档案；无则返回 null（由 FinCostEngine 抛 10008）
     */
    FinMaterialTax selectEffectiveByMaterial(@Param("materialCode") String materialCode);

    /**
     * 按物料编码查询生效税率（带 FOR UPDATE，供成本写入时锁定用）。
     *
     * @param materialCode 物料编码
     * @return 生效档案
     */
    FinMaterialTax selectEffectiveByMaterialForUpdate(@Param("materialCode") String materialCode);

    /**
     * 查询税率档案列表（维护页）
     *
     * @param tax 查询条件
     * @return 档案列表
     */
    List<FinMaterialTax> selectTaxList(FinMaterialTax tax);

    /**
     * 按ID查询
     *
     * @param materialTaxId 档案ID
     * @return 档案
     */
    FinMaterialTax selectTaxById(@Param("materialTaxId") Long materialTaxId);

    /**
     * 校验同一物料同一生效日是否重复建档。
     *
     * @param materialCode   物料编码
     * @param effectiveDate  生效日期
     * @param taxId          修改时传自身ID
     * @return 同条件记录数，0=不重复
     */
    int checkDuplicate(@Param("materialCode") String materialCode,
                       @Param("effectiveDate") java.util.Date effectiveDate,
                       @Param("taxId") Long taxId);

    /**
     * 新增税率档案。
     *
     * @param tax 档案
     * @return 影响行数
     */
    int insertTax(FinMaterialTax tax);

    /**
     * 修改税率档案。
     *
     * @param tax 档案
     * @return 影响行数
     */
    int updateTax(FinMaterialTax tax);

    /**
     * 逻辑删除税率档案。
     *
     * @param materialTaxIds 档案ID数组
     * @return 影响行数
     */
    int deleteTaxByIds(Long[] materialTaxIds);

    /**
     * 查询所有已建档的物料编码（用于批量初始化时提示已建档物料）。
     *
     * @return 物料编码列表
     */
    List<String> selectTaxedMaterialCodes();

    /**
     * 未建税率档案的物料（wms_material 中有、fin_material_tax 中无）。
     *
     * @param limit 上限
     * @return 物料列表
     */
    List<java.util.Map<String, Object>> selectUntaxedMaterials(@Param("limit") int limit);

    /**
     * 有效档案数（校验用）。
     *
     * @return 档案数
     */
    int countEffective();
}