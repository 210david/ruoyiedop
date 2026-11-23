-- ============================================================
-- 综合办公：司机台账去掉「工号」字段
-- 1) 页面不再维护/展示工号，导入改为按手机号识别重复数据
-- 2) 先备份原工号数据，再从表中删除列与索引
-- 执行库：按实际环境替换（开发库 ry-vue，其余环境同名库）
-- 执行日期：2026-11-23
-- 注意：需与新版本后端 jar 同时上线；旧 jar 仍会读写 driver_no 列。
-- ============================================================

USE `ruoyi_cfsp`;

-- ---------- 1. 备份工号数据（便于回溯/回滚） ----------
DROP TABLE IF EXISTS biz_oa_driver_bak_20261123;
CREATE TABLE biz_oa_driver_bak_20261123 AS
SELECT driver_id, driver_name, driver_no, phone, owner_dept, del_flag
FROM biz_oa_driver;

-- ---------- 2. 检查是否存在手机号重复（去掉工号后改用手机号识别重复） ----------
SELECT phone, COUNT(*) AS cnt, GROUP_CONCAT(driver_id) AS driver_ids, GROUP_CONCAT(driver_name) AS driver_names
FROM biz_oa_driver
WHERE del_flag = '0' AND phone IS NOT NULL AND phone <> ''
GROUP BY phone
HAVING COUNT(*) > 1;

-- 说明：如上面查询有结果，说明这些手机号对应多条司机记录。
--       导入时不带「更新已存在的数据」仍会因手机号重复而失败，需人工合并或补全手机号。

-- ---------- 3. 删除工号列与索引 ----------
ALTER TABLE biz_oa_driver
    DROP INDEX idx_oa_driver_no,
    DROP COLUMN driver_no;

-- ---------- 4. 校验 ----------
SELECT driver_id, driver_name, phone, license_type, license_expire, owner_dept, status
FROM biz_oa_driver
WHERE del_flag = '0'
ORDER BY driver_id DESC;

-- 回滚（如需恢复）：
-- ALTER TABLE biz_oa_driver ADD COLUMN driver_no varchar(64) DEFAULT NULL COMMENT '工号' AFTER driver_name;
-- ALTER TABLE biz_oa_driver ADD KEY idx_oa_driver_no (driver_no);
-- UPDATE biz_oa_driver d JOIN biz_oa_driver_bak_20261123 b ON d.driver_id = b.driver_id SET d.driver_no = b.driver_no;
