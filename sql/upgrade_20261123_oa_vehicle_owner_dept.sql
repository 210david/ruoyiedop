-- ============================================================
-- 综合办公：车辆台账「所属部门」改为部门弹框选择
-- 1) 新增 owner_dept_id（sys_dept.dept_id），用于精确筛选与责任归属
-- 2) owner_dept 保留，作为部门名称快照（列表/详情/导入导出展示）
-- 执行库：按实际环境替换（开发库 ry-vue，其余环境同名库）
-- 执行日期：2026-11-23
-- 注意：需与新版本后端 jar 同时上线；旧 jar 不会读写 owner_dept_id，可平滑兼容。
-- ============================================================

USE `ruoyi_cfsp`;

-- ---------- 1. 新增部门ID字段 ----------
ALTER TABLE biz_oa_vehicle
    ADD COLUMN owner_dept_id bigint DEFAULT NULL COMMENT '所属部门ID(sys_dept.dept_id)' AFTER owner_dept;

-- ---------- 2. 存量数据回填（按名称唯一匹配的部门） ----------
UPDATE biz_oa_vehicle v
JOIN sys_dept d ON TRIM(v.owner_dept) = d.dept_name AND d.del_flag = '0'
SET v.owner_dept_id = d.dept_id
WHERE v.owner_dept IS NOT NULL AND v.owner_dept <> '' AND v.owner_dept_id IS NULL;

-- ---------- 3. 校验：名称存在但未能匹配到唯一部门的记录（需人工处理） ----------
SELECT v.vehicle_id, v.plate_no, v.owner_dept,
       (SELECT COUNT(*) FROM sys_dept d WHERE TRIM(v.owner_dept) = d.dept_name AND d.del_flag = '0') AS match_cnt
FROM biz_oa_vehicle v
WHERE v.del_flag = '0' AND v.owner_dept IS NOT NULL AND v.owner_dept <> ''
ORDER BY match_cnt ASC, v.vehicle_id;

-- 回滚：
-- UPDATE biz_oa_vehicle SET owner_dept = (SELECT dept_name FROM sys_dept WHERE dept_id = owner_dept_id);
-- ALTER TABLE biz_oa_vehicle DROP COLUMN owner_dept_id;
