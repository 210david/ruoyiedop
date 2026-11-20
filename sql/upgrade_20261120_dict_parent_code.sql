-- ---------------------------------------------------------------------------
-- 补齐 803a28b4 提交遗漏的数据库升级：
--   fix(pjm): filter material picker by tab type + WBS task default value fix
-- 该提交在 SysDictData 实体与 SysDictDataMapper.xml 中新增了 parent_code
-- （父级字典编码，支持两级级联字典），但未附 ALTER 脚本，导致后端启动时
-- selectDictDataList 报 Unknown column 'parent_code'。
--
-- 幂等性说明：MySQL 不支持 ADD COLUMN IF NOT EXISTS，重复执行会报 Duplicate column；
-- 如需重复执行，请先核对 information_schema.COLUMNS 中列是否已存在。列类型与实体 Long parentCode 对齐为 bigint。
-- ---------------------------------------------------------------------------

ALTER TABLE sys_dict_data
    ADD COLUMN `parent_code` bigint DEFAULT NULL COMMENT '父级字典编码（顶级为NULL，支持两级级联字典）' AFTER `dict_type`;

SELECT COLUMN_NAME, COLUMN_TYPE, COLUMN_COMMENT
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
   AND TABLE_NAME = 'sys_dict_data'
   AND COLUMN_NAME = 'parent_code';
