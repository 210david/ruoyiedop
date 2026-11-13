-- ============================================================
-- 升级脚本：pjm_payment 回款凭证字段
-- 日期：2026-11-07
-- 背景：登记回款页面增加"回款凭证"附件上传（统一 FileUpload 组件，多个凭证以逗号分隔 url 存储），
--       后端 PjmPayment 实体与 PjmPaymentMapper.xml 同步增加 voucher_urls 字段。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- ============================================================

-- 回滚语句（如需移除本字段时执行，注意先确认无业务数据依赖）：
-- ALTER TABLE pjm_payment DROP COLUMN voucher_urls;

ALTER TABLE pjm_payment ADD COLUMN voucher_urls varchar(1000) DEFAULT NULL COMMENT '回款凭证(多个url逗号分隔)' AFTER payer;
