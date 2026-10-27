-- P3测试修复：消息redirect_url路由修正（与sys_menu真实路由对齐）
-- 2026-10-14  CFSP专用，禁止同步标准版
USE RUOYI_CFSP;

-- 1) 工序转移损耗预警：/wms/batch/wipTransfer → /mms/execution/wipTransfer
UPDATE sys_message SET redirect_url = '/mms/execution/wipTransfer'
WHERE redirect_url = '/wms/batch/wipTransfer';

-- 2) 健康证到期提醒：/hr/certificate → /hr/extra/certificate
UPDATE sys_message SET redirect_url = '/hr/extra/certificate'
WHERE redirect_url = '/hr/certificate';

-- 核对
SELECT message_id, biz_source, redirect_url, biz_entry_name FROM sys_message
WHERE message_id IN (10, 11, 16, 17);
