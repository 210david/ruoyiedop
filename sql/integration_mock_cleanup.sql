-- ----------------------------
-- 钉钉模拟数据清理脚本
-- 用途：mock 模式（integration.dingtalk.mockEnabled=true）验证完成后，一键清除所有模拟痕迹
-- 执行环境：ry-vue
--
-- 模拟数据识别规则：
--   1) 绑定表   integration_user_binding.external_userid 以 mock_ 开头
--   2) 同步日志 integration_sync_log.result_msg 含「模拟」
--   3) 考勤记录 biz_hr_attendance_record.remark = '【模拟】钉钉同步'
-- ----------------------------

-- ① 先看将删除多少数据（建议先跑查询确认）
SELECT '绑定' AS biz, COUNT(*) AS cnt FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';

-- ② 删除模拟绑定
DELETE FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%';

-- ③ 删除模拟同步日志
DELETE FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%';

-- ④ 删除模拟考勤记录
DELETE FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';

-- ⑤ 关闭模拟模式
--    注意：直接改库不会刷新后端缓存（sys_config 有 Redis/Spring 缓存），
--    推荐在「钉钉集成 - 集成配置」弹窗关闭「模拟数据模式」开关，会自动清缓存。
--    如需库内强制关闭，改完后需重启后端或清 Redis 键：sys_config:integration.dingtalk.mockEnabled
UPDATE sys_config SET config_value = 'false' WHERE config_key = 'integration.dingtalk.mockEnabled';

-- ⑥ 复核：三项应全部为 0
SELECT '绑定' AS biz, COUNT(*) AS remain FROM integration_user_binding WHERE connector_type = 'dingtalk' AND external_userid LIKE 'mock_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'dingtalk' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark LIKE '%【模拟】%';
