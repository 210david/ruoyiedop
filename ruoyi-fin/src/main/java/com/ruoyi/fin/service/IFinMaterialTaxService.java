package com.ruoyi.fin.service;

import com.ruoyi.fin.domain.FinMaterialTax;

import java.util.List;
import java.util.Map;

/**
 * 物料税率档案 Service 接口
 *
 * <p><b>为什么必须显式维护</b>：FinCostEngine 取税率时查不到档案会直接抛
 * 10008，<b>不做默认回落</b>——若回落13%，零税率物料
 * （农产品免税、出口零税）会被错按 13% 计税，成本失真且难以察觉。</p>
 *
 * @author ruoyi
 */
public interface IFinMaterialTaxService
{
    /**
     * 查询税率档案列表
     *
     * @param tax 查询条件
     * @return 档案列表
     */
    List<FinMaterialTax> selectTaxList(FinMaterialTax tax);

    /**
     * 按物料编码查询生效税率
     *
     * @param materialCode 物料编码
     * @return 档案，无则返回 null
     */
    FinMaterialTax getEffectiveTax(String materialCode);

    /**
     * 新增税率档案
     *
     * @param tax 档案
     * @return 影响行数
     */
    int insertTax(FinMaterialTax tax);

    /**
     * 修改税率档案
     *
     * @param tax 档案
     * @return 影响行数
     */
    int updateTax(FinMaterialTax tax);

    /**
     * 逻辑删除税率档案
     *
     * @param ids 档案ID数组
     * @return 影响行数
     */
    int deleteTaxByIds(Long[] ids);

    /**
     * 未建税率档案的物料（用于提示补录）
     *
     * @param limit 上限
     * @return 物料列表
     */
    List<Map<String, Object>> untaxedMaterials(int limit);

    /**
     * 有效档案数
     *
     * @return 档案数
     */
    int countEffective();

    /**
     * 按默认税率批量建档（快速初始化用）
     *
     * <p><b>慎用</b>：只适用于确认全部物料税率相同的场景。
     * 现实业务中零税率物料普遍存在，建议逐个确认后再建。</p>
     *
     * @param taxRate 默认税率
     * @return 实际建档条数
     */
    int batchInit(String taxRate);
}