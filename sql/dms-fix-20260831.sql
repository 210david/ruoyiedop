-- ============================================================
-- DMS 缺陷修复 SQL 脚本
-- 日期: 2026-08-31
-- 覆盖: DEF-01 (设备网关访问密钥链路) / DEF-02 (dms角色消息中心权限)
-- ============================================================

-- ------------------------------------------------------------
-- DEF-01: 采集配置表增加设备访问密钥字段
-- ------------------------------------------------------------
ALTER TABLE dms_data_collection_config
    ADD COLUMN access_key VARCHAR(64) DEFAULT NULL COMMENT '设备访问密钥(sk_前缀)' AFTER status;

-- 为已有采集配置生成初始密钥（32位随机，基于UUID去连字符）
UPDATE dms_data_collection_config
SET access_key = CONCAT('sk_', REPLACE(UUID(), '-', ''))
WHERE access_key IS NULL AND del_flag = '0';

-- ------------------------------------------------------------
-- DEF-02: 为 dms 四个角色补配消息中心权限
--   菜单 905404 = 消息中心 (system:message:list)
--   菜单 905405 = 消息查询 (system:message:query)
--   角色 200/201/202/203 = dms_* 系列
-- ------------------------------------------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN (SELECT 905404 AS menu_id UNION SELECT 905405) m
WHERE r.role_id IN (200, 201, 202, 203)
  AND NOT EXISTS (
      SELECT 1 FROM sys_role_menu rm
      WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id
  );
