-- 入职管理：材料清单/岗前体检/三级安全教育改为附件字段 + 新增审核日志表
-- 执行时间: 2026-09-01

-- 1. 三个字段改为附件存储（逗号分隔文件URL），char(1) 的两个扩为 text
ALTER TABLE biz_hr_entry MODIFY COLUMN materials_status text COMMENT '材料清单附件（逗号分隔文件URL）';
ALTER TABLE biz_hr_entry MODIFY COLUMN health_check text COMMENT '岗前体检附件（逗号分隔文件URL）';
ALTER TABLE biz_hr_entry MODIFY COLUMN safety_education text COMMENT '三级安全教育附件（逗号分隔文件URL）';

-- 2. 历史标志位数据清理（'0'/'1' 标志无附件含义，置空）
UPDATE biz_hr_entry SET health_check = NULL WHERE health_check IN ('0', '1');
UPDATE biz_hr_entry SET safety_education = NULL WHERE safety_education IN ('0', '1');

-- 3. 入职审核日志表（幂等）
CREATE TABLE IF NOT EXISTS biz_hr_entry_audit_log (
  log_id bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  entry_id bigint NOT NULL COMMENT '入职单ID',
  audit_action varchar(2) NOT NULL COMMENT '审核动作（1通过 2驳回）',
  audit_by varchar(64) NOT NULL COMMENT '审核人',
  audit_time datetime NOT NULL COMMENT '审核时间',
  audit_remark varchar(500) DEFAULT NULL COMMENT '审核意见',
  PRIMARY KEY (log_id),
  KEY idx_entry_id (entry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='入职审核日志';
