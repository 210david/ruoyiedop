-- ---------------------------------------------------------------------------
-- 首页顶部第 5 张卡：工单 → 项目
--
-- 背景：
--   原第 5 张卡为「工单」（metric_key = WORK_ORDER_OPEN，生产执行视角）。
--   工作台使用者更关注项目层面的在管/逾期情况，故将最后一张卡替换为「项目」。
--   前 4 张（物料 / 设备 / 销售订单 / 采购订单）保持不变。
--
-- 口径：
--   pjm_project 状态字典（0草稿 1待审 2通过 3执行 4完成 5关闭 6暂停 7驳回）
--   在管 = 通过(2) / 执行(3) / 暂停(6)，即已立项且未终结；
--   逾期 = 计划结束日 end_date 已过且仍处于在管状态。
--   与项目列表页 del_flag = '0' 口径一致。
--
-- 说明：
--   metric_key 用 PROJECT_OPEN，对应后端 HomeKpiService 的 case；
--   target_titles / target_path 指向「项目立项」菜单（sys_menu 906203 → /pjm/project）。
--
-- 执行：mysql -uroot -p ruoyi_lyjs < sql/home_kpi_project_card.sql （幂等，可重复执行）
-- ---------------------------------------------------------------------------

-- 1. 校验表内当前配置（执行前留档）
SELECT seq, metric_key, title, unit, target_titles, target_path, enabled
  FROM sys_role_kpi
 WHERE role_key = '__default__'
 ORDER BY seq;

-- 2. 就地把第 5 张卡由「工单」换成「项目」
--    用 metric_key 定位，避免依赖自增 id；若原行仍是 WORK_ORDER_OPEN 则更新，
--    已是 PROJECT_OPEN 时同样被覆盖为最新文案（幂等）。
UPDATE sys_role_kpi
   SET metric_key    = 'PROJECT_OPEN',
       title         = '项目',
       unit          = '个',
       target_titles = '项目立项',
       target_path   = '/pjm/project',
       enabled       = '0',
       remark        = '在管项目数 + 执行中/逾期数',
       update_time   = NOW()
 WHERE role_key = '__default__'
   AND seq = 5;

-- 3. 校验结果（应显示 5 行，最后一行为 PROJECT_OPEN）
SELECT seq, metric_key, title, unit, enabled, target_titles, target_path
  FROM sys_role_kpi
 WHERE role_key = '__default__'
 ORDER BY seq;
