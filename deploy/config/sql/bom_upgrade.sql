-- ============================================================
-- BOM 管理升级 - 增加多层级、基准数量、供应方式、发布状态等
-- ============================================================

USE `ry-vue`;
SET NAMES utf8mb4;

-- ============================================================
-- 1. mms_bom 主表增加字段
-- ============================================================
ALTER TABLE `mms_bom`
  ADD COLUMN `base_qty`  DECIMAL(18,6) DEFAULT 1 COMMENT '基准数量(生产多少个产品)' AFTER `version`,
  ADD COLUMN `base_unit` VARCHAR(20)  DEFAULT '' COMMENT '基准单位' AFTER `base_qty`;

-- 状态改为：0=草稿 1=已发布 2=停用（兼容旧数据：原 0=启用 统一视为草稿）
-- 旧数据 status='0' 的保持不变(草稿)，旧数据 status='1' 的改为 '2'(停用)
UPDATE `mms_bom` SET `status` = '2' WHERE `status` = '1';

-- ============================================================
-- 2. mms_bom_detail 明细表增加字段
-- ============================================================
ALTER TABLE `mms_bom_detail`
  ADD COLUMN `seq`           INT           DEFAULT 0    COMMENT '行序号' AFTER `bom_id`,
  ADD COLUMN `supply_type`    CHAR(1)       DEFAULT '1'  COMMENT '供应方式(1直接领料 2倒冲 3车间库存)' AFTER `is_key_material`,
  ADD COLUMN `pick_store_id` BIGINT(20)    DEFAULT NULL COMMENT '默认发料仓库ID' AFTER `supply_type`,
  ADD COLUMN `is_phantom`    CHAR(1)       DEFAULT '0'  COMMENT '是否虚拟件(0否 1是)' AFTER `pick_store_id`,
  ADD COLUMN `bom_ref_id`    BIGINT(20)    DEFAULT NULL COMMENT '引用BOM ID(子项为半成品时关联其BOM,支持多层级展开)' AFTER `is_phantom`;

-- 增加索引
ALTER TABLE `mms_bom_detail` ADD INDEX `idx_bom_ref_id` (`bom_ref_id`);

-- ============================================================
-- 3. 更新字典：BOM状态改为 草稿/已发布/停用
-- ============================================================
-- 先删除旧的 mms_bom_status 字典数据（如果存在）
DELETE FROM `sys_dict_data` WHERE `dict_type` = 'mms_bom_status';
DELETE FROM `sys_dict_type` WHERE `dict_type` = 'mms_bom_status';

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
VALUES ('BOM状态', 'mms_bom_status', '0', 'admin', sysdate(), 'BOM状态：草稿/已发布/停用');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES
(1, '草稿',   '0', 'mms_bom_status', '', 'warning', 'Y', '0', 'admin', sysdate(), '草稿-可编辑'),
(2, '已发布', '1', 'mms_bom_status', '', 'success', 'N', '0', 'admin', sysdate(), '已发布-不可编辑，可被工单引用'),
(3, '停用',   '2', 'mms_bom_status', '', 'info',    'N', '0', 'admin', sysdate(), '停用-不可被引用');

-- ============================================================
-- 4. 新增字典：BOM供应方式
-- ============================================================
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'BOM供应方式', 'mms_supply_type', '0', 'admin', sysdate(), 'BOM子件供应方式'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'mms_supply_type');

INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
VALUES
(1, '直接领料', '1', 'mms_supply_type', '', 'primary', 'Y', '0', 'admin', sysdate(), '按BOM明细直接领料'),
(2, '倒冲',     '2', 'mms_supply_type', '', 'success', 'N', '0', 'admin', sysdate(), '完工后按实际消耗倒冲库存'),
(3, '车间库存', '3', 'mms_supply_type', '', 'info',    'N', '0', 'admin', sysdate(), '从车间库存消耗');
