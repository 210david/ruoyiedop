-- ============================================================
-- 社保公积金"方案+覆盖"改造（2026-09-03）
-- 1) 新增社保方案表：全公司默认比例维护一处
-- 2) 员工社保记录增加 plan_id：员工只录缴费基数，比例继承方案
-- 3) 菜单：社保方案管理
-- ============================================================

-- 1. 社保方案表
CREATE TABLE IF NOT EXISTS `biz_hr_ss_plan` (
  `plan_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '方案ID',
  `plan_name` VARCHAR(100) NOT NULL COMMENT '方案名称',
  `year` INT NOT NULL COMMENT '适用年度',
  `is_default` TINYINT DEFAULT 0 COMMENT '默认方案(0否1是)',
  `base_lower` DECIMAL(10,2) DEFAULT NULL COMMENT '基数下限',
  `base_upper` DECIMAL(10,2) DEFAULT NULL COMMENT '基数上限',
  `pension_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '养老单位比例',
  `pension_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '养老个人比例',
  `medical_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '医疗单位比例',
  `medical_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '医疗个人比例',
  `unemployment_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '失业单位比例',
  `unemployment_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '失业个人比例',
  `injury_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '工伤单位比例',
  `maternity_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '生育单位比例',
  `fund_employer` DECIMAL(8,4) DEFAULT 0 COMMENT '公积金单位比例',
  `fund_employee` DECIMAL(8,4) DEFAULT 0 COMMENT '公积金个人比例',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常1停用)',
  `del_flag` CHAR(1) DEFAULT '0' COMMENT '删除标志',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`plan_id`),
  KEY `idx_year` (`year`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='HR社保方案表';

-- 2. 员工社保记录增加方案关联（存量数据不破坏）
ALTER TABLE `biz_hr_social_security`
  ADD COLUMN `plan_id` BIGINT DEFAULT NULL COMMENT '社保方案ID(空=用自身比例字段，兼容存量)' AFTER `base_amount`;

-- 3. 初始化曲靖地区默认方案（2026年度，比例可按实际政策调整）
INSERT INTO `biz_hr_ss_plan`
  (`plan_name`, `year`, `is_default`, `base_lower`, `base_upper`,
   `pension_employer`, `pension_employee`, `medical_employer`, `medical_employee`,
   `unemployment_employer`, `unemployment_employee`, `injury_employer`, `maternity_employer`,
   `fund_employer`, `fund_employee`, `status`, `create_by`, `create_time`, `remark`)
SELECT '曲靖标准方案', 2026, 1, 4000, 20000,
       0.1600, 0.0800, 0.0850, 0.0200,
       0.0050, 0.0050, 0.0090, 0.0080,
       0.1200, 0.1200, '0', 'admin', sysdate(), '曲靖地区默认比例，基数上下限请按当年公布值调整'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `biz_hr_ss_plan` WHERE `year` = 2026);

-- 4. 存量员工记录关联默认方案
UPDATE `biz_hr_social_security` s
JOIN `biz_hr_ss_plan` p ON p.`year` = s.`year` AND p.`is_default` = 1 AND p.`del_flag` = '0'
SET s.`plan_id` = p.`plan_id`
WHERE s.`plan_id` IS NULL;

-- 5. 菜单：社保方案管理（挂在社保公积金同级）
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保方案', parent.menu_id, 22, 'ssPlan', 'hr/ssPlan/index', 1, 0, 'C', '0', '0', 'hr:ssPlan:list', 'money', 'admin', sysdate(), '社保方案管理'
FROM (SELECT menu_id FROM sys_menu WHERE perms = 'hr:socialSecurity:list' AND menu_type = 'C' LIMIT 1) parent
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:ssPlan:list');

SET @planMenuId = (SELECT menu_id FROM sys_menu WHERE perms = 'hr:ssPlan:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保方案查询', @planMenuId, 1, '#', '', 1, 0, 'F', '0', '0', 'hr:ssPlan:query', '#', 'admin', sysdate(), ''
FROM DUAL WHERE @planMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:ssPlan:query');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保方案新增', @planMenuId, 2, '#', '', 1, 0, 'F', '0', '0', 'hr:ssPlan:add', '#', 'admin', sysdate(), ''
FROM DUAL WHERE @planMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:ssPlan:add');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保方案修改', @planMenuId, 3, '#', '', 1, 0, 'F', '0', '0', 'hr:ssPlan:edit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE @planMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:ssPlan:edit');
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保方案删除', @planMenuId, 4, '#', '', 1, 0, 'F', '0', '0', 'hr:ssPlan:remove', '#', 'admin', sysdate(), ''
FROM DUAL WHERE @planMenuId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:ssPlan:remove');
-- 社保公积金页新增按钮权限：批量初始化
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '社保批量初始化', (SELECT menu_id FROM sys_menu WHERE perms = 'hr:socialSecurity:list' LIMIT 1), 6, '#', '', 1, 0, 'F', '0', '0', 'hr:socialSecurity:batchInit', '#', 'admin', sysdate(), ''
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms = 'hr:socialSecurity:batchInit');

-- 6. 员工社保比例字段默认值改为 NULL（空=继承方案），存量 0 值清洗为 NULL（2026-09-07）
-- 注意：0 会被工资核算引擎当作"员工覆盖 0%"导致社保算 0，必须为 NULL 才继承方案
ALTER TABLE `biz_hr_social_security`
  MODIFY `pension_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '养老单位比例(空=继承方案)',
  MODIFY `pension_employee` DECIMAL(8,4) DEFAULT NULL COMMENT '养老个人比例(空=继承方案)',
  MODIFY `medical_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '医疗单位比例(空=继承方案)',
  MODIFY `medical_employee` DECIMAL(8,4) DEFAULT NULL COMMENT '医疗个人比例(空=继承方案)',
  MODIFY `unemployment_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '失业单位比例(空=继承方案)',
  MODIFY `unemployment_employee` DECIMAL(8,4) DEFAULT NULL COMMENT '失业个人比例(空=继承方案)',
  MODIFY `injury_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '工伤单位比例(空=继承方案)',
  MODIFY `maternity_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '生育单位比例(空=继承方案)',
  MODIFY `fund_employer` DECIMAL(8,4) DEFAULT NULL COMMENT '公积金单位比例(空=继承方案)',
  MODIFY `fund_employee` DECIMAL(8,4) DEFAULT NULL COMMENT '公积金个人比例(空=继承方案)';
UPDATE `biz_hr_social_security` SET `pension_employer` = NULL WHERE `pension_employer` = 0;
UPDATE `biz_hr_social_security` SET `pension_employee` = NULL WHERE `pension_employee` = 0;
UPDATE `biz_hr_social_security` SET `medical_employer` = NULL WHERE `medical_employer` = 0;
UPDATE `biz_hr_social_security` SET `medical_employee` = NULL WHERE `medical_employee` = 0;
UPDATE `biz_hr_social_security` SET `unemployment_employer` = NULL WHERE `unemployment_employer` = 0;
UPDATE `biz_hr_social_security` SET `unemployment_employee` = NULL WHERE `unemployment_employee` = 0;
UPDATE `biz_hr_social_security` SET `injury_employer` = NULL WHERE `injury_employer` = 0;
UPDATE `biz_hr_social_security` SET `maternity_employer` = NULL WHERE `maternity_employer` = 0;
UPDATE `biz_hr_social_security` SET `fund_employer` = NULL WHERE `fund_employer` = 0;
UPDATE `biz_hr_social_security` SET `fund_employee` = NULL WHERE `fund_employee` = 0;
