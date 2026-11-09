-- ============================================================
-- 升级脚本：pjm_task_worklog 增加附件字段
-- 日期：2026-11-08
-- 背景：项目管理-工时填报页面增加"附件"上传（统一 FileUpload 组件，多个附件以逗号分隔 url 存储），
--       后端 PjmTaskWorklog 实体与 PjmTaskWorklogMapper.xml 同步增加 attachment 字段。
-- 适用库：ruoyi_lyjs（本地开发库与客户库通用，可重复执行）
-- ============================================================

-- 回滚语句（如需移除本字段时执行，注意先确认无业务数据依赖）：
-- ALTER TABLE pjm_task_worklog DROP COLUMN attachment;

SET @col_exists := (
  SELECT COUNT(1) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = DATABASE()
    AND TABLE_NAME = 'pjm_task_worklog'
    AND COLUMN_NAME = 'attachment'
);

SET @ddl := IF(@col_exists = 0,
  'ALTER TABLE pjm_task_worklog ADD COLUMN attachment varchar(1000) DEFAULT NULL COMMENT ''工时附件(多个url逗号分隔)'' AFTER work_content',
  'SELECT ''pjm_task_worklog.attachment 已存在，跳过'' AS msg');

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
