-- ----------------------------
-- 企业微信模拟数据清理脚本
-- 用途：mock 模式（integration.wecom.mockEnabled=true）验证完成后，一键清除所有模拟痕迹
-- 执行环境：ry-vue
--
-- 模拟数据识别规则：
--   1) 绑定表   integration_user_binding.connector_type='wecom' 且 external_userid 以 mockwecom_ 开头
--   2) 同步日志 integration_sync_log.connector_type='wecom' 且 result_msg 含「模拟」
--   3) 考勤记录 biz_hr_attendance_record.remark = '【模拟】企业微信同步'
--
-- 注意：本脚本仅清理企业微信模拟数据，不影响钉钉的真实同步数据；
--       钉钉模拟数据请执行 integration_mock_cleanup.sql。
-- ----------------------------

-- ① 先看将删除多少数据（建议先跑查询确认）
SELECT '绑定' AS biz, COUNT(*) AS cnt FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';

-- ② 删除模拟绑定
DELETE FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%';

-- ③ 删除模拟同步日志
DELETE FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%';

-- ④ 删除模拟考勤记录
DELETE FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';

-- ⑤ 关闭模拟模式
--    注意：直接改库不会刷新后端缓存（sys_config 有 Spring 缓存），
--    推荐在「第三方考勤集成 → 企业微信 → 集成配置」弹窗关闭「模拟数据模式」开关，会自动刷新。
--    如需库内强制关闭，改完后需重启后端。
UPDATE sys_config SET config_value = 'false' WHERE config_key = 'integration.wecom.mockEnabled';

-- ⑥ 复核：三项应全部为 0
SELECT '绑定' AS biz, COUNT(*) AS remain FROM integration_user_binding WHERE connector_type = 'wecom' AND external_userid LIKE 'mockwecom_%'
UNION ALL
SELECT '同步日志', COUNT(*) FROM integration_sync_log WHERE connector_type = 'wecom' AND result_msg LIKE '%模拟%'
UNION ALL
SELECT '考勤记录', COUNT(*) FROM biz_hr_attendance_record WHERE remark = '【模拟】企业微信同步';
