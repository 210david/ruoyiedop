-- =============================================================
-- 升级脚本：上传大小上限后台可配置
-- 日期：2026-11-02
-- 说明：
--   1. 新增参数 system.upload.maxFileSizeMb，用于统一控制各模块
--      单文件上传上限，前端上传组件自动读取并据此校验与提示；
--   2. 原先前端组件写死 50MB，而后端 multipart 为 100MB、
--      nginx client_max_body_size 仅 20MB，三处不一致导致
--      20MB 以上文件（如 36MB 合同附件 PDF）上传失败；
--   3. 本脚本只新增配置项，不改动既有数据。
--
-- 注意：修改该参数后需同时确认 nginx 的 client_max_body_size
--       不低于该值（建议 : = 80m，略大于业务上限留余量）；
--       后端 multipart max-file-size 应不低于该值。
--
-- 幂等：已存在该配置项时重复执行不会重复插入。
-- 适用：已有数据的客户库增量升级（新服务器首装由全量脚本覆盖）
-- =============================================================

INSERT INTO `sys_config`
    (`config_name`, `config_key`, `config_value`, `config_type`, `remark`, `create_by`, `create_time`)
SELECT '单文件上传上限(MB)', 'system.upload.maxFileSizeMb', '50', 'N',
       '各模块上传文件大小上限，需与 nginx client_max_body_size 保持一致', 'admin', sysdate()
WHERE NOT EXISTS (
    SELECT 1 FROM `sys_config` WHERE `config_key` = 'system.upload.maxFileSizeMb'
);

-- 校验查询
-- SELECT config_key, config_value FROM sys_config
--  WHERE config_key = 'system.upload.maxFileSizeMb';

-- 回退语句
-- DELETE FROM sys_config WHERE config_key = 'system.upload.maxFileSizeMb';
