-- ============================================================
-- 20261117 增量 05：质量检验报告附件（内检 27 份 + 第三方外检 23 份）
-- 前置：文件需先按目录放到 uploadPath 下，路径与 file_path 一致
--   2026/11/16/qms/*.pdf  （第三方外检，23 份）
--   2026/11/17/qms/*.pdf  （出厂/进货/过程检验，27 份）
-- 特性：全部语句幂等，可重复执行
-- ============================================================
SET NAMES utf8mb4;

-- ---------- 0. 通用附件表（服务器若已存在则跳过） ----------
CREATE TABLE IF NOT EXISTS `sys_file` (
  `file_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `file_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '存储相对路径',
  `file_url` varchar(1000) DEFAULT NULL COMMENT '访问URL',
  `file_suffix` varchar(20) DEFAULT NULL COMMENT '扩展名',
  `file_size` bigint DEFAULT '0' COMMENT '文件大小(字节)',
  `file_hash` varchar(64) DEFAULT NULL COMMENT 'SHA256摘要(去重/防重导)',
  `mime_type` varchar(100) DEFAULT NULL COMMENT 'MIME类型',
  `storage_type` varchar(20) DEFAULT 'local' COMMENT '存储方式(local/oss/minio)',
  `biz_type` varchar(50) DEFAULT NULL COMMENT '业务类型',
  `biz_id` bigint DEFAULT NULL COMMENT '关联业务主键',
  `biz_no` varchar(64) DEFAULT NULL COMMENT '关联业务单号',
  `file_role` varchar(20) DEFAULT 'attach' COMMENT '文件角色',
  `upload_by` varchar(64) DEFAULT NULL COMMENT '上传人',
  `upload_time` datetime DEFAULT NULL COMMENT '上传时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`file_id`),
  KEY `idx_hash` (`file_hash`),
  KEY `idx_biz` (`biz_type`,`biz_id`),
  KEY `idx_time` (`upload_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通用附件表';

-- ---------- 1. sys_file 附件登记 ----------
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202361_广味香肠.pdf','upload/2026/11/16/qms/CTT26041202361_广味香肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202361_广味香肠.pdf','pdf',772311,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429001' LIMIT 1),'FQC20260429001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202361_广味香肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202365_鲜丰旺乌猪五花培根.pdf','upload/2026/11/16/qms/CTT26041202365_鲜丰旺乌猪五花培根.pdf','/profile/upload/2026/11/16/qms/CTT26041202365_鲜丰旺乌猪五花培根.pdf','pdf',771776,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429002' LIMIT 1),'FQC20260429002','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202365_鲜丰旺乌猪五花培根.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202372_川味香肠.pdf','upload/2026/11/16/qms/CTT26041202372_川味香肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202372_川味香肠.pdf','pdf',772268,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429003' LIMIT 1),'FQC20260429003','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202372_川味香肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202373_黑松露小甜肠.pdf','upload/2026/11/16/qms/CTT26041202373_黑松露小甜肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202373_黑松露小甜肠.pdf','pdf',773167,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429004' LIMIT 1),'FQC20260429004','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202373_黑松露小甜肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202374_蜂蜜小甜肠.pdf','upload/2026/11/16/qms/CTT26041202374_蜂蜜小甜肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202374_蜂蜜小甜肠.pdf','pdf',773005,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429005' LIMIT 1),'FQC20260429005','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202374_蜂蜜小甜肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202375_橙味小甜肠.pdf','upload/2026/11/16/qms/CTT26041202375_橙味小甜肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202375_橙味小甜肠.pdf','pdf',772750,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429006' LIMIT 1),'FQC20260429006','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202375_橙味小甜肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202376_大河乌猪五目肠.pdf','upload/2026/11/16/qms/CTT26041202376_大河乌猪五目肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202376_大河乌猪五目肠.pdf','pdf',771026,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429007' LIMIT 1),'FQC20260429007','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202376_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26041202377_乌猪经典猪肉肠.pdf','upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠.pdf','pdf',770936,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260429008' LIMIT 1),'FQC20260429008','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200727_大河乌猪咸鲜香肠.pdf','upload/2026/11/16/qms/CTT26091200727_大河乌猪咸鲜香肠.pdf','/profile/upload/2026/11/16/qms/CTT26091200727_大河乌猪咸鲜香肠.pdf','pdf',963226,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911001' LIMIT 1),'FQC20260911001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200727_大河乌猪咸鲜香肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200729_大河乌猪麻辣香肠.pdf','upload/2026/11/16/qms/CTT26091200729_大河乌猪麻辣香肠.pdf','/profile/upload/2026/11/16/qms/CTT26091200729_大河乌猪麻辣香肠.pdf','pdf',963256,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911002' LIMIT 1),'FQC20260911002','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200729_大河乌猪麻辣香肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200730_大河乌猪酱香猪耳.pdf','upload/2026/11/16/qms/CTT26091200730_大河乌猪酱香猪耳.pdf','/profile/upload/2026/11/16/qms/CTT26091200730_大河乌猪酱香猪耳.pdf','pdf',967007,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911003' LIMIT 1),'FQC20260911003','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200730_大河乌猪酱香猪耳.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200731_大河乌猪酱香猪脸.pdf','upload/2026/11/16/qms/CTT26091200731_大河乌猪酱香猪脸.pdf','/profile/upload/2026/11/16/qms/CTT26091200731_大河乌猪酱香猪脸.pdf','pdf',967611,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911004' LIMIT 1),'FQC20260911004','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200731_大河乌猪酱香猪脸.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200732_大河乌猪酱香猪蹄.pdf','upload/2026/11/16/qms/CTT26091200732_大河乌猪酱香猪蹄.pdf','/profile/upload/2026/11/16/qms/CTT26091200732_大河乌猪酱香猪蹄.pdf','pdf',967612,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911005' LIMIT 1),'FQC20260911005','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200732_大河乌猪酱香猪蹄.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200733_大河乌猪酱香猪舌.pdf','upload/2026/11/16/qms/CTT26091200733_大河乌猪酱香猪舌.pdf','/profile/upload/2026/11/16/qms/CTT26091200733_大河乌猪酱香猪舌.pdf','pdf',967132,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911006' LIMIT 1),'FQC20260911006','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200733_大河乌猪酱香猪舌.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200734_大河乌猪火腿酱.pdf','upload/2026/11/16/qms/CTT26091200734_大河乌猪火腿酱.pdf','/profile/upload/2026/11/16/qms/CTT26091200734_大河乌猪火腿酱.pdf','pdf',968195,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911007' LIMIT 1),'FQC20260911007','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200734_大河乌猪火腿酱.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200735_大河乌猪牛肝菌火腿酱.pdf','upload/2026/11/16/qms/CTT26091200735_大河乌猪牛肝菌火腿酱.pdf','/profile/upload/2026/11/16/qms/CTT26091200735_大河乌猪牛肝菌火腿酱.pdf','pdf',968680,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911008' LIMIT 1),'FQC20260911008','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200735_大河乌猪牛肝菌火腿酱.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200736_大河乌猪鸡枞火腿酱.pdf','upload/2026/11/16/qms/CTT26091200736_大河乌猪鸡枞火腿酱.pdf','/profile/upload/2026/11/16/qms/CTT26091200736_大河乌猪鸡枞火腿酱.pdf','pdf',968540,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911009' LIMIT 1),'FQC20260911009','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200736_大河乌猪鸡枞火腿酱.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200737_大河乌猪松茸火腿酱.pdf','upload/2026/11/16/qms/CTT26091200737_大河乌猪松茸火腿酱.pdf','/profile/upload/2026/11/16/qms/CTT26091200737_大河乌猪松茸火腿酱.pdf','pdf',968444,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911010' LIMIT 1),'FQC20260911010','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200737_大河乌猪松茸火腿酱.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200739_大河乌猪块状火腿.pdf','upload/2026/11/16/qms/CTT26091200739_大河乌猪块状火腿.pdf','/profile/upload/2026/11/16/qms/CTT26091200739_大河乌猪块状火腿.pdf','pdf',985911,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260911011' LIMIT 1),'FQC20260911011','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200739_大河乌猪块状火腿.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200741_鲜肉.pdf','upload/2026/11/16/qms/CTT26091200741_鲜肉.pdf','/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉.pdf','pdf',957196,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260914001' LIMIT 1),'FQC20260914001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200740_五花腊肉.pdf','upload/2026/11/16/qms/CTT26091200740_五花腊肉.pdf','/profile/upload/2026/11/16/qms/CTT26091200740_五花腊肉.pdf','pdf',974416,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260915001' LIMIT 1),'FQC20260915001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200740_五花腊肉.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26091200738_大河乌猪香辣肠.pdf','upload/2026/11/16/qms/CTT26091200738_大河乌猪香辣肠.pdf','/profile/upload/2026/11/16/qms/CTT26091200738_大河乌猪香辣肠.pdf','pdf',990703,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260921001' LIMIT 1),'FQC20260921001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26091200738_大河乌猪香辣肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'CTT26101201729_乌猪小甜肠.pdf','upload/2026/11/16/qms/CTT26101201729_乌猪小甜肠.pdf','/profile/upload/2026/11/16/qms/CTT26101201729_乌猪小甜肠.pdf','pdf',795723,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20261027001' LIMIT 1),'FQC20261027001','attach','admin','2026-11-16 16:31:12','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/16/qms/CTT26101201729_乌猪小甜肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf','pdf',111539,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20251025001' LIMIT 1),'FQC20251025001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20251028052_五花培根.pdf','upload/2026/11/17/qms/NBFQC20251028052_五花培根.pdf','/profile/upload/2026/11/17/qms/NBFQC20251028052_五花培根.pdf','pdf',105133,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20251028001' LIMIT 1),'FQC20251028001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20251028052_五花培根.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20251105053_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20251105053_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20251105053_大河乌猪五目肠.pdf','pdf',111360,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20251105001' LIMIT 1),'FQC20251105001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20251105053_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20251125054_大河乌猪香辣肠.pdf','upload/2026/11/17/qms/NBFQC20251125054_大河乌猪香辣肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20251125054_大河乌猪香辣肠.pdf','pdf',97482,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20251125001' LIMIT 1),'FQC20251125001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20251125054_大河乌猪香辣肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf','pdf',104193,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260128001' LIMIT 1),'FQC20260128001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260129056_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20260129056_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260129056_大河乌猪五目肠.pdf','pdf',104044,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260129001' LIMIT 1),'FQC20260129001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260129056_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260414057_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20260414057_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260414057_大河乌猪五目肠.pdf','pdf',103772,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260414001' LIMIT 1),'FQC20260414001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260414057_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260603058_五花培根碎.pdf','upload/2026/11/17/qms/NBFQC20260603058_五花培根碎.pdf','/profile/upload/2026/11/17/qms/NBFQC20260603058_五花培根碎.pdf','pdf',101655,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260603001' LIMIT 1),'FQC20260603001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260603058_五花培根碎.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260609059_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260609059_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260609059_乌猪经典猪肉肠.pdf','pdf',103600,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260609001' LIMIT 1),'FQC20260609001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260609059_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260613060_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260613060_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260613060_乌猪经典猪肉肠.pdf','pdf',103592,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260613001' LIMIT 1),'FQC20260613001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260613060_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260616061_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260616061_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260616061_乌猪经典猪肉肠.pdf','pdf',103956,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260616001' LIMIT 1),'FQC20260616001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260616061_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260617062_五花培根.pdf','upload/2026/11/17/qms/NBFQC20260617062_五花培根.pdf','/profile/upload/2026/11/17/qms/NBFQC20260617062_五花培根.pdf','pdf',101817,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260617001' LIMIT 1),'FQC20260617001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260617062_五花培根.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260630063_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260630063_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260630063_乌猪经典猪肉肠.pdf','pdf',103712,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260630001' LIMIT 1),'FQC20260630001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260630063_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260702064_大河乌猪香辣肠.pdf','upload/2026/11/17/qms/NBFQC20260702064_大河乌猪香辣肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260702064_大河乌猪香辣肠.pdf','pdf',97520,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260702001' LIMIT 1),'FQC20260702001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260702064_大河乌猪香辣肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260706065_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20260706065_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260706065_大河乌猪五目肠.pdf','pdf',103989,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260706001' LIMIT 1),'FQC20260706001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260706065_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260801066_五花培根.pdf','upload/2026/11/17/qms/NBFQC20260801066_五花培根.pdf','/profile/upload/2026/11/17/qms/NBFQC20260801066_五花培根.pdf','pdf',101870,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260801001' LIMIT 1),'FQC20260801001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260801066_五花培根.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260813067_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20260813067_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260813067_大河乌猪五目肠.pdf','pdf',104173,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260813001' LIMIT 1),'FQC20260813001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260813067_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260905068_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260905068_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260905068_乌猪经典猪肉肠.pdf','pdf',103844,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260905001' LIMIT 1),'FQC20260905001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260905068_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20260929069_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20260929069_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20260929069_乌猪经典猪肉肠.pdf','pdf',103962,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20260929001' LIMIT 1),'FQC20260929001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20260929069_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20261031070_乌猪经典猪肉肠.pdf','upload/2026/11/17/qms/NBFQC20261031070_乌猪经典猪肉肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20261031070_乌猪经典猪肉肠.pdf','pdf',103595,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20261031001' LIMIT 1),'FQC20261031001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20261031070_乌猪经典猪肉肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20261021071_大河乌猪五目肠.pdf','upload/2026/11/17/qms/NBFQC20261021071_大河乌猪五目肠.pdf','/profile/upload/2026/11/17/qms/NBFQC20261021071_大河乌猪五目肠.pdf','pdf',103851,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20261021001' LIMIT 1),'FQC20261021001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20261021071_大河乌猪五目肠.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20261027003_鲜丰旺乌猪培根.pdf','upload/2026/11/17/qms/NBFQC20261027003_鲜丰旺乌猪培根.pdf','/profile/upload/2026/11/17/qms/NBFQC20261027003_鲜丰旺乌猪培根.pdf','pdf',105811,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20261027003' LIMIT 1),'FQC20261027003','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20261027003_鲜丰旺乌猪培根.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBFQC20261027004_鸡枞火腿酱.pdf','upload/2026/11/17/qms/NBFQC20261027004_鸡枞火腿酱.pdf','/profile/upload/2026/11/17/qms/NBFQC20261027004_鸡枞火腿酱.pdf','pdf',105833,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='FQC20261027004' LIMIT 1),'FQC20261027004','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBFQC20261027004_鸡枞火腿酱.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBIPQC20261027006_广味香肠（灌装工序）.pdf','upload/2026/11/17/qms/NBIPQC20261027006_广味香肠（灌装工序）.pdf','/profile/upload/2026/11/17/qms/NBIPQC20261027006_广味香肠（灌装工序）.pdf','pdf',104718,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='IPQC20261027006' LIMIT 1),'IPQC20261027006','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBIPQC20261027006_广味香肠（灌装工序）.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf','upload/2026/11/17/qms/NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf','/profile/upload/2026/11/17/qms/NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf','pdf',105164,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='IQC20261027001' LIMIT 1),'IQC20261027001','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf','upload/2026/11/17/qms/NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf','/profile/upload/2026/11/17/qms/NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf','pdf',105283,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='IQC20261027002' LIMIT 1),'IQC20261027002','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf' AND del_flag='0');
INSERT INTO sys_file (file_name,file_path,file_url,file_suffix,file_size,mime_type,storage_type,biz_type,biz_id,biz_no,file_role,upload_by,upload_time,del_flag)
SELECT 'NBIQC20261026007_天然猪肠衣.pdf','upload/2026/11/17/qms/NBIQC20261026007_天然猪肠衣.pdf','/profile/upload/2026/11/17/qms/NBIQC20261026007_天然猪肠衣.pdf','pdf',103706,'application/pdf','local','qms_insp_task',
       (SELECT task_id FROM qms_insp_task WHERE task_no='IQC20261027007' LIMIT 1),'IQC20261027007','report','admin','2026-11-17 10:40:00','0'
FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM sys_file WHERE file_url='/profile/upload/2026/11/17/qms/NBIQC20261026007_天然猪肠衣.pdf' AND del_flag='0');

-- 修正历史记录中 biz_id 与任务主键不一致的问题
UPDATE sys_file f JOIN qms_insp_task t ON t.task_no COLLATE utf8mb4_general_ci = f.biz_no COLLATE utf8mb4_general_ci
SET f.biz_id = t.task_id WHERE f.biz_type='qms_insp_task' AND (f.biz_id IS NULL OR f.biz_id <> t.task_id);

-- ---------- 2. 检验任务报告字段回填 ----------
UPDATE qms_insp_task SET
  report_no = CASE task_no
    WHEN 'FQC20251025001' THEN 'NBFQC20251025051'
    WHEN 'FQC20251028001' THEN 'NBFQC20251028052'
    WHEN 'FQC20251105001' THEN 'NBFQC20251105053'
    WHEN 'FQC20251125001' THEN 'NBFQC20251125054'
    WHEN 'FQC20260128001' THEN 'NBFQC20260128055'
    WHEN 'FQC20260129001' THEN 'NBFQC20260129056'
    WHEN 'FQC20260414001' THEN 'NBFQC20260414057'
    WHEN 'FQC20260429001' THEN 'CTT26041202361'
    WHEN 'FQC20260429002' THEN 'CTT26041202365'
    WHEN 'FQC20260429003' THEN 'CTT26041202372'
    WHEN 'FQC20260429004' THEN 'CTT26041202373'
    WHEN 'FQC20260429005' THEN 'CTT26041202374'
    WHEN 'FQC20260429006' THEN 'CTT26041202375'
    WHEN 'FQC20260429007' THEN 'CTT26041202376'
    WHEN 'FQC20260429008' THEN 'CTT26041202377'
    WHEN 'FQC20260603001' THEN 'NBFQC20260603058'
    WHEN 'FQC20260609001' THEN 'NBFQC20260609059'
    WHEN 'FQC20260613001' THEN 'NBFQC20260613060'
    WHEN 'FQC20260616001' THEN 'NBFQC20260616061'
    WHEN 'FQC20260617001' THEN 'NBFQC20260617062'
    WHEN 'FQC20260630001' THEN 'NBFQC20260630063'
    WHEN 'FQC20260702001' THEN 'NBFQC20260702064'
    WHEN 'FQC20260706001' THEN 'NBFQC20260706065'
    WHEN 'FQC20260801001' THEN 'NBFQC20260801066'
    WHEN 'FQC20260813001' THEN 'NBFQC20260813067'
    WHEN 'FQC20260905001' THEN 'NBFQC20260905068'
    WHEN 'FQC20260911001' THEN 'CTT26091200727'
    WHEN 'FQC20260911002' THEN 'CTT26091200729'
    WHEN 'FQC20260911003' THEN 'CTT26091200730'
    WHEN 'FQC20260911004' THEN 'CTT26091200731'
    WHEN 'FQC20260911005' THEN 'CTT26091200732'
    WHEN 'FQC20260911006' THEN 'CTT26091200733'
    WHEN 'FQC20260911007' THEN 'CTT26091200734'
    WHEN 'FQC20260911008' THEN 'CTT26091200735'
    WHEN 'FQC20260911009' THEN 'CTT26091200736'
    WHEN 'FQC20260911010' THEN 'CTT26091200737'
    WHEN 'FQC20260911011' THEN 'CTT26091200739'
    WHEN 'FQC20260914001' THEN 'CTT26091200741'
    WHEN 'FQC20260915001' THEN 'CTT26091200740'
    WHEN 'FQC20260921001' THEN 'CTT26091200738'
    WHEN 'FQC20260929001' THEN 'NBFQC20260929069'
    WHEN 'FQC20261021001' THEN 'NBFQC20261021071'
    WHEN 'FQC20261027001' THEN 'CTT26101201729'
    WHEN 'FQC20261027003' THEN 'NBFQC20261027003'
    WHEN 'FQC20261027004' THEN 'NBFQC20261027004'
    WHEN 'FQC20261031001' THEN 'NBFQC20261031070'
    WHEN 'IPQC20261027006' THEN 'NBIPQC20261027006'
    WHEN 'IQC20261027001' THEN 'NBIQC20261026001'
    WHEN 'IQC20261027002' THEN 'NBIQC20261027002'
    WHEN 'IQC20261027007' THEN 'NBIQC20261026007'
  END,
  report_file_url = CASE task_no
    WHEN 'FQC20251025001' THEN '/profile/upload/2026/11/17/qms/NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf'
    WHEN 'FQC20251028001' THEN '/profile/upload/2026/11/17/qms/NBFQC20251028052_五花培根.pdf'
    WHEN 'FQC20251105001' THEN '/profile/upload/2026/11/17/qms/NBFQC20251105053_大河乌猪五目肠.pdf'
    WHEN 'FQC20251125001' THEN '/profile/upload/2026/11/17/qms/NBFQC20251125054_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260128001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260129001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260129056_大河乌猪五目肠.pdf'
    WHEN 'FQC20260414001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260414057_大河乌猪五目肠.pdf'
    WHEN 'FQC20260429001' THEN '/profile/upload/2026/11/16/qms/CTT26041202361_广味香肠.pdf'
    WHEN 'FQC20260429002' THEN '/profile/upload/2026/11/16/qms/CTT26041202365_鲜丰旺乌猪五花培根.pdf'
    WHEN 'FQC20260429003' THEN '/profile/upload/2026/11/16/qms/CTT26041202372_川味香肠.pdf'
    WHEN 'FQC20260429004' THEN '/profile/upload/2026/11/16/qms/CTT26041202373_黑松露小甜肠.pdf'
    WHEN 'FQC20260429005' THEN '/profile/upload/2026/11/16/qms/CTT26041202374_蜂蜜小甜肠.pdf'
    WHEN 'FQC20260429006' THEN '/profile/upload/2026/11/16/qms/CTT26041202375_橙味小甜肠.pdf'
    WHEN 'FQC20260429007' THEN '/profile/upload/2026/11/16/qms/CTT26041202376_大河乌猪五目肠.pdf'
    WHEN 'FQC20260429008' THEN '/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠.pdf,/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠_p01.png,/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠_p02.png,/profile/upload/2026/11/16/qms/CTT26041202377_乌猪经典猪肉肠_p03.png'
    WHEN 'FQC20260603001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260603058_五花培根碎.pdf'
    WHEN 'FQC20260609001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260609059_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260613001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260613060_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260616001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260616061_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260617001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260617062_五花培根.pdf'
    WHEN 'FQC20260630001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260630063_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260702001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260702064_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260706001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260706065_大河乌猪五目肠.pdf'
    WHEN 'FQC20260801001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260801066_五花培根.pdf'
    WHEN 'FQC20260813001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260813067_大河乌猪五目肠.pdf'
    WHEN 'FQC20260905001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260905068_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260911001' THEN '/profile/upload/2026/11/16/qms/CTT26091200727_大河乌猪咸鲜香肠.pdf'
    WHEN 'FQC20260911002' THEN '/profile/upload/2026/11/16/qms/CTT26091200729_大河乌猪麻辣香肠.pdf'
    WHEN 'FQC20260911003' THEN '/profile/upload/2026/11/16/qms/CTT26091200730_大河乌猪酱香猪耳.pdf'
    WHEN 'FQC20260911004' THEN '/profile/upload/2026/11/16/qms/CTT26091200731_大河乌猪酱香猪脸.pdf'
    WHEN 'FQC20260911005' THEN '/profile/upload/2026/11/16/qms/CTT26091200732_大河乌猪酱香猪蹄.pdf'
    WHEN 'FQC20260911006' THEN '/profile/upload/2026/11/16/qms/CTT26091200733_大河乌猪酱香猪舌.pdf'
    WHEN 'FQC20260911007' THEN '/profile/upload/2026/11/16/qms/CTT26091200734_大河乌猪火腿酱.pdf'
    WHEN 'FQC20260911008' THEN '/profile/upload/2026/11/16/qms/CTT26091200735_大河乌猪牛肝菌火腿酱.pdf'
    WHEN 'FQC20260911009' THEN '/profile/upload/2026/11/16/qms/CTT26091200736_大河乌猪鸡枞火腿酱.pdf'
    WHEN 'FQC20260911010' THEN '/profile/upload/2026/11/16/qms/CTT26091200737_大河乌猪松茸火腿酱.pdf'
    WHEN 'FQC20260911011' THEN '/profile/upload/2026/11/16/qms/CTT26091200739_大河乌猪块状火腿.pdf'
    WHEN 'FQC20260914001' THEN '/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉.pdf,/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉_p01.png,/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉_p02.png,/profile/upload/2026/11/16/qms/CTT26091200741_鲜肉_p03.png'
    WHEN 'FQC20260915001' THEN '/profile/upload/2026/11/16/qms/CTT26091200740_五花腊肉.pdf'
    WHEN 'FQC20260921001' THEN '/profile/upload/2026/11/16/qms/CTT26091200738_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260929001' THEN '/profile/upload/2026/11/17/qms/NBFQC20260929069_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20261021001' THEN '/profile/upload/2026/11/17/qms/NBFQC20261021071_大河乌猪五目肠.pdf'
    WHEN 'FQC20261027001' THEN '/profile/upload/2026/11/16/qms/CTT26101201729_乌猪小甜肠.pdf'
    WHEN 'FQC20261027003' THEN '/profile/upload/2026/11/17/qms/NBFQC20261027003_鲜丰旺乌猪培根.pdf'
    WHEN 'FQC20261027004' THEN '/profile/upload/2026/11/17/qms/NBFQC20261027004_鸡枞火腿酱.pdf'
    WHEN 'FQC20261031001' THEN '/profile/upload/2026/11/17/qms/NBFQC20261031070_乌猪经典猪肉肠.pdf'
    WHEN 'IPQC20261027006' THEN '/profile/upload/2026/11/17/qms/NBIPQC20261027006_广味香肠（灌装工序）.pdf'
    WHEN 'IQC20261027001' THEN '/profile/upload/2026/11/17/qms/NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf'
    WHEN 'IQC20261027002' THEN '/profile/upload/2026/11/17/qms/NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf'
    WHEN 'IQC20261027007' THEN '/profile/upload/2026/11/17/qms/NBIQC20261026007_天然猪肠衣.pdf'
  END,
  report_file_name = CASE task_no
    WHEN 'FQC20251025001' THEN 'NBFQC20251025051_鲜丰旺乌猪经典猪肉肠.pdf'
    WHEN 'FQC20251028001' THEN 'NBFQC20251028052_五花培根.pdf'
    WHEN 'FQC20251105001' THEN 'NBFQC20251105053_大河乌猪五目肠.pdf'
    WHEN 'FQC20251125001' THEN 'NBFQC20251125054_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260128001' THEN 'NBFQC20260128055_鲜丰旺乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260129001' THEN 'NBFQC20260129056_大河乌猪五目肠.pdf'
    WHEN 'FQC20260414001' THEN 'NBFQC20260414057_大河乌猪五目肠.pdf'
    WHEN 'FQC20260429001' THEN 'CTT26041202361_广味香肠.pdf'
    WHEN 'FQC20260429002' THEN 'CTT26041202365_鲜丰旺乌猪五花培根.pdf'
    WHEN 'FQC20260429003' THEN 'CTT26041202372_川味香肠.pdf'
    WHEN 'FQC20260429004' THEN 'CTT26041202373_黑松露小甜肠.pdf'
    WHEN 'FQC20260429005' THEN 'CTT26041202374_蜂蜜小甜肠.pdf'
    WHEN 'FQC20260429006' THEN 'CTT26041202375_橙味小甜肠.pdf'
    WHEN 'FQC20260429007' THEN 'CTT26041202376_大河乌猪五目肠.pdf'
    WHEN 'FQC20260429008' THEN 'CTT26041202377_乌猪经典猪肉肠.pdf,CTT26041202377_乌猪经典猪肉肠_p01.png,CTT26041202377_乌猪经典猪肉肠_p02.png,CTT26041202377_乌猪经典猪肉肠_p03.png'
    WHEN 'FQC20260603001' THEN 'NBFQC20260603058_五花培根碎.pdf'
    WHEN 'FQC20260609001' THEN 'NBFQC20260609059_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260613001' THEN 'NBFQC20260613060_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260616001' THEN 'NBFQC20260616061_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260617001' THEN 'NBFQC20260617062_五花培根.pdf'
    WHEN 'FQC20260630001' THEN 'NBFQC20260630063_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260702001' THEN 'NBFQC20260702064_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260706001' THEN 'NBFQC20260706065_大河乌猪五目肠.pdf'
    WHEN 'FQC20260801001' THEN 'NBFQC20260801066_五花培根.pdf'
    WHEN 'FQC20260813001' THEN 'NBFQC20260813067_大河乌猪五目肠.pdf'
    WHEN 'FQC20260905001' THEN 'NBFQC20260905068_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20260911001' THEN 'CTT26091200727_大河乌猪咸鲜香肠.pdf'
    WHEN 'FQC20260911002' THEN 'CTT26091200729_大河乌猪麻辣香肠.pdf'
    WHEN 'FQC20260911003' THEN 'CTT26091200730_大河乌猪酱香猪耳.pdf'
    WHEN 'FQC20260911004' THEN 'CTT26091200731_大河乌猪酱香猪脸.pdf'
    WHEN 'FQC20260911005' THEN 'CTT26091200732_大河乌猪酱香猪蹄.pdf'
    WHEN 'FQC20260911006' THEN 'CTT26091200733_大河乌猪酱香猪舌.pdf'
    WHEN 'FQC20260911007' THEN 'CTT26091200734_大河乌猪火腿酱.pdf'
    WHEN 'FQC20260911008' THEN 'CTT26091200735_大河乌猪牛肝菌火腿酱.pdf'
    WHEN 'FQC20260911009' THEN 'CTT26091200736_大河乌猪鸡枞火腿酱.pdf'
    WHEN 'FQC20260911010' THEN 'CTT26091200737_大河乌猪松茸火腿酱.pdf'
    WHEN 'FQC20260911011' THEN 'CTT26091200739_大河乌猪块状火腿.pdf'
    WHEN 'FQC20260914001' THEN 'CTT26091200741_鲜肉.pdf,CTT26091200741_鲜肉_p01.png,CTT26091200741_鲜肉_p02.png,CTT26091200741_鲜肉_p03.png'
    WHEN 'FQC20260915001' THEN 'CTT26091200740_五花腊肉.pdf'
    WHEN 'FQC20260921001' THEN 'CTT26091200738_大河乌猪香辣肠.pdf'
    WHEN 'FQC20260929001' THEN 'NBFQC20260929069_乌猪经典猪肉肠.pdf'
    WHEN 'FQC20261021001' THEN 'NBFQC20261021071_大河乌猪五目肠.pdf'
    WHEN 'FQC20261027001' THEN 'CTT26101201729_乌猪小甜肠.pdf'
    WHEN 'FQC20261027003' THEN 'NBFQC20261027003_鲜丰旺乌猪培根.pdf'
    WHEN 'FQC20261027004' THEN 'NBFQC20261027004_鸡枞火腿酱.pdf'
    WHEN 'FQC20261031001' THEN 'NBFQC20261031070_乌猪经典猪肉肠.pdf'
    WHEN 'IPQC20261027006' THEN 'NBIPQC20261027006_广味香肠（灌装工序）.pdf'
    WHEN 'IQC20261027001' THEN 'NBIQC20261026001_大河乌猪鲜肉（冷鲜肉）.pdf'
    WHEN 'IQC20261027002' THEN 'NBIQC20261027002_大河乌猪鲜肉（冷鲜肉）.pdf'
    WHEN 'IQC20261027007' THEN 'NBIQC20261026007_天然猪肠衣.pdf'
  END,
  inspect_basis = CASE task_no
    WHEN 'FQC20251025001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20251028001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20251105001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20251125001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260128001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260129001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260414001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260429001' THEN '第三方检测机构检验'
    WHEN 'FQC20260429002' THEN '第三方检测机构检验'
    WHEN 'FQC20260429003' THEN '第三方检测机构检验'
    WHEN 'FQC20260429004' THEN '第三方检测机构检验'
    WHEN 'FQC20260429005' THEN '第三方检测机构检验'
    WHEN 'FQC20260429006' THEN '第三方检测机构检验'
    WHEN 'FQC20260429007' THEN '第三方检测机构检验'
    WHEN 'FQC20260429008' THEN '第三方检测机构检验'
    WHEN 'FQC20260603001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260609001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260613001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260616001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260617001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260630001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260702001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260706001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260801001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260813001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260905001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20260911001' THEN '第三方检测机构检验'
    WHEN 'FQC20260911002' THEN '第三方检测机构检验'
    WHEN 'FQC20260911003' THEN '第三方检测机构检验'
    WHEN 'FQC20260911004' THEN '第三方检测机构检验'
    WHEN 'FQC20260911005' THEN '第三方检测机构检验'
    WHEN 'FQC20260911006' THEN '第三方检测机构检验'
    WHEN 'FQC20260911007' THEN '第三方检测机构检验'
    WHEN 'FQC20260911008' THEN '第三方检测机构检验'
    WHEN 'FQC20260911009' THEN '第三方检测机构检验'
    WHEN 'FQC20260911010' THEN '第三方检测机构检验'
    WHEN 'FQC20260911011' THEN '第三方检测机构检验'
    WHEN 'FQC20260914001' THEN '第三方检测机构检验'
    WHEN 'FQC20260915001' THEN '第三方检测机构检验'
    WHEN 'FQC20260921001' THEN '第三方检测机构检验'
    WHEN 'FQC20260929001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20261021001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20261027001' THEN '第三方检测机构检验'
    WHEN 'FQC20261027003' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20261027004' THEN 'GB 31644-2018《食品安全国家标准 复合调味料》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'FQC20261031001' THEN 'GB 2726-2016《食品安全国家标准 熟肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'IPQC20261027006' THEN 'GB 2730-2015《食品安全国家标准 腌腊肉制品》、GB 2760-2024《食品安全国家标准 食品添加剂使用标准》'
    WHEN 'IQC20261027001' THEN 'GB 2707-2016《食品安全国家标准 鲜（冻）畜、禽产品》'
    WHEN 'IQC20261027002' THEN 'GB 2707-2016《食品安全国家标准 鲜（冻）畜、禽产品》'
    WHEN 'IQC20261027007' THEN 'GB/T 7740-2006《天然肠衣》'
  END,
  report_date = CASE task_no
    WHEN 'FQC20251025001' THEN '2025-10-27'
    WHEN 'FQC20251028001' THEN '2025-10-30'
    WHEN 'FQC20251105001' THEN '2025-11-07'
    WHEN 'FQC20251125001' THEN '2025-11-25'
    WHEN 'FQC20260128001' THEN '2026-01-31'
    WHEN 'FQC20260129001' THEN '2026-01-31'
    WHEN 'FQC20260414001' THEN '2026-04-16'
    WHEN 'FQC20260429001' THEN '2026-04-29'
    WHEN 'FQC20260429002' THEN '2026-04-29'
    WHEN 'FQC20260429003' THEN '2026-04-29'
    WHEN 'FQC20260429004' THEN '2026-04-29'
    WHEN 'FQC20260429005' THEN '2026-04-29'
    WHEN 'FQC20260429006' THEN '2026-04-29'
    WHEN 'FQC20260429007' THEN '2026-04-29'
    WHEN 'FQC20260429008' THEN '2026-04-29'
    WHEN 'FQC20260603001' THEN '2026-06-04'
    WHEN 'FQC20260609001' THEN '2026-06-11'
    WHEN 'FQC20260613001' THEN '2026-06-15'
    WHEN 'FQC20260616001' THEN '2026-06-18'
    WHEN 'FQC20260617001' THEN '2026-06-19'
    WHEN 'FQC20260630001' THEN '2026-07-01'
    WHEN 'FQC20260702001' THEN '2026-07-02'
    WHEN 'FQC20260706001' THEN '2026-07-08'
    WHEN 'FQC20260801001' THEN '2026-08-03'
    WHEN 'FQC20260813001' THEN '2026-08-15'
    WHEN 'FQC20260905001' THEN '2026-09-07'
    WHEN 'FQC20260911001' THEN '2026-09-11'
    WHEN 'FQC20260911002' THEN '2026-09-11'
    WHEN 'FQC20260911003' THEN '2026-09-11'
    WHEN 'FQC20260911004' THEN '2026-09-11'
    WHEN 'FQC20260911005' THEN '2026-09-11'
    WHEN 'FQC20260911006' THEN '2026-09-11'
    WHEN 'FQC20260911007' THEN '2026-09-11'
    WHEN 'FQC20260911008' THEN '2026-09-11'
    WHEN 'FQC20260911009' THEN '2026-09-11'
    WHEN 'FQC20260911010' THEN '2026-09-11'
    WHEN 'FQC20260911011' THEN '2026-09-11'
    WHEN 'FQC20260914001' THEN '2026-09-14'
    WHEN 'FQC20260915001' THEN '2026-09-15'
    WHEN 'FQC20260921001' THEN '2026-09-21'
    WHEN 'FQC20260929001' THEN '2026-09-30'
    WHEN 'FQC20261021001' THEN '2026-10-23'
    WHEN 'FQC20261027001' THEN '2026-10-27'
    WHEN 'FQC20261027003' THEN '2026-10-27'
    WHEN 'FQC20261027004' THEN '2026-10-27'
    WHEN 'FQC20261031001' THEN '2026-11-01'
    WHEN 'IPQC20261027006' THEN '2026-10-27'
    WHEN 'IQC20261027001' THEN '2026-10-26'
    WHEN 'IQC20261027002' THEN '2026-10-27'
    WHEN 'IQC20261027007' THEN '2026-10-26'
  END
WHERE task_no IN ('FQC20251025001','FQC20251028001','FQC20251105001','FQC20251125001','FQC20260128001','FQC20260129001','FQC20260414001','FQC20260429001','FQC20260429002','FQC20260429003','FQC20260429004','FQC20260429005','FQC20260429006','FQC20260429007','FQC20260429008','FQC20260603001','FQC20260609001','FQC20260613001','FQC20260616001','FQC20260617001','FQC20260630001','FQC20260702001','FQC20260706001','FQC20260801001','FQC20260813001','FQC20260905001','FQC20260911001','FQC20260911002','FQC20260911003','FQC20260911004','FQC20260911005','FQC20260911006','FQC20260911007','FQC20260911008','FQC20260911009','FQC20260911010','FQC20260911011','FQC20260914001','FQC20260915001','FQC20260921001','FQC20260929001','FQC20261021001','FQC20261027001','FQC20261027003','FQC20261027004','FQC20261031001','IPQC20261027006','IQC20261027001','IQC20261027002','IQC20261027007');

-- ---------- 3. 第三方外检任务：结论与检验机构 ----------
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='400g' WHERE task_no='FQC20260429001';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2726-2016《食品安全国家标准熟肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='200g' WHERE task_no='FQC20260429002';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='400g' WHERE task_no='FQC20260429003';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='100g' WHERE task_no='FQC20260429004';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='100g' WHERE task_no='FQC20260429005';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='100g' WHERE task_no='FQC20260429006';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2726-2016《食品安全国家标准熟肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='200g' WHERE task_no='FQC20260429007';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2726-2016《食品安全国家标准熟肉制品》、GB2762-2022《食品安全国家标准食品中污染物限量》以及GB2760-2024《食品安全国家标准食品添加剂使用标准》的要求。',package_spec='300g' WHERE task_no='FQC20260429008';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='500g' WHERE task_no='FQC20260911001';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='500g' WHERE task_no='FQC20260911002';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB/T23586-2022《酱卤肉制品质量通则》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='280g' WHERE task_no='FQC20260911003';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB/T23586-2022《酱卤肉制品质量通则》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='300g' WHERE task_no='FQC20260911004';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB/T23586-2022《酱卤肉制品质量通则》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='300g' WHERE task_no='FQC20260911005';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB/T23586-2022《酱卤肉制品质量通则》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='300g' WHERE task_no='FQC20260911006';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB31644-2018《食品安全国家标准复合调味料》的要求。',package_spec='260g' WHERE task_no='FQC20260911007';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB31644-2018《食品安全国家标准复合调味料》的要求。',package_spec='260g' WHERE task_no='FQC20260911008';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB31644-2018《食品安全国家标准复合调味料》的要求。',package_spec='260g' WHERE task_no='FQC20260911009';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB31644-2018《食品安全国家标准复合调味料》的要求。',package_spec='260g' WHERE task_no='FQC20260911010';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合SB/T10004-1992《中国火腿》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='500g' WHERE task_no='FQC20260911011';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB31650-2019《食品安全国家标准食品中兽药最大残留限量》、农业农村部公告第250号《食品动物中禁止使用的药品及其他化合物清单》的要求。',package_spec='1000g' WHERE task_no='FQC20260914001';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='500g' WHERE task_no='FQC20260915001';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2726-2016《食品安全国家标准熟肉制品》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='200g' WHERE task_no='FQC20260921001';
UPDATE qms_insp_task SET inspect_org='国鼎检测技术（重庆）有限公司',conclusion='该样品经检验，所检项目符合GB2730-2015《食品安全国家标准腌腊肉制品》、GB2760-2024《食品安全国家标准食品添加剂使用标准》以及GB2762-2025《食品安全国家标准食品中污染物限量》的要求。',package_spec='100g' WHERE task_no='FQC20261027001';

-- 校验：应有 50 条任务带附件
SELECT COUNT(*) AS total, SUM(report_file_url IS NOT NULL AND report_file_url<>'') AS has_report FROM qms_insp_task;
