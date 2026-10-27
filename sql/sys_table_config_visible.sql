-- =============================================
-- 扩展 sys_table_config 表：支持列显隐配置
-- col_width 改为可空（仅保存列宽时有值）
-- 新增 col_visible 列（1=可见, 0=隐藏, NULL=未设置）
-- 同一行可同时存储列宽和列显隐
-- =============================================

ALTER TABLE `sys_table_config`
  MODIFY COLUMN `col_width` INT(11) DEFAULT NULL COMMENT '列宽（像素），NULL表示未设置',
  ADD COLUMN `col_visible` TINYINT(1) DEFAULT NULL COMMENT '列是否可见：1=可见，0=隐藏，NULL=未设置';
