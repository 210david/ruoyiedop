-- ============================================================
-- 增量升级脚本：项目采购清单物料类型字段
-- 日期：2026-11-08
-- 主题：pjm_project_procurement 增加 material_type 列（物料类型）
-- 说明：采购清单增加物料类型字段（wms_material_type 字典值：
--       0原材料 1半成品 2成品 3辅料 4服务 5工程），
--       采购清单页签按物料类型动态分类统计；
--       采购执行弹框出库仓库改为系统自动分配（单仓足量优先，不足跨仓拆分）。
-- 幂等：重复执行前请先核对列是否已存在（MySQL 8 可用 IF NOT EXISTS 语法时可直接执行）。
-- ============================================================

ALTER TABLE ruoyi_lyjs.pjm_project_procurement
    ADD COLUMN material_type VARCHAR(10) DEFAULT NULL
    COMMENT '物料类型（wms_material_type 字典值：0原材料 1半成品 2成品 3辅料 4服务 5工程）'
    AFTER detail_type;
