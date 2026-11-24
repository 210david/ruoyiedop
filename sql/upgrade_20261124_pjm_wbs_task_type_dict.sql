-- ============================================================
-- WBS 任务类型字典（pjm_wbs_task_type）
-- 日期：2026-11-24
--
-- 背景：原前端两处文件各硬编码一份 taskTypeOptions，且值与后端语义冲突：
--   前端下拉  1=单项工程  2=单位工程  3=分部工程  4=分项工程  5=检验批
--   后端提示词 1=工作项    2=里程碑
--   同一值 2 出现两种含义（"重复"），且"里程碑"仅存在于后端暗示、下拉里选不到。
--
-- 统一方案：以「工程分解层级」为主序列（符合 WBS 逐级分解语义），
--   1 单项工程  2 单位工程  3 分部工程  4 分项工程  5 检验批
--   6 工作项（不可再分的最小作业）
--   7 里程碑（无工程量的节点，如开竣工、验收）
--   9 其他
-- 说明：历史数据 task_type=1 在两套语义下都表示顶层，无需迁移。
-- 幂等：先清理重复定义，再按 dict_value upsert。
-- ============================================================

-- 1) 字典分类：同 dict_type 只保留一条
DELETE d FROM `sys_dict_type` d
JOIN (
    SELECT `dict_type`, MIN(`dict_id`) AS keep_id
    FROM `sys_dict_type`
    WHERE `dict_type` = 'pjm_wbs_task_type'
    GROUP BY `dict_type`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type`
WHERE d.`dict_id` <> t.keep_id;

INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`)
SELECT 'WBS任务类型', 'pjm_wbs_task_type', '0', 'admin', NOW(), 'WBS任务分解的类型（工程分解层级+工作项/里程碑/其他）'
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_type` WHERE `dict_type` = 'pjm_wbs_task_type');

-- 2) 字典数据：同 dict_type + dict_value 只保留最小 dict_code
DELETE d FROM `sys_dict_data` d
JOIN (
    SELECT `dict_type`, `dict_value`, MIN(`dict_code`) AS keep_code
    FROM `sys_dict_data`
    WHERE `dict_type` = 'pjm_wbs_task_type'
    GROUP BY `dict_type`, `dict_value`
    HAVING COUNT(*) > 1
) t ON d.`dict_type` = t.`dict_type` AND d.`dict_value` = t.`dict_value`
WHERE d.`dict_code` <> t.keep_code;

-- 3) 按 dict_value 逐项 upsert（存在则更新标签与排序，不存在则插入）
INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 1, '单项工程', '1', 'pjm_wbs_task_type', '', 'primary', 'Y', '0', 'admin', NOW(), '具备独立施工条件、能独立发挥效益的工程项目'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='1');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 2, '单位工程', '2', 'pjm_wbs_task_type', '', 'success', 'N', '0', 'admin', NOW(), '有独立设计、可独立施工但不具备独立投产条件的工程'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='2');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 3, '分部工程', '3', 'pjm_wbs_task_type', '', 'info', 'N', '0', 'admin', NOW(), '按工程部位或工种划分的工程（如土建、钢结构、安装）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='3');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 4, '分项工程', '4', 'pjm_wbs_task_type', '', 'warning', 'N', '0', 'admin', NOW(), '按施工工艺或材料划分的工程（如混凝土浇筑、门窗安装）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='4');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 5, '检验批', '5', 'pjm_wbs_task_type', '', 'info', 'N', '0', 'admin', NOW(), '按同一生产条件或按规定方式汇总起来供检验用的施工段'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='5');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 6, '工作项', '6', 'pjm_wbs_task_type', '', 'primary', 'N', '0', 'admin', NOW(), '不可再分的最小作业单元（不套用工程层级时使用）'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='6');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 7, '里程碑', '7', 'pjm_wbs_task_type', '', 'danger', 'N', '0', 'admin', NOW(), '无工程量的关键节点（开工、主体封顶、竣工验收等），原值2已被单位工程占用'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='7');

INSERT INTO `sys_dict_data`
  (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`, `remark`)
SELECT 9, '其他', '9', 'pjm_wbs_task_type', '', 'info', 'N', '0', 'admin', NOW(), '不属于以上分类的其他任务'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM `sys_dict_data` WHERE `dict_type`='pjm_wbs_task_type' AND `dict_value`='9');

-- 4) 兜底：清理历史已存在的里程碑=2 脏数据（若有按旧提示词写入的记录，统一改为 7）
UPDATE `pjm_wbs_task` SET `task_type` = '7' WHERE `del_flag` = '0' AND `task_type` = '2'
  AND `task_name` LIKE '%里程碑%';
