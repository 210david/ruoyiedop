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
