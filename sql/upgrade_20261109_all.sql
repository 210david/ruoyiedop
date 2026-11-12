-- =============================================================
-- EDOP JSLY（郎鹰金属）生产库增量升级脚本（合并幂等版）
-- 日期：2026-11-09
-- 基线：edop_jsly 全库脚本（sql/edop_full.sql，2026-10-21 全库备份导出）
--       之后的全部数据库变更；保守并入 2026-10-29 起的所有增量（含
--       upgrade_20261102_all 全部内容），生产重复执行无副作用。
-- -------------------------------------------------------------
-- 本脚本合并 43 段变更（按序执行），覆盖：
--   1. 仓库编码自动编码开关（mk_number_rule.auto_enabled）
--   2. 20261102 合并版（合同长期有效/上传上限参数/合同与订单状态字典）
--   3. WMS 供应商固定电话列
--   4. 20261106 合并版（PJM 建表+车辆台账+风险点补列+采购耗用+菜单幂等化）
--   5. PJM 发票/菜单排序/回款凭证/项目列扩容（20261107 系列）
--   6. PJM 采购物料类型/工时附件（20261108 系列）
--   7. MCP 网关新工具权限（20261109）
--   8. BI 决策支持模块全套（4 张业务表/指标字典 22 项/预警表+定时任务/
--      九域看板/菜单重组与 Legacy 双入口/口径补列/隐藏菜单清理）
--   9. 系统集成模块（钉钉/企业微信考勤表+菜单+绑定唯一索引+mock 清理）
--  10. 编号规则模块归属与动态前缀（dms/wms 规则初始化）
--  11. 连接器逐用户授权表+菜单、MCP 只读服务账号
--  12. 仓库结构树库区层级修复（wms_warehouse 补库区，幂等）
--  13. 模块开关补登决策支持/系统集成（root_menu_id 动态取菜单）
--  14. 全库排序规则统一（utf8mb4_general_ci，最后执行）
-- -------------------------------------------------------------
-- 【幂等性说明 — 重要】
--   本脚本可在同一数据库重复执行，结果一致：
--   - 建表一律 CREATE TABLE IF NOT EXISTS（不 DROP，绝不破坏已有数据）
--   - INSERT 均带 NOT EXISTS / NOT IN / INSERT IGNORE 前置判断
--   - 加列均用 information_schema 检查 + PREPARE 动态执行（MySQL 8.0 安全幂等）
--   - ALTER 改列一律 MODIFY；重复执行无副作用
--   - 第 0 段清理历史重复数据（保留最小主键，菜单删除前先迁移角色授权）
--   - 已将原脚本中的 MariaDB 语法 ADD COLUMN IF NOT EXISTS 转换为 MySQL 8.0 幂等形式
--   - 已移除硬编码库名（原 ruoyi_lyjs. 前缀，统一改用 DATABASE()）
--
-- 适用：已有数据的客户生产库增量升级（新服务器首装由全量脚本覆盖）
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 edop_jsly < 本文件
--       或双击同目录 run_upgrade_20261109_all.bat（密码交互输入）
-- =============================================================

SET NAMES utf8mb4;

-- =============================================================
-- 第 0 段：清理历史重复数据（保留最小主键的一条）
-- =============================================================

-- 0.1 字典分类：按 dict_type 保留最小 dict_id
DELETE d FROM `sys_dict_type` d
JOIN (
    SELECT `dict_type`, MIN(`dict_id`) AS keep_id
    FROM `sys_dict_type`
    GROUP BY `dict_type`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type`
WHERE d.`dict_id` <> t.keep_id;

-- 0.2 字典数据：同 dict_type + dict_value 只保留最小 dict_code
DELETE d FROM `sys_dict_data` d
JOIN (
    SELECT `dict_type`, `dict_value`, MIN(`dict_code`) AS keep_code
    FROM `sys_dict_data`
    GROUP BY `dict_type`, `dict_value`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type` AND d.`dict_value` = t.`dict_value`
WHERE d.`dict_code` <> t.keep_code;

-- 0.3 参数配置：同 config_key 只保留最小 config_id
DELETE c FROM `sys_config` c
JOIN (
    SELECT `config_key`, MIN(`config_id`) AS keep_id
    FROM `sys_config`
    GROUP BY `config_key`
    HAVING COUNT(*) > 1
) t ON c.`config_key` = t.`config_key`
WHERE c.`config_id` <> t.keep_id;

-- 0.4 菜单：同 parent_id + menu_type + perms（非空）保留最小 menu_id，
--     删除前先把角色授权迁移到保留项（幂等，NOT EXISTS 防重）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, k.keep_id
FROM sys_role_menu rm
JOIN (
    SELECT m.parent_id, m.menu_type, m.perms, MIN(m.menu_id) AS keep_id, m.menu_id AS dup_id
    FROM sys_menu m
    WHERE m.perms IS NOT NULL AND m.perms <> '' AND m.menu_type IN ('C','F')
    GROUP BY m.parent_id, m.menu_type, m.perms
    HAVING COUNT(*) > 1
) k ON rm.menu_id = k.dup_id
WHERE k.dup_id <> k.keep_id
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x
                  WHERE x.role_id = rm.role_id AND x.menu_id = k.keep_id);

DELETE m FROM sys_menu m
JOIN (
    SELECT m2.parent_id, m2.menu_type, m2.perms, MIN(m2.menu_id) AS keep_id
    FROM sys_menu m2
    WHERE m2.perms IS NOT NULL AND m2.perms <> '' AND m2.menu_type IN ('C','F')
    GROUP BY m2.parent_id, m2.menu_type, m2.perms
    HAVING COUNT(*) > 1
) k ON m.parent_id = k.parent_id AND m.menu_type = k.menu_type AND m.perms = k.perms
WHERE m.menu_id <> k.keep_id;

DELETE rm FROM sys_role_menu rm
LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_id IS NULL;

-- =============================================================
-- 以下第 1~37 段为并入的各变更脚本（加工后），按时间与依赖顺序执行
-- =============================================================


-- =============================================================
-- 第 1 段 / 共 42 段：upgrade_20261029_仓库编码自动编码开关.sql
-- =============================================================

-- =============================================================
-- 升级脚本：仓库编码规则「是否启用自动编码」开关
-- 日期：2026-10-29
-- 说明：mk_number_rule 增加 auto_enabled 列（0=关闭,1=开启）。
--       开启时仓库新建/编辑页面编码按规则自动生成（只读）；
--       关闭时编码必填、手动输入、后端校验不能重复。
--       仓库编码规则（wms_warehouse）默认开启，保持原有自动生成行为。
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- =============================================================

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='auto_enabled');
SET @ddl = IF(@has_col=0,
    'ALTER TABLE `mk_number_rule` ADD COLUMN `auto_enabled` CHAR(1) DEFAULT ''1'' COMMENT ''是否启用自动编码（0=关闭,1=开启）''',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 仓库编码规则默认开启自动编码
UPDATE `mk_number_rule` SET `auto_enabled` = '1' WHERE `rule_code` = 'wms_warehouse';

-- 回退语句
-- ALTER TABLE `mk_number_rule` DROP COLUMN `auto_enabled`;



-- =============================================================
-- 第 2 段 / 共 42 段：upgrade_20261102_all.sql
-- =============================================================

-- =============================================================
-- EDOP CFSP 增量升级脚本（合并版）
-- 日期：2026-11-02
-- 版本：v1.0（合并当日 4 个独立脚本）
-- -------------------------------------------------------------
-- 本脚本合并了以下 4 项变更，按顺序执行：
--   1. 合同到期日期改为可空（空=长期有效）
--   2. 新增单文件上传上限参数 system.upload.maxFileSizeMb
--   3. 营销合同状态字典与列表权限校验补建
--   4. 销售订单状态字典补充 7=已退货
--
-- 【幂等性说明 — 重要】
--   本脚本可在同一数据库重复执行，结果一致：
--   - ALTER 使用 MODIFY，重复执行不报错
--   - INSERT 均带 NOT EXISTS 前置判断，不会重复插入
--   - 执行前会先清理历史重复数据（见第 0 段）
--
-- 【为什么需要清理重复数据】
--   sys_dict_data / sys_config / sys_menu 均无业务唯一约束
--   （sys_dict_data 仅 dict_code 自增主键，sys_config 仅 config_id，
--     sys_menu 仅 menu_id），若此前分次执行过旧脚本或人工补过数据，
--   同一 dict_type+dict_value 可能出现多条。本脚本第 0 段会保留
--   最早的一条（最小主键），删除其余重复项，并同步处理菜单的角色授权。
--
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- 执行：mysql -uroot -p --default-character-set=utf8mb4 库名 < 本文件
--       或在 Windows 上双击同目录 run_upgrade_20261102_all.bat
-- =============================================================

-- =============================================================
-- 第 0 段：清理历史重复数据（保留最小主键的一条）
-- =============================================================

-- 0.1 字典分类：marketing_order_status / marketing_contract_status
DELETE d FROM `sys_dict_type` d
JOIN (
    SELECT `dict_type`, MIN(`dict_id`) AS keep_id
    FROM `sys_dict_type`
    WHERE `dict_type` IN ('marketing_order_status', 'marketing_contract_status')
    GROUP BY `dict_type`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type`
WHERE d.`dict_id` <> t.keep_id;

-- 0.2 字典数据：同 dict_type + dict_value 只保留最小 dict_code
DELETE d FROM `sys_dict_data` d
JOIN (
    SELECT `dict_type`, `dict_value`, MIN(`dict_code`) AS keep_code
    FROM `sys_dict_data`
    WHERE `dict_type` IN ('marketing_order_status', 'marketing_contract_status')
    GROUP BY `dict_type`, `dict_value`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type` AND d.`dict_value` = t.`dict_value`
WHERE d.`dict_code` <> t.keep_code;

-- 0.3 参数配置：同 config_key 只保留最小 config_id
DELETE c FROM `sys_config` c
JOIN (
    SELECT `config_key`, MIN(`config_id`) AS keep_id
    FROM `sys_config`
    WHERE `config_key` = 'system.upload.maxFileSizeMb'
    GROUP BY `config_key`
    HAVING COUNT(*) > 1
) t ON c.`config_key` = t.`config_key`
WHERE c.`config_id` <> t.keep_id;

-- 0.4 菜单：同 perms 只保留最小 menu_id
--     先把重复菜单上的角色授权迁移到保留项，再删除重复菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT DISTINCT rm.`role_id`, t.keep_id
FROM `sys_role_menu` rm
JOIN `sys_menu` m ON m.`menu_id` = rm.`menu_id`
JOIN (
    SELECT `perms`, MIN(`menu_id`) AS keep_id
    FROM `sys_menu`
    WHERE `perms` = 'marketing:contract:list'
    GROUP BY `perms`
    HAVING COUNT(*) > 1
) t ON t.`perms` = m.`perms`
WHERE rm.`menu_id` <> t.keep_id;

DELETE rm FROM `sys_role_menu` rm
JOIN (
    SELECT DISTINCT m.`menu_id` AS dup_id
    FROM `sys_menu` m
    JOIN (
        SELECT `perms`, MIN(`menu_id`) AS keep_id
        FROM `sys_menu`
        WHERE `perms` = 'marketing:contract:list'
        GROUP BY `perms`
        HAVING COUNT(*) > 1
    ) t ON t.`perms` = m.`perms`
    WHERE m.`menu_id` <> t.keep_id
) d ON d.dup_id = rm.`menu_id`;

DELETE m FROM `sys_menu` m
JOIN (
    SELECT MIN(`menu_id`) AS keep_id, `perms`
    FROM `sys_menu`
    WHERE `perms` = 'marketing:contract:list'
    GROUP BY `perms`
    HAVING COUNT(*) > 1
) t ON t.`perms` = m.`perms`
WHERE m.`menu_id` <> t.keep_id;

-- =============================================================
-- 第 1 段：合同到期日期改为允许 NULL（NULL = 长期有效）
-- -------------------------------------------------------------
-- 原为 NOT NULL，框架/年度协议无固定终止日时无法录入，只能填远期日期，
-- 导致到期提醒与报表失真。
-- MODIFY 为幂等操作，重复执行不报错。
-- 影响：定时任务「合同过期标记」用 expire_date < CURDATE()，
--       NULL 比较结果为 NULL（WHERE 中视为 false），不会被误标过期。
-- =============================================================
ALTER TABLE `mk_contract`
    MODIFY COLUMN `expire_date` DATE NULL COMMENT '到期日期（为空表示长期有效）';

-- =============================================================
-- 第 2 段：新增单文件上传上限参数
-- -------------------------------------------------------------
-- 前端上传组件读取该值做校验与提示，避免前端写死数值与
-- nginx(client_max_body_size 80m) / 后端 multipart(100MB) 不一致。
-- =============================================================
INSERT INTO `sys_config`
    (`config_name`, `config_key`, `config_value`, `config_type`, `remark`,
     `create_by`, `create_time`)
SELECT '单文件上传上限(MB)', 'system.upload.maxFileSizeMb', '50', 'N',
       '各模块上传文件大小上限，需与 nginx client_max_body_size 保持一致',
       'admin', sysdate()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_config` WHERE `config_key` = 'system.upload.maxFileSizeMb'
);

-- =============================================================
-- 第 3 段：营销合同状态字典与列表权限
-- -------------------------------------------------------------
-- 销售订单「关联合同」弹框依赖：状态字典（含 value=2 已生效）
-- 与 /mk/contract/list 接口权限 marketing:contract:list
-- =============================================================
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '合同状态', 'marketing_contract_status', '0', '营销合同状态'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'marketing_contract_status'
);

INSERT INTO `sys_dict_data`
    (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `is_default`, `status`, `remark`)
SELECT t.sort, t.label, t.val, 'marketing_contract_status', t.dft, '0', NULL
FROM (
    SELECT 1 AS sort, '草稿'       AS label, '0' AS val, 'Y' AS dft UNION ALL
    SELECT 2, '审批中',       '1', 'N' UNION ALL
    SELECT 3, '已生效',       '2', 'N' UNION ALL
    SELECT 4, '已过期',       '3', 'N' UNION ALL
    SELECT 5, '已终止',       '4', 'N' UNION ALL
    SELECT 6, '变更审批中',   '5', 'N'
) t
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data`
    WHERE `dict_type` = 'marketing_contract_status' AND `dict_value` = t.val
);

INSERT INTO `sys_menu`
    (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`,
     `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`)
SELECT '合同管理', 4005, 1, 'contract', 'mk/contract/index', 1, 0, 'C', '0', '0',
       'marketing:contract:list', 'list'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `perms` = 'marketing:contract:list'
)
AND EXISTS (
    SELECT 1 FROM `sys_menu` WHERE `menu_id` = 4005
);

-- =============================================================
-- 第 4 段：销售订单状态字典补充 7 = 已退货
-- -------------------------------------------------------------
-- 退货退款完成后 MkOrderReturnServiceImpl#refundReturn 会将订单
-- 状态置为 '7'，原字典只定义到 6，导致状态列空白、筛选页签缺失。
-- 排序值取 8，排在「已作废(6, sort=7)」之后。
-- =============================================================
INSERT INTO `sys_dict_data`
    (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`,
     `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 8, '已退货', '7', 'marketing_order_status', '', 'danger', 'N', '0',
       'admin', sysdate(), '退货退款完成后由系统自动置为该状态'
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_dict_data`
    WHERE `dict_type` = 'marketing_order_status' AND `dict_value` = '7'
)
AND EXISTS (
    SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'marketing_order_status'
);

-- =============================================================
-- 执行后自检（可选，手工执行下面语句核对）
-- =============================================================
-- 1) 到期日期应允许为空
-- SELECT IS_NULLABLE FROM information_schema.COLUMNS
--  WHERE TABLE_SCHEMA = DATABASE()
--    AND TABLE_NAME = 'mk_contract' AND COLUMN_NAME = 'expire_date';
--    期望 YES
--
-- 2) 上传上限参数应存在 1 条
-- SELECT config_key, config_value FROM sys_config
--  WHERE config_key = 'system.upload.maxFileSizeMb';
--
-- 3) 营销合同状态应 6 条；销售订单状态应 8 条（含 7 已退货）
-- SELECT dict_type, COUNT(*) FROM sys_dict_data
--  WHERE dict_type IN ('marketing_contract_status','marketing_order_status')
--  GROUP BY dict_type;
--
-- 4) 重复数据应全部为 0
-- SELECT dict_type, dict_value, COUNT(*) c FROM sys_dict_data
--  WHERE dict_type IN ('marketing_order_status','marketing_contract_status')
--  GROUP BY dict_type, dict_value HAVING c > 1;
-- SELECT config_key, COUNT(*) c FROM sys_config
--  WHERE config_key = 'system.upload.maxFileSizeMb' GROUP BY config_key HAVING c > 1;
-- SELECT perms, COUNT(*) c FROM sys_menu
--  WHERE perms = 'marketing:contract:list' GROUP BY perms HAVING c > 1;

-- =============================================================
-- 回退说明
-- -------------------------------------------------------------
-- 本次变更未修改既有业务数据，按需逆向即可：
--   1) 到期日期回退（若已有 NULL 值需先补齐到期日）：
--      ALTER TABLE `mk_contract`
--        MODIFY COLUMN `expire_date` DATE NOT NULL COMMENT '到期日期';
--   2) 删除上传上限参数：
--      DELETE FROM `sys_config` WHERE `config_key` = 'system.upload.maxFileSizeMb';
--   3) 删除已退货字典项（需先把 order_status='7' 的订单改为其他状态，
--      否则这些订单状态列会变空白）：
--      DELETE FROM `sys_dict_data`
--       WHERE `dict_type` = 'marketing_order_status' AND `dict_value` = '7';
-- 第 0 段的重复数据清理不可逆，但被删除的都是重复冗余项，不影响业务。
-- =============================================================



-- =============================================================
-- 第 3 段 / 共 42 段：upgrade_20261103_wms_supplier_fixed_phone.sql
-- =============================================================

-- =============================================================
-- 升级脚本：供应商管理新增"固定电话"字段
-- 日期：2026-11-03
-- 适用：已存在 wms_supplier 表的环境（幂等，可重复执行）
-- 说明：
--   1. wms_supplier 新增 fixed_phone（固定电话）字段， VARCHAR(20)，可空
--   2. 联系电话、固定电话均不强制必填（应用层已调整，无需数据变更）
-- =============================================================

SET @col_exists = (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'wms_supplier'
      AND COLUMN_NAME = 'fixed_phone'
);

SET @ddl = IF(
    @col_exists = 0,
    'ALTER TABLE `wms_supplier` ADD COLUMN `fixed_phone` VARCHAR(20) DEFAULT NULL COMMENT ''固定电话'' AFTER `contact_phone`',
    'SELECT ''fixed_phone 字段已存在，跳过'' AS notice'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;



-- =============================================================
-- 第 4 段 / 共 42 段：upgrade_20261106_merge_pjm_vehicle_risk.sql
-- =============================================================

-- ============================================================
-- 20261106 合并：同事功能合并增量 SQL（PJM项目管理 + 车辆台账 + 风险点完善 + 采购耗用登记）
-- 目标库：ruoyi_lyjs
-- 纪律：幂等可重跑（CREATE TABLE IF NOT EXISTS / INSERT IGNORE / WHERE NOT EXISTS / 幂等加列存储过程）
-- 注意：patch_20261104.sql 中车辆台账菜单 parent_id 4639（一企一档）→ 4638（安全生产管理）、
--       耗用登记菜单 parent_id 4441（采购订单执行）→ 4197（采购管理），
--       避免菜单挂错父目录（语义错位）。
-- ============================================================
SET NAMES utf8mb4;

-- §1 PMS/WMS 跨模块联动字段（幂等存储过程）
DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;
CALL p_add_column_if_missing('pms_purchase_request','purchase_type','`purchase_type` char(1) DEFAULT ''1'' COMMENT ''采购类型(1正常采购 2零散采购)''');
CALL p_add_column_if_missing('pms_purchase_request','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_request','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_purchase_plan','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_plan','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_purchase_order','purchase_type','`purchase_type` char(1) DEFAULT ''1'' COMMENT ''采购类型(1正常采购 2零散采购)''');
CALL p_add_column_if_missing('pms_purchase_order','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_purchase_order','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_contract','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID''');
CALL p_add_column_if_missing('pms_contract','source_project_no','`source_project_no` varchar(50) DEFAULT NULL COMMENT ''来源项目编号''');
CALL p_add_column_if_missing('pms_inquiry','purchase_type','`purchase_type` char(1) DEFAULT ''0'' COMMENT ''采购类型（字典 pms_purchase_type）''');
CALL p_add_column_if_missing('wms_outbound_order','source_project_id','`source_project_id` bigint DEFAULT NULL COMMENT ''来源项目ID（PJM联动）''');
CALL p_add_column_if_missing('wms_outbound_order','source_project_no','`source_project_no` varchar(64) DEFAULT NULL COMMENT ''来源项目编号（PJM联动）''');
DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- §2 PJM 11 张建表

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project` (
  `project_id` bigint NOT NULL AUTO_INCREMENT COMMENT '项目ID',
  `project_no` varchar(50) NOT NULL COMMENT '项目编号(PJM前缀)',
  `project_name` varchar(200) NOT NULL COMMENT '项目名称',
  `project_type` varchar(20) DEFAULT '1' COMMENT '类型(1-研发 2-实施 3-运维 4-产品 5-投标 6-内部)',
  `engineering_category` varchar(20) DEFAULT NULL COMMENT '工程类别(pjm_engineering_category)',
  `priority` varchar(10) DEFAULT '2' COMMENT '优先级(1低 2中 3高 4紧急)',
  `status` varchar(20) DEFAULT '0' COMMENT '状态(0草稿 1待审 2通过 3执行 4完成 5关闭 6暂停 7驳回)',
  `progress` decimal(5,2) DEFAULT '0.00' COMMENT '整体进度%',
  `budget_amount` decimal(18,2) DEFAULT '0.00' COMMENT '项目预算总金额',
  `planned_purchase` decimal(18,2) DEFAULT '0.00' COMMENT '计划采购金额(=Σ采购申请)',
  `actual_purchase` decimal(18,2) DEFAULT '0.00' COMMENT '实际采购金额(=Σ采购订单)',
  `received_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已收货金额',
  `invoice_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已开票金额',
  `paid_amount` decimal(18,2) DEFAULT '0.00' COMMENT '已付款金额',
  `purchase_count` int DEFAULT '0' COMMENT '关联采购申请数量',
  `start_date` date DEFAULT NULL COMMENT '计划开始',
  `end_date` date DEFAULT NULL COMMENT '计划结束',
  `actual_start_date` date DEFAULT NULL COMMENT '实际开始',
  `actual_end_date` date DEFAULT NULL COMMENT '实际结束',
  `manager_id` bigint DEFAULT NULL COMMENT '项目经理ID',
  `manager_name` varchar(64) DEFAULT NULL COMMENT '项目经理姓名',
  `dept_id` bigint DEFAULT NULL COMMENT '所属部门ID',
  `dept_name` varchar(100) DEFAULT NULL COMMENT '所属部门名称',
  `owner_unit` varchar(100) DEFAULT NULL COMMENT '建设单位',
  `build_unit` varchar(100) DEFAULT NULL COMMENT '施工单位',
  `supervisor_unit` varchar(100) DEFAULT NULL COMMENT '监理单位',
  `design_unit` varchar(100) DEFAULT NULL COMMENT '设计单位',
  `project_address` varchar(200) DEFAULT NULL COMMENT '工程地点',
  `build_scale` varchar(200) DEFAULT NULL COMMENT '建设规模',
  `contract_duration` int DEFAULT NULL COMMENT '合同工期(日历天)',
  `commence_date` date DEFAULT NULL COMMENT '开工日期',
  `completion_date` date DEFAULT NULL COMMENT '计划竣工日期',
  `quality_standard` varchar(50) DEFAULT NULL COMMENT '质量标准',
  `contract_amount` decimal(16,2) DEFAULT NULL COMMENT '合同总额(元)',
  `customer` varchar(200) DEFAULT NULL COMMENT '客户/甲方',
  `description` text COMMENT '项目描述',
  `scope` text COMMENT '项目范围',
  `deliverables` text COMMENT '交付物清单',
  `audit_by` varchar(64) DEFAULT NULL COMMENT '审批人',
  `audit_time` datetime DEFAULT NULL COMMENT '审批时间',
  `audit_opinion` varchar(500) DEFAULT NULL COMMENT '审批意见',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`project_id`),
  UNIQUE KEY `uk_project_no` (`project_no`)
) ENGINE=InnoDB AUTO_INCREMENT=9002 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项主表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_attachment` (
  `attachment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '附件ID',
  `project_id` bigint NOT NULL COMMENT '关联项目ID',
  `attachment_type` char(1) DEFAULT '1' COMMENT '类型(1招标文件 2投标文件 3项目图纸 4项目保险)',
  `file_name` varchar(200) NOT NULL COMMENT '原始文件名',
  `file_url` varchar(500) NOT NULL COMMENT '上传路径/URL',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `file_ext` varchar(20) DEFAULT NULL COMMENT '扩展名',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`attachment_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项附件表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_procurement` (
  `procurement_id` bigint NOT NULL AUTO_INCREMENT COMMENT '清单ID',
  `project_id` bigint NOT NULL COMMENT '关联项目ID',
  `material_id` bigint DEFAULT NULL COMMENT '系统物料ID（关联wms_material）',
  `item_no` int DEFAULT '0' COMMENT '序号',
  `item_code` varchar(50) DEFAULT NULL COMMENT '物料编码',
  `item_name` varchar(200) NOT NULL COMMENT '物料名称',
  `spec_model` varchar(200) DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(20) DEFAULT NULL COMMENT '单位',
  `qty` decimal(18,4) DEFAULT '0.0000' COMMENT '数量',
  `unit_price` decimal(18,2) DEFAULT '0.00' COMMENT '单价',
  `amount` decimal(18,2) DEFAULT '0.00' COMMENT '合计金额',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`procurement_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_material_id` (`material_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目立项采购清单表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_member` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `user_name` varchar(64) DEFAULT NULL,
  `dept_id` bigint DEFAULT NULL,
  `dept_name` varchar(100) DEFAULT NULL,
  `role_type` varchar(20) DEFAULT '3' COMMENT '1-项目经理 2-技术负责人 3-成员 4-外部协作',
  `join_date` date DEFAULT NULL,
  `leave_date` date DEFAULT NULL,
  `workload` decimal(5,2) DEFAULT '0.00' COMMENT '投入占比%',
  `task_count` int DEFAULT '0',
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`member_id`),
  KEY `idx_project_user` (`project_id`,`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=4010 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目团队成员表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_project_audit_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL,
  `audit_action` varchar(20) DEFAULT NULL COMMENT '1创建 2修改 3提交 4通过 5驳回 6关闭 7采购发起',
  `audit_by` varchar(64) DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `audit_remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`log_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7013 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目审核日志表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_wbs_task` (
  `task_id` bigint NOT NULL AUTO_INCREMENT,
  `project_id` bigint NOT NULL COMMENT '所属项目',
  `parent_task_id` bigint DEFAULT NULL COMMENT '父任务(自关联树)',
  `task_code` varchar(50) DEFAULT NULL COMMENT '编码(如 1.2.3)',
  `task_name` varchar(200) NOT NULL,
  `task_type` varchar(20) DEFAULT '1' COMMENT '1工作项 2里程碑 3交付物',
  `status` varchar(20) DEFAULT '0' COMMENT '0未开始 1进行中 2完成 3取消 4延期',
  `priority` varchar(10) DEFAULT '2',
  `progress` decimal(5,2) DEFAULT '0.00',
  `need_purchase` char(1) DEFAULT '0' COMMENT '0否 1是',
  `purchase_request_id` bigint DEFAULT NULL COMMENT '已关联采购申请ID',
  `owner_id` bigint DEFAULT NULL,
  `owner_name` varchar(64) DEFAULT NULL,
  `plan_start_date` date DEFAULT NULL,
  `plan_end_date` date DEFAULT NULL,
  `actual_start_date` date DEFAULT NULL,
  `actual_end_date` date DEFAULT NULL,
  `plan_hours` decimal(10,2) DEFAULT '0.00',
  `actual_hours` decimal(10,2) DEFAULT '0.00',
  `description` text,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`task_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_parent` (`parent_task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=2403 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='WBS工作分解任务表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_task_worklog` (
  `worklog_id` bigint NOT NULL AUTO_INCREMENT COMMENT '工时ID',
  `worklog_no` varchar(50) DEFAULT NULL COMMENT '工时单号',
  `task_id` bigint NOT NULL COMMENT 'WBS任务ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `user_id` bigint DEFAULT NULL COMMENT '填报人ID',
  `user_name` varchar(64) DEFAULT NULL COMMENT '填报人',
  `work_date` date DEFAULT NULL COMMENT '工时日期',
  `work_hours` decimal(10,2) DEFAULT '0.00' COMMENT '工时（小时）',
  `work_content` varchar(500) DEFAULT NULL COMMENT '工作内容',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`worklog_id`),
  KEY `idx_task` (`task_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=8111 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='WBS任务工时记录表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_risk` (
  `risk_id` bigint NOT NULL AUTO_INCREMENT COMMENT '风险/问题ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `task_id` bigint DEFAULT NULL COMMENT '关联WBS任务ID',
  `risk_type` varchar(10) DEFAULT '1' COMMENT '类型（1风险 2问题）',
  `engineering_category` varchar(20) DEFAULT NULL COMMENT '工程类别(pjm_engineering_category)',
  `risk_point_id` bigint DEFAULT NULL COMMENT '关联国家标准风险点ID(biz_safety_risk_point)',
  `risk_std_code` varchar(128) DEFAULT NULL COMMENT '国家标准/规范条款号(GB xxxx-xxxx)',
  `title` varchar(200) NOT NULL COMMENT '标题',
  `level` varchar(10) DEFAULT '2' COMMENT '等级（1低 2中 3高 4紧急）',
  `status` varchar(10) DEFAULT '0' COMMENT '状态（0开放 1处理中 2已解决 3已关闭）',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人ID',
  `owner_name` varchar(64) DEFAULT NULL COMMENT '负责人姓名',
  `occur_date` date DEFAULT NULL COMMENT '识别/发生日期',
  `resolve_date` date DEFAULT NULL COMMENT '解决日期',
  `solution` varchar(1000) DEFAULT NULL COMMENT '应对措施',
  `description` varchar(1000) DEFAULT NULL COMMENT '描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`risk_id`),
  KEY `idx_project` (`project_id`),
  KEY `idx_task` (`task_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6007 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目风险问题登记表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_milestone` (
  `milestone_id` bigint NOT NULL AUTO_INCREMENT COMMENT '里程碑ID',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `milestone_name` varchar(200) NOT NULL COMMENT '里程碑名称',
  `plan_date` date DEFAULT NULL COMMENT '计划日期',
  `actual_date` date DEFAULT NULL COMMENT '实际达成日期',
  `status` varchar(10) DEFAULT '0' COMMENT '状态（0未达成 1已达成 2延期）',
  `owner_id` bigint DEFAULT NULL COMMENT '负责人用户ID(sys_user.user_id)',
  `owner_name` varchar(64) DEFAULT NULL COMMENT '负责人',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`milestone_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=5012 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目里程碑表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_change` (
  `change_id` bigint NOT NULL AUTO_INCREMENT,
  `change_no` varchar(50) DEFAULT NULL,
  `project_id` bigint NOT NULL,
  `change_type` varchar(20) DEFAULT '1' COMMENT '1范围 2进度 3成本 4技术',
  `title` varchar(200) NOT NULL,
  `reason` text,
  `impact_analysis` text,
  `schedule_impact` int DEFAULT '0' COMMENT '影响工期(天)',
  `cost_impact` decimal(18,2) DEFAULT '0.00' COMMENT '影响金额',
  `status` varchar(20) DEFAULT '0' COMMENT '0草稿 1待审 2通过 3驳回 4实施',
  `requester_id` bigint DEFAULT NULL,
  `requester_name` varchar(64) DEFAULT NULL,
  `audit_by` varchar(64) DEFAULT NULL,
  `audit_time` datetime DEFAULT NULL,
  `audit_opinion` varchar(500) DEFAULT NULL,
  `del_flag` char(1) DEFAULT '0',
  `create_by` varchar(64) DEFAULT '',
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) DEFAULT '',
  `update_time` datetime DEFAULT NULL,
  `remark` varchar(500) DEFAULT NULL,
  PRIMARY KEY (`change_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=3004 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目变更请求表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pjm_payment` (
  `payment_id` bigint NOT NULL AUTO_INCREMENT COMMENT '收款ID',
  `payment_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '收款单号',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `period_no` int DEFAULT NULL COMMENT '期次',
  `amount` decimal(16,2) NOT NULL COMMENT '收款金额(元)',
  `payment_date` date NOT NULL COMMENT '收款日期',
  `payer` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '付款方',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`payment_id`),
  KEY `idx_project` (`project_id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='项目收款记录';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



-- §3 PJM 采购耗用登记建表

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pms_consumption` (
  `consumption_id` bigint NOT NULL AUTO_INCREMENT COMMENT '耗用ID',
  `consumption_no` varchar(64) COLLATE utf8mb4_general_ci NOT NULL COMMENT '耗用单号',
  `order_id` bigint DEFAULT NULL COMMENT '采购订单ID',
  `order_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '采购订单号',
  `receive_id` bigint DEFAULT NULL COMMENT '收货单ID',
  `receive_no` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '收货单号',
  `supplier_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '供应商',
  `total_qty` decimal(14,2) DEFAULT '0.00' COMMENT '耗用总数量',
  `total_amount` decimal(16,2) DEFAULT '0.00' COMMENT '耗用总金额',
  `dept_id` bigint DEFAULT NULL COMMENT '使用部门ID',
  `dept_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '使用部门',
  `user_id` bigint DEFAULT NULL COMMENT '领用人ID',
  `user_name` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '领用人',
  `use_date` date DEFAULT NULL COMMENT '使用日期',
  `usage_purpose` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '用途说明',
  `status` char(1) COLLATE utf8mb4_general_ci DEFAULT '0' COMMENT '状态(0待登记 1已登记)',
  `remark` varchar(500) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0',
  `create_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `create_time` datetime DEFAULT NULL,
  `update_by` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`consumption_id`),
  KEY `idx_order` (`order_id`),
  KEY `idx_receive` (`receive_id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='零散采购耗用登记';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE IF NOT EXISTS `pms_consumption_detail` (
  `detail_id` bigint NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `consumption_id` bigint NOT NULL COMMENT '耗用ID',
  `material_id` bigint DEFAULT NULL COMMENT '物料ID',
  `material_code` varchar(64) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料编码',
  `material_name` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '物料名称',
  `spec_model` varchar(128) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '规格型号',
  `unit` varchar(32) COLLATE utf8mb4_general_ci DEFAULT NULL COMMENT '单位',
  `qty` decimal(14,2) DEFAULT '0.00' COMMENT '数量',
  `unit_price` decimal(16,4) DEFAULT NULL COMMENT '单价',
  `amount` decimal(16,2) DEFAULT '0.00' COMMENT '金额',
  `del_flag` char(1) COLLATE utf8mb4_general_ci DEFAULT '0',
  PRIMARY KEY (`detail_id`),
  KEY `idx_consumption` (`consumption_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='零散采购耗用明细';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;



-- §4 车辆台账建表+字典+菜单（同事原样，幂等）
-- ============================================================
-- 增量补丁：车辆台账 + 字典化改造（2026-11-04）
-- 适用：已部署本系统基线版本，新增加载车辆台账、项目资料、采购明细类型等功能
-- 说明：脚本幂等，可重复执行；不写死主键，避免与生产库主键冲突
-- 执行：mysql -uroot -p ry-vue < patch_20261104.sql
-- ============================================================

-- ---------- 1. 车辆台账表 ----------
CREATE TABLE IF NOT EXISTS `biz_safety_vehicle` (
  `vehicle_id` bigint NOT NULL AUTO_INCREMENT COMMENT '车辆ID',
  `vehicle_type` char(1) NOT NULL DEFAULT '1' COMMENT '车辆类型(1特种车辆 2公用车辆)',
  `vehicle_code` varchar(64) DEFAULT NULL COMMENT '车辆编号',
  `plate_no` varchar(32) NOT NULL COMMENT '车牌号',
  `vehicle_model` varchar(128) DEFAULT NULL COMMENT '品牌型号',
  `vehicle_use` varchar(64) DEFAULT NULL COMMENT '车辆用途(如叉车/起重机/罐车/通勤/公务)',
  `special_sub_type` varchar(64) DEFAULT NULL COMMENT '特种车辆子类(如叉车/桥式起重机/压力容器车)',
  `operator_name` varchar(64) DEFAULT NULL COMMENT '驾驶员/操作员',
  `operator_phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `license_no` varchar(64) DEFAULT NULL COMMENT '驾驶证/操作证号',
  `cert_expire` date DEFAULT NULL COMMENT '操作证有效期',
  `inspect_expire` date DEFAULT NULL COMMENT '检验/年检有效期',
  `insurance_expire` date DEFAULT NULL COMMENT '保险有效期',
  `enterprise_id` bigint DEFAULT NULL COMMENT '所属企业ID',
  `enterprise_name` varchar(128) DEFAULT NULL COMMENT '所属企业名称(冗余)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0在用 1停用 2报废)',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`vehicle_id`),
  UNIQUE KEY `uk_plate_no` (`plate_no`),
  KEY `idx_enterprise` (`enterprise_id`),
  KEY `idx_type` (`vehicle_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='车辆台账表(特种车辆/公用车辆)';

-- ---------- 2. 字典类型 ----------
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目附件类型', 'pjm_project_attachment_type', '0', '项目立项附件类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_project_attachment_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '采购明细类型', 'pjm_purchase_detail_type', '0', '项目采购执行-明细分类' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_purchase_detail_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆类型', 'safety_vehicle_type', '0', '一企一档-特种车辆/公用车辆分类' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '特种车辆子类', 'safety_special_vehicle_sub', '0', '特种车辆细分类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_special_vehicle_sub');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆状态', 'safety_vehicle_status', '0', '车辆在用状态' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_status');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '车辆所属企业', 'safety_vehicle_enterprise', '0', '车辆台账所属企业下拉，可在字典管理维护' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'safety_vehicle_enterprise');

-- ---------- 3. 字典数据（逐条判重，支持基线已有 4 项时补齐到 10 项） ----------
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'招标文件','1','pjm_project_attachment_type','','primary','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'投标文件','2','pjm_project_attachment_type','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'项目图纸','3','pjm_project_attachment_type','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'项目保险','4','pjm_project_attachment_type','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='4');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'合同协议','5','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='5');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 6,'技术方案','6','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='6');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'会议纪要','7','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='7');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 8,'检查报告','8','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='8');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 9,'影像照片','9','pjm_project_attachment_type','','primary','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='9');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 10,'其他资料','10','pjm_project_attachment_type','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_project_attachment_type' AND `dict_value`='10');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'物料明细','1','pjm_purchase_detail_type','','primary','Y','0','走仓库库存/采购申请' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'劳务外包','2','pjm_purchase_detail_type','','warning','N','0','人工外包费用' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'机械设备','3','pjm_purchase_detail_type','','success','N','0','机械租赁/设备采购' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'其他','4','pjm_purchase_detail_type','','info','N','0','其他费用' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_purchase_detail_type' AND `dict_value`='4');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'特种车辆','1','safety_vehicle_type','','danger','Y','0','叉车/起重机/罐车等特种设备' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'公用车辆','2','safety_vehicle_type','','primary','N','0','通勤/公务/运输等内部车辆' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_type' AND `dict_value`='2');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'叉车','1','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'桥式起重机','2','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='2');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'门式起重机','3','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='3');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 4,'压力容器车','4','safety_special_vehicle_sub','','danger','N','0','危化品运输' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='4');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 5,'罐车','5','safety_special_vehicle_sub','','danger','N','0','液体运输' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='5');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 6,'装载机','6','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='6');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 7,'观光车','7','safety_special_vehicle_sub','','warning','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='7');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 8,'其他特种设备','99','safety_special_vehicle_sub','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_special_vehicle_sub' AND `dict_value`='99');

INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'在用','0','safety_vehicle_status','','success','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'停用','1','safety_vehicle_status','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'报废','2','safety_vehicle_status','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_status' AND `dict_value`='2');

-- 车辆所属企业（dict_value = 企业档案中的企业ID，按现场实际企业补充/调整）
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'云南新材料科技有限公司','12','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='12');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'云南精细化工股份有限公司','14','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='14');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'测试安全科技有限公司','1','safety_vehicle_enterprise','','default','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='safety_vehicle_enterprise' AND `dict_value`='1');

-- ---------- 4. 菜单（父菜单 4639=一企一档，906202=项目管理，需基线已存在） ----------
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '车辆台账', 4639, 6, 'vehicle', 'safety/arch/vehicle/index', 'SafetyVehicle', 1, 0, 'C', '0', '0', 'safety:vehicle:list', 'vehicle', 'admin', NOW(), '特种车辆/公用车辆台账' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'SafetyVehicle');

INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '项目资料', 906202, 2, 'attachment', 'pjm/attachment/index', 'PjmAttachment', 1, 0, 'C', '0', '0', 'pjm:attachment:list', 'upload', 'admin', NOW(), '项目资料管理菜单' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'PjmAttachment');

-- 已存在时同步图标
UPDATE `sys_menu` SET `icon` = 'vehicle', `update_time` = NOW() WHERE `route_name` = 'SafetyVehicle';

-- 耗用登记菜单（父菜单 4441=采购管理）
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`component`,`route_name`,`is_frame`,`is_cache`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`,`remark`)
SELECT '耗用登记', 4441, 9, 'consumption', 'pms/consumption/index', 'PmsConsumption', 1, 0, 'C', '0', '0', 'pms:consumption:list', 'shopping', 'admin', NOW(), '零散采购耗用登记' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `route_name` = 'PmsConsumption');

INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用查询', m.menu_id, 1, '#', 'F', '0', '0', 'pms:consumption:query', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:query');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用新增', m.menu_id, 2, '#', 'F', '0', '0', 'pms:consumption:add', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:add');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用修改', m.menu_id, 3, '#', 'F', '0', '0', 'pms:consumption:edit', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:edit');
INSERT INTO `sys_menu` (`menu_name`,`parent_id`,`order_num`,`path`,`menu_type`,`visible`,`status`,`perms`,`icon`,`create_by`,`create_time`)
SELECT '耗用删除', m.menu_id, 4, '#', 'F', '0', '0', 'pms:consumption:remove', '#', 'admin', NOW() FROM `sys_menu` m
WHERE m.route_name='PmsConsumption' AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms`='pms:consumption:remove');

-- ---------- 5. 菜单按钮权限（若需分配给非 admin 角色，执行后到「角色管理」勾选对应菜单即可） ----------
-- 可选：INSERT INTO sys_role_menu SELECT 2, menu_id FROM sys_menu WHERE route_name IN ('SafetyVehicle','PjmAttachment','PmsConsumption');

-- §5 幂等加列：风险点工程类别 + PJM采购清单明细类型
DROP PROCEDURE IF EXISTS p_add_column_if_missing;
DELIMITER $$
CREATE PROCEDURE p_add_column_if_missing(IN p_table VARCHAR(64), IN p_col VARCHAR(64), IN p_def TEXT)
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = p_table AND column_name = p_col
  ) THEN
    SET @ddl = CONCAT('ALTER TABLE `', p_table, '` ADD COLUMN ', p_def);
    PREPARE stmt FROM @ddl;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
  END IF;
END$$
DELIMITER ;
CALL p_add_column_if_missing('biz_safety_risk_point','engineering_category','`engineering_category` varchar(20) DEFAULT NULL COMMENT ''工程类别（字典 pjm_engineering_category）''');
CALL p_add_column_if_missing('pjm_project_procurement','detail_type','`detail_type` varchar(10) NOT NULL DEFAULT ''1'' COMMENT ''明细类型：1物料明细 2劳务外包 3机械设备 4其他''');
DROP PROCEDURE IF EXISTS p_add_column_if_missing;

-- §6 菜单链（INSERT IGNORE 幂等转换）
-- ==============================================================================
-- PJS 项目管理 菜单 + 按钮权限 + 角色授权 + sys_module 注册
-- 主菜单已预先创建 (menu_id = 906202)，本脚本补全子菜单
-- ==============================================================================

-- ------------------------------
-- 二级菜单 1: 项目立项
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES (906203, '项目立项', 906202, 1, 'project', 'pjm/project/index', '', NULL, 1, 0, 'M', '0', '0', '', 'list', 'admin', sysdate(), '', NULL, '项目立项子菜单');

-- 项目立项 按钮权限 (F 类型)
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906204, '项目查询',   906203, 1, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:query', '#', 'admin', sysdate()),
(906205, '项目新增',   906203, 2, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:add',   '#', 'admin', sysdate()),
(906206, '项目修改',   906203, 3, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:edit',  '#', 'admin', sysdate()),
(906207, '项目删除',   906203, 4, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:remove','#', 'admin', sysdate()),
(906208, '项目审批',   906203, 5, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:audit', '#', 'admin', sysdate()),
(906209, '项目导出',   906203, 6, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:export','#', 'admin', sysdate()),
(906210, '发起采购',   906203, 7, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:project:purchase','#', 'admin', sysdate()),
(906211, 'WBS任务创建', 906203, 8, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:add',  '#', 'admin', sysdate()),
(906212, 'WBS任务修改', 906203, 9, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:edit', '#', 'admin', sysdate()),
(906213, 'WBS任务删除', 906203, 10, '#', NULL, '', NULL, 1, 0, 'F', '0', '0', 'pjm:task:remove','#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 2: WBS 任务 (独立目录)
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906214, 'WBS任务分解', 906202, 2, 'task', 'pjm/task/index', '', NULL, 1, 0, 'M', '0', '0', '', 'tree', 'admin', sysdate(), 'WBS任务树');

-- ------------------------------
-- 二级菜单 3: 团队成员
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906215, '项目团队', 906202, 3, 'member', 'pjm/member/index', '', NULL, 1, 0, 'M', '0', '0', '', 'peoples', 'admin', sysdate(), '团队成员管理');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906216, '成员新增', 906215, 1, '#', 'F', '0', '0', 'pjm:member:add',    '#', 'admin', sysdate()),
(906217, '成员删除', 906215, 2, '#', 'F', '0', '0', 'pjm:member:remove', '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 4: 风险登记
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906218, '风险登记', 906202, 4, 'risk', 'pjm/risk/index', '', NULL, 1, 0, 'M', '0', '0', '', 'bug', 'admin', sysdate(), '风险登记册');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906219, '风险查询', 906218, 1, '#', 'F', '0', '0', 'pjm:risk:query',   '#', 'admin', sysdate()),
(906220, '风险新增', 906218, 2, '#', 'F', '0', '0', 'pjm:risk:add',     '#', 'admin', sysdate()),
(906221, '风险修改', 906218, 3, '#', 'F', '0', '0', 'pjm:risk:edit',    '#', 'admin', sysdate()),
(906222, '风险删除', 906218, 4, '#', 'F', '0', '0', 'pjm:risk:remove',  '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 5: 变更管理
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906223, '变更管理', 906202, 5, 'change', 'pjm/change/index', '', NULL, 1, 0, 'M', '0', '0', '', 'edit', 'admin', sysdate(), '变更请求管理');

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time) VALUES
(906224, '变更查询', 906223, 1, '#', 'F', '0', '0', 'pjm:change:query',  '#', 'admin', sysdate()),
(906225, '变更新增', 906223, 2, '#', 'F', '0', '0', 'pjm:change:add',    '#', 'admin', sysdate()),
(906226, '变更修改', 906223, 3, '#', 'F', '0', '0', 'pjm:change:edit',   '#', 'admin', sysdate()),
(906227, '变更审批', 906223, 4, '#', 'F', '0', '0', 'pjm:change:audit',  '#', 'admin', sysdate()),
(906228, '变更删除', 906223, 5, '#', 'F', '0', '0', 'pjm:change:remove', '#', 'admin', sysdate());

-- ------------------------------
-- 二级菜单 6: ★ 项目采购台账 (关联 PMS)
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906229, '项目采购台账', 906202, 6, 'pms-ledger', 'pjm/pms/index', '', NULL, 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate(), '★ 聚合 PMS 全链路采购数据');

-- ------------------------------
-- 二级菜单 7: 项目看板
-- ------------------------------
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, route_name, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES (906230, '项目看板', 906202, 7, 'dashboard', 'pjm/dashboard/index', '', NULL, 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate(), '项目综合看板');

-- ------------------------------
-- sys_module 注册
-- ------------------------------
INSERT IGNORE INTO sys_module (module_name, module_key, root_menu_id, status, is_core, order_num, create_by, create_time, remark)
VALUES ('项目管理', 'pjm', 906202, '0', '0', 26, 'admin', sysdate(), '项目管理模块（业务主线）');

-- ------------------------------
-- 授权 admin 角色 (role_id=1)
-- ------------------------------
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id BETWEEN 906203 AND 906230;

-- 补项目管理顶级目录（menu_id=906202，原 dump 缺失）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark, route_name)
VALUES (906202, '项目管理', 0, 6, 'project', NULL, 'M', '0', '0', '', 'chart', 'admin', NOW(), '', NULL, '项目管理顶级目录', 'Pjm');

-- 给 admin(role_id=1) 授权
INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 906202);

-- 补子菜单 route_name（之前规范是 Pjm*）
UPDATE sys_menu SET route_name='PjmProject' WHERE menu_id=906203 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmTask' WHERE menu_id=906214 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmMember' WHERE menu_id=906215 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmChange' WHERE menu_id=906223 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmPmsLedger' WHERE menu_id=906229 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmDashboard' WHERE menu_id=906230 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmMilestone' WHERE menu_id=906216 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmPayment' WHERE menu_id=906217 AND (route_name IS NULL OR route_name='');
UPDATE sys_menu SET route_name='PjmAttachment' WHERE menu_id=906219 AND (route_name IS NULL OR route_name='');

-- 补齐 PJM 下缺失的 4 个页面菜单（里程碑、付款、附件、工时），menu_id 避开 906544（当前最大）
INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name) VALUES
(906601, '里程碑管理', 906202, 6, 'milestone', 'pjm/milestone/index', 'C', '0', '0', '', 'date',   'admin', NOW(), '项目里程碑', 'PjmMilestone'),
(906602, '项目付款',   906202, 7, 'payment',   'pjm/payment/index',   'C', '0', '0', '', 'money',  'admin', NOW(), '项目付款',   'PjmPayment'),
(906603, '项目附件',   906202, 8, 'attachment','pjm/attachment/index','C', '0', '0', '', 'upload', 'admin', NOW(), '项目附件',   'PjmAttachment'),
(906604, '工时填报',   906202, 9, 'worklog',   'pjm/worklog/index',   'C', '0', '0', '', 'edit',   'admin', NOW(), '项目工时',   'PjmWorklog');

INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 906601), (1, 906602), (1, 906603), (1, 906604);

-- 修 menu_type（带 component 的子菜单 C，不是 M）
UPDATE sys_menu SET menu_type='C' WHERE parent_id=906202 AND component IS NOT NULL AND component != '' AND menu_type='M';

-- 风险登记整个子树：之前已改为安全生产管理里的风险点管理，PJM 里删掉避免重复
-- menu_id: 906218 风险登记（页面）+ 其子 906219-906222（按钮权限）
DELETE FROM sys_role_menu WHERE menu_id IN (906218, 906219, 906220, 906221, 906222);
DELETE FROM sys_menu WHERE menu_id IN (906219, 906220, 906221, 906222);
DELETE FROM sys_menu WHERE menu_id = 906218;

-- 验证
SELECT menu_id, menu_name, parent_id, path, component, menu_type, route_name, visible FROM sys_menu WHERE menu_id=906202 OR parent_id=906202 ORDER BY parent_id, order_num;

-- §7 验证

SELECT CONCAT('VERIFY: pjm_project exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pjm_project';
SELECT CONCAT('VERIFY: pjm tables = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name LIKE 'pjm%';
SELECT CONCAT('VERIFY: biz_safety_vehicle exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='biz_safety_vehicle';
SELECT CONCAT('VERIFY: pms_consumption exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pms_consumption';
SELECT CONCAT('VERIFY: risk_point engineering_category = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='biz_safety_risk_point' AND column_name='engineering_category';
SELECT CONCAT('VERIFY: pjm_project_procurement detail_type = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pjm_project_procurement' AND column_name='detail_type';
SELECT CONCAT('VERIFY: request source_project_id = ', COUNT(*)) FROM information_schema.columns WHERE table_schema=DATABASE() AND table_name='pms_purchase_request' AND column_name='source_project_id';
SELECT CONCAT('VERIFY: PJM menus = ', COUNT(*)) FROM sys_menu WHERE route_name LIKE 'Pjm%';
SELECT CONCAT('VERIFY: vehicle menu = ', COUNT(*)) FROM sys_menu WHERE route_name='SafetyVehicle';
SELECT CONCAT('VERIFY: consumption menu = ', COUNT(*)) FROM sys_menu WHERE route_name='PmsConsumption';



-- =============================================================
-- 第 5 段 / 共 42 段：upgrade_20261107_pjm_invoice.sql
-- =============================================================

-- ============================================================
-- 升级脚本：PJM 项目管理 - 开票管理功能
-- 日期：2026-11-07
-- 背景：客户提出在项目回款基础上增加开票管理，日常经常查开票信息。
--       开票与回款为弱关联（payment_id 可空），无审批流，
--       状态流转：待开票(0) -> 已开票(1) -> 已作废(2)。
--       发票附件必须上传（统一 FileUpload 组件，多个附件逗号分隔 url 存储）。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，幂等可重复执行）
-- ============================================================
SET NAMES utf8mb4;

-- ============================================================
-- §1 建表 pjm_invoice（幂等：已存在则跳过）
-- ============================================================
CREATE TABLE IF NOT EXISTS `pjm_invoice` (
  `invoice_id` bigint NOT NULL AUTO_INCREMENT COMMENT '开票ID',
  `invoice_no` varchar(50) NOT NULL COMMENT '开票单号(KP前缀)',
  `project_id` bigint NOT NULL COMMENT '项目ID',
  `payment_id` bigint DEFAULT NULL COMMENT '关联回款ID(弱关联,可空)',
  `invoice_type` char(1) DEFAULT '0' COMMENT '发票类型(字典 pjm_invoice_type)',
  `invoice_status` char(1) DEFAULT '0' COMMENT '发票状态(字典 pjm_invoice_status: 0待开票 1已开票 2已作废)',
  `invoice_amount` decimal(18,2) NOT NULL COMMENT '开票金额',
  `tax_rate` decimal(5,2) DEFAULT NULL COMMENT '税率(%)',
  `tax_amount` decimal(18,2) DEFAULT NULL COMMENT '税额',
  `invoice_title` varchar(200) DEFAULT NULL COMMENT '发票抬头',
  `tax_no` varchar(50) DEFAULT NULL COMMENT '纳税人识别号',
  `invoice_date` date DEFAULT NULL COMMENT '开票日期',
  `invoice_attachment` varchar(1000) DEFAULT NULL COMMENT '发票附件(多个url逗号分隔,必须上传)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志(0存在 2删除)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`invoice_id`),
  KEY `idx_pjm_invoice_project` (`project_id`),
  KEY `idx_pjm_invoice_payment` (`payment_id`),
  KEY `idx_pjm_invoice_status` (`invoice_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目开票记录表';

-- ============================================================
-- §2 编号规则种子（幂等：已存在则跳过）
-- 单号规则与生成器调用约定一致：
--   PjmInvoiceServiceImpl -> PJM_INVOICE
-- ============================================================

-- 回滚语句（如需移除本规则时执行）：
-- DELETE FROM mk_number_rule WHERE rule_code = 'PJM_INVOICE';

-- 开票单编号（每日重置，如 KP202611070001）
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'PJM_INVOICE', 'pjm', '开票单编号', 'KP', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '开票单编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='PJM_INVOICE');

-- ============================================================
-- §3 字典类型与字典数据（幂等：已存在则跳过）
-- ============================================================

-- 回滚语句（如需移除字典时执行）：
-- DELETE FROM sys_dict_data WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');
-- DELETE FROM sys_dict_type WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');

-- 字典类型
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目发票类型', 'pjm_invoice_type', '0', '项目管理-开票管理-发票类型' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_invoice_type');

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `remark`)
SELECT '项目发票状态', 'pjm_invoice_status', '0', '项目管理-开票管理-发票状态' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_invoice_status');

-- 字典数据：发票类型
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'增值税专用发票','0','pjm_invoice_type','','primary','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'增值税普通发票','1','pjm_invoice_type','','info','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'电子发票','2','pjm_invoice_type','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_type' AND `dict_value`='2');

-- 字典数据：发票状态
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 1,'待开票','0','pjm_invoice_status','','warning','Y','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='0');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 2,'已开票','1','pjm_invoice_status','','success','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='1');
INSERT INTO `sys_dict_data` (`dict_sort`,`dict_label`,`dict_value`,`dict_type`,`css_class`,`list_class`,`is_default`,`status`,`remark`)
SELECT 3,'已作废','2','pjm_invoice_status','','danger','N','0','' FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_invoice_status' AND `dict_value`='2');

-- ============================================================
-- §4 菜单（幂等：固定 menu_id + INSERT IGNORE）
-- 开票管理挂在项目管理(906202)下，与回款管理(项目付款 906602)平级
-- menu_id 避开 906604（已用），启用 906605
-- ============================================================

-- 回滚语句（如需移除菜单时执行）：
-- DELETE FROM sys_role_menu WHERE menu_id = 906605 OR parent_id = 906605;
-- DELETE FROM sys_menu WHERE menu_id = 906605 OR parent_id = 906605;

INSERT IGNORE INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, menu_type, visible, status, perms, icon, create_by, create_time, remark, route_name) VALUES
(906605, '开票管理', 906202, 10, 'invoice', 'pjm/invoice/index', 'C', '0', '0', '', 'ticket', 'admin', NOW(), '项目开票', 'PjmInvoice');

SET @invoiceMenuId = 906605;

-- 按钮权限（F 类型，menu_id 自增，逐条判重防止重复）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票查询', @invoiceMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:query', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:query' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票登记', @invoiceMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:add', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:add' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票修改', @invoiceMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:edit', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:edit' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票删除', @invoiceMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:remove', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:remove' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票确认', @invoiceMenuId, 5, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:issue', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:issue' AND parent_id = @invoiceMenuId);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '开票作废', @invoiceMenuId, 6, '#', '', 1, 0, 'F', '0', '0', 'pjm:invoice:void', '#', 'admin', NOW(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:invoice:void' AND parent_id = @invoiceMenuId);

-- 给 admin(role_id=1) 授权整个开票管理子树
INSERT IGNORE INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_id = 906605 OR parent_id = 906605;

-- ============================================================
-- §5 验证
-- ============================================================
SELECT CONCAT('VERIFY: pjm_invoice exists = ', COUNT(*)) FROM information_schema.tables WHERE table_schema=DATABASE() AND table_name='pjm_invoice';
SELECT CONCAT('VERIFY: PJM_INVOICE rule = ', COUNT(*)) FROM mk_number_rule WHERE rule_code='PJM_INVOICE';
SELECT CONCAT('VERIFY: pjm invoice dicts = ', COUNT(*)) FROM sys_dict_type WHERE dict_type IN ('pjm_invoice_type','pjm_invoice_status');
SELECT CONCAT('VERIFY: invoice menu = ', COUNT(*)) FROM sys_menu WHERE menu_id = 906605;
SELECT CONCAT('VERIFY: invoice buttons = ', COUNT(*)) FROM sys_menu WHERE parent_id = 906605;



-- =============================================================
-- 第 6 段 / 共 42 段：upgrade_20261107_pjm_menu_order.sql
-- =============================================================

-- ============================================================
-- 升级脚本：项目管理菜单调整（隐藏重复菜单 + 报表类移至最后）
-- 日期：2026-11-07
-- 背景：
--   1) "项目附件"(906603) 与 "项目资料"(9062239) 完全重复（path 均 attachment、component 均 pjm/attachment/index），
--      同一页面挂了两个菜单——隐藏"项目附件"（visible='1'，不删除，角色授权数据保留可随时恢复）。
--   2) "项目采购台账"(906229) 与 "项目看板"(906230) 属报表类，应排业务菜单之后；
--      原有 order_num 存在重复（2/2、6/6、7/7），一并整理为唯一递增。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- 注意：RuoYi getRouters 实时查库，刷新页面即生效，无需重启。
-- ============================================================

-- 回滚语句（如需还原时执行）：
-- UPDATE sys_menu SET visible='0' WHERE menu_id=906603;
-- UPDATE sys_menu SET order_num=1  WHERE menu_id=906203;  -- 项目立项
-- UPDATE sys_menu SET order_num=2  WHERE menu_id=906214;  -- WBS任务分解
-- UPDATE sys_menu SET order_num=2  WHERE menu_id=9062239; -- 项目资料
-- UPDATE sys_menu SET order_num=3  WHERE menu_id=906215;  -- 项目团队
-- UPDATE sys_menu SET order_num=5  WHERE menu_id=906223;  -- 变更管理
-- UPDATE sys_menu SET order_num=6  WHERE menu_id=906229;  -- 项目采购台账
-- UPDATE sys_menu SET order_num=6  WHERE menu_id=906601;  -- 里程碑管理
-- UPDATE sys_menu SET order_num=7  WHERE menu_id=906230;  -- 项目看板
-- UPDATE sys_menu SET order_num=7  WHERE menu_id=906602;  -- 项目付款
-- UPDATE sys_menu SET order_num=8  WHERE menu_id=906603;  -- 项目附件
-- UPDATE sys_menu SET order_num=9  WHERE menu_id=906604;  -- 工时填报
-- UPDATE sys_menu SET menu_name='项目付款' WHERE menu_id=906602;  -- 项目回款（如需还原名称）

-- 1) 隐藏重复菜单"项目附件"（与"项目资料"同一页面 pjm/attachment/index）
UPDATE sys_menu SET visible='1' WHERE menu_id=906603;

-- 1.1) 菜单更名："项目付款" → "项目回款"（收款明细页面口径统一为"回款"）
UPDATE sys_menu SET menu_name='项目回款' WHERE menu_id=906602;

-- 2) 业务菜单在前（按原相对次序，消除 order_num 重复）
UPDATE sys_menu SET order_num=1  WHERE menu_id=906203;   -- 项目立项
UPDATE sys_menu SET order_num=2  WHERE menu_id=906214;   -- WBS任务分解
UPDATE sys_menu SET order_num=3  WHERE menu_id=9062239;  -- 项目资料
UPDATE sys_menu SET order_num=4  WHERE menu_id=906215;   -- 项目团队
UPDATE sys_menu SET order_num=5  WHERE menu_id=906223;   -- 变更管理
UPDATE sys_menu SET order_num=6  WHERE menu_id=906601;   -- 里程碑管理
UPDATE sys_menu SET order_num=7  WHERE menu_id=906602;   -- 项目付款
UPDATE sys_menu SET order_num=8  WHERE menu_id=906604;   -- 工时填报

-- 3) 报表类移至最后
UPDATE sys_menu SET order_num=9  WHERE menu_id=906229;   -- 项目采购台账（报表）
UPDATE sys_menu SET order_num=10 WHERE menu_id=906230;   -- 项目看板（报表）
UPDATE sys_menu SET order_num=11 WHERE menu_id=906603;   -- 项目附件（已隐藏）



-- =============================================================
-- 第 7 段 / 共 42 段：upgrade_20261107_pjm_payment_voucher.sql
-- =============================================================

-- ============================================================
-- 升级脚本：pjm_payment 回款凭证字段
-- 日期：2026-11-07
-- 背景：登记回款页面增加"回款凭证"附件上传（统一 FileUpload 组件，多个凭证以逗号分隔 url 存储），
--       后端 PjmPayment 实体与 PjmPaymentMapper.xml 同步增加 voucher_urls 字段。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- ============================================================

-- 回滚语句（如需移除本字段时执行，注意先确认无业务数据依赖）：
-- ALTER TABLE pjm_payment DROP COLUMN voucher_urls;

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='pjm_payment' AND column_name='voucher_urls');
SET @ddl = IF(@has_col=0,
    'ALTER TABLE pjm_payment ADD COLUMN voucher_urls varchar(1000) DEFAULT NULL COMMENT ''回款凭证(多个url逗号分隔)'' AFTER payer',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;



-- =============================================================
-- 第 8 段 / 共 42 段：upgrade_20261107_pjm_project_columns.sql
-- =============================================================

-- ============================================================
-- 升级脚本：pjm_project 质量标准/单位类列扩容
-- 日期：2026-11-07
-- 背景：AI 解析填入的质量标准为完整标准句（如"符合《建筑工程施工质量验收统一标准》（GB50300-2013）…"），
--       原 quality_standard varchar(50) 超长导致保存失败（Data too long for column 'quality_standard'）；
--       单位类字段（建设单位/施工单位/监理单位/设计单位）全称含括号说明时也可能接近 100 上限，一并扩容。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- 注意：列上无索引，MODIFY 为无损扩容；重复执行无副作用。
-- ============================================================

-- 回滚语句（如需还原旧长度时执行，注意先确认现有数据不超过旧长度，否则回滚会失败）：
-- ALTER TABLE pjm_project MODIFY COLUMN quality_standard varchar(50) DEFAULT NULL COMMENT '质量标准';
-- ALTER TABLE pjm_project MODIFY COLUMN owner_unit varchar(100) DEFAULT NULL COMMENT '建设单位';
-- ALTER TABLE pjm_project MODIFY COLUMN build_unit varchar(100) DEFAULT NULL COMMENT '施工单位';
-- ALTER TABLE pjm_project MODIFY COLUMN supervisor_unit varchar(100) DEFAULT NULL COMMENT '监理单位';
-- ALTER TABLE pjm_project MODIFY COLUMN design_unit varchar(100) DEFAULT NULL COMMENT '设计单位';

-- 质量标准：50 -> 200（保存完整标准句）
ALTER TABLE pjm_project MODIFY COLUMN quality_standard varchar(200) DEFAULT NULL COMMENT '质量标准';

-- 建设单位/施工单位/监理单位/设计单位：100 -> 200（容纳单位全称）
ALTER TABLE pjm_project MODIFY COLUMN owner_unit varchar(200) DEFAULT NULL COMMENT '建设单位';
ALTER TABLE pjm_project MODIFY COLUMN build_unit varchar(200) DEFAULT NULL COMMENT '施工单位';
ALTER TABLE pjm_project MODIFY COLUMN supervisor_unit varchar(200) DEFAULT NULL COMMENT '监理单位';
ALTER TABLE pjm_project MODIFY COLUMN design_unit varchar(200) DEFAULT NULL COMMENT '设计单位';



-- =============================================================
-- 第 9 段 / 共 42 段：upgrade_20261108_pjm_procurement_material_type.sql
-- =============================================================

-- ============================================================
-- 增量升级脚本：项目采购清单物料类型字段
-- 日期：2026-11-08
-- 主题：pjm_project_procurement 增加 material_type 列（物料类型）
-- 说明：采购清单增加物料类型字段（wms_material_type 字典值：
--       0原材料 1半成品 2成品 3辅料 4服务 5工程），
--       采购清单页签按物料类型动态分类统计；
--       采购执行弹框出库仓库改为系统自动分配（单仓足量优先，不足跨仓拆分）。
-- 幂等：重复执行前请先核对列是否已存在（MySQL 8 可用 IF NOT EXISTS 语法时可直接执行）。
-- ============================================================

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='pjm_project_procurement' AND column_name='material_type');
SET @ddl = IF(@has_col=0,
    'ALTER TABLE pjm_project_procurement ADD COLUMN material_type VARCHAR(10) DEFAULT NULL COMMENT ''物料类型（wms_material_type 字典值：0原材料 1半成品 2成品 3辅料 4服务 5工程）'' AFTER detail_type',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;



-- =============================================================
-- 第 10 段 / 共 42 段：upgrade_20261108_pjm_worklog_attachment.sql
-- =============================================================

-- ============================================================
-- 升级脚本：pjm_task_worklog 增加附件字段
-- 日期：2026-11-08
-- 背景：项目管理-工时填报页面增加"附件"上传（统一 FileUpload 组件，多个附件以逗号分隔 url 存储），
--       后端 PjmTaskWorklog 实体与 PjmTaskWorklogMapper.xml 同步增加 attachment 字段。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- ============================================================

-- 回滚语句（如需移除本字段时执行，注意先确认无业务数据依赖）：
-- ALTER TABLE pjm_task_worklog DROP COLUMN attachment;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pjm_task_worklog'
    AND COLUMN_NAME = 'attachment'
);

SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE pjm_task_worklog ADD COLUMN attachment varchar(1000) DEFAULT NULL COMMENT ''工时附件(多个url逗号分隔)'' AFTER work_content',
  'SELECT ''pjm_task_worklog.attachment 已存在，跳过'' AS msg');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;



-- =============================================================
-- 第 11 段 / 共 42 段：upgrade_20261109_mcp_new_tools_perms.sql
-- =============================================================

-- ============================================================
-- 增量升级脚本：MCP 网关新增 4 个模块查询工具的权限授权
-- 日期：2026-11-09
-- 主题：mcp-gateway lyjs 分支新增 query_projects / query_safety_hazards /
--       query_quality_tasks / query_bid_projects 四个工具的接口权限
-- 前置：先执行 sql/edop_mcp_service_account.sql（创建 edop-mcp 账号与
--       edop_mcp_readonly 角色并授权标准 5 工具权限点）
-- 幂等：全部 NOT EXISTS 保护，可重复执行
-- ============================================================

-- 1. 项目管理列表权限菜单（pjm 模块原本无 pjm:project:list 权限字符）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '项目列表查询', 906203, 1, '', NULL, NULL, NULL, 1, 0, 'F', '0', '0', 'pjm:project:list', '#', 'admin', NOW(), 'MCP 只读服务账号权限（LYJS 分支）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'pjm:project:list');

-- 2. 服务账号角色授权 4 个新工具接口权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND m.perms IN ('pjm:project:list', 'safety:hazard:list', 'qms:task:list', 'bid:project:list')
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- 3. 验证（应返回 4 行）
SELECT m.perms, r.role_key
FROM sys_role_menu rm
JOIN sys_role r ON r.role_id = rm.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key = 'edop_mcp_readonly'
  AND m.perms IN ('pjm:project:list', 'safety:hazard:list', 'qms:task:list', 'bid:project:list')
ORDER BY m.perms;

-- 4. 逐用户模式业务角色授权（方案 A，2026-11-09 用户确认）
--    说明：网关 HTTP 部署走逐用户鉴权（grant_token 换发用户 JWT，@DataScope 按人裁剪），
--    服务账号权限仅覆盖 stdio/service 模式；实际使用网关查询的用户角色需持有对应权限点。
--    4.1 项目管理查询 → 管理类 4 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key IN ('common_user', 'dept_leader', 'business_manager', 'general_manager')
  AND r.del_flag = '0' AND r.status = '0'
  AND m.perms = 'pjm:project:list'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

--    4.2 投标管理查询 → 采购类 5 角色
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r, sys_menu m
WHERE r.role_key IN ('pms_director', 'pms_manager', 'pms_buyer', 'pms_receiver', 'pms_supplier_mgr')
  AND r.del_flag = '0' AND r.status = '0'
  AND m.perms = 'bid:project:list'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

--    4.3 验证（应返回 9 行：4 个管理类角色持 pjm:project:list，5 个采购类角色持 bid:project:list）
SELECT r.role_key, GROUP_CONCAT(m.perms ORDER BY m.perms) AS granted
FROM sys_role r
JOIN sys_role_menu rm ON rm.role_id = r.role_id
JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE r.role_key IN ('common_user', 'dept_leader', 'business_manager', 'general_manager',
                     'pms_director', 'pms_manager', 'pms_buyer', 'pms_receiver', 'pms_supplier_mgr')
  AND m.perms IN ('pjm:project:list', 'bid:project:list')
GROUP BY r.role_key
ORDER BY r.role_key;



-- =============================================================
-- 第 12 段 / 共 42 段：bi_all.sql
-- =============================================================

-- =============================================
-- BI 决策支持子系统 - 全量SQL脚本 (阶段一)
-- 包含：4张业务表 + 指标字典初始数据(22个指标) + 菜单权限
-- 依据：doc/plans/PRD-决策支持模块-功能需求方案.md v1.2
-- 规范：统一开发规范 3.1/3.4/3.5/7（显式 COLLATE、M/C/F 三级菜单、统计类数据权限豁免声明）
-- =============================================

-- =============================================
-- 一、业务表 DDL
-- 数据权限声明（规范 3.5.1）：以下 4 张表均属「统计/追溯类」，豁免归属字段自动填充；
-- 其中 bi_metric_snapshot 携带 dept_id 作为统计维度（非归属），驾驶舱按用户数据权限过滤该维度。
-- =============================================

-- 1. 指标字典表
CREATE TABLE IF NOT EXISTS bi_metric_dict (
    id              bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code     varchar(50)  NOT NULL COMMENT '指标编码（唯一）',
    metric_name     varchar(100) NOT NULL COMMENT '指标名称',
    domain          varchar(20)  NOT NULL COMMENT '所属域（sales销售/bid投标/production生产/quality质量/purchase采购/inventory库存/equipment设备/safety安全/hr人力/synthesis综合）',
    caliber_desc    varchar(500) NOT NULL COMMENT '口径定义（文字描述，最终解释以本字段为准）',
    calc_ref        varchar(100) DEFAULT NULL COMMENT '计算实现引用（如 MkStatsProvider）',
    refresh_type    varchar(20)  NOT NULL DEFAULT 'snapshot' COMMENT '刷新方式（snapshot快照/realtime实时）',
    freq            varchar(10)  NOT NULL DEFAULT 'day' COMMENT '统计频率（day日/month月）',
    threshold_red    decimal(18,4) DEFAULT NULL COMMENT '红线阈值（P1预警启用）',
    threshold_yellow decimal(18,4) DEFAULT NULL COMMENT '黄线阈值（P1预警启用）',
    threshold_dir   varchar(10)  DEFAULT NULL COMMENT '阈值方向（lte低于预警/gte高于预警）',
    owner           varchar(50)  DEFAULT NULL COMMENT '指标责任人',
    status          char(1)      NOT NULL DEFAULT '0' COMMENT '状态（0正常 1停用）',
    del_flag        char(1)      DEFAULT '0' COMMENT '删除标志（0存在 2删除）',
    create_by       varchar(64)  DEFAULT '' COMMENT '创建者',
    create_time     datetime     DEFAULT NULL COMMENT '创建时间',
    update_by       varchar(64)  DEFAULT '' COMMENT '更新者',
    update_time     datetime     DEFAULT NULL COMMENT '更新时间',
    remark          varchar(500) DEFAULT NULL COMMENT '备注',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典';

-- 2. 指标字典变更留痕表（F1.1：谁改的口径、何时生效）
CREATE TABLE IF NOT EXISTS bi_metric_dict_log (
    id          bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code varchar(50) NOT NULL COMMENT '指标编码',
    change_type varchar(20) NOT NULL COMMENT '变更类型（create新增/update修改/disable停用）',
    field_name  varchar(50) DEFAULT NULL COMMENT '变更字段',
    old_value   varchar(500) DEFAULT NULL COMMENT '变更前内容',
    new_value   varchar(500) DEFAULT NULL COMMENT '变更后内容',
    change_by   varchar(64) DEFAULT '' COMMENT '变更人',
    change_time datetime    DEFAULT NULL COMMENT '变更时间',
    PRIMARY KEY (id),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标字典变更留痕';

-- 3. 指标日快照表（F1.3：Quartz 每日 01:00 幂等写入，同日重跑先删后插）
CREATE TABLE IF NOT EXISTS bi_metric_snapshot (
    id            bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code   varchar(50) NOT NULL COMMENT '指标编码',
    metric_date   date        NOT NULL COMMENT '指标日期（快照粒度：日）',
    dept_id       bigint      NOT NULL DEFAULT 0 COMMENT '部门维度（sys_dept.dept_id；0=全部门汇总，避免 NULL 使唯一键失效）。统计维度列，非归属字段',
    metric_value  decimal(18,4) DEFAULT NULL COMMENT '指标值（无数据为 null，前端展示 —）',
    unit          varchar(20) DEFAULT NULL COMMENT '单位（元/个/%/小时）',
    data_flag     varchar(20) DEFAULT 'real' COMMENT '数据标记（real实测/estimate估算，如 DMS_OEE）',
    snapshot_time datetime    DEFAULT NULL COMMENT '快照时间',
    PRIMARY KEY (id),
    UNIQUE KEY uk_metric (metric_code, metric_date, dept_id),
    KEY idx_metric_date (metric_date),
    KEY idx_dept_id (dept_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标日快照';

-- 4. 指标对账日志表（F1.5：每日快照后比对实时值与快照值）
CREATE TABLE IF NOT EXISTS bi_metric_check_log (
    id             bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
    metric_code    varchar(50) NOT NULL COMMENT '指标编码',
    check_date     date        NOT NULL COMMENT '对账日期',
    dept_id        bigint      DEFAULT NULL COMMENT '部门维度',
    snapshot_value decimal(18,4) DEFAULT NULL COMMENT '快照值',
    realtime_value decimal(18,4) DEFAULT NULL COMMENT '实时值',
    diff_value     decimal(18,4) DEFAULT NULL COMMENT '差异值',
    check_status   char(1)     NOT NULL DEFAULT '0' COMMENT '对账结果（0一致 1不一致）',
    create_time    datetime    DEFAULT NULL COMMENT '记录时间',
    PRIMARY KEY (id),
    KEY idx_check_date (check_date),
    KEY idx_metric_code (metric_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='决策支持-指标对账日志';

-- =============================================
-- 二、指标字典初始数据（22个，口径依据 PRD v1.2 §7 定稿）
-- 使用 WHERE NOT EXISTS 避免重复执行插入
-- =============================================

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_SIG_AMT','月签约额','sales','当月生效合同金额合计','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_SIG_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_RCPT_AMT','月回款额','sales','当月回款登记金额合计（mk_payment_plan 实际回款金额）','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_RCPT_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_WIN_RATE','商机赢单率','sales','赢单商机数 / 关闭商机数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MK_LEAD_CVR','线索转化率','sales','成交线索 / 线索总数','MkStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MK_LEAD_CVR');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'BID_WIN_RATE','中标率','bid','中标项目 / 参与项目','BidStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='BID_WIN_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_WO_RATE','工单完成率','production','已完工工单 / 应完工工单（扣除取消）','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_WO_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'MMS_EXCEPTION','生产异常单数','production','当日新增异常工单数','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='MMS_EXCEPTION');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_PASS_RATE','一次检验合格率','quality','一次合格数 / 已完成检验数（复用 QMS 看板 passRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_PASS_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_NCR_OPEN','未关闭NCR数','quality','状态≠关闭的 NCR 数量','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_NCR_OPEN');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'QMS_CAPA_RATE','CAPA按期关闭率','quality','按期关闭的 CAPA / 应关闭 CAPA（复用 QMS 看板 capaCloseRate 口径）','QmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='QMS_CAPA_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_ONTIME_RATE','到货及时率','purchase','按合同交期判定：按期到货订单行 / 总订单行（订单行粒度）【已确认 v1】','PmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_ONTIME_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'PMS_TOP_SUP','供应商集中度','purchase','Top5 供应商采购额占比','PmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='PMS_TOP_SUP');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_TURNOVER','库存周转率','inventory','出库成本（按入库单价取价）/ 平均库存金额【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_TURNOVER');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_ALERT_CNT','库存预警数','inventory','低库存+积压+临期+过期预警数量合计（复用 WMS 统一预警模型）','WmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_ALERT_CNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'WMS_SLOW_AMT','呆滞库存金额','inventory','超 90 天无出库记录的库存金额（不含委外/在制）【已确认 v1】','WmsStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='WMS_SLOW_AMT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_OEE','设备综合效率','equipment','估算口径（日运行16h假设），data_flag=estimate，接入实测采集后切换【已确认 v1：接受估算上线】。数据源 mms_oee_snapshot 表归属 MMS 模块，由 MmsStatsProvider 提供','MmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_OEE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'DMS_DOWN_HRS','故障停机时长','equipment','当月故障工单停机时长合计','DmsStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='DMS_DOWN_HRS');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'SAFETY_RECT_RATE','隐患整改率','safety','已整改隐患 / 累计隐患','SafetyStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='SAFETY_RECT_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_HEADCOUNT','在册人数','hr','在职状态员工数','HrStatsProvider','snapshot','day','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_HEADCOUNT');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_LEAVE_RATE','月离职率','hr','当月离职人数 / 期初在职人数','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_LEAVE_RATE');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'HR_COST_RATIO','人工成本占比','hr','当月人工成本 / 当月完工入库产值（MMS）【已确认 v1】','HrStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='HR_COST_RATIO');

INSERT INTO bi_metric_dict (metric_code, metric_name, domain, caliber_desc, calc_ref, refresh_type, freq, create_by, create_time)
SELECT 'OTC_CYCLE','现金周期','synthesis','合同签订到全额回清的平均天数，未回清合同不纳入；回款取 mk_payment_plan 实际回款【已确认 v1】','MkStatsProvider','snapshot','month','admin',sysdate() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM bi_metric_dict WHERE metric_code='OTC_CYCLE');

-- =============================================
-- 三、菜单权限（规范 3.4：M/C/F 三级，权限标识 bi:*）
-- =============================================

-- 一级菜单（M=目录，幂等：先查后插）
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name='决策支持' AND menu_type='M' AND parent_id=0 LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, icon, create_by, create_time)
SELECT '决策支持', 0, 60, '/bi', NULL, '', 1, 0, 'M', '0', '0', 'chart', 'admin', sysdate()
FROM DUAL WHERE @biId IS NULL;
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name='决策支持' AND menu_type='M' AND parent_id=0 LIMIT 1);

-- 二级菜单（C=菜单）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '决策驾驶舱', @biId, 1, 'dashboard', 'bi/dashboard/index', 'BiDashboard',
    1, 0, 'C', '0', '0', 'bi:dashboard:view', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:dashboard:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '指标字典', @biId, 2, 'metric', 'bi/metric/index', 'BiMetric',
    1, 0, 'C', '0', '0', 'bi:metric:list', 'dict', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:list');

-- 三级按钮权限（F=按钮，按 perms 判重逐条幂等插入）
SET @metricMenuId = (SELECT menu_id FROM sys_menu WHERE perms='bi:metric:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '指标查询', @metricMenuId, 1, '', 'F', '0', '0', 'bi:metric:query',  'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '指标新增', @metricMenuId, 2, '', 'F', '0', '0', 'bi:metric:add',    'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '指标修改', @metricMenuId, 3, '', 'F', '0', '0', 'bi:metric:edit',   'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '指标删除', @metricMenuId, 4, '', 'F', '0', '0', 'bi:metric:remove', 'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:remove');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '指标导出', @metricMenuId, 5, '', 'F', '0', '0', 'bi:metric:export', 'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:export');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '对账查询', @metricMenuId, 6, '', 'F', '0', '0', 'bi:metric:check',  'admin', sysdate() FROM DUAL
WHERE @metricMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='bi:metric:check');

-- 给 admin 角色分配权限（含无 perms 的一级目录 M：RuoYi 菜单树按祖先过滤，
-- 一级目录未授权时其下所有 C 菜单都不会出现在侧边栏）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE (perms LIKE 'bi:%' OR menu_id = @biId)
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 四、Quartz 定时任务（阶段一开发完成后在「系统监控-定时任务」界面配置）
-- 调用目标：biMetricSnapshotTask.snapshotMetrics   cron 建议：0 0 1 * * ?（每日 01:00）
-- 调用目标：biMetricSnapshotTask.checkMetrics      cron 建议：0 30 1 * * ?（每日 01:30 对账）
-- 幂等说明：任务内部同日重跑先删后插，可安全重复执行
-- =============================================



-- =============================================================
-- 第 13 段 / 共 42 段：bi_supplement.sql
-- =============================================================

-- =============================================
-- 决策支持模块增量补充（bi_supplement.sql）
-- 内容：F3 五域主题看板菜单（销售/采购/生产/库存/质量，5 个 C 菜单共用 bi/board/index 组件）
-- 前置：需先执行 bi_all.sql（决策支持目录已存在）；可重复执行（先查后插）
-- =============================================

-- 定位「决策支持」一级目录（增量执行不用 LAST_INSERT_ID，避免依赖 bi_all.sql 执行时机）
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' LIMIT 1);

-- F3 五域主题看板（C=菜单，5 个菜单共用组件 bi/board/index，按 path 尾段区分域）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '销售看板', @biId, 3, 'board-sales', 'bi/board/index', 'BiBoardSales',
    1, 0, 'C', '0', '0', 'bi:board:sales:view', 'money', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:sales:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购看板', @biId, 4, 'board-purchase', 'bi/board/index', 'BiBoardPurchase',
    1, 0, 'C', '0', '0', 'bi:board:purchase:view', 'shopping', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:purchase:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产看板', @biId, 5, 'board-production', 'bi/board/index', 'BiBoardProduction',
    1, 0, 'C', '0', '0', 'bi:board:production:view', 'build', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:production:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '库存看板', @biId, 6, 'board-inventory', 'bi/board/index', 'BiBoardInventory',
    1, 0, 'C', '0', '0', 'bi:board:inventory:view', 'dict', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:inventory:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量看板', @biId, 7, 'board-quality', 'bi/board/index', 'BiBoardQuality',
    1, 0, 'C', '0', '0', 'bi:board:quality:view', 'validCode', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:quality:view');

-- P1 增补（使用反馈）：安全/设备/人力/投标 四域独立看板（共用组件，权限动态校验 bi:board:{domain}:view）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全看板', @biId, 8, 'board-safety', 'bi/board/index', 'BiBoardSafety',
    1, 0, 'C', '0', '0', 'bi:board:safety:view', 'warning', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:safety:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备看板', @biId, 9, 'board-equipment', 'bi/board/index', 'BiBoardEquipment',
    1, 0, 'C', '0', '0', 'bi:board:equipment:view', 'server', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:equipment:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力看板', @biId, 10, 'board-hr', 'bi/board/index', 'BiBoardHr',
    1, 0, 'C', '0', '0', 'bi:board:hr:view', 'education', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:hr:view');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标看板', @biId, 11, 'board-bid', 'bi/board/index', 'BiBoardBid',
    1, 0, 'C', '0', '0', 'bi:board:bid:view', 'documentation', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'bi:board:bid:view');

-- 给 admin 角色分配九域看板权限（幂等）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu
WHERE perms IN ('bi:board:sales:view', 'bi:board:purchase:view', 'bi:board:production:view',
                'bi:board:inventory:view', 'bi:board:quality:view',
                'bi:board:safety:view', 'bi:board:equipment:view', 'bi:board:hr:view', 'bi:board:bid:view')
  AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 1);

-- =============================================
-- 2026-11-07 增量：bi_metric_dict 补列（F4 下钻URL + F7 通知角色）
-- 背景：Mapper XML 引用 drill_url/notify_roles，旧建表 DDL 缺列导致指标字典/看板/驾驶舱全部查询报错
-- 幂等：information_schema 检查列存在性，已存在时跳过（MySQL 8.0 无 ADD COLUMN IF NOT EXISTS）
-- =============================================
SET @has_drill = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'bi_metric_dict' AND column_name = 'drill_url');
SET @ddl = IF(@has_drill = 0,
    'ALTER TABLE bi_metric_dict ADD COLUMN drill_url VARCHAR(500) DEFAULT NULL COMMENT ''下钻跳转URL（F4：指标卡点击跳转，支持{value}占位符）'' AFTER owner',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
SET @has_notify = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'bi_metric_dict' AND column_name = 'notify_roles');
SET @ddl = IF(@has_notify = 0,
    'ALTER TABLE bi_metric_dict ADD COLUMN notify_roles VARCHAR(200) DEFAULT NULL COMMENT ''预警通知角色（F7：逗号分隔role_key，空=默认admin）'' AFTER drill_url',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;



-- =============================================================
-- 第 14 段 / 共 42 段：bi_alert.sql
-- =============================================================

-- =============================================================
-- BI 决策支持模块 · F7 阈值预警推送（PRD v1.2）
-- 依赖：bi_all.sql（bi_metric_dict / bi_metric_snapshot 已建）
-- 内容：
--   1. bi_metric_alert 预警记录表
--   2. 注册 Quartz 任务：biMetricSnapshotTask.alertMetrics（01:10，快照后执行）
-- 幂等说明：alertMetrics 按 uk_alert 唯一键 upsert，可安全手动重跑；
--           抑制规则（24h 同级不重复推）由代码保证，重复执行不会刷屏。
-- 备注：bi_metric_alert 属统计/追溯类，豁免归属字段填充（规范 3.5.4），
--       保留 dept_id 维度列（NOT NULL DEFAULT 0=全部门，教训同快照表：
--       NULL 会使唯一索引不去重）。
-- =============================================================

-- ----------------------------
-- 1. BI指标预警记录表
-- ----------------------------
CREATE TABLE IF NOT EXISTS bi_metric_alert (
  id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
  metric_code     VARCHAR(64)  NOT NULL                COMMENT '指标编码',
  metric_date     DATE         NOT NULL                COMMENT '预警数据日期（快照日期）',
  dept_id         BIGINT       NOT NULL DEFAULT 0      COMMENT '部门ID（0=全部门汇总）',
  alert_level     CHAR(1)      NOT NULL                COMMENT '预警级别（1黄线 2红线）',
  metric_value    DECIMAL(20,4) DEFAULT NULL           COMMENT '触发时指标值',
  threshold_value DECIMAL(20,4) DEFAULT NULL           COMMENT '触发时阈值',
  alert_message   VARCHAR(500) DEFAULT NULL            COMMENT '预警内容',
  push_status     CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '推送状态（0未推送 1已推送 2抑制未推）',
  push_time       DATETIME     DEFAULT NULL            COMMENT '最近推送时间',
  push_count      INT          NOT NULL DEFAULT 0      COMMENT '累计推送次数',
  push_remark     VARCHAR(200) DEFAULT NULL            COMMENT '最近一次未推送原因（抑制说明）',
  recover_time    DATETIME     DEFAULT NULL            COMMENT '恢复时间（值回到阈值内）',
  status          CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '预警状态（0未恢复 1已恢复）',
  create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
  update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
  PRIMARY KEY (id),
  UNIQUE KEY uk_alert (metric_code, metric_date, dept_id, alert_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='BI指标预警记录表';

-- ----------------------------
-- 2. 注册 Quartz 预警任务（每日 01:10，快照后 10 分钟执行）
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, module_key, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT 'BI指标阈值预警', 'DEFAULT', 'bi', 'biMetricSnapshotTask.alertMetrics', '0 10 1 * * ?', '3', '1', '0', 'admin', NOW(), '每日快照后匹配阈值并按抑制规则推送站内预警（PRD F7）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target='biMetricSnapshotTask.alertMetrics' AND status='0');



-- =============================================================
-- 第 15 段 / 共 42 段：bi_metric_caliber_20261108.sql
-- =============================================================

-- =============================================
-- BI 指标字典 - 口径说明补全（统计周期 + 计算口径）
-- 日期：2026-11-08
-- 背景：看板卡片未区分「当月 / 全量累计 / 时点存量」，同屏混用易误读
--       （如线索转化率为建库至今累计，而非当月转化率）
-- 说明：复用既有 caliber_desc 字段（varchar(500)，字典维护页可编辑、变更留痕），
--       不改表结构；按各 StatsProvider 实际取数代码核定，非按 freq 字段推断
--       （freq 表示快照频率，不等于统计周期）
-- 幂等：全部为按 metric_code 的 UPDATE，可重复执行
-- =============================================

-- ---------- 营销销售 sales ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月回款登记金额合计（mk_payment_plan 实际回款金额，按实际回款日期归月；已回款/部分回款均计入）'
WHERE metric_code = 'MK_RCPT_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月生效合同金额合计（按合同签订日期归月）'
WHERE metric_code = 'MK_SIG_AMT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：成交线索 / 线索总数，取建库至今全部线索，无日期过滤'
WHERE metric_code = 'MK_LEAD_CVR';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：赢单商机数 / 已关闭商机数（赢单+输单），取全部历史商机，无日期过滤'
WHERE metric_code = 'MK_WIN_RATE';

-- ---------- 投标管理 bid ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：中标项目 / 已出结果项目数（中标 + 未中标 + 废标流标），取全部历史项目，无日期过滤'
WHERE metric_code = 'BID_WIN_RATE';

-- ---------- 生产制造 production ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已完工工单 / 有效工单总数（不含草稿、已作废），取全部历史工单，无日期过滤'
WHERE metric_code = 'MMS_WO_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月新增异常工单数（全部状态，按上报时间归月）'
WHERE metric_code = 'MMS_EXCEPTION';

-- ---------- 设备管理 equipment ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：设备综合效率，估算口径（日运行 16 小时假设），按快照日期归月；接入实测采集后切换实测值'
WHERE metric_code = 'DMS_OEE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月故障工单停机时长合计（按报修时间归月）'
WHERE metric_code = 'DMS_DOWN_HRS';

-- ---------- 质量管理 quality ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：一次检验合格数 / 已完成检验数，取全部历史检验任务，无日期过滤'
WHERE metric_code = 'QMS_PASS_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前状态不等于“已关闭”的 NCR 数量（驳回亦视为未关闭），取当前快照时点'
WHERE metric_code = 'QMS_NCR_OPEN';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已关闭 CAPA / CAPA 总数，取全部历史 CAPA 记录，无日期过滤'
WHERE metric_code = 'QMS_CAPA_RATE';

-- ---------- 采购供应 purchase ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：按期到货订单数 / 已到货订单数，“按期”以订单要求交期为基准，取全部已到货订单，无日期过滤'
WHERE metric_code = 'PMS_ONTIME_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月采购金额 Top5 供应商金额合计 / 当月采购总额（仅统计订单日期在当月、有供应商且金额有效的订单）'
WHERE metric_code = 'PMS_TOP_SUP';

-- ---------- 仓储库存 inventory ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月出库成本（按入库单价取价）/ 库存金额；平均库存金额以统计日时点库存金额近似'
WHERE metric_code = 'WMS_TURNOVER';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：低库存 + 积压 + 临期 + 过期预警数量合计，取当前快照时点'
WHERE metric_code = 'WMS_ALERT_CNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：超 90 天无出库记录的库存金额（不含委外/在制），取当前快照时点'
WHERE metric_code = 'WMS_SLOW_AMT';

-- ---------- 人力资源 hr ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：时点存量｜口径：当前在职状态员工数，取当前快照时点'
WHERE metric_code = 'HR_HEADCOUNT';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月离职人数 / 期初在职人数（按离职日期归月）'
WHERE metric_code = 'HR_LEAVE_RATE';

UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：当月人工成本 / 当月完工入库产值'
WHERE metric_code = 'HR_COST_RATIO';

-- ---------- 安全生产 safety ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：全量累计（非当月）｜口径：已整改隐患 / 应整改隐患总数（草稿、待审批、驳回不纳入分母），取全部历史隐患，无日期过滤'
WHERE metric_code = 'SAFETY_RECT_RATE';

-- ---------- 综合价值链 synthesis ----------
UPDATE bi_metric_dict SET caliber_desc = '统计周期：当月｜口径：合同签订到全额回清的平均天数；仅统计回清完成日期在当月的合同，未回清合同不纳入'
WHERE metric_code = 'OTC_CYCLE';

-- =============================================
-- 校验：确认 22 个指标均已标注统计周期
-- =============================================
-- SELECT metric_code, metric_name, domain, caliber_desc FROM bi_metric_dict
--  WHERE metric_code IN ('MK_RCPT_AMT','MK_SIG_AMT','MK_LEAD_CVR','MK_WIN_RATE','BID_WIN_RATE',
--        'MMS_WO_RATE','MMS_EXCEPTION','DMS_OEE','DMS_DOWN_HRS','QMS_PASS_RATE','QMS_NCR_OPEN',
--        'QMS_CAPA_RATE','PMS_ONTIME_RATE','PMS_TOP_SUP','WMS_TURNOVER','WMS_ALERT_CNT',
--        'WMS_SLOW_AMT','HR_HEADCOUNT','HR_LEAVE_RATE','HR_COST_RATIO','SAFETY_RECT_RATE','OTC_CYCLE')
--  ORDER BY domain, metric_code;



-- =============================================================
-- 第 16 段 / 共 42 段：bi_menu_restructure.sql
-- =============================================================

-- =============================================
-- 决策支持菜单重组（看板 + 统计分析 二级菜单归集）
-- 内容：
--   1) 决策支持下新建「指标看板分析」「统计分析」两个二级目录（M）
--   2) 九域看板（9 个）从决策支持平铺挪入「指标看板分析」；各模块看板挪入对应模块目录
--   3) 统计分析下按模块名称分一级目录（含采购管理），各模块统计报表归入对应模块目录
--   4) 采购看板(4461) 归入 统计分析>采购管理 并更名「采购统计分析」；
--      商机看板(4070)、谱系完整度(4844) 不纳入决策支持（物理删除，原模块保留 Legacy 入口）
--   5) 各模块原报表目录挪空后隐藏（visible='1'），不删除
-- 原则：挪动只改 parent_id/order_num/path（防同父路由冲突），不改 menu_id/perms/component，
--       sys_role_menu 角色权限关联不受影响
-- 幂等：先查后插/先查后更，可重复执行；二级目录按 path 判重而非 menu_name，
--       避免用户在菜单管理里改名后重跑本脚本又新建一条同名空目录（2026-11-08 修复）
-- =============================================

-- =============================================
-- 一、新建「指标看板分析」「统计分析」二级目录
-- =============================================
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' AND parent_id = 0 LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
-- 注意：按 path 判重（menu_name 可被用户在菜单管理里改名，按名称判重会重复建空目录）
SELECT '指标看板分析', @biId, 3, 'board', NULL, '',
    1, 0, 'M', '0', '0', '', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'board' AND parent_id = @biId AND menu_type = 'M');
SET @boardId = (SELECT menu_id FROM sys_menu WHERE path = 'board' AND parent_id = @biId AND menu_type = 'M' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '统计分析', @biId, 4, 'statistics', NULL, '',
    1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate()
FROM DUAL WHERE @biId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE path = 'statistics' AND parent_id = @biId AND menu_type = 'M');
SET @statsId = (SELECT menu_id FROM sys_menu WHERE path = 'statistics' AND parent_id = @biId AND menu_type = 'M' LIMIT 1);

-- =============================================
-- 二、统计分析下按模块名称分一级目录（11 个，名称/图标与一级模块一致）
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓储管理', @statsId, 1, 'wms', NULL, '', 1, 0, 'M', '0', '0', '', 'scan', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '仓储管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备管理', @statsId, 2, 'dms', NULL, '', 1, 0, 'M', '0', '0', '', 'server', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '设备管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '营销管理', @statsId, 3, 'mk', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '营销管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购管理', @statsId, 4, 'pms', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '采购管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全生产管理', @statsId, 5, 'safety', NULL, '', 1, 0, 'M', '0', '0', '', 'shield', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量管理', @statsId, 6, 'qms', NULL, '', 1, 0, 'M', '0', '0', '', 'chart', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '质量管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产管控', @statsId, 7, 'mms', NULL, '', 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '生产管控' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '产品设计', @statsId, 8, 'pdm', NULL, '', 1, 0, 'M', '0', '0', '', 'component', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '产品设计' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源管理', @statsId, 9, 'hr', NULL, '', 1, 0, 'M', '0', '0', '', 'people', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '人力资源管理' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'AI中心', @statsId, 10, 'ai', NULL, '', 1, 0, 'M', '0', '0', '', 'skill', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = 'AI中心' AND parent_id = @statsId AND menu_type = 'M');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '财务管理', @statsId, 11, 'fin', NULL, '', 1, 0, 'M', '0', '0', '', 'money', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '财务管理' AND parent_id = @statsId AND menu_type = 'M');

SET @stWms = (SELECT menu_id FROM sys_menu WHERE menu_name='仓储管理' AND parent_id=@statsId LIMIT 1);
SET @stDms = (SELECT menu_id FROM sys_menu WHERE menu_name='设备管理' AND parent_id=@statsId LIMIT 1);
SET @stMk  = (SELECT menu_id FROM sys_menu WHERE menu_name='营销管理' AND parent_id=@statsId LIMIT 1);
SET @stPms = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=@statsId LIMIT 1);
SET @stSafety = (SELECT menu_id FROM sys_menu WHERE menu_name='安全生产管理' AND parent_id=@statsId LIMIT 1);
SET @stQms = (SELECT menu_id FROM sys_menu WHERE menu_name='质量管理' AND parent_id=@statsId LIMIT 1);
SET @stMms = (SELECT menu_id FROM sys_menu WHERE menu_name='生产管控' AND parent_id=@statsId LIMIT 1);
SET @stPdm = (SELECT menu_id FROM sys_menu WHERE menu_name='产品设计' AND parent_id=@statsId LIMIT 1);
SET @stHr  = (SELECT menu_id FROM sys_menu WHERE menu_name='人力资源管理' AND parent_id=@statsId LIMIT 1);
SET @stAi  = (SELECT menu_id FROM sys_menu WHERE menu_name='AI中心' AND parent_id=@statsId LIMIT 1);
SET @stFin = (SELECT menu_id FROM sys_menu WHERE menu_name='财务管理' AND parent_id=@statsId LIMIT 1);

-- =============================================
-- 三、九域看板（9 个）从决策支持平铺挪入「指标看板分析」（order 保持 3-11）
-- =============================================
UPDATE sys_menu SET parent_id = @boardId
WHERE @boardId IS NOT NULL AND parent_id = @biId
  AND menu_id IN (9063013,9063014,9063015,9063016,9063017,9063021,9063022,9063023,9063024);

-- =============================================
-- 四、各模块看板挪入「统计分析」下对应模块目录（order 20-30；path 冲突的加模块前缀防同父路由重复）
-- =============================================
UPDATE sys_menu SET parent_id=@stDms, order_num=20, menu_name='设备统计分析' WHERE menu_id=3090 AND @stDms IS NOT NULL AND parent_id=3006;   -- 设备统计分析(设备)
UPDATE sys_menu SET parent_id=@stDms, order_num=21, menu_name='设备数据大屏' WHERE menu_id=3091 AND @stDms IS NOT NULL AND parent_id=3006;   -- 设备数据大屏(设备)
UPDATE sys_menu SET parent_id=@stMk, order_num=22, menu_name='营销统计分析' WHERE menu_id=4120 AND @stMk IS NOT NULL AND parent_id=4006;   -- 营销统计分析(营销)
UPDATE sys_menu SET parent_id=@stMk, order_num=23, menu_name='推广分析' WHERE menu_id=4583 AND @stMk IS NOT NULL AND parent_id=4006;   -- 推广分析(营销)
UPDATE sys_menu SET parent_id=@stMk, order_num=24, menu_name='投标分析' WHERE menu_id=905894 AND @stMk IS NOT NULL AND parent_id=905886;   -- 投标分析(营销)
-- 4461 采购看板不进「指标看板分析」：归入 统计分析>采购管理（见第五节）
UPDATE sys_menu SET parent_id=@stQms, order_num=26, menu_name='质量统计分析' WHERE menu_id=4544 AND @stQms IS NOT NULL AND parent_id=4813;   -- 质量统计分析(质量)
UPDATE sys_menu SET parent_id=@stMms, order_num=27, menu_name='生产统计分析' WHERE menu_id=5001 AND @stMms IS NOT NULL AND parent_id=5000;   -- 生产统计分析(生产)
UPDATE sys_menu SET parent_id=@stSafety, order_num=28, menu_name='安全生产驾驶舱' WHERE menu_id=4726 AND @stSafety IS NOT NULL AND parent_id=4638;   -- 安全生产驾驶舱(安全)
-- 4070 商机看板 / 4844 谱系完整度：不纳入决策支持，物理删除（2026-11-08 用户确认）
-- 原模块入口由 bi_menu_restore_boards.sql 以 Legacy 路由恢复（9063062 / 9063069），功能不受影响
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id IN (4070, 4844) OR parent_id IN (4070, 4844)
  ) t
);
DELETE FROM sys_menu WHERE parent_id IN (4070, 4844);
DELETE FROM sys_menu WHERE menu_id IN (4070, 4844);

-- =============================================
-- 五、各模块统计报表挪入「统计分析」对应模块目录（子菜单 path 不冲突，保持原 path）
-- =============================================
-- 采购看板(4461) -> 统计分析>采购管理，更名「采购统计分析」（path 保持 pms-dashboard，新目录下唯一）
UPDATE sys_menu SET parent_id=@stPms, order_num=1, menu_name='采购统计分析', path='pms-dashboard'
WHERE menu_id=4461 AND @stPms IS NOT NULL AND (parent_id <> @stPms OR menu_name <> '采购统计分析');

UPDATE sys_menu SET parent_id=@stWms, order_num=1, menu_name='仓储统计分析' WHERE menu_id=2110 AND @stWms IS NOT NULL AND parent_id=2000;   -- 仓储统计分析(仓储)
UPDATE sys_menu SET parent_id=@stDms, order_num=1 WHERE menu_id=3108    AND @stDms IS NOT NULL AND parent_id=3006;     -- 库存流水报表(设备)
UPDATE sys_menu SET parent_id=@stMk,  order_num=1 WHERE menu_id=4156    AND @stMk  IS NOT NULL AND parent_id=4006;     -- 销售人员分析(营销)
UPDATE sys_menu SET parent_id=@stMk,  order_num=2 WHERE menu_id=4215    AND @stMk  IS NOT NULL AND parent_id=4006;     -- 经销商专项报表(营销)
UPDATE sys_menu SET parent_id=@stSafety, order_num=1 WHERE menu_id=4870 AND @stSafety IS NOT NULL AND parent_id=4691;  -- 学时统计(安全)
UPDATE sys_menu SET parent_id=@stQms, order_num=1 WHERE menu_id=4851    AND @stQms IS NOT NULL AND parent_id=4813;     -- 数据报表(质量)
UPDATE sys_menu SET parent_id=@stMms, order_num=1 WHERE menu_id=5215    AND @stMms IS NOT NULL AND parent_id=5235;     -- OEE分析(生产)
UPDATE sys_menu SET parent_id=@stMms, order_num=2 WHERE menu_id=5216    AND @stMms IS NOT NULL AND parent_id=5235;     -- 生产日报(生产)
UPDATE sys_menu SET parent_id=@stMms, order_num=3 WHERE menu_id=5345    AND @stMms IS NOT NULL AND parent_id=5235;     -- 工单完工分析(生产)
UPDATE sys_menu SET parent_id=@stPdm, order_num=1 WHERE menu_id=11440   AND @stPdm IS NOT NULL AND parent_id=11000;    -- 统计分析(产品设计)
UPDATE sys_menu SET parent_id=@stHr,  order_num=1 WHERE menu_id=906148  AND @stHr  IS NOT NULL AND parent_id=905538;   -- 统计报表(人力)
UPDATE sys_menu SET parent_id=@stAi,  order_num=1 WHERE menu_id=905791  AND @stAi  IS NOT NULL AND parent_id=905788;   -- 用量统计(AI)
-- 财务报表目录(905998)下 5 张报表直接入「统计分析>财务管理」，目录挪空后隐藏
UPDATE sys_menu SET parent_id=@stFin, order_num=1 WHERE menu_id=906110 AND @stFin IS NOT NULL AND parent_id=905998;    -- 资产负债表
UPDATE sys_menu SET parent_id=@stFin, order_num=2 WHERE menu_id=906111 AND @stFin IS NOT NULL AND parent_id=905998;    -- 利润表
UPDATE sys_menu SET parent_id=@stFin, order_num=3 WHERE menu_id=906112 AND @stFin IS NOT NULL AND parent_id=905998;    -- 现金流量表
UPDATE sys_menu SET parent_id=@stFin, order_num=4 WHERE menu_id=906113 AND @stFin IS NOT NULL AND parent_id=905998;    -- 往来对账
UPDATE sys_menu SET parent_id=@stFin, order_num=5 WHERE menu_id=906114 AND @stFin IS NOT NULL AND parent_id=905998;    -- 账龄分析

-- =============================================
-- 六、各模块原报表目录挪空后隐藏（visible='1'，不删除，保留角色关联）
-- 说明：完工分析(5235)下仍保留「批次追溯」，不隐藏
-- =============================================
UPDATE sys_menu SET visible='1' WHERE menu_id=4006 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4006 AND visible='0' AND status='0') t);
UPDATE sys_menu SET visible='1' WHERE menu_id=4460 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4460 AND visible='0' AND status='0') t);
UPDATE sys_menu SET visible='1' WHERE menu_id=4813 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=4813 AND visible='0' AND status='0') t);
UPDATE sys_menu SET menu_name='统计分析' WHERE menu_id=3006;   -- 报表看板改名统计分析
UPDATE sys_menu SET visible='1' WHERE menu_id=3006 AND NOT EXISTS
  (SELECT 1 FROM (SELECT 1 FROM sys_menu WHERE parent_id=3006 AND visible='0' AND status='0') t);
-- 财务报表(905998)目录下剩余为 F 按钮权限（不构成菜单显示），直接隐藏
UPDATE sys_menu SET visible='0' WHERE menu_id=905998;   -- 财务报表目录保持可见（Legacy财务报表入口）

-- =============================================
-- 七、admin 角色授权（幂等；普通角色请在 系统管理-角色管理 自行勾选）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.parent_id IN (@biId, @statsId) AND m.menu_type='M'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);



-- =============================================================
-- 第 17 段 / 共 42 段：bi_menu_restore_boards.sql
-- =============================================================

-- =============================================
-- 决策支持菜单重组增量：恢复原模块看板入口（两边都保留——看板类补充）
-- 背景：bi_menu_restructure.sql 将各模块看板挪入决策支持>看板；bi_menu_restore_entries.sql
--       仅恢复了 17 张统计报表。本脚本补齐 11 个看板类页面的原模块入口
--       （同 component/perms/icon，route_name 加 Legacy 前缀防路由冲突），
--       并取消隐藏采购>统计分析目录。
-- 幂等：先查后插/先查后更，可重复执行
-- =============================================

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备统计分析', 3006, 1, 'overview', 'dms/dashboard/index', 'LegacyDmsDashboard',
    1, 0, 'C', '0', '0', 'dms:dashboard:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '设备大屏', 3006, 2, 'screen', 'dms/equipment/screen', 'LegacyDmsEquipmentScreen',
    1, 0, 'C', '0', '0', 'dms:equipment:query', 'monitor', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsEquipmentScreen' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '商机看板', 4003, 2, 'kanban', 'mk/opportunity/kanban', 'LegacyMkOpportunityKanban',
    1, 0, 'C', '0', '0', 'marketing:opportunity:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkOpportunityKanban' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '营销统计分析', 4006, 1, 'dashboard', 'mk/dashboard/index', 'LegacyMkDashboard',
    1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '推广统计分析', 4006, 2, 'promotion', 'mk/promotionReport/index', 'LegacyMkPromotionReport',
    1, 0, 'C', '0', '0', 'marketing:promotionReport:view', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkPromotionReport' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购统计分析', 4460, 1, 'dashboard', 'pms/dashboard/index', 'LegacyPmsDashboard',
    1, 0, 'C', '0', '0', 'pms:dashboard:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyPmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量管理驾驶舱', 4813, 2, 'dashboard', 'qms/dashboard/index', 'LegacyQmsDashboard',
    1, 0, 'C', '0', '0', 'qms:dashboard:view', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产驾驶舱', 5000, 10, 'dashboard', 'mms/dashboard/index', 'LegacyMmsDashboard',
    1, 0, 'C', '0', '0', 'mms:dashboard:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '安全生产驾驶舱', 4638, 8, 'dashboard', 'safety/dashboard/index', 'LegacySafetyDashboard',
    1, 0, 'C', '0', '0', 'safety:dashboard:list', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacySafetyDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '谱系完整度', 4833, 3, 'dashboard', 'qms/trace/dashboard', 'LegacyQmsTraceDashboard',
    1, 0, 'C', '0', '0', 'qms:trace:dashboard', 'dashboard', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsTraceDashboard' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '投标数据分析', 905886, 8, 'dashboard', 'bid/dashboard/index', 'LegacyBidDashboard',
    1, 0, 'C', '0', '0', 'bid:dashboard:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyBidDashboard' AND menu_type='C');

-- =============================================
-- 取消隐藏采购>统计分析目录（visible='1' -> '0'，幂等）
-- =============================================
UPDATE sys_menu SET visible='0' WHERE menu_id=4460 AND visible='1';

-- =============================================
-- admin 授权（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_type='C' AND m.parent_id IN (3006,4003,4006,4460,4813,4638,4833,5000,905886)
  AND m.route_name LIKE 'Legacy%'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id=1 AND r.menu_id=m.menu_id);

-- =============================================
-- 普通角色授权迁移（按 perms 匹配挪动看板：角色原持有该看板权限 => 补授 Legacy 菜单）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, lm.menu_id
FROM sys_role_menu rm
-- 4070/4844 已于 2026-11-08 物理删除，保留 id 仅作历史兼容（JOIN 自然无匹配）
JOIN sys_menu om ON rm.menu_id = om.menu_id
  AND om.menu_id IN (3090,3091,4070,4120,4461,4544,4583,4726,4844,5001,905894)
JOIN sys_menu lm ON lm.route_name = CONCAT('Legacy', om.route_name) AND lm.menu_type='C'
WHERE NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=rm.role_id AND x.menu_id=lm.menu_id);

-- =============================================
-- 取消隐藏原模块目录（有 Legacy 子菜单的目录保持可见，保证执行顺序无关）
-- =============================================
UPDATE sys_menu p LEFT JOIN sys_menu c ON c.parent_id=p.menu_id AND c.visible='0' AND c.status='0'
SET p.visible='0'
WHERE p.menu_id IN (3006,4006,4460,4813,905998) AND c.menu_id IS NOT NULL;



-- =============================================================
-- 第 18 段 / 共 42 段：bi_menu_restore_entries.sql
-- =============================================================

-- =============================================
-- 决策支持菜单重组增量：恢复原模块统计报表入口（两边都保留）
-- 背景：bi_menu_restructure.sql 将各模块统计报表挪入决策支持>统计分析（移动语义），
--       原模块报表目录被隐藏。本脚本在原模块位置新建指向同一页面的菜单项
--       （同 component/perms/icon，route_name 加 Legacy 前缀避免与决策支持下路由名冲突），
--       并取消隐藏相关目录，实现模块内与决策支持下双入口。
-- 幂等：先查后插/先查后更，可重复执行
-- =============================================

-- 一、新建原位置菜单项（17 个）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '仓储统计分析', 2000, 7, 'statistics', 'wms/statistics/index', 'LegacyWmsStatistics',
    1, 0, 'C', '0', '0', 'wms:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyWmsStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '库存流水报表', 3006, 3, 'partflow', 'dms/partstock/index', 'LegacyDmsPartFlow',
    1, 0, 'C', '0', '0', 'dms:partstock:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyDmsPartFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '销售人员分析', 4006, 2, 'salesperson', 'mk/salespersonReport/index', 'LegacyMkSalesperson',
    1, 0, 'C', '0', '0', 'marketing:report:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkSalesperson' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '经销商专项报表', 4006, 3, 'dealerReport', 'mk/dealerReport/index', 'LegacyMkDealerReport',
    1, 0, 'C', '0', '0', 'marketing:dealerReport:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMkDealerReport' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '学时统计', 4691, 7, 'hours', 'safety/training/hours/index', 'LegacySafetyTrainingHours',
    1, 0, 'C', '0', '0', 'safety:training:hours:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacySafetyTrainingHours' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '质量数据报表', 4813, 1, 'list', 'qms/report/index', 'LegacyQmsReportList',
    1, 0, 'C', '0', '0', 'qms:report:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyQmsReportList' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT 'OEE分析', 5235, 2, 'oee', 'mms/oee/index', 'LegacyMmsOee',
    1, 0, 'C', '0', '0', 'mms:oee:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsOee' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '生产日报', 5235, 3, 'daily', 'mms/daily/index', 'LegacyMmsDaily',
    1, 0, 'C', '0', '0', 'mms:daily:list', 'documentation', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsDaily' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '工单完工分析', 5235, 5, 'finishAnalysis', 'mms/finishAnalysis/index', 'LegacyMmsFinishAnalysis',
    1, 0, 'C', '0', '0', 'mms:finishAnalysis:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyMmsFinishAnalysis' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '产品设计驾驶舱', 11000, 99, 'statistics', 'pdm/statistics/index', 'LegacyPdmStatistics',
    1, 0, 'C', '0', '0', 'pdm:statistics:view', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyPdmStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', 'LegacyHrStatistics',
    1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyHrStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '用量统计', 905788, 11, 'usage', 'ai/usage/index', 'LegacyAiUsage',
    1, 0, 'C', '0', '0', 'ai:usage:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyAiUsage' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '资产负债表', 905998, 1, 'balanceSheet', 'fin/report/index', 'LegacyFinBalanceSheet',
    1, 0, 'C', '0', '0', 'fin:report:assets', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinBalanceSheet' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '利润表', 905998, 2, 'incomeStatement', 'fin/report/index', 'LegacyFinIncomeStatement',
    1, 0, 'C', '0', '0', 'fin:report:profit', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinIncomeStatement' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '现金流量表', 905998, 3, 'cashFlow', 'fin/report/index', 'LegacyFinCashFlow',
    1, 0, 'C', '0', '0', 'fin:report:cashflow', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinCashFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '往来对账', 905998, 4, 'reconciliation', 'fin/report/index', 'LegacyFinReconciliation',
    1, 0, 'C', '0', '0', 'fin:report:ledger', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinReconciliation' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '账龄分析', 905998, 5, 'aging', 'fin/report/index', 'LegacyFinAging',
    1, 0, 'C', '0', '0', 'fin:report:aging', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinAging' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '人力资源统计报表', 905538, 8, 'statistics', 'hr/statistics/index', 'LegacyHrStatistics',
    1, 0, 'C', '0', '0', 'hr:statistics:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyHrStatistics' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '用量统计', 905788, 11, 'usage', 'ai/usage/index', 'LegacyAiUsage',
    1, 0, 'C', '0', '0', 'ai:usage:list', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyAiUsage' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '资产负债表', 905998, 1, 'balanceSheet', 'fin/report/index', 'LegacyFinBalanceSheet',
    1, 0, 'C', '0', '0', 'fin:report:assets', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinBalanceSheet' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '利润表', 905998, 2, 'incomeStatement', 'fin/report/index', 'LegacyFinIncomeStatement',
    1, 0, 'C', '0', '0', 'fin:report:profit', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinIncomeStatement' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '现金流量表', 905998, 3, 'cashFlow', 'fin/report/index', 'LegacyFinCashFlow',
    1, 0, 'C', '0', '0', 'fin:report:cashflow', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinCashFlow' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '往来对账', 905998, 4, 'reconciliation', 'fin/report/index', 'LegacyFinReconciliation',
    1, 0, 'C', '0', '0', 'fin:report:ledger', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinReconciliation' AND menu_type='C');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '账龄分析', 905998, 5, 'aging', 'fin/report/index', 'LegacyFinAging',
    1, 0, 'C', '0', '0', 'fin:report:aging', 'chart', 'admin', sysdate()
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE route_name='LegacyFinAging' AND menu_type='C');

-- =============================================
-- 二、取消隐藏原报表目录（visible='1' → '0'，幂等）
-- 采购>统计分析(4460)无统计报表子菜单，保持隐藏
-- =============================================
UPDATE sys_menu SET visible='0' WHERE menu_id=4006 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=4813 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=3006 AND visible='1';
UPDATE sys_menu SET visible='0' WHERE menu_id=905998 AND visible='1';

-- =============================================
-- 三、admin 角色授权（幂等）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, n.menu_id FROM sys_menu n
WHERE n.route_name LIKE 'Legacy%' AND n.menu_type='C'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id=1 AND x.menu_id=n.menu_id);

-- =============================================
-- 四、普通角色授权迁移（按 perms 匹配：角色持有决策支持下挪动菜单 → 授权对应新菜单）
-- =============================================
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT rm.role_id, n.menu_id
FROM sys_menu n
JOIN sys_menu o ON o.perms = n.perms
  AND o.menu_id IN (2110,3108,4156,4215,4851,5215,5216,5345,11440,906148,905791,906110,906111,906112,906113,906114)
JOIN sys_role_menu rm ON rm.menu_id = o.menu_id
WHERE n.route_name LIKE 'Legacy%'
  AND n.parent_id IN (2000,3006,4006,4691,4813,5235,11000,905538,905998,906110,906111,906112,906113,906114)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = rm.role_id AND x.menu_id = n.menu_id);



-- =============================================================
-- 第 19 段 / 共 42 段：bi_menu_adjust_20261108.sql
-- =============================================================

-- =============================================
-- 决策支持菜单调整（2026-11-08）
--  1) 「统计分析」下新增「采购管理」目录，位于「营销管理」之后（order 4，原 4~10 顺延为 5~11）
--  2) 「指标看板分析」最下面的采购看板(4461, pms/dashboard/index) 挪入 统计分析>采购管理，
--     更名「采购统计分析」；path/route_name/component/perms 不变，角色授权(menu_id)不受影响
--  3) 谱系完整度(4844)、商机看板(4070) 从决策支持移除：
--     原按项目规范用 visible='1' 隐藏（软删除），2026-11-08 按用户要求升级为**物理删除**，
--     同时清理 sys_role_menu 授权；独立执行脚本见 bi_menu_purge_hidden_20261108.sql。
--     两者在原模块均有可见 Legacy 入口（9063069 质量追溯 / 9063062 商机管理），功能不受影响。
--  4) 「指标看板分析」下九域看板之一的采购看板(9063014, bi/board/index) 保留不动。
-- 幂等：先查后插 / 按目标值更新 / DELETE 对不存在记录无影响，可重复执行；
--       新目录 menu_id 由自增生成，脚本内不写死；二级目录按 path 定位而非 menu_name，
--       用户在菜单管理里改名后重跑本脚本不会新建重复空目录。
-- 仅调整菜单数据：后端无需重启，刷新页面即生效。
-- =============================================

SET @biId    = (SELECT menu_id FROM sys_menu WHERE menu_name='决策支持' AND menu_type='M' AND parent_id=0 LIMIT 1);
SET @statsId = (SELECT menu_id FROM sys_menu WHERE path='statistics' AND parent_id=@biId AND menu_type='M' LIMIT 1);
SET @boardId = (SELECT menu_id FROM sys_menu WHERE path='board' AND parent_id=@biId AND menu_type='M' LIMIT 1);

-- 一、新建「采购管理」目录（统计分析第 4 位，图标与一级采购管理 4197 一致）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, route_name,
    is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '采购管理', @statsId, 4, 'pms', NULL, '', 1, 0, 'M', '0', '0', '', 'shopping', 'admin', sysdate()
FROM DUAL WHERE @statsId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='采购管理' AND parent_id=@statsId AND menu_type='M');

SET @stPms = (SELECT menu_id FROM sys_menu WHERE menu_name='采购管理' AND parent_id=@statsId AND menu_type='M' LIMIT 1);

-- 二、原第 4~10 位目录顺延为 5~11（固定目标值，重复执行结果一致）
UPDATE sys_menu SET order_num = CASE menu_name
      WHEN '安全生产管理' THEN 5
      WHEN '质量管理'     THEN 6
      WHEN '生产管控'     THEN 7
      WHEN '产品设计'     THEN 8
      WHEN '人力资源管理' THEN 9
      WHEN 'AI中心'       THEN 10
      WHEN '财务管理'     THEN 11
    END
WHERE parent_id = @statsId AND menu_type = 'M'
  AND menu_name IN ('安全生产管理','质量管理','生产管控','产品设计','人力资源管理','AI中心','财务管理');

-- 三、采购看板(4461) 挪入 统计分析>采购管理 并更名
UPDATE sys_menu
SET parent_id   = @stPms,
    order_num   = 1,
    menu_name   = '采购统计分析',
    update_by   = 'admin',
    update_time = sysdate()
WHERE menu_id = 4461 AND @stPms IS NOT NULL
  AND (parent_id <> @stPms OR menu_name <> '采购统计分析');

-- 四、从决策支持物理移除两项（含可能存在的子菜单/按钮与角色授权）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id IN (4070, 4844) OR parent_id IN (4070, 4844)
  ) t
);
DELETE FROM sys_menu WHERE parent_id IN (4070, 4844);
DELETE FROM sys_menu WHERE menu_id IN (4070, 4844);

-- 五、admin 授权新目录（幂等；普通角色在 系统管理-角色管理 自行勾选）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, @stPms FROM DUAL
WHERE @stPms IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu WHERE role_id = 1 AND menu_id = @stPms);

-- 六、复核
SELECT '统计分析目录' AS scope, menu_id, menu_name, order_num, visible
FROM sys_menu WHERE parent_id = @statsId AND menu_type = 'M' ORDER BY order_num;
SELECT '采购统计分析双入口' AS scope, menu_id, menu_name, parent_id, order_num, visible, component, route_name
FROM sys_menu WHERE component = 'pms/dashboard/index' AND menu_type = 'C' ORDER BY menu_id;
SELECT '看板可见菜单' AS scope, menu_id, menu_name, order_num, visible
FROM sys_menu WHERE parent_id = @boardId AND menu_type = 'C' AND visible = '0' ORDER BY order_num;
SELECT '已物理删除项残留(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id IN (4070, 4844) OR parent_id IN (4070, 4844);

-- 回滚（如需）：
--   恢复被删除的 4070/4844：mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_purge_20261108.sql
-- DELETE FROM sys_role_menu WHERE menu_id = @stPms;
-- DELETE FROM sys_menu WHERE menu_id = @stPms;
-- UPDATE sys_menu SET parent_id = @boardId, order_num = 25, menu_name = '采购看板' WHERE menu_id = 4461;
-- 原目录排序恢复：安全生产管理=4 ... 财务管理=10（按 bi_menu_restructure.sql 二节原值）



-- =============================================================
-- 第 20 段 / 共 42 段：bi_menu_permission_fix_20261108.sql
-- =============================================================

-- =============================================
-- 决策支持菜单权限收尾修复（2026-11-08）
-- 依据：全库权限核查结论 + 用户 2026-11-08 确认的两项处理
--
-- 1) 「指标看板分析」(9063031) 目录被授予 36 个非 admin 角色，但其下 9 个 BI
--    主题看板仅 admin 授权。RuoYi 的 SidebarItem 在无可见子菜单时会把目录本身
--    渲染成一个点不开的空菜单项，等于侧边栏多出一个空壳。
--    处理：收回这 36 条目录授权；不扩大任何数据可见范围，BI 看板仍仅 admin 可见。
--    注意：请不要把本段反向写回 bi_menu_restructure.sql 第七节——该节原本就只
--          授权 admin（WHERE role_id=1），不会把普通角色授权补回来。
--
-- 2) 「经销商专项报表」(4215，决策支持>统计分析>营销管理) 授权数为 0，
--    决策支持下的入口对所有人不可见；原模块 Legacy 入口 9063046 仅 admin 授权。
--    处理：把 9063046 的角色授权同步给 4215，使两个入口对同一批角色可见。
--
-- 备份：sql/backup_bi_menu_permission_fix_20261108.sql（36 条待删授权原行）
-- 幂等：DELETE 对不存在行无影响；INSERT 按主键 (role_id, menu_id) 去重，可重复执行
-- 范围：仅 sys_role_menu，不动菜单定义；后端无需重启，刷新页面即生效
-- =============================================

-- 一、执行前复核
SELECT '待收回授权(应为36)' AS scope, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id = 9063031 AND role_id <> 1;
SELECT '4215/9063046 授权对比' AS scope, menu_id, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id IN (4215, 9063046) GROUP BY menu_id ORDER BY menu_id;

-- 二、收回「指标看板分析」目录的非 admin 授权（保留 admin）
DELETE FROM sys_role_menu WHERE menu_id = 9063031 AND role_id <> 1;

-- 三、把 Legacy 入口 9063046 的授权同步给决策支持入口 4215
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT t.role_id, 4215
FROM (SELECT role_id FROM sys_role_menu WHERE menu_id = 9063046) t
WHERE NOT EXISTS (
  SELECT 1 FROM sys_role_menu x WHERE x.role_id = t.role_id AND x.menu_id = 4215
);

-- 四、执行后复核
SELECT '9063031 剩余授权(应仅admin 1条)' AS scope, role_id FROM sys_role_menu
WHERE menu_id = 9063031 ORDER BY role_id;
SELECT '4215/9063046 授权对比(应各1条)' AS scope, menu_id, COUNT(*) AS cnt FROM sys_role_menu
WHERE menu_id IN (4215, 9063046) GROUP BY menu_id ORDER BY menu_id;
WITH RECURSIVE t AS (
  SELECT menu_id, menu_type FROM sys_menu WHERE menu_id = 9062263
  UNION ALL
  SELECT m.menu_id, m.menu_type FROM sys_menu m JOIN t ON m.parent_id = t.menu_id
)
SELECT '决策支持子树内零授权C菜单(应为0)' AS scope, COUNT(*) AS cnt
FROM t WHERE t.menu_type = 'C'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.menu_id = t.menu_id);

-- 回滚（如需）：
--   mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_permission_fix_20261108.sql
--   DELETE FROM sys_role_menu WHERE menu_id = 4215 AND role_id = 1;
-- =============================================



-- =============================================================
-- 第 21 段 / 共 42 段：bi_menu_purge_hidden_20261108.sql
-- =============================================================

-- =============================================
-- 决策支持模块：软删除菜单物理清除 + 重复空壳目录清理（2026-11-08）
-- 背景：
--   1) bi_menu_adjust_20261108.sql 曾把 4070/4844 用 visible='1' 隐藏（软删除），
--      本次按用户要求改为物理删除（含 sys_role_menu 授权）。
--   2) 9063031 已由用户改名为「指标看板分析」，但系统随后又生成一条同 parent、
--      同 path='board' 的空目录 9063090「看板」（无任何子菜单），导致侧边栏仍显示「看板」，
--      属于重复空壳，一并清理。
-- 范围：仅决策支持(9062263)子树内 visible='1' 的 4070/4844 + 空壳 9063090。
--       其他业务模块自有的历史隐藏菜单不动：
--       111 数据监控、2072/2073 仓储、3007/3126 设备AI、4923 质量、
--       5120/5185/5214 生产（均不属于决策支持，且部分为 status='1' 停用功能）。
-- 前置：4070/4844 在原模块均有可见 Legacy 入口（9063062 商机看板 / 9063069 谱系完整度），
--       同 component / perms，物理删除后功能入口不受影响。
-- 备份：执行前已导出 sql/backup_bi_menu_purge_20261108.sql（3 条菜单 + 16 条授权原行），
--       回滚：mysql -u root -p ry-vue --default-character-set=utf8mb4 < backup_bi_menu_purge_20261108.sql
-- 幂等：DELETE 对不存在的记录无影响，可重复执行。
-- 仅动菜单数据，后端无需重启，刷新页面即生效。
-- =============================================

-- 一、执行前复核（应为 3 条菜单 / 16 条授权 / 0 条子菜单）
SELECT '待删菜单' AS scope, menu_id, menu_name, parent_id, menu_type, visible, component, route_name
FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090) ORDER BY menu_id;
SELECT '待删授权' AS scope, menu_id, COUNT(*) AS role_cnt
FROM sys_role_menu WHERE menu_id IN (4070, 4844, 9063090) GROUP BY menu_id ORDER BY menu_id;
SELECT '子菜单检查(应为空)' AS scope, menu_id, menu_name, parent_id
FROM sys_menu WHERE parent_id IN (4070, 4844, 9063090);

-- 二、物理删除：先清角色授权 -> 再清可能存在的子菜单/按钮 -> 最后删主记录
--     （用派生表包装，规避 MySQL "can't specify target table for update in FROM clause"）
DELETE FROM sys_role_menu WHERE menu_id IN (
  SELECT menu_id FROM (
    SELECT menu_id FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090)
       OR parent_id IN (4070, 4844, 9063090)
  ) t
);
DELETE FROM sys_menu WHERE parent_id IN (4070, 4844, 9063090);
DELETE FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090);

-- 三、执行后复核
SELECT '残留菜单(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_menu WHERE menu_id IN (4070, 4844, 9063090) OR parent_id IN (4070, 4844, 9063090);
SELECT '残留授权(应为0)' AS scope, COUNT(*) AS cnt
FROM sys_role_menu WHERE menu_id IN (4070, 4844, 9063090);
SELECT '决策支持一级目录' AS scope, menu_id, menu_name, path, order_num, visible
FROM sys_menu WHERE parent_id = 9062263 AND menu_type = 'M' ORDER BY order_num, menu_id;
SELECT '同父path冲突(应为空)' AS scope, parent_id, path, COUNT(*) AS cnt
FROM sys_menu WHERE menu_type IN ('M','C') AND status='0'
GROUP BY parent_id, path HAVING COUNT(*) > 1;
SELECT '原模块Legacy入口(应各1条可见)' AS scope, menu_id, menu_name, parent_id, visible, component
FROM sys_menu WHERE component IN ('mk/opportunity/kanban','qms/trace/dashboard') AND menu_type='C' ORDER BY component, menu_id;

-- 回滚（如需）：
--   mysql -u root -p ry-vue --default-character-set=utf8mb4 < sql/backup_bi_menu_purge_20261108.sql
--   注意：回滚后 4070/4844 恢复为 visible='1' 隐藏态，9063090 恢复为可见空目录，
--         如需彻底回到「隐藏」状态，只导入备份中的 4070/4844 两行即可。



-- =============================================================
-- 第 22 段 / 共 42 段：add_integration_menu.sql
-- =============================================================

-- =============================================================
-- 集成模块菜单脚本（增量）：系统集成目录 + 钉钉考勤集成菜单 + 按钮权限
-- 版本: v1.0  日期: 2026-11-07
-- 幂等：NOT EXISTS 防重，可重复执行
-- =============================================================

-- 1. 顶级目录：系统集成
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063000, '系统集成', 0, 90, 'integration', NULL, '', 1, 0, 'M', '0', '0', '', 'link', 'admin', sysdate(), '第三方集成管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063000);

-- 2. 菜单：钉钉考勤集成
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063010, '钉钉集成', 9063000, 1, 'attendance', 'integration/attendance/index', '', 1, 0, 'C', '0', '0', 'integration:attendance:list', 'date', 'admin', sysdate(), '钉钉第三方集成（考勤同步、后续消息等）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063010);

-- 3. 按钮：集成同步（同步通讯录/打卡同步/人工绑定）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063011, '集成同步', 9063010, 1, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:sync', '#', 'admin', sysdate(), '同步通讯录、打卡同步与人工绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063011);

-- 4. 按钮：绑定查询
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063012, '绑定查询', 9063010, 2, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:query', '#', 'admin', sysdate(), '绑定关系明细查询'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063012);

-- 5. 按钮：集成配置（读写钉钉集成参数，AppSecret 只写不回显）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063020, '集成配置', 9063010, 3, '#', '', '', 1, 0, 'F', '0', '0', 'integration:attendance:config', '#', 'admin', sysdate(), '钉钉集成参数配置（AppKey/AppSecret/开关/匹配策略）'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063020);

-- 6. 授权角色 1
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, m.menu_id FROM sys_menu m
WHERE m.menu_id IN (9063000, 9063010, 9063011, 9063012, 9063020)
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu r WHERE r.role_id = 1 AND r.menu_id = m.menu_id);



-- =============================================================
-- 第 23 段 / 共 42 段：integration_attendance.sql
-- =============================================================

-- =============================================================
-- 第三方集成模块（钉钉考勤同步）初始化脚本
-- 版本: v1.1  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.1 建表 / 3.4 菜单 / 7 SQL目录）
--
-- v1.1 变更（重要）：改为全幂等，可安全重复执行
--   1) 去掉 DROP TABLE，改 CREATE TABLE IF NOT EXISTS
--      —— 原脚本重复执行会清空 integration_user_binding 已绑定关系与同步日志；
--   2) 参数与定时任务改 INSERT ... SELECT ... WHERE NOT EXISTS
--      —— 原脚本重复执行会产生重复参数（+4）与重复任务（+2）；
--   3) 唯一索引 uk_connector_employee 见 integration_ensure_unique_index.sql。
--
-- 执行顺序：本脚本 → integration_wecom.sql → integration_ensure_unique_index.sql
--          → add_integration_menu.sql → integration_menu_split.sql
--          → module_switch_add_integration.sql
-- =============================================================

-- ----------------------------
-- 1. 外部平台用户绑定表
--    注意：禁止 DROP TABLE——本脚本可能在生产环境重复执行，
--          DROP 会直接清空已绑定关系，改用 IF NOT EXISTS
-- ----------------------------
CREATE TABLE IF NOT EXISTS integration_user_binding (
    binding_id      BIGINT       NOT NULL AUTO_INCREMENT COMMENT '绑定ID',
    connector_type  VARCHAR(32)  NOT NULL DEFAULT 'dingtalk' COMMENT '连接器类型(dingtalk/wecom)',
    external_userid VARCHAR(64)  NOT NULL                COMMENT '外部平台用户ID(钉钉userid)',
    external_name   VARCHAR(64)  DEFAULT NULL            COMMENT '外部平台姓名',
    mobile          VARCHAR(32)  DEFAULT NULL            COMMENT '外部平台手机号',
    external_job_no VARCHAR(64)  DEFAULT NULL            COMMENT '外部平台工号(钉钉jobNumber)',
    employee_no     VARCHAR(64)  DEFAULT NULL            COMMENT '平台员工工号(匹配后回填)',
    employee_id     BIGINT       DEFAULT NULL            COMMENT '平台员工ID',
    match_type      VARCHAR(16)  DEFAULT NULL            COMMENT '匹配方式(auto自动/manual人工)',
    sync_time       DATETIME     DEFAULT NULL            COMMENT '最后同步时间',
    remark          VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    create_by       VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
    create_time     DATETIME     DEFAULT NULL            COMMENT '创建时间',
    update_by       VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
    update_time     DATETIME     DEFAULT NULL            COMMENT '更新时间',
    PRIMARY KEY (binding_id),
    UNIQUE KEY uk_connector_user (connector_type, external_userid),
    KEY idx_employee (employee_id)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='外部平台用户绑定表';

-- ----------------------------
-- 2. 集成同步日志表
--    注意：禁止 DROP TABLE，重复执行不得清空同步日志
-- ----------------------------
CREATE TABLE IF NOT EXISTS integration_sync_log (
    log_id        BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    connector_type VARCHAR(32)  NOT NULL DEFAULT 'dingtalk' COMMENT '连接器类型',
    biz_type      VARCHAR(64)   NOT NULL                COMMENT '业务类型(contact_sync/attendance_sync)',
    biz_key       VARCHAR(128)  DEFAULT NULL            COMMENT '业务关键字(日期范围等)',
    status        CHAR(1)       NOT NULL DEFAULT '0'    COMMENT '状态(0成功 1失败)',
    result_msg    VARCHAR(1000) DEFAULT NULL            COMMENT '结果摘要',
    cost_ms       BIGINT        DEFAULT NULL            COMMENT '耗时(毫秒)',
    create_time   DATETIME      DEFAULT NULL            COMMENT '创建时间',
    PRIMARY KEY (log_id),
    KEY idx_type_time (connector_type, biz_type, create_time)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='集成同步日志表';

-- ----------------------------
-- 3. 钉钉集成参数（系统管理-参数设置 中维护；enabled=true 且凭据齐全后生效）
-- ----------------------------
-- 幂等：全部用 NOT EXISTS 判重，重复执行不会产生重复参数
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成开关', 'integration.dingtalk.enabled', 'false', 'Y', 'admin', sysdate(), '钉钉集成总开关(true/false)，启用前先配置 appKey/appSecret'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.enabled');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成AppKey', 'integration.dingtalk.appKey', '', 'Y', 'admin', sysdate(), '钉钉企业内部应用 AppKey'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.appKey');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成AppSecret', 'integration.dingtalk.appSecret', '', 'Y', 'admin', sysdate(), '钉钉企业内部应用 AppSecret（仅管理员可见，注意保密）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.appSecret');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉集成匹配策略', 'integration.dingtalk.matchType', 'mobile', 'Y', 'admin', sysdate(), '通讯录自动匹配策略：mobile=按手机号 / jobNumber=按工号(需钉钉维护jobNumber) / auto=工号优先手机号兜底；切换仅影响未匹配绑定'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.matchType');
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '钉钉模拟数据模式', 'integration.dingtalk.mockEnabled', 'false', 'Y', 'admin', sysdate(), '无真实凭据时开启：返回内置模拟通讯录与打卡数据，不调用钉钉接口；生产环境必须关闭，定时任务在该模式下自动跳过'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.dingtalk.mockEnabled');

-- ----------------------------
-- 4. 打卡来源字典新增：5=钉钉同步（hr_clock_source；3/4 已被外出/出差占用，勿用）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '钉钉同步', '5', 'hr_clock_source', '', 'primary', 'N', '0', 'admin', sysdate(), '钉钉打卡数据自动同步'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'hr_clock_source' AND dict_value = '5');

-- ----------------------------
-- 5. 定时任务示例（默认暂停状态，配置好凭据后在 系统监控-定时任务 中启用）
-- ----------------------------
-- 幂等：按 invoke_target 判重，重复执行不会产生重复任务
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '钉钉通讯录同步', 'DEFAULT', 'dingtalkAttendanceSyncTask.syncContacts', '0 10 1 * * ?', '3', '1', '1', 'admin', sysdate(), '每日01:10同步钉钉通讯录并自动匹配员工(按手机号)'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'dingtalkAttendanceSyncTask.syncContacts');
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '钉钉考勤同步', 'DEFAULT', 'dingtalkAttendanceSyncTask.syncYesterday', '0 30 1 * * ?', '3', '1', '1', 'admin', sysdate(), '每日01:30同步昨日打卡数据到考勤模块'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'dingtalkAttendanceSyncTask.syncYesterday');

-- ----------------------------
-- 6. 使用说明（非SQL）
-- ----------------------------
-- ① 钉钉开放平台创建企业内部应用，开通「考勤」权限点，将 AppKey/AppSecret 填入参数设置；
-- ② 参数 integration.dingtalk.enabled 改为 true，刷新系统缓存（或重启）；
-- ③ 按 integration.dingtalk.matchType 策略（mobile/jobNumber/auto）执行
--    dingtalkAttendanceSyncTask.syncContacts 同步通讯录并自动匹配员工，未匹配者在
--    GET /integration/attendance/bindings 查看后用 PUT /integration/attendance/binding/bind 人工绑定；
-- ④ 执行 dingtalkAttendanceSyncTask.syncYesterday 验证打卡数据落库
--    （考勤记录列表来源列应显示「钉钉同步」，备注为「钉钉同步」）；
-- ⑤ 首次上线需回溯历史时调用 POST /integration/attendance/sync/{beginDate}/{endDate}（单次≤92天）。



-- =============================================================
-- 第 24 段 / 共 42 段：integration_wecom.sql
-- =============================================================

-- =============================================================
-- 第三方集成模块（企业微信考勤同步）初始化脚本
-- 版本: v1.0  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.1 建表 / 3.4 菜单 / 7 SQL目录）
-- 说明: 绑定表与同步日志表复用 integration_attendance.sql，通过 connector_type='wecom' 区分，
--       本脚本仅新增企业微信参数、打卡来源字典、定时任务与菜单文案优化，可重复执行。
-- =============================================================

-- ----------------------------
-- 1. 企业微信集成参数（系统管理-参数设置 中维护）
--    corpId : 企业管理后台 → 我的企业 → 企业信息 → 企业ID
--    secret : 自建应用 Secret（应用管理 → 应用 → 自建）
--    使用前提：自建应用可见范围覆盖需同步成员；同步打卡需在「打卡 → 可调用接口的应用」中加入该应用；
--              自建应用服务器出口 IP 需加入「企业可信IP」
-- ----------------------------
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信集成开关', 'integration.wecom.enabled', 'false', 'Y', 'admin', sysdate(), '企业微信集成总开关(true/false)，启用前先配置 corpId/secret'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信CorpId', 'integration.wecom.corpId', '', 'Y', 'admin', sysdate(), '企业微信 CorpId（我的企业 → 企业信息）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.corpId');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信Secret', 'integration.wecom.secret', '', 'Y', 'admin', sysdate(), '企业微信自建应用 Secret（仅管理员可见，注意保密）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.secret');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信匹配策略', 'integration.wecom.matchType', 'mobile', 'Y', 'admin', sysdate(),
       '通讯录自动匹配策略：mobile=手机号优先、工号兜底(默认) / jobNumber=工号优先 / auto=同mobile；工号取自成员别名 alias 或 userid'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.matchType');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '企业微信模拟数据模式', 'integration.wecom.mockEnabled', 'false', 'Y', 'admin', sysdate(),
       '无真实凭据时开启：返回内置模拟通讯录与打卡数据，不调用企业微信接口；生产环境必须关闭，定时任务在该模式下自动跳过'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'integration.wecom.mockEnabled');

-- ----------------------------
-- 2. 打卡来源字典新增：6=企业微信同步（hr_clock_source；0考勤机/1补卡/2手动/3外出/4出差/5钉钉 已被占用）
-- ----------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 6, '企业微信同步', '6', 'hr_clock_source', '', 'primary', 'N', '0', 'admin', sysdate(), '企业微信打卡数据自动同步'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'hr_clock_source' AND dict_value = '6');

-- ----------------------------
-- 3. 定时任务（默认暂停状态 status='1'，配置好凭据后在 系统监控-定时任务 中启用）
--    与钉钉任务错开 10 分钟，避免同时段争抢数据库连接
-- ----------------------------
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '企业微信通讯录同步', 'DEFAULT', 'wecomAttendanceSyncTask.syncContacts', '0 20 1 * * ?', '3', '1', '1', 'admin', sysdate(),
       '每日01:20同步企业微信通讯录并自动匹配员工（手机号优先、工号兜底）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'wecomAttendanceSyncTask.syncContacts');

INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '企业微信考勤同步', 'DEFAULT', 'wecomAttendanceSyncTask.syncYesterday', '0 40 1 * * ?', '3', '1', '1', 'admin', sysdate(),
       '每日01:40同步昨日企业微信打卡数据到考勤模块'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'wecomAttendanceSyncTask.syncYesterday');

-- ----------------------------
-- 4. 菜单（已由 integration_menu_split.sql 接管，此处不再改名）
--    历史做法是将菜单名改为「第三方考勤集成」共用一页；现已拆分为
--    「钉钉集成」（9063010 / integration/attendance/index）与
--    「企业微信集成」（9063030 / integration/wecom/index）两个独立菜单，
--    请执行 sql/integration_menu_split.sql，本节保留为空以免重复执行时回退菜单名。
-- ----------------------------

-- ----------------------------
-- 5. 一一绑定并发保护（可选，建议执行）
--    唯一索引在数据库层防止同一连接器下同一员工被并发绑定两次；
--    执行前请先运行「检查脚本」确认无重复，若有重复需先清理，否则建索引会失败。
-- ----------------------------
-- 检查是否存在重复绑定（正常应返回 0 行）
-- SELECT connector_type, employee_id, COUNT(*) AS cnt
--   FROM integration_user_binding
--  WHERE employee_id IS NOT NULL
--  GROUP BY connector_type, employee_id
-- HAVING COUNT(*) > 1;
--
-- ALTER TABLE integration_user_binding
--   ADD UNIQUE KEY uk_connector_employee (connector_type, employee_id);



-- =============================================================
-- 第 25 段 / 共 42 段：integration_menu_split.sql
-- =============================================================

-- =============================================================
-- 集成模块菜单拆分脚本：钉钉集成 / 企业微信集成 两个独立菜单
-- 版本: v1.0  日期: 2026-11-07
-- 依赖规范: unified-development-standards.md（3.4 菜单权限 M/C/F 三级）
-- 说明: 前端页面已拆分为 integration/attendance/index（钉钉）与 integration/wecom/index（企业微信），
--       两页复用同一业务面板组件；本脚本仅调整菜单结构与角色授权，可重复执行。
-- =============================================================

-- ----------------------------
-- 1. 钉钉菜单名称还原（integration_wecom.sql 曾将其临时改名为「第三方考勤集成」，拆分后恢复）
-- ----------------------------
UPDATE sys_menu SET menu_name = '钉钉集成', remark = '钉钉考勤同步与用户绑定'
WHERE menu_id = 9063010 AND menu_name <> '钉钉集成';

-- ----------------------------
-- 2. 新增菜单：企业微信集成
--    权限点复用 integration:attendance:list：接口层与钉钉共用同一套 Controller，
--    菜单可见性由 sys_role_menu 授权独立控制，无需新增权限字符串。
-- ----------------------------
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT 9063030, '企业微信集成', 9063000, 2, 'wecom', 'integration/wecom/index', '', 1, 0, 'C', '0', '0', 'integration:attendance:list', 'wechat', 'admin', sysdate(), '企业微信考勤同步与用户绑定'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063030);

-- ----------------------------
-- 3. 角色授权：沿用已获钉钉菜单授权的角色，自动补授企业微信菜单
--    （按钮权限 integration:attendance:sync / query / config 两页共用，无需重复授权）
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT DISTINCT r.role_id, 9063030 FROM sys_role_menu r
WHERE r.menu_id = 9063010
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x WHERE x.role_id = r.role_id AND x.menu_id = 9063030);

-- ----------------------------
-- 4. 校验：确认两个菜单与授权情况
-- ----------------------------
-- SELECT menu_id, menu_name, path, component, perms, order_num FROM sys_menu WHERE parent_id = 9063000 ORDER BY order_num;
-- SELECT menu_id, COUNT(*) AS role_cnt FROM sys_role_menu WHERE menu_id IN (9063010, 9063030) GROUP BY menu_id;



-- =============================================================
-- 第 26 段 / 共 42 段：integration_ensure_unique_index.sql
-- =============================================================

-- =============================================================
-- 确保 integration_user_binding 的「一一绑定」唯一索引存在
-- 版本: v1.0  日期: 2026-11-07
--
-- 背景：uk_connector_employee (connector_type, employee_id) 约束
--       「同一连接器下一个员工只能绑定一个外部账号」。该索引在
--       integration_wecom.sql 中曾被注释为「需手工执行」，新环境
--       只跑脚本会缺失约束，导致并发/误操作可重复绑定同一员工。
--
-- 说明：employee_id 允许为 NULL（未匹配的绑定行），MySQL 唯一索引
--       允许多个 NULL，不影响未匹配数据。
-- 幂等：已存在同名索引则跳过，可重复执行。
-- =============================================================

SET @idx_exists := (
  SELECT COUNT(*) FROM information_schema.STATISTICS
   WHERE TABLE_SCHEMA = DATABASE()
     AND TABLE_NAME = 'integration_user_binding'
     AND INDEX_NAME = 'uk_connector_employee'
);

SET @ddl := IF(@idx_exists = 0,
  'ALTER TABLE `integration_user_binding` ADD UNIQUE KEY `uk_connector_employee` (`connector_type`, `employee_id`)',
  'SELECT ''uk_connector_employee 已存在，跳过'' AS result');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 复核：应能看到 uk_connector_employee
SHOW INDEX FROM `integration_user_binding` WHERE Key_name = 'uk_connector_employee';



-- =============================================================
-- 第 27 段 / 共 42 段：module_switch_add_integration.sql
-- =============================================================

-- =============================================================================
-- 模块开关补齐脚本：系统集成（integration）
-- 适用：分支仓（无决策支持 bi 模块）首次接入系统集成模块
-- 版本：v1.0  日期：2026-11-07
--
-- 背景：系统集成（顶层菜单 9063000「系统集成」）已上线，需在 sys_module
--       登记后才能被「系统管理 → 模块开关」按模块停用/启用；同时该模块的
--       4 个定时任务（钉钉/企业微信 通讯录 + 考勤同步）在 INSERT 时未写
--       module_key，必须回填，否则模块停用后任务仍会照跑（幽灵任务）。
--
-- 规范：design/unified-development-standards.md 第 1.2 节「步骤 6：模块开关登记」
-- 幂等：模块登记用 NOT EXISTS 防重；任务回填仅改空值，可重复执行
-- 执行：mysql -uroot -p 库名 < module_switch_add_integration.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记系统集成模块（幂等）
--     root_menu_id = 9063000（系统集成目录），级联范围含钉钉集成 / 企业微信集成
--     order_num = 28：为后续决策支持（bi，27）预留位置
-- -----------------------------------------------------------------------------
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '系统集成', 'integration', 9063000, '0', '0', 28, 'admin', sysdate(), '系统集成模块（钉钉/企业微信），级联菜单 9063000 及其子菜单、定时任务 module_key=integration'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'integration');

-- -----------------------------------------------------------------------------
-- 二、定时任务回填 module_key（级联停用/恢复依赖该字段，为空则不会被级联）
--     注意：括号必须包住 OR 分支，否则会因 AND 优先级误改全部 NULL 任务
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'integration'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND (   `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
        OR `invoke_target` LIKE 'wecomAttendanceSyncTask%');

-- -----------------------------------------------------------------------------
-- 三、角色回填 module_key
--     说明：本模块未设专属角色（菜单权限复用现有角色 + sys_role_menu 授权），
--           无角色级联对象，故默认不处理。后续若新增 integration_* 角色，
--           可解除下方注释执行回填。
-- -----------------------------------------------------------------------------
-- UPDATE `sys_role` SET `module_key` = 'integration' WHERE `role_key` LIKE 'integration\_%' AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 四、执行结果复核
-- -----------------------------------------------------------------------------
-- 4.1 模块清单（应能看到 integration，job_count = 4）
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 WHERE m.module_key = 'integration';

-- 4.2 级联菜单范围（含目录、页面与按钮级权限）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = 9063000
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT '系统集成(integration)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu;

-- 4.3 定时任务归属复核（应无 NULL）
SELECT job_id, job_name, invoke_target, module_key, status FROM `sys_job`
 WHERE invoke_target LIKE 'dingtalkAttendanceSyncTask%'
    OR invoke_target LIKE 'wecomAttendanceSyncTask%'
 ORDER BY job_id;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- 注意：回滚后该模块将从模块开关页面消失，其定时任务不再受模块级停用控制
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` = 'integration';
-- UPDATE `sys_job` SET `module_key` = NULL
--  WHERE `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
--     OR `invoke_target` LIKE 'wecomAttendanceSyncTask%';



-- =============================================================
-- 第 28 段 / 共 42 段：integration_mock_cleanup.sql
-- =============================================================

-- ----------------------------
-- 钉钉模拟数据清理脚本
-- 用途：mock 模式（integration.dingtalk.mockEnabled=true）验证完成后，一键清除所有模拟痕迹
-- 执行环境：ry-vue
--
-- 模拟数据识别规则：
--   1) 绑定表   integration_user_binding.external_userid 以 mock_ 开头
--   2) 同步日志 integration_sync_log.result_msg 含「模拟」
--   3) 考勤记录 biz_hr_attendance_record.remark = '【模拟】钉钉同步'
-- ----------------------------

-- ① 先看将删除多少数据（建议先跑查询确认）
SELECT '绑定' AS biz, COUNT(*) AS cnt FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';

-- ② 删除模拟绑定
DELETE FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%';

-- ③ 删除模拟同步日志
DELETE FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%';

-- ④ 删除模拟考勤记录
DELETE FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';

-- ⑤ 关闭模拟模式
--    注意：直接改库不会刷新后端缓存（sys_config 有 Redis/Spring 缓存），
--    推荐在「钉钉集成 - 集成配置」弹窗关闭「模拟数据模式」开关，会自动清缓存。
--    如需库内强制关闭，改完后需重启后端或清 Redis 键：sys_config:integration.dingtalk.mockEnabled
UPDATE sys_config SET config_value = 'false' WHERE config_key = 'integration.dingtalk.mockEnabled';

-- ⑥ 复核：三项应全部为 0
SELECT '绑定' AS biz, COUNT(*) AS remain FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';



-- =============================================================
-- 第 29 段 / 共 42 段：integration_wecom_mock_cleanup.sql
-- =============================================================

-- ----------------------------
-- 企业微信模拟数据清理脚本
-- 用途：mock 模式（integration.wecom.mockEnabled=true）验证完成后，一键清除所有模拟痕迹
-- 执行环境：ry-vue
--
-- 模拟数据识别规则：
--   1) 绑定表   integration_user_binding.connector_type='wecom' 且 external_userid 以 mockwecom_ 开头
--   2) 同步日志 integration_sync_log.connector_type='wecom' 且 result_msg 含「模拟」
--   3) 考勤记录 biz_hr_attendance_record.remark = '【模拟】企业微信同步'
--
-- 注意：本脚本仅清理企业微信模拟数据，不影响钉钉的真实同步数据；
--       钉钉模拟数据请执行 integration_mock_cleanup.sql。
-- ----------------------------

-- ① 先看将删除多少数据（建议先跑查询确认）
SELECT '绑定' AS biz, COUNT(*) AS cnt FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';

-- ② 删除模拟绑定
DELETE FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%';

-- ③ 删除模拟同步日志
DELETE FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%';

-- ④ 删除模拟考勤记录
DELETE FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';

-- ⑤ 关闭模拟模式
--    注意：直接改库不会刷新后端缓存（sys_config 有 Spring 缓存），
--    推荐在「第三方考勤集成 → 企业微信 → 集成配置」弹窗关闭「模拟数据模式」开关，会自动刷新。
--    如需库内强制关闭，改完后需重启后端。
UPDATE sys_config SET config_value = 'false' WHERE config_key = 'integration.wecom.mockEnabled';

-- ⑥ 复核：三项应全部为 0
SELECT '绑定' AS biz, COUNT(*) AS remain FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';



-- =============================================================
-- 第 30 段 / 共 42 段：module_switch_fill_job_module_key_20261107.sql
-- =============================================================

-- ============================================================
-- 定时任务模块归属回填（补 sys_job.module_key 漏项）
-- 日期：2026-11-07
--
-- 背景：模块开关停用模块时，SysModuleServiceImpl.disableModuleJobs()
--       的过滤条件是 module_key = #{moduleKey}。module_key 为空的任务
--       永远不会随模块停用而停止，形成“模块已停、任务照跑”的幽灵任务；
--       enableModuleJobs() 同样无法恢复。
-- 规范：design/unified-development-standards.md 第 1.2 节「步骤 6：模块开关登记」
--
-- 安全性：
--   1) 仅更新 module_key 为 NULL/空的记录，不覆盖任何已有归属；
--   2) 按 invoke_target 精确匹配，不依赖 job_id（各分支库 id 不一致）；
--   3) 可重复执行，二次执行影响 0 行。
--
-- 归属判定：以 Task 类所在 Maven 模块为准；代码在公共模块但服务特定业务
--           的按功能菜单挂载的一级模块归属（如 food 批次任务归 wms）。
-- ============================================================

-- 1. 食品批次临期预警与过期自动冻结 → 仓储物流（wms）
--    代码在 com.ruoyi.food.task，功能菜单挂在「仓储管理 → 批次管理」下
UPDATE sys_job SET module_key = 'wms'
 WHERE invoke_target LIKE 'foodBatchAlertTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 2. 健康证到期提醒 → 人力资源（hr）
--    代码在 com.ruoyi.hr.task.HrCertAlertTask
UPDATE sys_job SET module_key = 'hr'
 WHERE invoke_target LIKE 'hrCertAlertTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 3. 考试状态兜底任务 → 安全生产（safety）
--    代码在 com.ruoyi.safety.task.SafetyExamTask（注意：不属 hr）
UPDATE sys_job SET module_key = 'safety'
 WHERE invoke_target LIKE 'safetyExamTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 4. 安全守则学习逾期提醒 → 安全生产（safety）
--    代码在 com.ruoyi.safety.task.SafetyRuleOverdueTask
UPDATE sys_job SET module_key = 'safety'
 WHERE invoke_target LIKE 'safetyRuleOverdueTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 5. 客诉供应商整改逾期升级提醒 → 质量管理（qms）
--    代码在 com.ruoyi.qms.task.SupplierRectifyOverdueTask
UPDATE sys_job SET module_key = 'qms'
 WHERE invoke_target LIKE 'supplierRectifyOverdueTask.%'
   AND (module_key IS NULL OR module_key = '');

-- 6. 营销业绩月度归集 → 人力资源（hr）
--    代码在 com.ruoyi.hr.task.HrPerfAggTask（按代码模块归属，非营销 mk）
UPDATE sys_job SET module_key = 'hr'
 WHERE invoke_target LIKE 'hrPerfAggTask.%'
   AND (module_key IS NULL OR module_key = '');

-- ------------------------------------------------------------
-- 验收 1：应返回 0 行（无归属不明的定时任务）
SELECT job_id, job_name, invoke_target
  FROM sys_job
 WHERE module_key IS NULL OR module_key = '';

-- 验收 2：模块与任务数对应表，便于发现漏回填
SELECT m.module_key, m.module_name, COUNT(j.job_id) AS job_count
  FROM sys_module m
  LEFT JOIN sys_job j ON j.module_key = m.module_key
 GROUP BY m.module_key, m.module_name
 ORDER BY m.order_num;



-- =============================================================
-- 第 31 段 / 共 42 段：fix_wms_warehouse_zone_level.sql
-- =============================================================

-- ============================================================
-- 修复：仓库结构树缺库区层级（仓库下直接挂仓位）
-- 库：ruoyi_lyjs / edop_jsly（客户库通用，已去硬编码 USE）
-- 内容：
--   1. 每个仓库下补一个库区（node_type='2'，存储区，编码=仓库编码+A01）
--   2. 把直接挂在仓库下的仓位（node_type='3'）挪到对应库区下，重算 ancestors
-- 说明：
--   - 幂等：库区仅在“该仓库下无库区”时插入；重复执行不会产生重复数据
--   - 仓位主键 warehouse_id 与编码（K01W01 等）均不变，wms_inventory 的
--     location_id 引用不受影响；仅调整 parent_id / ancestors
--   - 库区 area_type='0'（存储区），dept/manager 沿用所属仓库
-- 日期：2026-11-06
-- ============================================================
-- （已移除 USE ruoyi_lyjs；库名由执行命令/提示符决定，适配客户库 edop_jsly）

-- 1. 补库区（幂等）
INSERT INTO wms_warehouse
    (parent_id, ancestors, node_type, warehouse_code, warehouse_name,
     dept_id, manager_id, area_type, status, del_flag, create_by, create_time)
SELECT w.warehouse_id,
       CONCAT(w.ancestors, ',', w.warehouse_id),
       '2',
       CONCAT(w.warehouse_code, 'A01'),
       CONCAT(w.warehouse_name, '-存储区'),
       w.dept_id, w.manager_id, '0', '0', '0', 'admin', NOW()
FROM wms_warehouse w
WHERE w.node_type = '1' AND w.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM wms_warehouse z
                  WHERE z.parent_id = w.warehouse_id
                    AND z.node_type = '2' AND z.del_flag = '0');

-- 2. 仓位挪到库区下（仅处理父节点仍是仓库的仓位；幂等）
UPDATE wms_warehouse loc
JOIN wms_warehouse p ON p.warehouse_id = loc.parent_id
                    AND p.node_type = '1' AND p.del_flag = '0'
JOIN wms_warehouse z ON z.parent_id = p.warehouse_id
                    AND z.node_type = '2' AND z.del_flag = '0'
SET loc.parent_id = z.warehouse_id,
    loc.ancestors = CONCAT(z.ancestors, ',', z.warehouse_id),
    loc.update_by = 'admin',
    loc.update_time = NOW()
WHERE loc.node_type = '3' AND loc.del_flag = '0';



-- =============================================================
-- 第 32 段 / 共 42 段：module_switch_add_bi_integration.sql
-- =============================================================

-- =============================================================================
-- 模块开关补齐脚本：决策支持（bi）、系统集成（integration）
-- 背景：决策支持（顶层菜单 9062263「决策支持」）与系统集成（顶层菜单
--       9063000「系统集成」）两个模块已上线，但 sys_module 未登记，导致
--       「系统管理 → 模块开关」页面缺少这两项，无法按模块停用/启用；
--       同时系统集成的 4 个定时任务 module_key 为空，即使登记模块也无法级联。
-- 适用：已执行过 module_switch_init_ryvue.sql 的环境（增量执行，幂等，可重复跑）
-- 执行：mysql -uroot -p 库名 < module_switch_add_bi_integration.sql
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 一、登记决策支持模块（幂等：已存在则跳过）
--     root_menu_id = 9062263（决策支持目录），级联范围含驾驶舱/指标字典/各业务看板
-- -----------------------------------------------------------------------------
SET @biRootMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' AND parent_id = 0 LIMIT 1);
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '决策支持', 'bi', @biRootMenuId, '0', '0', 27, 'admin', sysdate(), '决策支持模块（BI），级联「决策支持」目录及其子菜单、定时任务 module_key=bi'
FROM DUAL
WHERE @biRootMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'bi');

-- -----------------------------------------------------------------------------
-- 二、登记系统集成模块（幂等）
--     root_menu_id 动态取「系统集成」一级目录（合并版改为按名称查询，适配客户库自增 id），级联范围含钉钉集成 / 企业微信集成
-- -----------------------------------------------------------------------------
SET @itgRootMenuId = (SELECT menu_id FROM sys_menu WHERE menu_name = '系统集成' AND menu_type = 'M' AND parent_id = 0 LIMIT 1);
INSERT INTO `sys_module` (`module_name`, `module_key`, `root_menu_id`, `status`, `is_core`, `order_num`, `create_by`, `create_time`, `remark`)
SELECT '系统集成', 'integration', @itgRootMenuId, '0', '0', 28, 'admin', sysdate(), '系统集成模块（钉钉/企业微信），级联「系统集成」目录及其子菜单、定时任务 module_key=integration'
FROM DUAL
WHERE @itgRootMenuId IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM `sys_module` WHERE `module_key` = 'integration');

-- -----------------------------------------------------------------------------
-- 三、定时任务回填 module_key（级联停用/恢复依赖该字段，为空则不会被级联）
--     3.1 系统集成：钉钉通讯录/考勤同步、企业微信通讯录/考勤同步
--     3.2 决策支持：BI 指标快照/对账/阈值预警（新建时已写 bi，此处兜底历史数据）
-- -----------------------------------------------------------------------------
UPDATE `sys_job` SET `module_key` = 'integration'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND (   `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
        OR `invoke_target` LIKE 'wecomAttendanceSyncTask%');

UPDATE `sys_job` SET `module_key` = 'bi'
 WHERE (`module_key` IS NULL OR `module_key` = '')
   AND `invoke_target` LIKE 'biMetricSnapshotTask%';

-- -----------------------------------------------------------------------------
-- 四、角色回填 module_key
--     说明：这两模块当前未设专属角色（菜单权限复用现有角色 + sys_role_menu 授权），
--           无角色级联对象，故默认不处理。后续若新增 bi_*/integration_* 角色，
--           可解除下方注释执行回填。
-- -----------------------------------------------------------------------------
-- UPDATE `sys_role` SET `module_key` = 'bi'          WHERE `role_key` LIKE 'bi\_%'          AND (`module_key` IS NULL OR `module_key` = '');
-- UPDATE `sys_role` SET `module_key` = 'integration' WHERE `role_key` LIKE 'integration\_%' AND (`module_key` IS NULL OR `module_key` = '');

-- -----------------------------------------------------------------------------
-- 五、执行结果复核
-- -----------------------------------------------------------------------------
-- 5.1 模块清单（应能看到 bi / integration，job_count 分别为 3 / 4）
SELECT m.module_id, m.module_name, m.module_key, m.root_menu_id, m.status, m.is_core, m.order_num,
       (SELECT COUNT(*) FROM `sys_role` r WHERE r.module_key = m.module_key AND r.del_flag = '0') AS role_count,
       (SELECT COUNT(*) FROM `sys_job` j WHERE j.module_key = m.module_key) AS job_count
  FROM `sys_module` m
 ORDER BY m.order_num;

-- 5.2 级联菜单范围（含目录、页面与按钮级权限，实际范围随菜单增减变化）
WITH RECURSIVE sub_menu AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = @biRootMenuId
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu sm ON cm.parent_id = sm.menu_id
)
SELECT '决策支持(bi)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu;

WITH RECURSIVE sub_menu2 AS (
  SELECT menu_id FROM `sys_menu` WHERE menu_id = @itgRootMenuId
  UNION ALL
  SELECT cm.menu_id FROM `sys_menu` cm INNER JOIN sub_menu2 sm ON cm.parent_id = sm.menu_id
)
SELECT '系统集成(integration)' AS module_name, COUNT(*) AS cascade_menu_count FROM sub_menu2;

-- 5.3 定时任务归属复核（应无 NULL）
SELECT job_id, job_name, invoke_target, module_key, status FROM `sys_job`
 WHERE invoke_target LIKE 'biMetricSnapshotTask%'
    OR invoke_target LIKE 'dingtalkAttendanceSyncTask%'
    OR invoke_target LIKE 'wecomAttendanceSyncTask%'
 ORDER BY job_id;

-- =============================================================================
-- 回滚脚本（仅注释备用，需要时手工解除注释执行）
-- 注意：回滚后这两个模块将从模块开关页面消失，其定时任务不再受模块级停用控制
-- =============================================================================
-- DELETE FROM `sys_module` WHERE `module_key` IN ('bi', 'integration');
-- UPDATE `sys_job` SET `module_key` = NULL
--  WHERE `invoke_target` LIKE 'dingtalkAttendanceSyncTask%'
--     OR `invoke_target` LIKE 'wecomAttendanceSyncTask%';



-- =============================================================
-- 第 33 段 / 共 42 段：dms_wms_number_rules.sql
-- =============================================================

-- DMS和WMS编号规则初始化
-- 执行前请确保 mk_number_rule 表已存在

-- =============================================
-- 0. 增加 module 及动态前缀相关字段（兼容旧版数据库）
-- =============================================
SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='module');
SET @ddl = IF(@has_col=0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `module` VARCHAR(20) DEFAULT ''mk'' COMMENT ''所属模块(mk=营销,dms=设备,wms=仓储,pms=采购,safety=安全生产)'' AFTER `rule_code`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='prefix_field');
SET @ddl = IF(@has_col=0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field` VARCHAR(50) DEFAULT NULL COMMENT ''动态前缀关联字段名'' AFTER `connector`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='prefix_field_dict_type');
SET @ddl = IF(@has_col=0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_dict_type` VARCHAR(100) DEFAULT NULL COMMENT ''动态前缀关联字典类型'' AFTER `prefix_field`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @has_col = (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema=DATABASE() AND table_name='mk_number_rule' AND column_name='prefix_field_enabled');
SET @ddl = IF(@has_col=0, 'ALTER TABLE `mk_number_rule` ADD COLUMN `prefix_field_enabled` CHAR(1) DEFAULT ''0'' COMMENT ''是否启用动态前缀(0=否,1=是)'' AFTER `prefix_field_dict_type`', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS `mk_number_rule_prefix` (
  `prefix_id`     BIGINT(20)    NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
  `rule_id`       BIGINT(20)    NOT NULL                 COMMENT '规则ID',
  `field_value`   VARCHAR(50)   NOT NULL                 COMMENT '字段值（字典值）',
  `field_label`   VARCHAR(100)  DEFAULT ''               COMMENT '字段标签（字典标签）',
  `prefix`        VARCHAR(20)   DEFAULT ''               COMMENT '对应前缀',
  `enabled`       CHAR(1)       DEFAULT '1'              COMMENT '是否启用（0=否,1=是）',
  `current_seq`   BIGINT(20)    DEFAULT 0                COMMENT '当前序列号（按动态前缀重置时使用）',
  `current_date_str` VARCHAR(20) DEFAULT ''              COMMENT '当前日期串',
  `create_by`     VARCHAR(64)   DEFAULT ''               COMMENT '创建者',
  `create_time`   DATETIME                               COMMENT '创建时间',
  `update_by`     VARCHAR(64)   DEFAULT ''               COMMENT '更新者',
  `update_time`   DATETIME                               COMMENT '更新时间',
  PRIMARY KEY (`prefix_id`),
  KEY `idx_rule_id` (`rule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='编号规则动态前缀映射表';

-- 更新已有规则的模块归属
UPDATE `mk_number_rule` SET module='mk' WHERE rule_code IN ('activity','customer','lead','opportunity','order','contract');
UPDATE `mk_number_rule` SET module='dms' WHERE rule_code LIKE 'dms_%';
UPDATE `mk_number_rule` SET module='wms' WHERE rule_code LIKE 'wms_%';

-- DMS 设备管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_equipment', '设备编号', 'SB', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '设备编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_equipment');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_part', '备件编号', 'BJ', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_part');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_in', '备件入库单据号', 'BJIN', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件入库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_in');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_spare_out', '备件出库单据号', 'BJOUT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '备件出库单据号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_spare_out');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_work_order', '工单号', 'WO', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '维修工单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_work_order');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_route', '巡检路线编号', 'RT', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '巡检路线编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_route');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'dms_inspection_task', '点检任务编号', 'INS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '点检任务编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='dms_inspection_task');

-- WMS 仓库管理模块编号规则
INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material', '物料编号', 'WL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '物料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `auto_enabled`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_warehouse', '仓库编码', 'WH', '', '0', 3, 1, 1, 0, '', '', '1', '0', 'admin', sysdate(), '仓库编码，不重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_warehouse');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_supplier', '供应商编码', 'GYS', 'yyyyMMdd', '1', 4, 1, 1, 0, '', '', '0', 'admin', sysdate(), '供应商编码，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_supplier');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_inbound', '入库单号', 'IN', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '入库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_inbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_outbound', '出库单号', 'OUT', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '出库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_outbound');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_move', '移库单号', 'MV', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '移库单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_move');

INSERT INTO `mk_number_rule` (`rule_code`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_stocktake', '盘点单号', 'ST', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '盘点单号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_stocktake');

-- =============================================
-- 在设备管理、仓储管理菜单下添加编号规则入口
-- =============================================
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 3000, 99, 'dmsNumberRule', 'mk/numberRule/index', '', 'DmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '设备管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=3000);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '编号规则', 2000, 99, 'wmsNumberRule', 'mk/numberRule/index', '', 'WmsNumberRule', 1, 0, 'C', '0', '0', 'marketing:numberRule:list', 'number', 'admin', sysdate(), '仓储管理编号规则'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name='编号规则' AND parent_id=2000);

-- 给admin角色分配编号规则菜单权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE menu_name='编号规则' AND parent_id IN (3000, 2000)
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 更新各模块编号规则菜单的query参数，传递module标识
-- =============================================
UPDATE sys_menu SET query='module=mk' WHERE menu_name='编号规则' AND parent_id=4000;
UPDATE sys_menu SET query='module=dms' WHERE menu_name='编号规则' AND parent_id=3000;
UPDATE sys_menu SET query='module=wms' WHERE menu_name='编号规则' AND parent_id=2000;

-- =============================================
-- 物料按类型分组编码规则（原材料/半成品/成品/辅料）
-- =============================================
INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_0', 'wms', '物料编号-原材料', 'YL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '原材料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_0');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_1', 'wms', '物料编号-半成品', 'BC', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '半成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_1');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_2', 'wms', '物料编号-成品', 'CP', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '成品编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_2');

INSERT INTO `mk_number_rule` (`rule_code`, `module`, `rule_name`, `prefix`, `date_format`, `reset_type`, `seq_length`, `seq_start`, `step`, `current_seq`, `current_date_str`, `connector`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'wms_material_3', 'wms', '物料编号-辅料', 'FL', 'yyyyMMdd', '1', 5, 1, 1, 0, '', '', '0', 'admin', sysdate(), '辅料编号，每日重置'
WHERE NOT EXISTS (SELECT 1 FROM mk_number_rule WHERE rule_code='wms_material_3');



-- =============================================================
-- 第 34 段 / 共 42 段：edop_connector_grant.sql
-- =============================================================

-- ==================================================================
-- EDOP 连接器逐用户授权（Phase 2-2.2）
-- 表：edop_connector_grant（grant_token 哈希入库，明文仅颁发时返回一次）
-- 菜单：系统管理 → 连接器授权（C）+ 查询/吊销按钮（F）
-- 规范：3.1/3.6 显式 COLLATE；3.4 M/C/F 三级
-- ==================================================================

-- 1. 授权表
CREATE TABLE IF NOT EXISTS `edop_connector_grant` (
  `grant_id`         BIGINT(20)   NOT NULL AUTO_INCREMENT COMMENT '授权ID',
  `user_id`          BIGINT(20)   NOT NULL                COMMENT 'EDOP用户ID',
  `grant_token_hash` CHAR(64)     NOT NULL                COMMENT 'grant_token的SHA-256哈希',
  `phone_snapshot`   VARCHAR(20)  DEFAULT ''              COMMENT '授权时手机号快照(换绑检测)',
  `client_desc`      VARCHAR(100) DEFAULT ''              COMMENT '授权来源描述',
  `status`           CHAR(1)      NOT NULL DEFAULT '0'    COMMENT '状态（0正常 1停用）',
  `expire_time`      DATETIME     DEFAULT NULL            COMMENT '过期时间(90天滚动续期)',
  `last_used_time`   DATETIME     DEFAULT NULL            COMMENT '最近使用时间(僵尸授权依据)',
  `create_by`        VARCHAR(64)  DEFAULT ''              COMMENT '创建者',
  `create_time`      DATETIME     DEFAULT NULL            COMMENT '创建时间',
  `update_by`        VARCHAR(64)  DEFAULT ''              COMMENT '更新者',
  `update_time`      DATETIME     DEFAULT NULL            COMMENT '更新时间',
  `remark`           VARCHAR(255) DEFAULT NULL            COMMENT '备注',
  `del_flag`         CHAR(1)      DEFAULT '0'             COMMENT '删除标志（0存在 2删除）',
  PRIMARY KEY (`grant_id`),
  UNIQUE KEY `uk_token_hash` (`grant_token_hash`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='连接器逐用户授权表';

-- 2. 菜单（挂在 系统管理=1 下）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '连接器授权', 1, 12, 'connectorGrant', 'system/connectorGrant/index', 1, 0, 'C', '0', '0', 'edop:grant:list', 'guide', 'admin', SYSDATE(), 'EDOP连接器逐用户授权管理菜单'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='edop:grant:list');

SET @grantMenuId = (SELECT menu_id FROM sys_menu WHERE perms='edop:grant:list' LIMIT 1);

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '授权查询', @grantMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'edop:grant:query', '#', 'admin', SYSDATE()
FROM DUAL WHERE @grantMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='edop:grant:query');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time)
SELECT '授权吊销', @grantMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'edop:grant:revoke', '#', 'admin', SYSDATE()
FROM DUAL WHERE @grantMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='edop:grant:revoke');



-- =============================================================
-- 第 35 段 / 共 42 段：edop_mcp_service_account.sql
-- =============================================================

-- ----------------------------------------------------------------------------
-- EDOP × WorkBuddy 连接器：服务账号初始化脚本
-- 脚本: edop_mcp_service_account.sql
-- 用途: 新建 MCP 网关专用只读服务账号 edop-mcp 及其角色（仅 5 个查询工具所需权限）
-- 规范: 主规范 3.4 菜单权限 / 3.5 数据权限；按 perms 动态授权，幂等可重跑
-- 注意: 密码哈希对应明文 Icx7RZ-2N9XDjQk3（已写入 mcp-gateway/config.json，勿入库、勿提交）
-- ----------------------------------------------------------------------------

-- 1. 服务账号角色（只读，全部数据权限——服务账号代表平台级查询，
--    客户交付时如需部门隔离，将 data_scope 改为 '5' 并调整部门归属）
INSERT INTO sys_role (role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_by, create_time, remark)
SELECT 'EDOP-MCP只读服务', 'edop_mcp_readonly', 99, '1', 1, 1, '0', '0', 'admin', NOW(), 'WorkBuddy 连接器专用只读服务账号角色（勿人工登录）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_role WHERE role_key = 'edop_mcp_readonly' AND del_flag = '0');

-- 2. 服务账号（部门挂载到一级部门下第一个部门）
INSERT INTO sys_user (dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_by, create_time, remark)
SELECT (SELECT dept_id FROM sys_dept WHERE status = '0' AND del_flag = '0' ORDER BY parent_id, order_num LIMIT 1),
       'edop-mcp', 'MCP服务账号', '00', '', '', '2', '',
       '$2a$10$bbMq9tKvDJT2Of744WRJgO/ipX7HZKgrSDzuQ0BbGXolRZQBxnBTO',
       '0', '0', 'admin', NOW(), 'WorkBuddy 连接器专用服务账号（勿人工登录、勿改密）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_user WHERE user_name = 'edop-mcp' AND del_flag = '0');

-- 3. 用户-角色关联
INSERT INTO sys_user_role (user_id, role_id)
SELECT u.user_id, r.role_id
FROM sys_user u, sys_role r
WHERE u.user_name = 'edop-mcp' AND u.del_flag = '0'
  AND r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND NOT EXISTS (SELECT 1 FROM sys_user_role ur WHERE ur.user_id = u.user_id AND ur.role_id = r.role_id);

-- 4. 角色-菜单授权（按权限字符动态关联，覆盖 5 个工具所需 8 个权限点：
--    bi:dashboard:view / bi:metric:list / wms:inventory:list / wms:statistics:list
--    marketing:order:list / marketing:report:view / pms:order:list / pms:dashboard:view）
--    注意：RuoYi 菜单树授权需连带父级 M/C 菜单，否则 hasPermi 部分实现会校验菜单链，
--    此处同时授权按钮本身与其父级菜单，保证权限解析完整。
INSERT INTO sys_role_menu (role_id, menu_id)
WITH RECURSIVE target_menus AS (
    SELECT m.menu_id, m.parent_id
    FROM sys_menu m
    WHERE m.perms IN (
        'bi:dashboard:view', 'bi:metric:list',
        'wms:inventory:list', 'wms:statistics:list',
        'marketing:order:list', 'marketing:report:view',
        'pms:order:list', 'pms:dashboard:view'
    )
    UNION ALL
    SELECT p.menu_id, p.parent_id
    FROM sys_menu p
    JOIN target_menus t ON p.menu_id = t.parent_id
)
SELECT r.role_id, tm.menu_id
FROM sys_role r
CROSS JOIN (SELECT DISTINCT menu_id FROM target_menus) tm
WHERE r.role_key = 'edop_mcp_readonly' AND r.del_flag = '0'
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.role_id AND rm.menu_id = tm.menu_id
  );

-- 5. 校验查询（执行后人工核对）
SELECT '角色' AS item, role_id, role_name, role_key, data_scope FROM sys_role WHERE role_key = 'edop_mcp_readonly' AND del_flag = '0';
SELECT '账号' AS item, user_id, user_name, nick_name, status FROM sys_user WHERE user_name = 'edop-mcp' AND del_flag = '0';
SELECT '授权菜单数' AS item, COUNT(*) AS menu_count FROM sys_role_menu rm JOIN sys_role r ON r.role_id = rm.role_id WHERE r.role_key = 'edop_mcp_readonly';



-- =============================================================
-- 第 36 段 / 共 42 段：fix_collation_unify.sql
-- =============================================================

-- =============================================================================
-- 数据库字符集统一脚本（utf8mb4 / utf8mb4_general_ci）
-- 背景：同一库内混用 utf8mb4_0900_ai_ci / utf8mb4_general_ci / utf8_bin，
--       跨表 JOIN 时触发 MySQL error 1267：
--       Illegal mix of collations (utf8mb4_general_ci,IMPLICIT) and
--       (utf8mb4_0900_ai_ci,IMPLICIT) for operation '='
--       典型表现：QMS IQC 自动生成定时任务（每日 01:00）失败。
-- 方案：库默认 + 全表 + 全列统一为 utf8mb4 / utf8mb4_general_ci
-- 适用：ry-vue（标准）/ ruoyi_lyjs / ruoyi_cfsp / ruoyi_dwx
-- 执行：先备份，再 mysql -uroot -p < fix_collation_unify.sql
-- 注意：409 张表转换需数分钟，请在业务低峰执行；脚本幂等，重复执行安全
-- =============================================================================

-- =============================================================================
-- 步骤 0：执行前体检（记录转换前状态，便于对比）
-- =============================================================================
SELECT '=== 转换前：表级排序规则分布 ===' AS stage;
SELECT table_collation, COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
 GROUP BY table_collation ORDER BY table_count DESC;

SELECT '=== 转换前：列级排序规则分布 ===' AS stage;
SELECT collation_name, COUNT(*) AS column_count
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND collation_name IS NOT NULL
 GROUP BY collation_name ORDER BY column_count DESC;

-- =============================================================================
-- 步骤 1：动态生成全表转换语句并执行
-- 说明：对每张非 general_ci 的基表执行 CONVERT TO CHARACTER SET，
--       该语句会同时重写表默认字符集与所有字符型列的排序规则。
-- =============================================================================
-- 关键：Flowable 引擎表的字符集是分裂的（act_ge_*/act_hi_*/act_id_*/act_re_*/act_ru_*
-- 为 utf8_bin，其余 act_* 已是 utf8mb4_general_ci），且表间存在 65 个外键约束
-- （如 ACT_FK_BYTEARR_DEPL: act_ge_bytearray.DEPLOYMENT_ID_ -> act_re_deployment.ID_）。
-- 逐表转换时外键两端排序规则会瞬间不一致，触发 ERROR 3780。
-- 因此必须在同一会话内临时关闭外键检查，整批转换完成后再恢复。
SET @OLD_FK_CHECKS = @@FOREIGN_KEY_CHECKS;
SET FOREIGN_KEY_CHECKS = 0;

DROP PROCEDURE IF EXISTS `sp_unify_collation`;
DELIMITER $$
CREATE PROCEDURE `sp_unify_collation`()
BEGIN
    DECLARE done INT DEFAULT 0;
    DECLARE tname VARCHAR(128);
    DECLARE tcoll VARCHAR(64);
    DECLARE cnt INT DEFAULT 0;
    DECLARE cur CURSOR FOR
        SELECT table_name, IFNULL(table_collation, '')
          FROM information_schema.tables
         WHERE table_schema = DATABASE()
           AND table_type = 'BASE TABLE'
           AND (table_collation IS NULL
                OR table_collation NOT IN ('utf8mb4_general_ci'));
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = 1;

    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO tname, tcoll;
        IF done = 1 THEN
            LEAVE read_loop;
        END IF;
        SET @sql = CONCAT('ALTER TABLE `', tname,
                          '` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci');
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        SET cnt = cnt + 1;
    END LOOP;
    CLOSE cur;

    SELECT CONCAT('已转换表数量: ', cnt) AS result;
END$$
DELIMITER ;

CALL `sp_unify_collation`();
DROP PROCEDURE IF EXISTS `sp_unify_collation`;

-- 恢复外键检查（整批转换完成后）
SET FOREIGN_KEY_CHECKS = @OLD_FK_CHECKS;

-- 外键完整性验证（应返回空结果集）
SELECT '=== 外键完整性验证（应为空）===' AS stage;
SELECT constraint_name, table_name
  FROM information_schema.table_constraints
 WHERE table_schema = DATABASE() AND constraint_type = 'FOREIGN KEY'
   AND constraint_name NOT IN (
       SELECT constraint_name FROM information_schema.key_column_usage
        WHERE table_schema = DATABASE() AND referenced_table_name IS NOT NULL);

-- =============================================================================
-- 步骤 2：视图重建（视图不随基表自动转换，需重建才能继承新排序规则）
-- 说明：若库中存在视图，其字符集由定义时的列决定；此处仅提示人工确认。
-- =============================================================================
SELECT '=== 视图清单（如有，请评估是否需要重建）===' AS stage;
SELECT table_name, table_collation
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'VIEW';

-- =============================================================================
-- 步骤 3：库默认字符集统一
-- =============================================================================
ALTER DATABASE CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

-- =============================================================================
-- 步骤 4：转换后复核（应只剩 utf8mb4_general_ci 一种）
-- =============================================================================
SELECT '=== 转换后：表级排序规则分布 ===' AS stage;
SELECT table_collation, COUNT(*) AS table_count
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
 GROUP BY table_collation ORDER BY table_count DESC;

SELECT '=== 转换后：列级排序规则分布 ===' AS stage;
SELECT collation_name, COUNT(*) AS column_count
  FROM information_schema.columns
 WHERE table_schema = DATABASE() AND collation_name IS NOT NULL
 GROUP BY collation_name ORDER BY column_count DESC;

SELECT '=== 转换后：库默认字符集 ===' AS stage;
SELECT default_character_set_name, default_collation_name
  FROM information_schema.schemata WHERE schema_name = DATABASE();

SELECT '=== 残留异常表（应为空）===' AS stage;
SELECT table_name, table_collation
  FROM information_schema.tables
 WHERE table_schema = DATABASE() AND table_type = 'BASE TABLE'
   AND (table_collation IS NULL OR table_collation <> 'utf8mb4_general_ci');

-- =============================================================================
-- 步骤 5：跨模块 JOIN 验证（应无 error 1267）
-- =============================================================================
SELECT '=== 跨模块 JOIN 验证 ===' AS stage;
SELECT COUNT(*) AS join_ok_count
  FROM qms_insp_task a
  JOIN wms_material b ON a.material_code = b.material_code;

-- =============================================================================
-- 回滚说明：字符集转换不可逆（原始排序规则信息已丢失）。
-- 如需回退，请使用执行前的全库备份恢复：
--   mysqldump -uroot -p 库名 > backup_before_collation.sql
-- =============================================================================

-- =============================================================
-- 第 37 段 / 共 42 段：菜单同父 path 冲突自愈（必须在 restructure 段之后执行）
-- 背景：第 4 段两处插入判重键不一致会插入隐藏的重复菜单（项目附件）；
--       第 16 段 restructure 挪动会把 bid 看板 path 重置为 dashboard 与营销统计分析冲突。
--       本段在每次执行时自愈：A 类完全重复对（同 parent+path+component）保留可见项；
--       B 类 bid 看板 path 改为 bid-dashboard 消除路由冲突。幂等，可重复执行。
-- =============================================================

-- 1. A 类完全重复对：角色授权迁移到保留项（幂等，NOT EXISTS 防重）
--    保留项 = visible='0' 优先，其次 menu_id 最小
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT rm.role_id, k.keep_id
FROM sys_role_menu rm
JOIN (
    SELECT c.menu_id AS dup_id,
           (SELECT x.menu_id FROM sys_menu x
             WHERE x.parent_id = c.parent_id AND x.path = c.path AND x.menu_type = 'C'
             ORDER BY (x.visible = '0') DESC, x.menu_id
             LIMIT 1) AS keep_id
    FROM sys_menu c
    WHERE c.menu_type = 'C'
      AND EXISTS (SELECT 1 FROM sys_menu d
                   WHERE d.parent_id = c.parent_id AND d.path = c.path
                     AND d.menu_type = 'C' AND d.menu_id <> c.menu_id
                     AND d.component = c.component)
) k ON rm.menu_id = k.dup_id
WHERE k.dup_id <> k.keep_id
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu x
                   WHERE x.role_id = rm.role_id AND x.menu_id = k.keep_id);

-- 2. A 类完全重复对：重复项下的按钮（F）挂到保留项（保留项无同 perms 按钮时移动）
UPDATE sys_menu b
JOIN (
    SELECT c.menu_id AS dup_id,
           (SELECT x.menu_id FROM sys_menu x
             WHERE x.parent_id = c.parent_id AND x.path = c.path AND x.menu_type = 'C'
             ORDER BY (x.visible = '0') DESC, x.menu_id
             LIMIT 1) AS keep_id
    FROM sys_menu c
    WHERE c.menu_type = 'C'
      AND EXISTS (SELECT 1 FROM sys_menu d
                   WHERE d.parent_id = c.parent_id AND d.path = c.path
                     AND d.menu_type = 'C' AND d.menu_id <> c.menu_id
                     AND d.component = c.component)
) k ON b.parent_id = k.dup_id AND b.menu_type = 'F'
LEFT JOIN sys_menu kb ON kb.parent_id = k.keep_id AND kb.menu_type = 'F'
                     AND kb.perms = b.perms AND kb.perms <> ''
SET b.parent_id = k.keep_id
WHERE k.dup_id <> k.keep_id AND kb.menu_id IS NULL;

-- 3. A 类完全重复对：删除重复项下的剩余按钮（保留项已有同 perms 按钮的）
DELETE b FROM sys_menu b
JOIN (
    SELECT c.menu_id AS dup_id,
           (SELECT x.menu_id FROM sys_menu x
             WHERE x.parent_id = c.parent_id AND x.path = c.path AND x.menu_type = 'C'
             ORDER BY (x.visible = '0') DESC, x.menu_id
             LIMIT 1) AS keep_id
    FROM sys_menu c
    WHERE c.menu_type = 'C'
      AND EXISTS (SELECT 1 FROM sys_menu d
                   WHERE d.parent_id = c.parent_id AND d.path = c.path
                     AND d.menu_type = 'C' AND d.menu_id <> c.menu_id
                     AND d.component = c.component)
) k ON b.parent_id = k.dup_id
WHERE k.dup_id <> k.keep_id;

-- 4. A 类完全重复对：删除重复的 C 菜单（保留可见的一条）
DELETE m FROM sys_menu m
JOIN (
    SELECT c.menu_id AS dup_id,
           (SELECT x.menu_id FROM sys_menu x
             WHERE x.parent_id = c.parent_id AND x.path = c.path AND x.menu_type = 'C'
             ORDER BY (x.visible = '0') DESC, x.menu_id
             LIMIT 1) AS keep_id
    FROM sys_menu c
    WHERE c.menu_type = 'C'
      AND EXISTS (SELECT 1 FROM sys_menu d
                   WHERE d.parent_id = c.parent_id AND d.path = c.path
                     AND d.menu_type = 'C' AND d.menu_id <> c.menu_id
                     AND d.component = c.component)
) k ON m.menu_id = k.dup_id
WHERE m.menu_id <> k.keep_id;

-- 5. B 类存量页面对：bid 看板入口 path 改为 'bid-dashboard'（幂等：改后不再满足冲突条件）
--    仅当同父下存在另一条同 path 的 C 菜单时才改
UPDATE sys_menu b
JOIN sys_menu s ON s.parent_id = b.parent_id AND s.path = b.path
                AND s.menu_type = 'C' AND s.menu_id <> b.menu_id
SET b.path = 'bid-dashboard'
WHERE b.menu_type = 'C' AND b.component = 'bid/dashboard/index' AND b.path = 'dashboard';

-- 6. 清理孤儿授权
DELETE rm FROM sys_role_menu rm
LEFT JOIN sys_menu m ON m.menu_id = rm.menu_id
WHERE m.menu_id IS NULL;

-- 7. 执行后复核（应为空）
SELECT m.parent_id, m.path, COUNT(*) AS cnt
FROM sys_menu m
WHERE m.menu_type = 'C'
GROUP BY m.parent_id, m.path
HAVING COUNT(*) > 1;

-- =============================================================
-- 第 38 段 / 共 42 段：菜单结构对齐（对齐本地开发环境终态；幂等）
-- 背景：restructure 挪九域看板用历史硬编码 id（9063013-9063024），
--       在从基线全新执行的客户库上落空；开票管理等菜单为脚本初版
--       order/icon，与本地人工调整后的终态不一致。本段按
--       route_name/perms 动态定位，全部幂等（重复执行无副作用）。
-- =============================================================

-- 38.1 九域看板挪入「指标看板分析」（按 perms 动态定位，修复硬编码 id 失效）
SET @biId = (SELECT menu_id FROM sys_menu WHERE menu_name = '决策支持' AND menu_type = 'M' AND parent_id = 0 LIMIT 1);
SET @boardId = (SELECT menu_id FROM sys_menu WHERE path = 'board' AND parent_id = @biId AND menu_type = 'M' LIMIT 1);
UPDATE sys_menu SET parent_id = @boardId
WHERE @boardId IS NOT NULL AND @biId IS NOT NULL
  AND parent_id = @biId
  AND perms LIKE 'bi:board:%:view';

-- 38.2 决策支持一级排序（对齐本地：驾驶舱1 / 指标字典2 / 指标看板分析3 / 统计分析4）
UPDATE sys_menu m JOIN (SELECT menu_id AS id FROM sys_menu WHERE route_name = 'BiDashboard' AND menu_type = 'C' LIMIT 1) d ON m.menu_id = d.id SET m.order_num = 1;
UPDATE sys_menu m JOIN (SELECT menu_id AS id FROM sys_menu WHERE route_name = 'BiMetric' AND menu_type = 'C' LIMIT 1) d ON m.menu_id = d.id SET m.order_num = 2;
UPDATE sys_menu m JOIN (SELECT menu_id AS id FROM sys_menu WHERE path = 'board' AND parent_id = @biId AND menu_type = 'M' LIMIT 1) d ON m.menu_id = d.id SET m.order_num = 3;
UPDATE sys_menu m JOIN (SELECT menu_id AS id FROM sys_menu WHERE path = 'statistics' AND parent_id = @biId AND menu_type = 'M' LIMIT 1) d ON m.menu_id = d.id SET m.order_num = 4;

-- 38.3 项目管理子树 order/icon 对齐本地终态（按 route_name 定位，幂等）
UPDATE sys_menu SET order_num = 1,  icon = 'list'     WHERE route_name = 'PjmProject'   AND menu_type = 'C';
UPDATE sys_menu SET order_num = 2,  icon = 'tree'     WHERE route_name = 'PjmTask'      AND menu_type = 'C';
UPDATE sys_menu SET order_num = 3,  icon = 'upload'   WHERE route_name = 'PjmAttachment' AND menu_type = 'C';
UPDATE sys_menu SET order_num = 4,  icon = 'peoples'  WHERE route_name = 'PjmMember'    AND menu_type = 'C';
UPDATE sys_menu SET order_num = 5,  icon = 'edit'     WHERE route_name = 'PjmChange'    AND menu_type = 'C';
UPDATE sys_menu SET order_num = 6,  icon = 'date'     WHERE route_name = 'PjmMilestone' AND menu_type = 'C';
UPDATE sys_menu SET order_num = 6,  icon = 'money'    WHERE route_name = 'PjmInvoice'   AND menu_type = 'C';
UPDATE sys_menu SET order_num = 7,  icon = 'money'    WHERE route_name = 'PjmPayment'   AND menu_type = 'C';
UPDATE sys_menu SET order_num = 8,  icon = 'edit'     WHERE route_name = 'PjmWorklog'   AND menu_type = 'C';
UPDATE sys_menu SET order_num = 9,  icon = 'shopping' WHERE route_name = 'PjmPmsLedger' AND menu_type = 'C';
UPDATE sys_menu SET order_num = 10, icon = 'chart'    WHERE route_name = 'PjmDashboard' AND menu_type = 'C';

-- 38.4 附件菜单名称对齐本地（生产保留的是「项目附件」，本地叫「项目资料」；幂等）
UPDATE sys_menu SET menu_name = '项目资料' WHERE route_name = 'PjmAttachment' AND menu_name = '项目附件' AND menu_type = 'C';

-- 38.5 执行后复核
-- 38.5.1 九域看板应全部挂在「指标看板分析」下（应 9 条）
SELECT COUNT(*) AS boards_under_board FROM sys_menu
WHERE parent_id = @boardId AND perms LIKE 'bi:board:%:view';
-- 38.5.2 决策支持一级（应 4 条：驾驶舱/指标字典/指标看板分析/统计分析）
SELECT menu_id, menu_name, order_num, menu_type FROM sys_menu
WHERE parent_id = @biId AND menu_type IN ('M','C') ORDER BY order_num, menu_id;
-- 38.5.3 项目管理子树（应与本地一致：立项1/WBS2/项目资料3/团队4/变更5/里程碑6/开票6/回款7/工时8/采购台账9/看板10）
SELECT menu_name, order_num, icon, visible, route_name FROM sys_menu
WHERE parent_id = 906202 AND menu_type = 'C' ORDER BY order_num, menu_id;
-- =============================================================

-- =============================================================
-- 第 39 段 / 共 42 段：部门/人员表字段完善（2026-11-09 17:13 提交，幂等）
-- 内容：sys_dept 增部门简称/部门编码/分管领导/在职人员数量、删联系电话/邮箱；
--       sys_user 增上级领导/分管领导；回填 sys_dept.staff_count
-- =============================================================

-- ============================================================
-- 升级脚本: upgrade_20261109_dept_user_fields.sql
-- 主题: 部门表字段调整(去掉联系电话/邮箱, 增加部门简称/部门编码/分管领导/在职人员数量)
--       人员表增加上级领导/分管领导(人员选择)
-- 幂等: 可重复执行
-- 目标库: ruoyi_lyjs
-- ============================================================

-- ---------- 1. sys_dept 新增字段 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_short_name');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_short_name varchar(50) default null comment ''部门简称'' after leader',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'dept_code');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column dept_code varchar(50) default null comment ''部门编码'' after dept_short_name',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after dept_code',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'staff_count');
set @sql := if(@col_exists = 0,
    'alter table sys_dept add column staff_count int default null comment ''在职人员数量'' after charge_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 2. sys_dept 删除联系电话/邮箱 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'phone');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column phone',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_dept' and column_name = 'email');
set @sql := if(@col_exists > 0,
    'alter table sys_dept drop column email',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 3. sys_user 新增上级领导/分管领导 ----------
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_id bigint default null comment ''上级领导(人员ID,关联sys_user.user_id)'' after remark',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_id bigint default null comment ''分管领导(人员ID,关联sys_user.user_id)'' after superior_leader_id',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- ---------- 4. 回填 sys_dept.staff_count(在职人员数量 = 在职用户数) ----------
update sys_dept d
set d.staff_count = (
    select count(*) from sys_user u
    where u.dept_id = d.dept_id and u.status = '0' and u.del_flag = '0'
);

-- =============================================================
-- 第 40 段 / 共 42 段：安全生产 · 在线考试模块（表/字典/菜单/题库种子，哨兵幂等）
-- 来源：safety_exam_all.sql + safety_exam_seed_food.sql（2026-11-07 提交，此前遗漏未并入）
-- 幂等策略：整段包进哨兵存储过程——biz_safety_exam 表已存在则整体跳过，
--           绝不 DROP/DELETE 已有数据，重复执行零副作用。
-- =============================================================
SET @exam_installed := (SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam');
DROP PROCEDURE IF EXISTS p_install_safety_exam;
DELIMITER $$
CREATE PROCEDURE p_install_safety_exam()
BEGIN
  IF @exam_installed = 0 THEN
-- =============================================
-- 一、业务表 DDL（7张）
-- =============================================

-- 1. 题库分类表（主数据/共享资源，豁免数据权限归属字段）
CREATE TABLE IF NOT EXISTS biz_safety_exam_category (
    category_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '分类ID',
    parent_id          BIGINT       DEFAULT 0                COMMENT '父分类ID（支持两级）',
    category_name      VARCHAR(50)  NOT NULL                 COMMENT '分类名称',
    order_num          INT          DEFAULT 0                COMMENT '显示顺序',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0正常 1停用）',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (category_id),
    KEY idx_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试题库分类表';

-- 2. 题库表（主数据/共享资源，豁免数据权限归属字段）
CREATE TABLE IF NOT EXISTS biz_safety_exam_question (
    question_id        BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '题目ID',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干',
    options            TEXT         NOT NULL                 COMMENT '选项（JSON数组，如["A内容","B内容"]；判断题固定["正确","错误"]）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '正确答案（如 A / ABD / 对）',
    analysis           VARCHAR(500)                          COMMENT '答案解析',
    category_id        BIGINT                                COMMENT '题目分类ID',
    category_name      VARCHAR(50)                           COMMENT '分类名称（冗余）',
    difficulty         CHAR(1)      DEFAULT '2'              COMMENT '难度（字典 safety_exam_difficulty）',
    job_types          VARCHAR(200)                          COMMENT '适用工种（逗号分隔，字典 safety_job_type）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '状态（0启用 1停用）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (question_id),
    KEY idx_category (category_id),
    KEY idx_type_diff (question_type, difficulty)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='安全生产考试题库表';

-- 3. 试卷表（主数据，发布后题目集合冻结）
CREATE TABLE IF NOT EXISTS biz_safety_exam_paper (
    paper_id           BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '试卷ID',
    paper_name         VARCHAR(100) NOT NULL                 COMMENT '试卷名称',
    course_id          BIGINT                                COMMENT '关联培训课程ID（biz_safety_training_course）',
    course_name        VARCHAR(255)                          COMMENT '课程名称（冗余）',
    total_score        DECIMAL(5,1) DEFAULT 100.0            COMMENT '总分（按题目分值自动计算）',
    pass_score         DECIMAL(5,1) DEFAULT 60.0             COMMENT '及格分',
    duration           INT          DEFAULT 60               COMMENT '考试时长（分钟）',
    shuffle_question   CHAR(1)      DEFAULT 'N'              COMMENT '题目乱序（Y是 N否）',
    shuffle_option     CHAR(1)      DEFAULT 'N'              COMMENT '选项乱序（Y是 N否）',
    switch_limit       INT          DEFAULT 0                COMMENT '切屏限制次数（0不限）',
    show_analysis      CHAR(1)      DEFAULT 'Y'              COMMENT '交卷后展示解析（Y是 N否）',
    status             CHAR(1)      DEFAULT '0'              COMMENT '试卷状态（字典 safety_exam_paper_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (paper_id),
    KEY idx_course (course_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试试卷表';

-- 4. 试卷题目明细表（快照式：发布时冻结题干/选项/答案，判分依据）
CREATE TABLE IF NOT EXISTS biz_safety_exam_paper_question (
    id                 BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '明细ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    question_id        BIGINT       NOT NULL                 COMMENT '来源题目ID（biz_safety_exam_question）',
    question_type      CHAR(1)      NOT NULL                 COMMENT '题型（字典 safety_exam_question_type）',
    content            TEXT         NOT NULL                 COMMENT '题干快照',
    options            TEXT         NOT NULL                 COMMENT '选项快照（JSON数组）',
    answer             VARCHAR(20)  NOT NULL                 COMMENT '答案快照（判分依据）',
    analysis           VARCHAR(500)                          COMMENT '解析快照',
    score              DECIMAL(4,1) DEFAULT 1.0              COMMENT '本题分值',
    sort_no            INT          DEFAULT 0                COMMENT '题目顺序号',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (id),
    KEY idx_paper (paper_id),
    KEY idx_question (question_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='试卷题目明细表（快照）';

-- 5. 考试场次表（事务单据：必须带 dept_id，接入 @DataScope）
CREATE TABLE IF NOT EXISTS biz_safety_exam (
    exam_id            BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试ID',
    exam_name          VARCHAR(100) NOT NULL                 COMMENT '考试名称',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    paper_name         VARCHAR(100)                          COMMENT '试卷名称（冗余）',
    record_id          BIGINT                                COMMENT '关联培训记录ID（biz_safety_training_record，可空）',
    start_time         DATETIME     NOT NULL                 COMMENT '考试开始时间',
    end_time           DATETIME     NOT NULL                 COMMENT '考试结束时间（窗口截止）',
    exam_status        CHAR(1)      DEFAULT '0'              COMMENT '考试状态（字典 safety_exam_exam_status）',
    publish_by         VARCHAR(64)                           COMMENT '发布人',
    publish_time       DATETIME                              COMMENT '发布时间',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限，创建时自动填充）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (exam_id),
    KEY idx_paper (paper_id),
    KEY idx_record (record_id),
    KEY idx_status (exam_status),
    KEY idx_start_end (start_time, end_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试场次表';

-- 6. 考生考试记录表（人员归属型：dept_id + user_id，接入 @DataScope）
CREATE TABLE IF NOT EXISTS biz_safety_exam_record (
    record_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '考试记录ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试ID',
    paper_id           BIGINT       NOT NULL                 COMMENT '试卷ID',
    user_id            BIGINT       NOT NULL                 COMMENT '考生用户ID（关联sys_user）',
    user_name          VARCHAR(128)                          COMMENT '考生姓名（冗余）',
    worker_id          BIGINT                                COMMENT '关联员工ID（biz_safety_worker，可空）',
    attendee_id        BIGINT                                COMMENT '关联培训参训人ID（biz_safety_training_attendee，可空）',
    start_time         DATETIME                              COMMENT '本人开始答题时间',
    submit_time        DATETIME                              COMMENT '提交时间',
    used_seconds       INT                                   COMMENT '实际用时（秒）',
    submit_type        CHAR(1)                               COMMENT '提交方式（字典 safety_exam_submit_type）',
    total_score        DECIMAL(5,1)                          COMMENT '总得分',
    is_pass            CHAR(1)                               COMMENT '是否合格（0不合格 1合格）',
    judge_status       CHAR(1)      DEFAULT '0'              COMMENT '判分状态（0未交卷 1已自动判分）',
    switch_count       INT          DEFAULT 0                COMMENT '切屏次数',
    exam_result        CHAR(1)      DEFAULT '0'              COMMENT '考生状态（字典 safety_exam_result）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (record_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id),
    KEY idx_exam (exam_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考生考试记录表';

-- 7. 答题明细表（追溯数据，随考试记录权限）
CREATE TABLE IF NOT EXISTS biz_safety_exam_answer (
    answer_id          BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '答题明细ID',
    record_id          BIGINT       NOT NULL                 COMMENT '考试记录ID',
    paper_question_id  BIGINT       NOT NULL                 COMMENT '试卷题目明细ID',
    user_answer        VARCHAR(20)                           COMMENT '考生答案（如 A / ABD；空=未答）',
    is_correct         CHAR(1)                               COMMENT '是否正确（1正确 0错误）',
    score              DECIMAL(4,1) DEFAULT 0                COMMENT '本题得分',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    update_by          VARCHAR(64)  DEFAULT ''               COMMENT '更新者',
    update_time        DATETIME                              COMMENT '更新时间',
    remark             VARCHAR(500)                          COMMENT '备注',
    PRIMARY KEY (answer_id),
    UNIQUE KEY uk_record_question (record_id, paper_question_id),
    KEY idx_record (record_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试答题明细表';

-- =============================================
-- 二、字典类型
-- =============================================
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark) VALUES
('考试题型', 'safety_exam_question_type', '0', 'admin', NOW(), '考试题目题型'),
('考试题目难度', 'safety_exam_difficulty', '0', 'admin', NOW(), '考试题目难度等级'),
('试卷状态', 'safety_exam_paper_status', '0', 'admin', NOW(), '考试试卷状态'),
('考试状态', 'safety_exam_exam_status', '0', 'admin', NOW(), '考试场次状态'),
('考生考试状态', 'safety_exam_result', '0', 'admin', NOW(), '考生在考试中的状态'),
('考试提交方式', 'safety_exam_submit_type', '0', 'admin', NOW(), '考生交卷方式');

-- =============================================
-- 三、字典数据
-- =============================================
INSERT INTO sys_dict_data (dict_code, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark) VALUES
-- 考试题型
(NULL, 1, '单选题', '1', 'safety_exam_question_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '多选题', '2', 'safety_exam_question_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '判断题', '3', 'safety_exam_question_type', '', 'success', 'N', '0', 'admin', NOW(), ''),

-- 考试题目难度
(NULL, 1, '简单', '1', 'safety_exam_difficulty', '', 'info', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '中等', '2', 'safety_exam_difficulty', '', 'primary', 'Y', '0', 'admin', NOW(), ''),
(NULL, 3, '困难', '3', 'safety_exam_difficulty', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 试卷状态
(NULL, 1, '草稿', '0', 'safety_exam_paper_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_paper_status', '', 'success', 'N', '0', 'admin', NOW(), '发布后题目集合冻结'),
(NULL, 3, '停用', '2', 'safety_exam_paper_status', '', 'danger', 'N', '0', 'admin', NOW(), ''),

-- 考试状态
(NULL, 1, '草稿', '0', 'safety_exam_exam_status', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '已发布', '1', 'safety_exam_exam_status', '', 'primary', 'N', '0', 'admin', NOW(), '未到开始时间'),
(NULL, 3, '考试中', '2', 'safety_exam_exam_status', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '已结束', '3', 'safety_exam_exam_status', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 5, '已撤回', '4', 'safety_exam_exam_status', '', 'danger', 'N', '0', 'admin', NOW(), '仅未开始的场次可撤回'),

-- 考生考试状态
(NULL, 1, '未开始', '0', 'safety_exam_result', '', 'info', 'Y', '0', 'admin', NOW(), ''),
(NULL, 2, '进行中', '1', 'safety_exam_result', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '已交卷', '2', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), ''),
(NULL, 4, '缺考', '3', 'safety_exam_result', '', 'danger', 'N', '0', 'admin', NOW(), '窗口结束无答题记录'),
(NULL, 5, '已判分', '4', 'safety_exam_result', '', 'success', 'N', '0', 'admin', NOW(), '成绩已回写'),

-- 考试提交方式
(NULL, 1, '手动提交', '1', 'safety_exam_submit_type', '', 'primary', 'N', '0', 'admin', NOW(), ''),
(NULL, 2, '超时自动提交', '2', 'safety_exam_submit_type', '', 'warning', 'N', '0', 'admin', NOW(), ''),
(NULL, 3, '切屏强制交卷', '3', 'safety_exam_submit_type', '', 'danger', 'N', '0', 'admin', NOW(), '切屏超限自动交卷');

-- =============================================
-- 四、菜单权限（教育培训下新建「考试管理」二级目录，置于学时统计之前；权限域独立为 safety:exam:*）
-- =============================================

-- 获取教育培训目录ID
SELECT @trainingMenuId := menu_id FROM sys_menu WHERE menu_name = '教育培训' AND parent_id IN (SELECT menu_id FROM sys_menu WHERE menu_name = '安全生产管理' AND parent_id = 0);

-- ---- 「考试管理」二级目录（教育培训下排序6，位于学时统计之前） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '考试管理', @trainingMenuId, 6, 'exam', 'M', '0', '0', '', 'form', 'admin', NOW(), '考试功能子目录'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M');
SELECT @examDirMenuId := menu_id FROM sys_menu WHERE menu_name = '考试管理' AND parent_id = @trainingMenuId AND menu_type = 'M';

-- 学时统计顺移至考试管理之后（存在才更新）
UPDATE sys_menu SET order_num = 7 WHERE menu_name = '学时统计' AND parent_id = @trainingMenuId;

-- ---- 题库管理（考试管理下排序1） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('题库管理', @examDirMenuId, 1, 'examQuestion', 'safety/exam/question/index', 1, 0, 'C', '0', '0', 'safety:exam:question:list', 'education', 'admin', NOW(), '考试题库管理菜单');
SET @questionMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('题库查询', @questionMenuId, 1, '#', 'F', '0', '0', 'safety:exam:question:query',  'admin', NOW(), ''),
('题库新增', @questionMenuId, 2, '#', 'F', '0', '0', 'safety:exam:question:add',    'admin', NOW(), ''),
('题库修改', @questionMenuId, 3, '#', 'F', '0', '0', 'safety:exam:question:edit',   'admin', NOW(), ''),
('题库删除', @questionMenuId, 4, '#', 'F', '0', '0', 'safety:exam:question:remove', 'admin', NOW(), ''),
('题库导入', @questionMenuId, 5, '#', 'F', '0', '0', 'safety:exam:question:import', 'admin', NOW(), ''),
('题库导出', @questionMenuId, 6, '#', 'F', '0', '0', 'safety:exam:question:export', 'admin', NOW(), '');

-- ---- 试卷管理（考试管理下排序2） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('试卷管理', @examDirMenuId, 2, 'examPaper', 'safety/exam/paper/index', 1, 0, 'C', '0', '0', 'safety:exam:paper:list', 'form', 'admin', NOW(), '考试试卷管理菜单');
SET @paperMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('试卷查询', @paperMenuId, 1, '#', 'F', '0', '0', 'safety:exam:paper:query',   'admin', NOW(), ''),
('试卷新增', @paperMenuId, 2, '#', 'F', '0', '0', 'safety:exam:paper:add',     'admin', NOW(), ''),
('试卷修改', @paperMenuId, 3, '#', 'F', '0', '0', 'safety:exam:paper:edit',    'admin', NOW(), ''),
('试卷删除', @paperMenuId, 4, '#', 'F', '0', '0', 'safety:exam:paper:remove',  'admin', NOW(), ''),
('试卷发布', @paperMenuId, 5, '#', 'F', '0', '0', 'safety:exam:paper:publish', 'admin', NOW(), '发布后题目集合冻结');

-- ---- 考试场次（考试管理下排序3） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('考试场次', @examDirMenuId, 3, 'examManage', 'safety/exam/manage/index', 1, 0, 'C', '0', '0', 'safety:exam:list', 'date-range', 'admin', NOW(), '考试场次管理菜单');
SET @examMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('考试查询',     @examMenuId, 1, '#', 'F', '0', '0', 'safety:exam:query',    'admin', NOW(), ''),
('考试新增',     @examMenuId, 2, '#', 'F', '0', '0', 'safety:exam:add',      'admin', NOW(), ''),
('考试修改',     @examMenuId, 3, '#', 'F', '0', '0', 'safety:exam:edit',     'admin', NOW(), ''),
('考试删除',     @examMenuId, 4, '#', 'F', '0', '0', 'safety:exam:remove',   'admin', NOW(), ''),
('考试发布',     @examMenuId, 5, '#', 'F', '0', '0', 'safety:exam:publish',  'admin', NOW(), ''),
('考试强制结束', @examMenuId, 6, '#', 'F', '0', '0', 'safety:exam:forceEnd', 'admin', NOW(), ''),
('成绩导出',     @examMenuId, 7, '#', 'F', '0', '0', 'safety:exam:export',   'admin', NOW(), ''),
('成绩统计',     @examMenuId, 8, '#', 'F', '0', '0', 'safety:exam:stats',    'admin', NOW(), ''),
('成绩调分',     @examMenuId, 9, '#', 'F', '0', '0', 'safety:exam:score:edit', 'admin', NOW(), '人工调分留痕');

-- ---- 我的考试（考生端，考试管理下排序4，需授权给普通员工角色） ----
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark) VALUES
('我的考试', @examDirMenuId, 4, 'examTake', 'safety/exam/take/index', 1, 0, 'C', '0', '0', 'safety:exam:myList', 'checkbox', 'admin', NOW(), '考生端我的考试菜单');
SET @takeMenuId = LAST_INSERT_ID();
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time, remark) VALUES
('在线答题', @takeMenuId, 1, '#', 'F', '0', '0', 'safety:exam:take', 'admin', NOW(), '进入考试/答题/提交/查分');

-- =============================================
-- 五、角色授权
-- =============================================
-- 管理员角色（role_id=2，与 safety_all.sql 口径一致）分配全部考试权限（含考试管理目录）
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 2, menu_id FROM sys_menu WHERE perms LIKE 'safety:exam:%'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id = 2);

-- 普通员工角色如需在线考试，请在【系统管理→角色管理】为对应角色勾选
-- 「教育培训→我的考试」菜单及其「在线答题」按钮权限

-- =============================================
-- 六、定时任务（考试状态兜底：状态流转 + 缺考标记，幂等可重复）
-- =============================================
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '考试状态兜底任务', 'DEFAULT', 'safetyExamTask.run', '0 * * * * ?', '3', '1', '0', 'admin', NOW(), '场次状态流转与缺考标记（幂等），建议每分钟'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'safetyExamTask.run');

-- ---- 题库种子数据（食品行业，与模块同批首装） ----

-- ============================================================
-- 食品行业安全考试题库种子数据（幂等，可重复执行）
-- 内容：1 个一级分类 + 5 个二级分类 + 29 道题（单选11/多选4/判断14）
-- 判重规则：分类按名称+父级，题目按题干 content
-- ============================================================

-- 1. 一级分类：食品安全生产
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT 0, '食品安全生产', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0');

SET @food_root := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全生产' AND parent_id = 0 AND del_flag = '0' LIMIT 1);

-- 2. 二级分类（幂等）
INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '食品安全法规', 1, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '卫生与消毒规范', 2, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '仓储与物料管理', 3, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '添加剂与质量管理', 4, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0');

INSERT INTO biz_safety_exam_category (parent_id, category_name, order_num, status, del_flag, create_by, create_time)
SELECT @food_root, '消防与作业安全', 5, '0', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0');

SET @cat_law := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '食品安全法规' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_hyg := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '卫生与消毒规范' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_wh  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '仓储与物料管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_qa  := (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '添加剂与质量管理' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);
SET @cat_fire:= (SELECT category_id FROM biz_safety_exam_category WHERE category_name = '消防与作业安全' AND parent_id = @food_root AND del_flag = '0' LIMIT 1);

-- 3. 题目（幂等：按题干判重）
-- 语法：INSERT INTO ... SELECT ... FROM DUAL WHERE NOT EXISTS (...)

-- ============ 食品安全法规 ============
-- 单选1（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '从事食品生产活动，应当依法取得（　）。',
'["营业执照","食品生产许可证","卫生登记证","安全生产许可证"]', 'B',
'《食品安全法》规定，从事食品生产活动应当依法取得食品生产许可证。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从事食品生产活动，应当依法取得（　）。' AND del_flag = '0');

-- 单选2（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品生产许可证的有效期为（　）年。',
'["3","5","8","10"]', 'B',
'《食品生产许可管理办法》规定，食品生产许可证有效期为5年。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产许可证的有效期为（　）年。' AND del_flag = '0');

-- 单选3（困难）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。',
'["继续生产，降低损失","立即停止生产、召回问题食品并按规定报告","降价促销尽快处理","更换包装后继续销售"]', 'B',
'应立即停止生产、主动召回，并按规定向监管部门报告，防止危害扩大。',
@cat_law, '食品安全法规', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业发现生产的食品不符合食品安全标准时，正确的做法是（　）。' AND del_flag = '0');

-- 单选4（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '发生食品安全事故后，涉事企业应当（　）。',
'["立即采取措施防止事故扩大，并按规定及时报告","隐瞒不报，内部消化处理","等事故平息后再向上级汇报","只通知相关客户即可"]', 'A',
'发生食品安全事故的单位应当立即处置、防止事故扩大，并按规定及时向监管部门报告，不得隐瞒。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '发生食品安全事故后，涉事企业应当（　）。' AND del_flag = '0');

-- 多选1（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品生产企业应当建立的食品安全管理制度包括（　）。',
'["食品安全自查制度","从业人员健康管理制度","进货查验记录制度","不安全食品召回制度"]', 'ABCD',
'以上四项均为《食品安全法》要求食品生产经营者建立的食品安全管理制度。',
@cat_law, '食品安全法规', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产企业应当建立的食品安全管理制度包括（　）。' AND del_flag = '0');

-- 判断1（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》规定，从事接触直接入口食品工作的从业人员应当每年进行健康检查，取得健康证明后方可上岗。',
@cat_law, '食品安全法规', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '从业人员应当取得健康证明后方可从事接触直接入口食品的工作。' AND del_flag = '0');

-- ============ 卫生与消毒规范 ============
-- 单选5（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '接触直接入口食品的人员正确的洗手消毒做法是（　）。',
'["用清水冲一下即可","按规程洗手后使用消毒液消毒并烘干双手","佩戴手套后可以不洗手","洗完手用衣袖擦干"]', 'B',
'应按规程洗手并使用消毒液消毒，防止手部污染直接入口食品。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '接触直接入口食品的人员正确的洗手消毒做法是（　）。' AND del_flag = '0');

-- 单选6（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '进入食品生产车间前的正确做法是（　）。',
'["直接进入车间开始作业","更换工作服、佩戴口罩和工作帽，洗手消毒后经消毒通道进入","穿便服快速进入","只需简单洗手即可进入"]', 'B',
'进入车间应严格执行更衣、戴口罩帽子、洗手消毒等卫生程序，防止人员带入污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入食品生产车间前的正确做法是（　）。' AND del_flag = '0');

-- 单选7（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '生产车间使用清洗消毒剂的要求是（　）。',
'["任意采购价格便宜的即可","使用符合要求的食品级清洗消毒产品，按规定浓度配制并专柜存放、专人管理","与食品原料放在一起方便取用","浓度越高消毒效果越好，无需按比例配制"]', 'B',
'清洗消毒剂应符合相关标准，按规定浓度配制，专柜存放、专人管理，防止误用和污染食品。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '生产车间使用清洗消毒剂的要求是（　）。' AND del_flag = '0');

-- 多选2（中等）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '进入车间人员的个人卫生要求包括（　）。',
'["持有效健康证上岗","穿戴清洁的工作服、帽，头发不外露","不得佩戴首饰手表，不留长指甲、不涂指甲油","不得在车间内吸烟和饮食"]', 'ABCD',
'以上均为食品生产人员基本的个人卫生要求，目的是防止人员对食品造成污染。',
@cat_hyg, '卫生与消毒规范', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '进入车间人员的个人卫生要求包括（　）。' AND del_flag = '0');

-- 判断2（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。',
'["正确","错误"]', '对',
'《食品安全法》明确规定，患有国务院卫生行政部门规定的有碍食品安全疾病的人员，不得从事接触直接入口食品的工作。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '患有痢疾、伤寒、甲型病毒性肝炎等消化道传染病的人员，不得从事接触直接入口食品的工作。' AND del_flag = '0');

-- 判断3（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了节省时间，工作人员可以穿着工作服直接进出卫生间。',
'["正确","错误"]', '错',
'进出卫生间必须脱去工作服，回来后重新洗手消毒，防止交叉污染。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了节省时间，工作人员可以穿着工作服直接进出卫生间。' AND del_flag = '0');

-- 判断4（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。',
'["正确","错误"]', '对',
'食品接触面应定期清洗消毒并记录，保证卫生状态可追溯。',
@cat_hyg, '卫生与消毒规范', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '直接接触食品的设备、工具和容器应按规程定期清洗消毒，并如实记录。' AND del_flag = '0');

-- ============ 仓储与物料管理 ============
-- 单选8（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。',
'["5、10","10、30","30、50","50、50"]', 'B',
'通行仓储卫生要求：物料离地不低于10cm、离墙不低于30cm，便于清洁、通风和检查。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库内物料码放一般要求离地面不低于（　）厘米、离墙面不低于（　）厘米。' AND del_flag = '0');

-- 多选3（中等）ABC
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '食品仓库日常管理的基本要求包括（　）。',
'["分区分类存放，离墙离地","定期检查记录温湿度","先进先出，账物相符","与有毒有害化学品混放，提高装卸效率"]', 'ABC',
'仓库应分区分类、离墙离地存放，定期记录温湿度，执行先进先出；严禁与有毒有害物品混放。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品仓库日常管理的基本要求包括（　）。' AND del_flag = '0');

-- 判断5（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。',
'["正确","错误"]', '错',
'化学品必须单独存放并明显标识，与食品原料、辅料、包装材料严格隔离，防止化学污染。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '原料、辅料、包装材料可以与农药、清洁剂等化学品存放在同一仓库。' AND del_flag = '0');

-- 判断6（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '物料发放应遵循先进先出的原则。',
'["正确","错误"]', '对',
'先进先出可防止原料过期变质，保证物料在保质期内使用。',
@cat_wh, '仓储与物料管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '物料发放应遵循先进先出的原则。' AND del_flag = '0');

-- 判断7（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。',
'["正确","错误"]', '对',
'霉变、过期原料存在食品安全隐患，必须隔离标识并按规定处理，严禁流入生产环节。',
@cat_wh, '仓储与物料管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '仓库发现霉变或超过保质期的原料，应立即隔离存放并按规定处理，不得投入生产。' AND del_flag = '0');

-- ============ 添加剂与质量管理 ============
-- 单选9（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '食品添加剂的使用应当符合（　）的要求。',
'["GB 2760《食品安全国家标准 食品添加剂使用标准》","GB 14881《食品生产通用卫生规范》","GB 7718《预包装食品标签通则》","企业内部自定标准"]', 'A',
'GB 2760规定了食品添加剂的允许品种、使用范围和最大使用量，是添加剂使用的强制标准。',
@cat_qa, '添加剂与质量管理', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品添加剂的使用应当符合（　）的要求。' AND del_flag = '0');

-- 判断8（简单）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '为了改善产品口感，食品添加剂可以超范围、超限量使用。',
'["正确","错误"]', '错',
'食品添加剂必须严格按照GB 2760规定的品种、范围和限量使用，超范围超限量使用属于违法行为。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '为了改善产品口感，食品添加剂可以超范围、超限量使用。' AND del_flag = '0');

-- 判断9（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。',
'["正确","错误"]', '对',
'《食品安全法》要求食品生产企业对其产品进行检验，合格后方可出厂销售。',
@cat_qa, '添加剂与质量管理', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '企业应按规定对产品进行出厂检验，检验合格后方可出厂销售。' AND del_flag = '0');

-- 判断10（困难）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
'["正确","错误"]', '对',
'《食品安全法》规定，记录和凭证保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。',
@cat_qa, '添加剂与质量管理', '3', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '食品生产过程的相关记录和凭证，保存期限不得少于产品保质期满后六个月；没有明确保质期的，保存期限不得少于二年。' AND del_flag = '0');

-- ============ 消防与作业安全 ============
-- 单选10（简单）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。',
'["上风方向、根部","下风方向、上部","上风方向、上部","任意方向、中部"]', 'A',
'应站在上风方向，对准火焰根部喷射，才能有效灭火并保障自身安全。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '使用干粉灭火器灭火时，应站在（　），将喷嘴对准火焰（　）喷射。' AND del_flag = '0');

-- 单选11（中等）
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '1', '车间内驾驶叉车的安全要求，正确的是（　）。',
'["速度不限，尽快完成搬运","限速行驶，禁止载人，转弯减速鸣笛","可以顺路搭载同事","倒车时无需观察后方"]', 'B',
'叉车应限速行驶、禁止载人，转弯和通道口减速鸣笛，防止碰撞伤人。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '车间内驾驶叉车的安全要求，正确的是（　）。' AND del_flag = '0');

-- 多选4（简单）ABCD
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '2', '灭火的基本方法包括（　）。',
'["冷却灭火法","窒息灭火法","隔离灭火法","化学抑制灭火法"]', 'ABCD',
'灭火的四种基本方法：冷却、窒息、隔离、化学抑制，实际扑救常组合使用。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火的基本方法包括（　）。' AND del_flag = '0');

-- 判断11（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。',
'["正确","错误"]', '对',
'对准火焰根部喷射才能隔绝燃烧源头，喷向上部无法有效灭火。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '灭火时应将灭火剂喷射到火焰根部，而不是火焰上部。' AND del_flag = '0');

-- 判断12（中等）错
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电动叉车可以停放在存放易燃易爆物品的仓库内充电。',
'["正确","错误"]', '错',
'充电过程可能产生电火花，易燃易爆场所严禁充电作业，应在专用充电区域进行。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电动叉车可以停放在存放易燃易爆物品的仓库内充电。' AND del_flag = '0');

-- 判断13（中等）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。',
'["正确","错误"]', '对',
'带电灭火有触电危险，必须先断电；水导电，严禁用水扑救电气火灾。',
@cat_fire, '消防与作业安全', '2', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '电气设备发生火灾时，应先切断电源，再使用干粉或二氧化碳灭火器扑救，严禁用水直接扑救。' AND del_flag = '0');

-- 判断14（简单）对
INSERT INTO biz_safety_exam_question (question_type, content, options, answer, analysis, category_id, category_name, difficulty, status, create_by, create_time)
SELECT '3', '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。',
'["正确","错误"]', '对',
'发现隐患及时报告是每名员工的职责，便于第一时间处置、防止事故扩大。',
@cat_fire, '消防与作业安全', '1', '0', 'admin', NOW() FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM biz_safety_exam_question WHERE content = '员工发现食品安全隐患或安全事故隐患，应立即报告车间负责人或安全管理人员。' AND del_flag = '0');
  END IF;
END$$
DELIMITER ;
CALL p_install_safety_exam();
DROP PROCEDURE IF EXISTS p_install_safety_exam;

-- =============================================================
-- 第 41 段 / 共 42 段：考试功能 P1 增量（补考字段+提交方式字典，幂等）
-- =============================================================
-- =====================================================================
-- 安全生产模块-考试功能 P1 增量升级脚本（FR-7/8/10/11/12/13）
-- 依赖：safety_exam_all.sql（P0 全量脚本）已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- ---------------------------------------------------------------------
-- 一、biz_safety_exam 增加补考相关字段（FR-8 补考管理）
--   retake_from_id  补考来源场次ID（NULL=非补考）
--   retake_count    补考代数（0=首次考试，1=第1次补考...）
--   score_strategy  成绩覆盖策略（1=取最高 2=取最新，回写参训人时生效）
-- ---------------------------------------------------------------------
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_from_id');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_from_id bigint NULL COMMENT ''补考来源场次ID'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'retake_count');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN retake_count int NOT NULL DEFAULT 0 COMMENT ''补考代数（0=首次考试）'' AFTER retake_from_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'score_strategy');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN score_strategy char(1) NULL DEFAULT ''2'' COMMENT ''成绩覆盖策略（1取最高 2取最新）'' AFTER retake_count',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- ---------------------------------------------------------------------
-- 二、交卷方式字典补充「无操作锁卷」（FR-13 答题页基础防护）
-- ---------------------------------------------------------------------
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '无操作锁卷', '4', 'safety_exam_submit_type', NULL, 'warning', 'N', '0', 'admin', NOW(), 'FR-13：无操作超时自动锁卷交卷'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'safety_exam_submit_type' AND dict_value = '4');

-- =============================================================
-- 第 42 段 / 共 42 段：考试考生来源双模式增量（幂等）
-- =============================================================
-- =====================================================================
-- 安全生产模块-考试功能 增量升级：考生来源双模式
--   1) biz_safety_exam 增加 attendee_source（1=来自培训 2=手动添加）
--   2) 新增 biz_safety_exam_user 手动考试名单表
-- 依赖：safety_exam_all.sql 已执行
-- 幂等性：可重复执行；新增列通过 information_schema 判断
-- 执行库：ry-vue
-- =====================================================================

-- 一、biz_safety_exam 增加考生来源字段
SET @col_exists := (SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'biz_safety_exam' AND column_name = 'attendee_source');
SET @ddl := IF(@col_exists = 0,
    'ALTER TABLE biz_safety_exam ADD COLUMN attendee_source char(1) NOT NULL DEFAULT ''1'' COMMENT ''考生来源（1=来自培训 2=手动添加）'' AFTER record_id',
    'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 二、手动考试名单表
CREATE TABLE IF NOT EXISTS biz_safety_exam_user (
    exam_user_id       BIGINT       NOT NULL AUTO_INCREMENT  COMMENT '主键ID',
    exam_id            BIGINT       NOT NULL                 COMMENT '考试场次ID（biz_safety_exam）',
    user_id            BIGINT       NOT NULL                 COMMENT '系统用户ID（sys_user）',
    user_name          VARCHAR(64)                           COMMENT '用户显示名（冗余）',
    dept_id            BIGINT                                COMMENT '归属部门ID（数据权限）',
    enterprise_id      BIGINT                                COMMENT '所属企业ID',
    del_flag           CHAR(1)      DEFAULT '0'              COMMENT '删除标志',
    create_by          VARCHAR(64)  DEFAULT ''               COMMENT '创建者',
    create_time        DATETIME                              COMMENT '创建时间',
    PRIMARY KEY (exam_user_id),
    UNIQUE KEY uk_exam_user (exam_id, user_id),
    KEY idx_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='考试手动添加人员表';
-- =============================================================
-- 第 43 段 / 共 43 段：金蝶云星辰对接（配置/字段/菜单/授权/定时任务，幂等）
-- 来源：deploy/config/sql/add_kingdee_full_upgrade_20261111.sql（此前遗漏未并入）
-- 加工：剥离 USE edop_yhny；一级菜单改 perms 判重自增 id（生产 9063109 有占用风险）；
--       历史修正 DELETE 9063108 加 menu_name 限定（防误删生产同 id 菜单）；授权引用改变量
-- =============================================================

-- 43.1 sys_config 金蝶配置项（NOT EXISTS 幂等）
INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶对接开关', 'kingdee.enabled', 'true', 'Y', 'admin', NOW(), '是否启用金蝶云星辰对接'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.enabled');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶接口超时', 'kingdee.timeout', '30000', 'Y', 'admin', NOW(), 'API 超时毫秒数'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.timeout');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '金蝶重试次数', 'kingdee.retry_times', '2', 'Y', 'admin', NOW(), 'API 失败重试次数'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.retry_times');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT 'appSecret 刷新 cron', 'kingdee.secret_refresh_cron', '0 0 2,14 * * ?', 'Y', 'admin', NOW(), '每天凌晨 2 点、下午 2 点主动刷新'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.secret_refresh_cron');

INSERT INTO sys_config (config_name, config_key, config_value, config_type, create_by, create_time, remark)
SELECT '同步 cron', 'kingdee.sync_cron', '0 */30 * * * ?', 'Y', 'admin', NOW(), '每 30 分钟自动同步一次'
WHERE NOT EXISTS (SELECT 1 FROM sys_config WHERE config_key = 'kingdee.sync_cron');

-- ★ 密钥字段（kingdee.app_key/client_id/outer_instance_id/base_url/router_addr/app_secret/client_secret）
--   部署时通过前端「金蝶配置」页填写，或用 SecureKeyUtils 加密后插入（AES 加密，
--   加密密钥在 application.yml 的 ruoyi.fileEncryptionKey）

-- 43.2 sys_user 补 6 列（information_schema + PREPARE 幂等加列）
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column leader_id bigint default null comment ''直接领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column leader_name varchar(64) default null comment ''直接领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_id bigint default null comment ''上级领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'superior_leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column superior_leader_name varchar(64) default null comment ''上级领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_id');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_id bigint default null comment ''分管领导ID''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'sys_user' and column_name = 'charge_leader_name');
set @sql := if(@col_exists = 0,
    'alter table sys_user add column charge_leader_name varchar(64) default null comment ''分管领导姓名''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 43.3 sys_logininfor msg 列加长（MODIFY 幂等）
ALTER TABLE sys_logininfor MODIFY COLUMN msg VARCHAR(500) DEFAULT '' COMMENT '提示消息';

-- 43.4 mk_customer 补 5 列（information_schema + PREPARE 幂等加列）
set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'credit_days');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column credit_days int default 0 comment ''信用天数''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'credit_limit');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column credit_limit decimal(18,2) default null comment ''信用额度''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'customer_type');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column customer_type varchar(32) default null comment ''客户类型''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'parent_customer_id');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column parent_customer_id varchar(64) default null comment ''上级客户编码''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

set @col_exists := (select count(*) from information_schema.columns
    where table_schema = database() and table_name = 'mk_customer' and column_name = 'contact_phone');
set @sql := if(@col_exists = 0,
    'alter table mk_customer add column contact_phone varchar(32) default null comment ''联系电话''',
    'select 1');
prepare stmt from @sql; execute stmt; deallocate prepare stmt;

-- 43.5 金蝶菜单（一级菜单按 perms 判重自增 id，生产 9063109 有占用风险）
SET @kingdeeMenuId := (SELECT menu_id FROM sys_menu WHERE perms = 'kingdee:config:list' AND menu_type = 'C' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, query, route_name, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶云星辰集成', 9063000, 3, 'kingdee', 'kingdee/index', '', '', 1, 0, 'C', '0', '0', 'kingdee:config:list', 'edit',
       'admin', NOW(), '金蝶云星辰对接（基础数据同步）'
WHERE @kingdeeMenuId IS NULL
  AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_id = 9063109);
SET @kingdeeMenuId := (SELECT menu_id FROM sys_menu WHERE perms = 'kingdee:config:list' AND menu_type = 'C' LIMIT 1);

-- 历史环境修正（仅针对金蝶旧结构；menu_name 限定防误删生产同 id 菜单）
DELETE FROM sys_menu WHERE menu_id = 9063108 AND menu_name IN ('金蝶对接', '金蝶云星辰集成', '金蝶配置');

-- 按钮权限（5 个，按 perms 判重）
INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '金蝶配置修改', m.menu_id, 1, 'F', '0', '0', 'kingdee:config:edit', '#', 'admin', NOW(), '保存配置 + 测试连通性'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:config:edit');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '客户同步', m.menu_id, 2, 'F', '0', '0', 'kingdee:sync:customer', '#', 'admin', NOW(), '手动触发客户全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:customer');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '物料同步', m.menu_id, 3, 'F', '0', '0', 'kingdee:sync:material', '#', 'admin', NOW(), '手动触发物料全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:material');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '供应商同步', m.menu_id, 4, 'F', '0', '0', 'kingdee:sync:supplier', '#', 'admin', NOW(), '手动触发供应商全量同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:supplier');

INSERT INTO sys_menu (menu_name, parent_id, order_num, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '全量同步', m.menu_id, 5, 'F', '0', '0', 'kingdee:sync:all', '#', 'admin', NOW(), '客户+物料+供应商一起同步'
FROM sys_menu m WHERE m.perms = 'kingdee:config:list' AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'kingdee:sync:all');

-- 43.6 管理员角色授权（先删后插，范围限定金蝶菜单）
DELETE FROM sys_role_menu
WHERE menu_id IN (
  SELECT menu_id FROM sys_menu WHERE perms LIKE 'kingdee:%'
);
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms LIKE 'kingdee:%';

-- 43.7 金蝶同步定时任务（Quartz，NOT EXISTS 幂等）
INSERT INTO sys_job (job_name, job_group, invoke_target, cron_expression, misfire_policy, concurrent, status, create_by, create_time, remark)
SELECT '金蝶基础数据同步', 'DEFAULT', 'kingdeeSyncJob.runAll()', '0 */30 * * * ?', '1', '1', '0', 'admin', NOW(), '每 30 分钟同步一次客户/物料/供应商'
WHERE NOT EXISTS (SELECT 1 FROM sys_job WHERE invoke_target = 'kingdeeSyncJob.runAll()');

-- 43.8 执行后复核
SELECT 'KINGDEE_MENU_COUNT' tag, COUNT(*) cnt FROM sys_menu WHERE perms LIKE 'kingdee:%';
SELECT 'KINGDEE_CONFIG' tag, COUNT(*) cnt FROM sys_config WHERE config_key LIKE 'kingdee.%';
SELECT 'KINGDEE_JOB' tag, COUNT(*) cnt FROM sys_job WHERE invoke_target = 'kingdeeSyncJob.runAll()';
-- =============================================================
