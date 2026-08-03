-- =============================================
-- 供应商评估表增强 - 权重/单项得分/状态/审批
-- 功能：增加权重设置、单项得分、状态(待审核/已审核)、审批功能
-- =============================================

-- 1. 增加权重字段、单项得分字段、状态字段、审批字段
ALTER TABLE pms_supplier_eval
    ADD COLUMN delivery_weight   DECIMAL(5,2)  DEFAULT 30.00 COMMENT '交货评分权重(%)'    AFTER delivery_score,
    ADD COLUMN quality_weight    DECIMAL(5,2)  DEFAULT 30.00 COMMENT '质量评分权重(%)'    AFTER quality_score,
    ADD COLUMN price_weight      DECIMAL(5,2)  DEFAULT 20.00 COMMENT '价格评分权重(%)'    AFTER price_score,
    ADD COLUMN service_weight    DECIMAL(5,2)  DEFAULT 20.00 COMMENT '服务评分权重(%)'    AFTER service_score,
    ADD COLUMN delivery_item_score DECIMAL(5,2) DEFAULT 0    COMMENT '交货单项得分(=评分*权重/100)' AFTER delivery_weight,
    ADD COLUMN quality_item_score  DECIMAL(5,2) DEFAULT 0    COMMENT '质量单项得分(=评分*权重/100)' AFTER quality_weight,
    ADD COLUMN price_item_score    DECIMAL(5,2) DEFAULT 0    COMMENT '价格单项得分(=评分*权重/100)' AFTER price_weight,
    ADD COLUMN service_item_score  DECIMAL(5,2) DEFAULT 0    COMMENT '服务单项得分(=评分*权重/100)' AFTER service_weight,
    ADD COLUMN status             VARCHAR(10)   DEFAULT '0'  COMMENT '状态（0待审核 1已审核）' AFTER eval_level,
    ADD COLUMN audit_by           VARCHAR(64)                COMMENT '审核人'             AFTER status,
    ADD COLUMN audit_time         DATETIME                   COMMENT '审核时间'           AFTER audit_by,
    ADD COLUMN audit_remark       VARCHAR(500)               COMMENT '审核意见'           AFTER audit_time;

-- 增加状态索引
ALTER TABLE pms_supplier_eval ADD INDEX idx_eval_status (status);

-- 2. 回填已有数据：默认权重计算单项得分
UPDATE pms_supplier_eval SET
    delivery_weight = 30.00,
    quality_weight  = 30.00,
    price_weight    = 20.00,
    service_weight  = 20.00,
    delivery_item_score = ROUND(delivery_score * 0.30, 2),
    quality_item_score  = ROUND(quality_score  * 0.30, 2),
    price_item_score    = ROUND(price_score    * 0.20, 2),
    service_item_score  = ROUND(service_score  * 0.20, 2),
    status = '0'
WHERE delivery_weight IS NULL OR delivery_item_score IS NULL OR status IS NULL;

-- 3. 字典类型：评估状态
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT '评估状态', 'pms_eval_status', '0', 'admin', sysdate(), '供应商评估状态字典'
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'pms_eval_status');

-- 4. 字典数据：评估状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '待审核', '0', 'pms_eval_status', '', 'warning', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_status' AND dict_value='0');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已审核', '1', 'pms_eval_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_status' AND dict_value='1');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '已驳回', '2', 'pms_eval_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='pms_eval_status' AND dict_value='2');

-- 5. 菜单权限：评估审批
SET @evalMenuId = (SELECT menu_id FROM sys_menu WHERE perms='pms:eval:list' LIMIT 1);
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, menu_type, visible, status, perms, create_by, create_time)
SELECT '评估审批', @evalMenuId, 6, '', 'F', '0', '0', 'pms:eval:audit', 'admin', sysdate()
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE perms='pms:eval:audit' AND parent_id=@evalMenuId);

-- 给admin角色分配审批权限
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id FROM sys_menu WHERE perms = 'pms:eval:audit'
AND menu_id NOT IN (SELECT menu_id FROM sys_role_menu WHERE role_id=1);

-- =============================================
-- 执行完毕
-- =============================================
