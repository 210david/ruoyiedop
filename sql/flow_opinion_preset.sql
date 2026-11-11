-- ----------------------------
-- 签字意见「常用意见」两级配置
-- 1. 系统默认短语：字典 flow_opinion_preset（管理员在「系统管理-字典管理」维护，全员生效）
-- 2. 个人常用意见：flow_opinion_preset 表（办理人在办理页「设置」里自己维护，只影响本人）
--    个人未配置时自动回退系统默认短语。
-- 参考泛微 E9「签字意见常用语」：个人可自定义，管理员可配企业统一话术。
-- ----------------------------
DROP TABLE IF EXISTS flow_opinion_preset;
CREATE TABLE flow_opinion_preset (
  preset_id     bigint       NOT NULL AUTO_INCREMENT           COMMENT '主键',
  user_id       bigint       NOT NULL                          COMMENT '所属用户ID（数据边界，接口只操作本人数据）',
  content       varchar(200) NOT NULL                          COMMENT '常用意见内容（纯文本）',
  show_order    int          DEFAULT 0                         COMMENT '排序（越小越靠前）',
  create_by     varchar(64)  DEFAULT ''                        COMMENT '创建者',
  create_time   datetime     DEFAULT NULL                      COMMENT '创建时间',
  update_by     varchar(64)  DEFAULT ''                        COMMENT '更新者',
  update_time   datetime     DEFAULT NULL                      COMMENT '更新时间',
  PRIMARY KEY (preset_id),
  KEY idx_user_order (user_id, show_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签字意见-个人常用意见';

-- ----------------------------
-- 系统默认短语（字典），管理员可在字典管理里改文案 / 加条目
-- 已存在则跳过，避免重复执行报错
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, dict_category, status, create_by, create_time, remark)
SELECT '流程签字意见常用语', 'flow_opinion_preset', 'system', '0', 'admin', NOW(),
       '办理页「常用意见」系统默认短语；个人可在办理页「设置」中自定义覆盖'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_type WHERE dict_type = 'flow_opinion_preset');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
SELECT t.sort_no, t.label, t.val, 'flow_opinion_preset', NULL, 'default', 'N', '0', 'admin', NOW(), '签字意见常用语'
FROM (
            SELECT 1 AS sort_no, '同意' AS label, '1' AS val
  UNION ALL SELECT 2, '情况属实，同意办理', '2'
  UNION ALL SELECT 3, '同意，请领导审批', '3'
  UNION ALL SELECT 4, '请补充相关材料后再提交', '4'
  UNION ALL SELECT 5, '不同意，请修改后重新提交', '5'
) t
WHERE NOT EXISTS (SELECT 1 FROM sys_dict_data WHERE dict_type = 'flow_opinion_preset');

-- ----------------------------
-- 回退语句（如需还原）
-- ----------------------------
-- DROP TABLE IF EXISTS flow_opinion_preset;
-- DELETE FROM sys_dict_data WHERE dict_type = 'flow_opinion_preset';
-- DELETE FROM sys_dict_type WHERE dict_type = 'flow_opinion_preset';
