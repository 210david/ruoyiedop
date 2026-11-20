package com.ruoyi.web.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 首页岗位 KPI 取数 Mapper
 *
 * 说明：
 * 1. 项目已关闭 MyBatis 驼峰自动映射（ruoyi-admin/src/main/resources/mybatis/mybatis-config.xml），
 *    因此这里全部显式写列别名，禁止依赖 mapUnderscoreToCamelCase；
 * 2. 全部为只读统计 SQL，口径与各模块列表页保持一致（金额取含税总额，逾期按 due_date 判定）；
 * 3. 放在 ruoyi-admin（依赖全部业务模块）而非各业务模块内，避免为一张首页卡片反向增加模块依赖。
 *
 * @author ruoyi
 */
public interface HomeKpiMapper
{
    /** 首页岗位 KPI 配置（按岗位取 4 条） */
    @Select("SELECT id, role_key AS roleKey, seq, metric_key AS metricKey, title, unit, "
            + "target_titles AS targetTitles, target_path AS targetPath "
            + "FROM sys_role_kpi WHERE role_key = #{roleKey} AND enabled = '0' ORDER BY seq")
    public List<Map<String, Object>> selectByRoleKey(@Param("roleKey") String roleKey);

    /** 本月开票应收：金额 + 单数 */
    @Select("SELECT IFNULL(SUM(total_amount), 0) AS amount, COUNT(*) AS cnt FROM fin_ar_order "
            + "WHERE del_flag = '0' AND ar_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')")
    public Map<String, Object> selectArMonth();

    /** 逾期未收：未结清且已过到期日 */
    @Select("SELECT IFNULL(SUM(total_amount - IFNULL(received_amount, 0) - IFNULL(bad_debt_amount, 0)), 0) AS amount, "
            + "COUNT(*) AS cnt FROM fin_ar_order "
            + "WHERE del_flag = '0' AND status <> 'SETTLED' AND due_date IS NOT NULL AND due_date < CURDATE()")
    public Map<String, Object> selectArOverdue();

    /** 应付未付：未结清应付单的未付金额 */
    @Select("SELECT IFNULL(SUM(total_amount - IFNULL(paid_amount, 0)), 0) AS amount, COUNT(*) AS cnt "
            + "FROM fin_ap_order WHERE del_flag = '0' AND status <> 'SETTLED'")
    public Map<String, Object> selectApUnpaid();

    /** 待审核凭证（DRAFT） */
    @Select("SELECT COUNT(*) AS cnt FROM fin_voucher WHERE del_flag = '0' AND status = 'DRAFT'")
    public Map<String, Object> selectVoucherDraft();

    /**
     * 未结账会计期间（仅统计"已到期应结"的月份）
     * 口径说明：fin_period 会预建未来月份（状态同为 OPEN），若不加月份上限，
     * 卡片会显示 20+ 个"未结账"，与业务事实不符，故限定 year_month &lt;= 当前月。
     */
    @Select("SELECT COUNT(*) AS cnt FROM fin_period WHERE del_flag = '0' AND status = 'OPEN' "
            + "AND `year_month` <= DATE_FORMAT(CURDATE(), '%Y%m')")
    public Map<String, Object> selectPeriodOpen();

    /** 在册人数（在职 0 + 试用期 1）、在职数、试用期数 */
    @Select("SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN employee_status = '0' THEN 1 ELSE 0 END) AS onjob, "
            + "SUM(CASE WHEN employee_status = '1' THEN 1 ELSE 0 END) AS probation "
            + "FROM biz_hr_employee WHERE del_flag = '0' AND employee_status IN ('0', '1')")
    public Map<String, Object> selectHrHeadcount();

    /** 待审批请假单（audit_status 0=待审批） */
    @Select("SELECT COUNT(*) AS cnt FROM biz_hr_leave_request WHERE del_flag = '0' AND audit_status = '0'")
    public Map<String, Object> selectHrLeavePending();

    /** 待转正人数（转正流程 audit_status 0=待审批） */
    @Select("SELECT COUNT(*) AS cnt FROM biz_hr_regular WHERE del_flag = '0' AND audit_status = '0'")
    public Map<String, Object> selectHrRegularPending();

    /** 30 天内到期劳动合同（履行中 0，且到期日落在今天至 30 天内） */
    @Select("SELECT COUNT(*) AS cnt FROM biz_hr_contract WHERE del_flag = '0' AND contract_status = '0' "
            + "AND end_date IS NOT NULL AND end_date >= CURDATE() AND end_date <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)")
    public Map<String, Object> selectHrContractExpire();

    /** 公务车辆总数 + 年检/保险 30 天内到期数 */
    @Select("SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN (inspect_expire IS NOT NULL AND inspect_expire <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)) "
            + "OR (insurance_expire IS NOT NULL AND insurance_expire <= DATE_ADD(CURDATE(), INTERVAL 30 DAY)) "
            + "THEN 1 ELSE 0 END) AS due "
            + "FROM biz_oa_vehicle WHERE del_flag = '0'")
    public Map<String, Object> selectOaVehicle();

    /** 启用中的印章数（status 0=启用） */
    @Select("SELECT COUNT(*) AS cnt FROM biz_oa_seal WHERE del_flag = '0' AND status = '0'")
    public Map<String, Object> selectOaSeal();

    /** 物料档案数（status 0=启用） */
    @Select("SELECT COUNT(*) AS total, SUM(CASE WHEN status = '0' THEN 1 ELSE 0 END) AS enabled "
            + "FROM wms_material WHERE del_flag = '0'")
    public Map<String, Object> selectMaterialTotal();

    /** 库存低于安全库存的物料数（按物料去重，仅统计设置了安全库存下限的物料） */
    @Select("SELECT COUNT(DISTINCT i.material_id) AS cnt FROM wms_inventory i "
            + "JOIN wms_material m ON m.material_id = i.material_id "
            + "WHERE i.del_flag = '0' AND m.del_flag = '0' "
            + "AND m.safety_stock_min IS NOT NULL AND m.safety_stock_min > 0 AND i.qty < m.safety_stock_min")
    public Map<String, Object> selectStockBelowSafety();

    /** 设备台账数 + 在用数 + 维修/故障数（字典 dms_equipment_status：0在用 2维修中 4故障） */
    @Select("SELECT COUNT(*) AS total, "
            + "SUM(CASE WHEN equipment_status = '0' THEN 1 ELSE 0 END) AS inuse, "
            + "SUM(CASE WHEN equipment_status IN ('2', '4') THEN 1 ELSE 0 END) AS repair "
            + "FROM dms_equipment WHERE del_flag = '0'")
    public Map<String, Object> selectEquipmentTotal();

    /** 待发货销售订单（字典 marketing_order_status：2=已审核待发货） */
    @Select("SELECT COUNT(*) AS cnt, IFNULL(SUM(order_amount), 0) AS amount "
            + "FROM mk_order WHERE del_flag = '0' AND order_status = '2'")
    public Map<String, Object> selectSalesOrderWaitShip();

    /** 在途采购订单（0草稿 1待审批 2已审批 3已下单 4部分到货，均未完成） */
    @Select("SELECT COUNT(*) AS cnt, IFNULL(SUM(total_amount), 0) AS amount, "
            + "SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS pending "
            + "FROM pms_purchase_order WHERE del_flag = '0' AND status IN ('0', '1', '2', '3', '4')")
    public Map<String, Object> selectPurchaseOrderOpen();

    /**
     * 未完工生产工单 + 执行中 + 已下达
     * 口径：mms_work_order 字典 mms_workorder_status（0草稿 1已下达 2执行中 3已完工 4已关闭 5已暂停 6已取消）；
     *      未完工取已下达/执行中/已暂停，草稿与已取消不计入在制。
     */
    @Select("SELECT COUNT(*) AS cnt, "
            + "SUM(CASE WHEN status = '2' THEN 1 ELSE 0 END) AS doing, "
            + "SUM(CASE WHEN status = '1' THEN 1 ELSE 0 END) AS released "
            + "FROM mms_work_order WHERE del_flag = '0' AND status IN ('1', '2', '5')")
    public Map<String, Object> selectWorkOrderOpen();

    /**
     * 在管项目 + 执行中 + 已逾期
     * 口径：pjm_project 状态字典（0草稿 1待审 2通过 3执行 4完成 5关闭 6暂停 7驳回）；
     *      在管取通过/执行中/暂停（已立项且未终结），草稿、待审、已完成、已关闭、驳回不计入；
     *      逾期 = 计划结束日已过且仍处于在管状态（与项目列表页预警口径一致）。
     */
    @Select("SELECT COUNT(*) AS cnt, "
            + "SUM(CASE WHEN status = '3' THEN 1 ELSE 0 END) AS doing, "
            + "SUM(CASE WHEN end_date IS NOT NULL AND end_date < CURDATE() THEN 1 ELSE 0 END) AS overdue "
            + "FROM pjm_project WHERE del_flag = '0' AND status IN ('2', '3', '6')")
    public Map<String, Object> selectProjectOpen();
}
