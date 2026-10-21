-- =============================================
-- 供应商表字段扩展（WMS + PMS 共用）
-- 执行前请备份数据库
-- =============================================

-- =============================================
-- 一、ALTER TABLE 扩展 wms_supplier 字段
-- =============================================

ALTER TABLE wms_supplier
    ADD COLUMN supplier_short_name  VARCHAR(128)                          COMMENT '供应商简称'           AFTER supplier_name,
    ADD COLUMN supplier_type        VARCHAR(20)  DEFAULT '0'              COMMENT '供应商类型（字典 wms_supplier_type）' AFTER supplier_short_name,
    ADD COLUMN supplier_level       VARCHAR(10)                           COMMENT '供应商等级（字典 wms_supplier_level）' AFTER supplier_type,
    ADD COLUMN unified_credit_code  VARCHAR(64)                           COMMENT '统一社会信用代码'       AFTER supplier_level,
    ADD COLUMN email                VARCHAR(128)                          COMMENT '邮箱'                  AFTER contact_phone,
    ADD COLUMN fax                  VARCHAR(20)                           COMMENT '传真'                  AFTER email,
    ADD COLUMN website              VARCHAR(255)                          COMMENT '网址'                  AFTER fax,
    ADD COLUMN postcode             VARCHAR(10)                           COMMENT '邮政编码'              AFTER website,
    ADD COLUMN legal_person         VARCHAR(64)                           COMMENT '法人代表'              AFTER address,
    ADD COLUMN registered_capital   DECIMAL(18,2) DEFAULT 0               COMMENT '注册资本（万元）'      AFTER legal_person,
    ADD COLUMN enterprise_nature    VARCHAR(20)                           COMMENT '企业性质（字典 wms_enterprise_nature）' AFTER registered_capital,
    ADD COLUMN business_scope       VARCHAR(1000)                         COMMENT '经营范围'              AFTER enterprise_nature,
    ADD COLUMN established_date     DATE                                  COMMENT '成立日期'              AFTER business_scope,
    ADD COLUMN bank_name            VARCHAR(128)                          COMMENT '开户银行'              AFTER established_date,
    ADD COLUMN bank_branch          VARCHAR(128)                          COMMENT '开户支行'              AFTER bank_name,
    ADD COLUMN bank_account         VARCHAR(64)                           COMMENT '银行账号'              AFTER bank_branch,
    ADD COLUMN tax_number           VARCHAR(64)                           COMMENT '税号'                  AFTER bank_account,
    ADD COLUMN invoice_title        VARCHAR(255)                          COMMENT '发票抬头'              AFTER tax_number,
    ADD COLUMN invoice_address      VARCHAR(500)                          COMMENT '发票地址'              AFTER invoice_title,
    ADD COLUMN invoice_phone        VARCHAR(20)                           COMMENT '发票联系电话'          AFTER invoice_address,
    ADD COLUMN payment_method       VARCHAR(20)  DEFAULT '0'              COMMENT '付款方式（字典 wms_payment_method）' AFTER invoice_phone,
    ADD COLUMN payment_days         INT          DEFAULT 0                COMMENT '账期天数'              AFTER payment_method,
    ADD COLUMN settlement_type      VARCHAR(20)  DEFAULT '0'              COMMENT '结算方式（字典 wms_settlement_type）' AFTER payment_days,
    ADD COLUMN currency             VARCHAR(10)  DEFAULT 'CNY'            COMMENT '结算币种（字典 wms_currency）' AFTER settlement_type,
    ADD COLUMN delivery_cycle       INT                                   COMMENT '交货周期（天）'        AFTER currency,
    ADD COLUMN min_order_amount     DECIMAL(18,2) DEFAULT 0               COMMENT '最小订单金额'          AFTER delivery_cycle,
    ADD COLUMN cooperation_date     DATE                                  COMMENT '合作开始日期'          AFTER min_order_amount,
    ADD COLUMN qualification_status CHAR(1)      DEFAULT '0'              COMMENT '资质状态（0未审核 1已审核 2审核不通过）' AFTER cooperation_date;

-- 补充索引
ALTER TABLE wms_supplier
    ADD KEY idx_supplier_type (supplier_type),
    ADD KEY idx_supplier_level (supplier_level),
    ADD KEY idx_unified_credit_code (unified_credit_code);

-- 修改表注释
ALTER TABLE wms_supplier COMMENT='供应商表（WMS/PMS共用）';

-- =============================================
-- 二、新增字典类型
-- =============================================

INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
SELECT * FROM (
    SELECT '供应商类型' AS dict_name, 'wms_supplier_type' AS dict_type, '0' AS status, 'admin' AS create_by, sysdate() AS create_time, '供应商类型字典' AS remark
    UNION ALL SELECT '供应商等级', 'wms_supplier_level', '0', 'admin', sysdate(), '供应商等级字典'
    UNION ALL SELECT '企业性质', 'wms_enterprise_nature', '0', 'admin', sysdate(), '企业性质字典'
    UNION ALL SELECT '付款方式', 'wms_payment_method', '0', 'admin', sysdate(), '付款方式字典'
    UNION ALL SELECT '结算方式', 'wms_settlement_type', '0', 'admin', sysdate(), '结算方式字典'
    UNION ALL SELECT '结算币种', 'wms_currency', '0', 'admin', sysdate(), '结算币种字典'
    UNION ALL SELECT '资质状态', 'wms_qualification_status', '0', 'admin', sysdate(), '供应商资质状态字典'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type dt WHERE dt.dict_type = t.dict_type);

-- =============================================
-- 三、新增字典数据
-- =============================================

-- 供应商类型
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '厂商', '0', 'wms_supplier_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '经销商', '1', 'wms_supplier_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '代理商', '2', 'wms_supplier_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '服务商', '3', 'wms_supplier_type', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_type' AND dict_value='3');

-- 供应商等级
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, 'A级（战略）', 'A', 'wms_supplier_level', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='A');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, 'B级（优秀）', 'B', 'wms_supplier_level', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='B');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, 'C级（合格）', 'C', 'wms_supplier_level', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='C');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, 'D级（待改进）', 'D', 'wms_supplier_level', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_supplier_level' AND dict_value='D');

-- 企业性质
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '国有企业', '0', 'wms_enterprise_nature', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '民营企业', '1', 'wms_enterprise_nature', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '外资企业', '2', 'wms_enterprise_nature', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='2');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 4, '合资企业', '3', 'wms_enterprise_nature', '', 'info', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='3');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 5, '个体工商户', '4', 'wms_enterprise_nature', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_enterprise_nature' AND dict_value='4');

-- 付款方式
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '现结', '0', 'wms_payment_method', '', 'success', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_payment_method' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '月结', '1', 'wms_payment_method', '', 'primary', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_payment_method' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '预付', '2', 'wms_payment_method', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_payment_method' AND dict_value='2');

-- 结算方式
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '银行转账', '0', 'wms_settlement_type', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_settlement_type' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '承兑汇票', '1', 'wms_settlement_type', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_settlement_type' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '现金', '2', 'wms_settlement_type', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_settlement_type' AND dict_value='2');

-- 结算币种
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '人民币', 'CNY', 'wms_currency', '', 'primary', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_currency' AND dict_value='CNY');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '美元', 'USD', 'wms_currency', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_currency' AND dict_value='USD');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '欧元', 'EUR', 'wms_currency', '', 'warning', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_currency' AND dict_value='EUR');

-- 资质状态
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 1, '未审核', '0', 'wms_qualification_status', '', 'info', 'Y', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_qualification_status' AND dict_value='0');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 2, '已审核', '1', 'wms_qualification_status', '', 'success', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_qualification_status' AND dict_value='1');
INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT 3, '审核不通过', '2', 'wms_qualification_status', '', 'danger', 'N', '0', 'admin', sysdate(), ''
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type='wms_qualification_status' AND dict_value='2');

-- =============================================
-- 执行完毕
-- =============================================
