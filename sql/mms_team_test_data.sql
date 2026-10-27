-- ============================================================
-- MMS 班组管理 - 测试数据
-- ============================================================
USE `ry-vue`;
SET NAMES utf8mb4;

INSERT INTO `mms_team` (`team_code`, `team_name`, `resource_id`, `leader`, `phone`, `member_count`, `status`, `del_flag`, `create_by`, `create_time`, `update_by`, `update_time`, `remark`) VALUES
('TM-20260821-0001', '数控加工甲班', 2,  '王伟', '13800001001', 8, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '数控机床A早班班组'),
('TM-20260821-0002', '数控加工乙班', 2,  '刘洋', '13800001002', 7, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '数控机床A晚班班组'),
('TM-20260821-0003', '总装甲班',     7,  '陈杰', '13800001003', 12, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '总装线A早班班组'),
('TM-20260821-0004', '注塑班组',     8,  '张敏', '13800002001', 6, '0', '0', 'admin', sysdate(), 'admin', sysdate(), '注塑机组B班组'),
('TM-20260821-0005', '质检班组',     10, '赵磊', '13800002002', 4, '1', '0', 'admin', sysdate(), 'admin', sysdate(), '质检工作站F班组（已停用）');
