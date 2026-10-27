-- 采购合同表新增 付款条款、交货条款 字段
ALTER TABLE pms_contract ADD COLUMN payment_terms VARCHAR(1000) COMMENT '付款条款' AFTER contract_amount;
ALTER TABLE pms_contract ADD COLUMN delivery_terms VARCHAR(1000) COMMENT '交货条款' AFTER payment_terms;
