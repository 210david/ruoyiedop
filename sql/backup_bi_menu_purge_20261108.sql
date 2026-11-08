-- 决策支持菜单物理删除前备份（2026-11-08）
-- 恢复方式：mysql -u root -p ry-vue --default-character-set=utf8mb4 < 本文件
-- 包含：sys_menu 3 条（4070/4844/9063090）+ sys_role_menu 16 条授权

INSERT INTO `sys_menu` VALUES (4070,'商机看板',9063035,29,'kanban','mk/opportunity/kanban','','MkOpportunityKanban',1,0,'C','1','0','marketing:opportunity:list','dashboard','admin','2026-07-19 13:27:35','admin','2026-11-08 12:17:13',''),(4844,'谱系完整度',9063031,30,'qms-trace-dashboard','qms/trace/dashboard','','QmsTraceDashboard',1,0,'C','1','0','qms:trace:dashboard','dashboard','admin','2026-08-08 17:52:10','admin','2026-11-08 12:17:13','谱系完整度看板'),(9063090,'看板',9062263,3,'board',NULL,NULL,'',1,0,'M','0','0','','dashboard','admin','2026-11-08 12:30:30','',NULL,'');
INSERT INTO `sys_role_menu` VALUES (1,4070),(1,4844),(1,9063090),(104,4070),(120,4844),(121,4844),(122,4844),(123,4844),(124,4844),(125,4844),(126,4844),(300,4070),(301,4070),(302,4070),(304,4070),(608,4070);
