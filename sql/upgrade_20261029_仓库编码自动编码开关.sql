-- =============================================================
-- 升级脚本：仓库编码规则「是否启用自动编码」开关
-- 日期：2026-10-29
-- 说明：mk_number_rule 增加 auto_enabled 列（0=关闭,1=开启）。
--       开启时仓库新建/编辑页面编码按规则自动生成（只读）；
--       关闭时编码必填、手动输入、后端校验不能重复。
--       仓库编码规则（wms_warehouse）默认开启，保持原有自动生成行为。
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- =============================================================

ALTER TABLE `mk_number_rule`
    ADD COLUMN `auto_enabled` CHAR(1) DEFAULT '1' COMMENT '是否启用自动编码（0=关闭,1=开启）';

-- 仓库编码规则默认开启自动编码
UPDATE `mk_number_rule` SET `auto_enabled` = '1' WHERE `rule_code` = 'wms_warehouse';

-- 回退语句
-- ALTER TABLE `mk_number_rule` DROP COLUMN `auto_enabled`;
